import requests
import json

BASE_URL = "http://localhost:8081"

# 1. 测试获取 secret key
def test_get_secret_key():
    response = requests.get(f"{BASE_URL}/get_secret_key")
    if response.status_code == 200:
        secret_key = response.json()["secret_key"]
        return secret_key
    else:
        print("Error getting secret key:", response.text)

# 2. 测试获取 public key
def test_get_public_key(secret_key):
    data = {"secret_key": secret_key}
    response = requests.post(f"{BASE_URL}/get_public_key", json=data)
    if response.status_code == 200:
        public_key = response.json()["public_key"]
        return public_key
    else:
        print("Error getting public key:", response.text)

# 3. 测试加密数值
def test_encrypt_with_public_key(public_key, value):
    data = {"public_key": public_key, "value": value}
    response = requests.post(f"{BASE_URL}/encrypt_with_public_key", json=data)
    if response.status_code == 200:
        ciphertext = response.json()["ciphertext"]
        return ciphertext
    else:
        print("Error encrypting value:", response.text)

# 4. 测试解密 ciphertext
def test_decrypt(secret_key, ciphertext):
    data = {"secret_key": secret_key, "ciphertext": ciphertext}
    response = requests.post(f"{BASE_URL}/decrypt", json=data)
    if response.status_code == 200:
        plaintext = response.json()["plaintext"]
        return plaintext
    else:
        print("Error decrypting ciphertext:", response.text)

# 5. 测试 ciphertext 加法
def test_add_to_ciphertext(ciphertext, value_to_add, secret_key):
    data = {"ciphertext": ciphertext, "value": value_to_add}
    response = requests.post(f"{BASE_URL}/add_to_ciphertext", json=data)
    if response.status_code == 200:
        updated_ciphertext = response.json()["updated_ciphertext"]

        # 解密更新后的密文
        decrypted_result = test_decrypt(secret_key, updated_ciphertext)
        if decrypted_result is not None:
            print(f"{test_decrypt(secret_key, ciphertext)} + {value_to_add} = {decrypted_result}")
        return updated_ciphertext
    else:
        print("Error adding to ciphertext:", response.text)

if __name__ == "__main__":
    # Step 1: Get Secret Key
    secret_key = test_get_secret_key()

    # Step 2: Get Public Key
    if secret_key:
        public_key = test_get_public_key(secret_key)

    # Step 3: Encrypt a Value
    if public_key:
        value_to_encrypt = 42
        ciphertext = test_encrypt_with_public_key(public_key, value_to_encrypt)
        if ciphertext:
            print(f"Initial Value Encrypted: {value_to_encrypt}")

    # Step 4: Perform Multiple Additions
    if ciphertext:
        total_additions = 3
        value_to_add = 10
        current_ciphertext = ciphertext
        current_value = value_to_encrypt

        for i in range(total_additions):
            current_ciphertext = test_add_to_ciphertext(current_ciphertext, value_to_add, secret_key)
#             if current_ciphertext:
#                 current_value += value_to_add
#                 print(f"Step {i + 1}: {value_to_encrypt} + {value_to_add * (i + 1)} = {current_value}")
