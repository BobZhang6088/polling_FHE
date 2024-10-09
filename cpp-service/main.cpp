#include <httplib.h>
#include <seal/seal.h>
#include <iostream>

int main() {
    // 设置 SEAL 加密上下文
    seal::EncryptionParameters parms(seal::scheme_type::bfv);
    parms.set_poly_modulus_degree(2048);
    parms.set_coeff_modulus(seal::CoeffModulus::BFVDefault(2048));
    parms.set_plain_modulus(1024);

    // auto context = seal::SEALContext::SEALContext(parms);
    seal::SEALContext context = seal::SEALContext(parms);

    std::cout << "SEAL context created" << std::endl;

    // 设置 HTTP 服务
    httplib::Server svr;

    svr.Get("/encrypt", [&](const httplib::Request& req, httplib::Response& res) {
        // 加密逻辑示例
        seal::KeyGenerator keygen(context);
        auto secret_key = keygen.secret_key();
        seal::PublicKey public_key;
        keygen.create_public_key(public_key);
        seal::Encryptor encryptor(context, public_key);

        seal::Plaintext plain("6");
        seal::Ciphertext encrypted;
        encryptor.encrypt(plain, encrypted);

        res.set_content("Data encrypted!", "text/plain");
    });

    std::cout << "Starting server at http://localhost:8081" << std::endl;
    svr.listen("0.0.0.0", 8081);

    return 0;
}