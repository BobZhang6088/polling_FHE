const express = require('express');
const SEAL = require('node-seal');
const bodyParser = require('body-parser');
const fs = require('fs');

const app = express();
const port = 3000;

app.use(bodyParser.json());

async function initialize() {
    const seal = await SEAL();

    // Encryption Parameters Setup
    const parms = seal.EncryptionParameters(seal.SchemeType.bfv);
    parms.setPolyModulusDegree(1024);
    parms.setCoeffModulus(seal.CoeffModulus.BFVDefault(1024));
    parms.setPlainModulus(seal.PlainModulus.Batching(1024, 20));
    const context = seal.Context(parms, true, seal.SecurityLevel.tc128);

    const secretKeyFile = 'secret_key.txt';
    if (fs.existsSync(secretKeyFile)){
        fs.unlinkSync(secretKeyFile);
    }
    // Helper function to load or generate the secret key
    const loadOrGenerateSecretKey = () => {
        if (fs.existsSync(secretKeyFile)) {


            const savedKeyString = fs.readFileSync(secretKeyFile, 'utf-8');
            const secretKey = seal.SecretKey();
            secretKey.load(context, savedKeyString);
            console.log('Loaded secret key from file.');
            return secretKey;
        } else {
            const keyGenerator = seal.KeyGenerator(context);
            const secretKey = keyGenerator.secretKey();
            const savedKeyString = secretKey.save();
            fs.writeFileSync(secretKeyFile, savedKeyString, 'utf-8');
            console.log('Generated and saved new secret key.');
            return secretKey;
        }
    };

    // Load or generate the secret key
    const secretKey = loadOrGenerateSecretKey();
    const keyGenerator = seal.KeyGenerator(context, secretKey);
    const publicKey = keyGenerator.createPublicKey();

    const encryptor = seal.Encryptor(context, publicKey);
    const decryptor = seal.Decryptor(context, secretKey);
    const evaluator = seal.Evaluator(context);

    // 1. Get Secret Key (read-only since it's already generated)
    app.get('/get_secret_key', (req, res) => {
        res.json({ secret_key: secretKey.save() });
    });

    // 2. Generate Public Key
    app.get('/get_public_key', (req, res) => {
        try {
            res.json({ public_key: publicKey.save() });
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 3. Decrypt
    app.post('/decrypt', (req, res) => {
        const { ciphertext } = req.body;
        try {
            const ct = seal.CipherText();
            ct.load(context, ciphertext);
            const plaintext = seal.PlainText();
            decryptor.decrypt(ct, plaintext);

            res.json({ plaintext: plaintext.toString() });
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 4. Encrypt
    app.post('/encrypt_with_public_key', (req, res) => {
        const { value } = req.body;
        try {
            const plaintext = seal.PlainText(value.toString());
            const ciphertext = seal.CipherText();
            encryptor.encrypt(plaintext, ciphertext);

            res.json({ ciphertext: ciphertext.save() });
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 5. Perform Addition on Ciphertext
    app.post('/add_to_ciphertext', (req, res) => {
        const { ciphertext, value } = req.body;
        try {
            const ct = seal.CipherText();
            ct.load(context, ciphertext);

            const plainToAdd = seal.PlainText(value.toString());
            evaluator.addPlainInplace(ct, plainToAdd);

            res.json({ updated_ciphertext: ct.save() });
        } catch (err) {
            res.status(400).json({ error: err.message });
        }
    });

    // 6. Get Encryption Parameters
    app.get('/get_encryption_parameters', (req, res) => {
        res.json({ encryption_parameters: parms.save() });
    });

    app.listen(port, () => {
        console.log(`Node.js homomorphic encryption service running at http://localhost:${port}`);
    });
}

// Call the initialization function
initialize().catch((err) => {
    console.error('Failed to initialize SEAL:', err);
});