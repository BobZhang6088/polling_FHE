import SEAL from 'node-seal/throws_wasm_web_umd';

// 等待 SEAL 库加载完毕
SEAL().then(seal => {
    console.log('SEAL library loaded', seal);

    // 设置 BFV 加密方案和其他参数
    ////////////////////////
    // Encryption Parameters
    ////////////////////////

const schemeType = seal.SchemeType.bfv
const securityLevel = seal.SecurityLevel.tc128
const polyModulusDegree = 4096
const bitSizes = [36, 36, 37]
const bitSize = 20

const encParms = seal.EncryptionParameters(schemeType)

// Set the PolyModulusDegree
encParms.setPolyModulusDegree(polyModulusDegree)

// Create a suitable set of CoeffModulus primes
encParms.setCoeffModulus(
  seal.CoeffModulus.Create(polyModulusDegree, Int32Array.from(bitSizes))
)

// Set the PlainModulus to a prime of bitSize 20.
encParms.setPlainModulus(seal.PlainModulus.Batching(polyModulusDegree, bitSize))

////////////////////////
// Context
////////////////////////

// Create a new Context
const context = seal.Context(
    encParms, // Encryption Parameters
    true, // ExpandModChain
    securityLevel // Enforce a security level
  )
  
  if (!context.parametersSet()) {
    throw new Error(
      'Could not set the parameters in the given context. Please try different encryption parameters.'
    )
  }

  // Create a new KeyGenerator (creates a new keypair internally)
const keyGenerator = seal.KeyGenerator(context)

const secretKey = keyGenerator.secretKey()
const publicKey = keyGenerator.createPublicKey()

const batchEncoder = seal.BatchEncoder(context)

const encryptor = seal.Encryptor(context, publicKey)

// Encode data to a PlainText
const plainText = batchEncoder.encode(
    Int32Array.from([1, 2, 3,2,3,4,1,1,1,1,1]) // This could also be a Uint32Array
  );

  const cipherText = encryptor.encrypt(plainText);

  const decryptor = seal.Decryptor(context, secretKey);

  const plainTextResult = decryptor.decrypt(cipherText);

  const decoded = batchEncoder.decode(
    plainTextResult,
    true // Can be omitted since this defaults to true.
  );
  
  console.log('decoded', decoded);

    // 加密逻辑
    // document.querySelector('#encrypt-btn').addEventListener('click', () => {
    //     const userInput = document.querySelector('#plaintext').value;
    //     const inputArray = Array(encoder.slotCount()).fill(0);
    //     inputArray[0] = parseInt(userInput); // 把用户输入的值放在数组第一位

    //     const plainText = encoder.encode(inputArray);
    //     const cipherText = seal.CipherText();
    //     encryptor.encrypt(plainText, cipherText);

    //     console.log('Encrypted CipherText:', cipherText);
    //     document.querySelector('#encrypted').textContent = '加密后的数据已生成，请查看控制台日志。';
    // });

    // // 解密逻辑（如果你需要）
    // document.querySelector('#decrypt-btn').addEventListener('click', () => {
    //     const cipherText = "123";
    //     const decryptedPlainText = seal.PlainText();
    //     decryptor.decrypt(cipherText, decryptedPlainText);

    //     const decodedArray = encoder.decode(decryptedPlainText);
    //     console.log('Decrypted value:', decodedArray[0]);
    //     document.querySelector('#decrypted').textContent = `解密后的值是: ${decodedArray[0]}`;
    // });

}).catch(err => {
    console.error('Failed to load SEAL:', err);
});
