import requests

# Node.js service URL
BASE_URL = "http://localhost:3000"

def encrypt_value(value):
    response = requests.post(f"{BASE_URL}/encrypt_with_public_key", json={"value": value})
    if response.status_code == 200:
        return response.json()['ciphertext']
    else:
        print(f"Error during encryption: {response.json()}")
        return None

def decrypt_value(ciphertext):
    response = requests.post(f"{BASE_URL}/decrypt", json={"ciphertext": ciphertext})
    if response.status_code == 200:
        return response.json()['plaintext']
    else:
        print(f"Error during decryption: {response}")
        return None

def test_encrypt_and_decrypt():
    value = 20
    print(f"Testing encryption and decryption for value: {value}")
    
    # Encrypt the value
    encrypted_value = encrypt_value(value)
    if not encrypted_value:
        return
    
#    print(f"Encrypted Value: {encrypted_value}")
    
    # Decrypt the encrypted value
    decrypted_value = decrypt_value(encrypted_value)
    if decrypted_value is not None:
        print(f"Decrypted Value: {decrypted_value}")

def get_a_test():
  response = requests.get(f"{BASE_URL}/get_a_test_value")
  if response.status_code == 200:
      value = response.json()['plaintext']
      print(f"get a test value: {value}")
  else:
      print(f"got an error: {response}")
      return None
if __name__ == "__main__":
    test_encrypt_and_decrypt()
    get_a_test()