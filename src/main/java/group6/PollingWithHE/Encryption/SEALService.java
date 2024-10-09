package group6.PollingWithHE.Encryption;

import org.springframework.stereotype.Service;
import group6.PollingWithHE.CPP.*;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Service
public class SEALService {

    public byte[] generatePrivateKey() {
        Pointer pointer = SEALLibrary.INSTANCE.generatePrivateKey();
        byte[] result = bufferToByteArray(pointer);
        SEALLibrary.INSTANCE.releaseMemory(pointer);  // Free memory
        return result;
    }

//    public byte[] generatePublicKey(byte[] secretKey) {
//        Pointer secretKeyPointer = Pointer.createConstant(secretKey);
//        Pointer pointer = SEALLibrary.INSTANCE.generatePublicKey(secretKeyPointer);
//        return bufferToByteArray(pointer);
//    }
//
//    public byte[] encryptInteger(int value, byte[] publicKey) {
//        Pointer publicKeyPointer = Pointer.createConstant(publicKey);
//        Pointer pointer = SEALLibrary.INSTANCE.encryptInteger(value, publicKeyPointer);
//        return bufferToByteArray(pointer);
//    }
//
//    public byte[] addEncryptedIntegers(byte[] ciphertext1, byte[] ciphertext2) {
//        Pointer pointer1 = Pointer.createConstant(ciphertext1);
//        Pointer pointer2 = Pointer.createConstant(ciphertext2);
//        Pointer resultPointer = SEALLibrary.INSTANCE.addEncryptedIntegers(pointer1, pointer2);
//        return bufferToByteArray(resultPointer);
//    }
//
//    // Utility to convert ByteBuffer/Pointer to byte array
    private byte[] bufferToByteArray(Pointer pointer) {
        if (pointer == null) return null;
        ByteBuffer buffer = pointer.getByteBuffer(0, 4096); // Adjust the size accordingly
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return byteArray;
    }
}