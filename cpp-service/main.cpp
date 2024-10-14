#include <httplib.h>
#include <seal/seal.h>
#include <iostream>
#include <sstream>
#include <nlohmann/json.hpp>
#include "base64.h"

using json = nlohmann::json;

std::string secret_key_to_string(const seal::SecretKey& secret_key) {
    std::stringstream ss;
    secret_key.save(ss);
    return base64_encode(ss.str());
}

std::string public_key_to_string(const seal::PublicKey& public_key) {
    std::stringstream ss;
    public_key.save(ss);
    return base64_encode(ss.str());
}

seal::SecretKey string_to_secret_key(const std::string& base64_str, const seal::SEALContext& context) {
    std::string decoded_str = base64_decode(base64_str);
    std::stringstream ss(decoded_str);
    seal::SecretKey secret_key;
    secret_key.load(context, ss);
    return secret_key;
}

seal::PublicKey string_to_public_key(const std::string& base64_str, const seal::SEALContext& context) {
    std::string decoded_str = base64_decode(base64_str);
    std::stringstream ss(decoded_str);
    seal::PublicKey public_key;
    public_key.load(context, ss);
    return public_key;
}

seal::Ciphertext string_to_ciphertext(const std::string& base64_str, const seal::SEALContext& context) {
    std::string decoded_str = base64_decode(base64_str);
    std::stringstream ss(decoded_str);
    seal::Ciphertext ciphertext;
    ciphertext.load(context, ss);
    return ciphertext;
}

std::string ciphertext_to_string(const seal::Ciphertext& ciphertext) {
    std::stringstream ss;
    ciphertext.save(ss);
    return base64_encode(ss.str());
}

std::string plaintext_to_string(const seal::Plaintext& plaintext) {
    return std::to_string(std::stoi(plaintext.to_string()));
}

seal::Plaintext int_to_plaintext(int value) {
    return seal::Plaintext(value);
}

/*
Helper function: Convert a value into a hexadecimal string, e.g., uint64_t(17) --> "11".
*/
inline std::string uint64_to_hex_string(std::uint64_t value)
{
    return seal::util::uint_to_hex_string(&value, std::size_t(1));
}

int main() {
    // 设置 SEAL 加密上下文
    seal::EncryptionParameters parms(seal::scheme_type::bfv);
    parms.set_poly_modulus_degree(2048);
    parms.set_coeff_modulus(seal::CoeffModulus::BFVDefault(2048));
    parms.set_plain_modulus(1024);

    seal::SEALContext context(parms);

    std::cout << "SEAL context created" << std::endl;

    // 设置 HTTP 服务
    httplib::Server svr;

    // 1. 返回一个 secret key
    svr.Get("/get_secret_key", [&](const httplib::Request& req, httplib::Response& res) {
        seal::KeyGenerator keygen(context);
        auto secret_key = keygen.secret_key();
        std::string secret_key_str = secret_key_to_string(secret_key);

        json response_json = {
            {"secret_key", secret_key_str}
        };

        res.set_content(response_json.dump(), "application/json");
    });

    // 2. 输入一个 secret key，返回一个 public key
    svr.Post("/get_public_key", [&](const httplib::Request& req, httplib::Response& res) {
        try {
            json request_json = json::parse(req.body);
            std::string secret_key_base64 = request_json["secret_key"].get<std::string>();

            seal::SecretKey secret_key = string_to_secret_key(secret_key_base64, context);
            seal::KeyGenerator keygen(context, secret_key);
            seal::PublicKey public_key;
            keygen.create_public_key(public_key);

            std::string public_key_str = public_key_to_string(public_key);

            json response_json = {
                {"public_key", public_key_str}
            };

            res.set_content(response_json.dump(), "application/json");
        } catch (const std::exception& e) {
            res.status = 400;
            res.set_content("{\"error\":\"" + std::string(e.what()) + "\"}", "application/json");
        }
    });

    // 3. 输入 secret key 和 ciphertext，返回 plaintext
    svr.Post("/decrypt", [&](const httplib::Request& req, httplib::Response& res) {
        try {
            json request_json = json::parse(req.body);
            std::string secret_key_base64 = request_json["secret_key"].get<std::string>();
            std::string ciphertext_base64 = request_json["ciphertext"].get<std::string>();

            seal::SecretKey secret_key = string_to_secret_key(secret_key_base64, context);
            seal::Decryptor decryptor(context, secret_key);

            seal::Ciphertext ciphertext = string_to_ciphertext(ciphertext_base64, context);
            seal::Plaintext plaintext;
            decryptor.decrypt(ciphertext, plaintext);

            // 将 Plaintext 转换为十进制整数
            uint64_t plaintext_value = plaintext[0];

            json response_json = {
                {"plaintext", plaintext_value}
            };

            res.set_content(response_json.dump(), "application/json");
        } catch (const std::exception& e) {
            res.status = 400;
            res.set_content("{\"error\":\"" + std::string(e.what()) + "\"}", "application/json");
        }
    });

    // 4. 输入一个 public key 和一个整数，返回 ciphertext
    svr.Post("/encrypt_with_public_key", [&](const httplib::Request& req, httplib::Response& res) {
        try {
            json request_json = json::parse(req.body);
            std::string public_key_base64 = request_json["public_key"].get<std::string>();
            uint64_t value_to_encrypt = static_cast<uint64_t>(request_json["value"].get<uint64_t>());

            seal::PublicKey public_key = string_to_public_key(public_key_base64, context);
            seal::Encryptor encryptor(context, public_key);

            seal::Plaintext plaintext(uint64_to_hex_string(value_to_encrypt));
            seal::Ciphertext ciphertext;
            encryptor.encrypt(plaintext, ciphertext);

            std::string ciphertext_str = ciphertext_to_string(ciphertext);

            json response_json = {
                {"ciphertext", ciphertext_str}
            };

            res.set_content(response_json.dump(), "application/json");
        } catch (const std::exception& e) {
            res.status = 400;
            res.set_content("{\"error\":\"" + std::string(e.what()) + "\"}", "application/json");
        }
    });

    // 5. 输入一个 ciphertext 和一个整数，做加法并返回结果
    svr.Post("/add_to_ciphertext", [&](const httplib::Request& req, httplib::Response& res) {
    try {
        json request_json = json::parse(req.body);
        std::string ciphertext_base64 = request_json["ciphertext"].get<std::string>();
        uint64_t value_to_add = static_cast<uint64_t>(request_json["value"].get<uint64_t>());

        seal::Ciphertext ciphertext = string_to_ciphertext(ciphertext_base64, context);

        seal::Evaluator evaluator(context);
        // 使用整数直接创建 Plaintext
        seal::Plaintext plain_to_add(uint64_to_hex_string(value_to_add));
        evaluator.add_plain_inplace(ciphertext, plain_to_add);

        std::string updated_ciphertext_str = ciphertext_to_string(ciphertext);

        json response_json = {
            {"updated_ciphertext", updated_ciphertext_str}
        };

        res.set_content(response_json.dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content("{\"error\":\"" + std::string(e.what()) + "\"}", "application/json");
    }
    });


    std::cout << "Starting server at http://localhost:8081" << std::endl;
    svr.listen("0.0.0.0", 8081);

    return 0;
}