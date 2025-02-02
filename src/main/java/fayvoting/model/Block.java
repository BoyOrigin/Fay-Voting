package fayvoting.model;

import fayvoting.util.AESUtil;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Block implements Serializable {
    String previousHash;
    String encryptedData;
    String hash;
    long timestamp;

    public Block(String data, String previousHash) {
        try {
            this.encryptedData = AESUtil.encrypt(data);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = previousHash + timestamp + encryptedData;
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDecryptedData() {
        try {
            return AESUtil.decrypt(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}