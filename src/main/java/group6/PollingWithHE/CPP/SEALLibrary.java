package group6.PollingWithHE.CPP;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
public interface SEALLibrary extends Library {
    SEALLibrary INSTANCE = (SEALLibrary) Native.load("sealwrapper", SEALLibrary.class);

    // Generate a private key as a String
    // Declare the native functions with Pointer return types
    Pointer generatePrivateKey();
    Pointer generatePublicKey(Pointer secretKey);
    Pointer encryptInteger(int value, Pointer publicKey);
    Pointer addEncryptedIntegers(Pointer ciphertext1, Pointer ciphertext2);
    void releaseMemory(Pointer buffer);  // Add this line
}