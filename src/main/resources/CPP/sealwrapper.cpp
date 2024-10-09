#include <seal/seal.h>
#include <jni.h>
#include <iostream>
#include <vector>
#include <cstring>

extern "C" {

// Helper function to allocate a DirectByteBuffer
// Helper function to allocate a DirectByteBuffer
jobject createDirectByteBuffer(JNIEnv *env, const std::string &str) {
    jbyte *result = (jbyte *)malloc(str.size());
    if (result == nullptr) {
        std::cerr << "Memory allocation failed." << std::endl;
        return nullptr;  // Out of memory
    }

    // Copy data to the allocated memory
    std::memcpy(result, str.c_str(), str.size());

    // Check if result is a valid pointer
    if (result == nullptr) {
        std::cerr << "Pointer is null, cannot create DirectByteBuffer." << std::endl;
        return nullptr;
    }

    // Create a direct ByteBuffer and return
    jobject byteBuffer = env->NewDirectByteBuffer(result, str.size());
    if (byteBuffer == nullptr) {
        std::cerr << "Failed to create DirectByteBuffer." << std::endl;
        free(result);  // Free memory if creating DirectByteBuffer fails
    }
    return byteBuffer;
}
// Function to release memory allocated by malloc
JNIEXPORT void JNICALL releaseMemory(JNIEnv *env, jobject obj, jobject buffer) {
    void *address = env->GetDirectBufferAddress(buffer);
    if (address != nullptr) {
        free(address);
    }
}

// Function to generate a secret key and return as Pointer
JNIEXPORT jobject JNICALL generatePrivateKey(JNIEnv *env, jobject obj) {
    try {
        // SEAL setup
        seal::EncryptionParameters params(seal::scheme_type::bfv);
        params.set_poly_modulus_degree(4096);
        params.set_coeff_modulus(seal::CoeffModulus::BFVDefault(4096));
        params.set_plain_modulus(256);

        seal::SEALContext context(params);
        seal::KeyGenerator keygen(context);
        seal::SecretKey secret_key = keygen.secret_key();

        std::stringstream secret_key_stream;
        secret_key.save(secret_key_stream);
        std::string secret_key_str = secret_key_stream.str();

        // Create and return DirectByteBuffer
        return createDirectByteBuffer(env, secret_key_str);
    } catch (const std::exception &e) {
        std::cerr << "Error generating secret key: " << e.what() << std::endl;
        return nullptr;
    }
}

// Function to generate a public key using a provided secret key, return as Pointer
JNIEXPORT jobject JNICALL generatePublicKey(JNIEnv *env, jobject obj, jobject secretKeyBuffer) {
    try {
        // SEAL setup
        seal::EncryptionParameters params(seal::scheme_type::bfv);
        params.set_poly_modulus_degree(4096);
        params.set_coeff_modulus(seal::CoeffModulus::BFVDefault(4096));
        params.set_plain_modulus(256);

        seal::SEALContext context(params);

        // Get the secret key from the ByteBuffer
        jbyte *secretKeyBytes = (jbyte *)env->GetDirectBufferAddress(secretKeyBuffer);
        jlong secretKeySize = env->GetDirectBufferCapacity(secretKeyBuffer);
        std::string secret_key_str(reinterpret_cast<char*>(secretKeyBytes), secretKeySize);

        // Load the secret key
        seal::SecretKey secret_key;
        std::stringstream secret_key_stream(secret_key_str);
        secret_key.load(context, secret_key_stream);

        // Create KeyGenerator using the secret key
        seal::KeyGenerator keygen(context, secret_key);
        seal::PublicKey public_key;
        keygen.create_public_key(public_key);

        std::stringstream public_key_stream;
        public_key.save(public_key_stream);
        std::string public_key_str = public_key_stream.str();

        return createDirectByteBuffer(env, public_key_str);
    } catch (const std::exception &e) {
        return nullptr;
    }
}

// Function to encrypt an integer using the provided public key, return as Pointer
JNIEXPORT jobject JNICALL encryptInteger(JNIEnv *env, jobject obj, jint value, jobject publicKeyBuffer) {
    try {
        // SEAL setup
        seal::EncryptionParameters params(seal::scheme_type::bfv);
        params.set_poly_modulus_degree(4096);
        params.set_coeff_modulus(seal::CoeffModulus::BFVDefault(4096));
        params.set_plain_modulus(256);

        seal::SEALContext context(params);

        // Get the public key from the ByteBuffer
        jbyte *publicKeyBytes = (jbyte *)env->GetDirectBufferAddress(publicKeyBuffer);
        jlong publicKeySize = env->GetDirectBufferCapacity(publicKeyBuffer);
        std::string public_key_str(reinterpret_cast<char*>(publicKeyBytes), publicKeySize);

        // Load the public key
        seal::PublicKey public_key;
        std::stringstream public_key_stream(public_key_str);
        public_key.load(context, public_key_stream);

        // Create encryptor, encoder, and encrypt the integer
        seal::Encryptor encryptor(context, public_key);
        seal::BatchEncoder batch_encoder(context);

        // Encode the integer into a plaintext
        std::vector<uint64_t> pod_matrix(batch_encoder.slot_count(), 0);
        pod_matrix[0] = static_cast<uint64_t>(value);
        seal::Plaintext plaintext;
        batch_encoder.encode(pod_matrix, plaintext);

        // Encrypt the plaintext
        seal::Ciphertext ciphertext;
        encryptor.encrypt(plaintext, ciphertext);

        std::stringstream ciphertext_stream;
        ciphertext.save(ciphertext_stream);
        std::string ciphertext_str = ciphertext_stream.str();

        return createDirectByteBuffer(env, ciphertext_str);
    } catch (const std::exception &e) {
        return nullptr;
    }
}

// Function to add two encrypted integers (ciphertexts), return the result as Pointer
JNIEXPORT jobject JNICALL addEncryptedIntegers(JNIEnv *env, jobject obj, jobject ciphertextBuffer1, jobject ciphertextBuffer2) {
    try {
        // SEAL setup
        seal::EncryptionParameters params(seal::scheme_type::bfv);
        params.set_poly_modulus_degree(4096);
        params.set_coeff_modulus(seal::CoeffModulus::BFVDefault(4096));
        params.set_plain_modulus(256);

        seal::SEALContext context(params);
        seal::Evaluator evaluator(context);

        // Get the first ciphertext from the ByteBuffer
        jbyte *ciphertextBytes1 = (jbyte *)env->GetDirectBufferAddress(ciphertextBuffer1);
        jlong ciphertextSize1 = env->GetDirectBufferCapacity(ciphertextBuffer1);
        std::string ciphertext_str1(reinterpret_cast<char*>(ciphertextBytes1), ciphertextSize1);

        seal::Ciphertext ciphertext1;
        std::stringstream ciphertext_stream1(ciphertext_str1);
        ciphertext1.load(context, ciphertext_stream1);

        // Get the second ciphertext from the ByteBuffer
        jbyte *ciphertextBytes2 = (jbyte *)env->GetDirectBufferAddress(ciphertextBuffer2);
        jlong ciphertextSize2 = env->GetDirectBufferCapacity(ciphertextBuffer2);
        std::string ciphertext_str2(reinterpret_cast<char*>(ciphertextBytes2), ciphertextSize2);

        seal::Ciphertext ciphertext2;
        std::stringstream ciphertext_stream2(ciphertext_str2);
        ciphertext2.load(context, ciphertext_stream2);

        // Perform homomorphic addition
        seal::Ciphertext result_ciphertext;
        evaluator.add(ciphertext1, ciphertext2, result_ciphertext);

        std::stringstream result_stream;
        result_ciphertext.save(result_stream);
        std::string result_str = result_stream.str();

        return createDirectByteBuffer(env, result_str);
    } catch (const std::exception &e) {
        return nullptr;
    }
}

}