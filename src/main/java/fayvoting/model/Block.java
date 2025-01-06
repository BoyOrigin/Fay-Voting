package fayvoting.model;

import com.google.gson.Gson;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class Block {
    private static final Gson GSON = new Gson();
    private int index;
    private long timestamp;
    private String data;  // AES-encrypted data
    private String previousHash;
    private String hash;

    public Block(int index, List<Vote> votes, String previousHash, SecretKey secretKey) throws Exception {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.previousHash = previousHash;
        assert votes instanceof ArrayList;
        this.data = encryptData(votes, secretKey);
        this.hash = calculateHash();
    }

    private String calculateHash() throws Exception {
        String input = index + timestamp + data + previousHash;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private String encryptData(List<Vote> votes, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(GSON.toJson(votes).getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decryptData(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public List<Vote> getVotes(SecretKey secretKey) throws Exception {
        return GSON.fromJson(decryptData(data, secretKey), ArrayList.class);
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                ", timestamp=" + timestamp +
                ", data='" + data + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}