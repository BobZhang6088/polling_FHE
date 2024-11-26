const express = require('express');
const SEAL = require('node-seal');
const bodyParser = require('body-parser');
const fs = require('fs');
const zlib = require('zlib');

const app = express();

// Adjust payload size limits
app.use(express.json({ limit: '50mb' })); // Adjust size as needed
app.use(express.urlencoded({ limit: '50mb', extended: true }));

const port = 3000;

app.use(bodyParser.json());

async function initialize() {

    const secretKeyFile = 'secret_key.txt';
    const publicKeyFile = 'public_key.txt';

//    try {
//        if (fs.existsSync(secretKeyFile)) {
//            fs.unlinkSync(secretKeyFile);
//            console.log('Deleted secret_key.txt');
//        }
//
//        if (fs.existsSync(publicKeyFile)) {
//            fs.unlinkSync(publicKeyFile);
//            console.log('Deleted public_key.txt');
//        }
//
//    } catch (err) {
//        console.error('Error deleting key files:', err);
//    }

    const seal = await SEAL();

     // Create a new EncryptionParameters
    const schemeType = seal.SchemeType.bfv
    const securityLevel = seal.SecurityLevel.none
    const polyModulusDegree = 4096
    const bitSizes = [36,36,37]
    const bitSize = 20
    
    const parms = seal.EncryptionParameters(schemeType)

    // Assign Poly Modulus Degree
    parms.setPolyModulusDegree(polyModulusDegree)
    
    // Create a suitable set of CoeffModulus primes
    parms.setCoeffModulus(
      seal.CoeffModulus.Create(
        polyModulusDegree,
        Int32Array.from(bitSizes)
      )
    )

    // Assign a PlainModulus (only for bfv/bgv scheme type)
    parms.setPlainModulus(
      seal.PlainModulus.Batching(
        polyModulusDegree,
        bitSize
      )
    )

    ////////////////////////
    // Context
    ////////////////////////
    
    // Create a new Context
    const context = seal.Context(
      parms,
      true,
      securityLevel
    )

    // Helper to check if the Context was created successfully
    if (!context.parametersSet()) {
      throw new Error('Could not set the parameters in the given context. Please try different encryption parameters.')
    }

    const loadOrGenerateKeys = () => {
        let secretKey;
        let publicKey;

        if (fs.existsSync(secretKeyFile) && fs.existsSync(publicKeyFile)) {
            // Load existing keys
            const savedSecretKeyString = fs.readFileSync(secretKeyFile, 'utf-8');
            secretKey = seal.SecretKey();
            secretKey.load(context, savedSecretKeyString);

            const savedPublicKeyString = fs.readFileSync(publicKeyFile, 'utf-8');
            publicKey = seal.PublicKey();
            publicKey.load(context, savedPublicKeyString);

            console.log('Loaded secret key and public key from file.');

            console.log("Secect Key length", savedSecretKeyString.length);
            console.log("Public Key length", savedPublicKeyString.length);

        } else {
            // Generate new keys
            const keyGenerator = seal.KeyGenerator(context);
            secretKey = keyGenerator.secretKey();
            publicKey = keyGenerator.createPublicKey();

            const savedSecretKeyString = secretKey.save();
            const savedPublicKeyString = publicKey.save();

            fs.writeFileSync(secretKeyFile, savedSecretKeyString, 'utf-8');
            fs.writeFileSync(publicKeyFile, savedPublicKeyString, 'utf-8');

            console.log('Generated and saved new secret key and public key.');
            console.log("Secect Key length", savedSecretKeyString.length);
            console.log("Public Key length", savedPublicKeyString.length);
        }


        return { secretKey, publicKey };
    };

    // Load or generate the keys
    const { secretKey, publicKey } = loadOrGenerateKeys();


    const encryptor = seal.Encryptor(context, publicKey);
    const batchEncoder = seal.BatchEncoder(context)
    const decryptor = seal.Decryptor(context, secretKey);
    const evaluator = seal.Evaluator(context);

    // Helper function for compression and decompression
    const compress = (data) => zlib.gzipSync(data).toString('base64');
    const decompress = (data) => zlib.gunzipSync(Buffer.from(data, 'base64')).toString();

    // 1. Get Secret Key (read-only since it's already generated)
    app.get('/get_secret_key', (req, res) => {
        const compressedSecretKey = compress(secretKey.save());
        res.json({ secret_key: compressedSecretKey });
    });

    // 2. Generate Public Key
    app.get('/get_public_key', (req, res) => {
        const compressedPublicKey = compress(publicKey.save());
        res.json({ public_key: compressedPublicKey });
    });

    // 3. Decrypt
    app.post('/decrypt', (req, res) => {
        const { ciphertext } = req.body;
        try {
            const decompressedCipherText = decompress(ciphertext);
            const ct = seal.CipherText();
            ct.load(context, decompressedCipherText);
    
            const plaintext = decryptor.decrypt(ct);
            const decoded = batchEncoder.decode(plaintext);
    
            // Return the first value in the decoded array
            res.json({ plaintext: decoded[0] });
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 4. Encrypt
    app.post('/encrypt_with_public_key', (req, res) => {
        const { value } = req.body;
        try {
            // Encode as a proper array with a single value
            const plainText = batchEncoder.encode(
                Int32Array.from([Number(value)])
            );
            const ciphertext = seal.CipherText();
            encryptor.encrypt(plainText, ciphertext);
    
            const compressedCipherText = compress(ciphertext.save());
            res.json({ ciphertext: compressedCipherText});
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 5. Perform Addition on Ciphertext
    app.post('/add_to_ciphertext', (req, res) => {
        const { ciphertext, value } = req.body;
        try {
            const decompressedCipherText = decompress(ciphertext);
            const ct = seal.CipherText();
            ct.load(context, decompressedCipherText);

            const plainText = batchEncoder.encode(
                Int32Array.from([Number(value)]) // This could also be a Uint32Array
                );
            const timestampBeforeAddtion = Date.now();
            console.log("timestampBefore: " + timestampBeforeAddtion); // 输出：当前时间的毫秒数，例如 1700659152764
            const result = evaluator.addPlain(ct, plainText);
            const timestampAfterAddtion = Date.now();
            console.log("timestampAfter: " + timestampAfterAddtion); // 输出：当前时间的毫秒数，例如 1700659152764
            console.log("Cipher text lenght", result.save().length);
            res.json({ updated_ciphertext: compress(result.save()) });
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 6. Get Encryption Parameters
    app.get('/get_encryption_parameters', (req, res) => {
        res.json({ encryption_parameters: parms.save() });
    });

    app.get('/get_a_test_value', (req, res) => {
        // Encode as a proper array with a single value
        try {
            const plainText = batchEncoder.encode(
                Int32Array.from([10])
            );
            const ciphertext = seal.CipherText();
            encryptor.encrypt(plainText, ciphertext);
            const ct64 = ciphertext.save();
        
            const cp = seal.CipherText()
            cp.load(context, ct64)
            const decy = decryptor.decrypt(cp);
            const decoded = batchEncoder.decode(decy);
            res.json({plaintext:decoded[0]});
        } catch (err) {
            res.status(400).json({error: err.message});
        }
        
    });

    app.listen(port, () => {
        console.log(`Node.js homomorphic encryption service running at http://localhost:${port}`);
    });
}

// Call the initialization function
initialize().catch((err) => {
    console.error('Failed to initialize SEAL:', err);
});