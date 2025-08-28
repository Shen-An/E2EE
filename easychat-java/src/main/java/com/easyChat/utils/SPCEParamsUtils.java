package com.easyChat.utils;

import com.easyChat.constants.Constants;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;

import static com.easyChat.utils.CuckooHashUtils.H;
import static com.easyChat.utils.CuckooHashUtils.cuckooHash;

public class SPCEParamsUtils {
    public int g;
    public int alpha;

    public SPCEParamsUtils() {

        g = 3;
        alpha = 5;

    }



    // 随机自规约
    public static long[] randomSelfReduction(int g, int A, long H_y, long B) {
        // 固定beta和gamma进行测试
        int beta = 100;
        int gamma = 200;
        long Q = (long) ((Math.pow(g, beta) % 1000 * Math.pow(H_y, gamma) % 1000) % 1000);
        long S = (long) ((Math.pow(A, beta) % 1000 * Math.pow(B, gamma) % 1000) % 1000);
        return new long[]{Q, S};
    }
    public List<List<BigInteger>> generateB() {
        List<List<BigInteger>> B = new ArrayList<>();
        int tableSize = Constants.D.length * 100;
        for (int i = 0; i < tableSize; i++) {
            B.add(new ArrayList<>());
        }
        for (String word : Constants.D) {
            Long index0 = cuckooHash(word, tableSize, 0);
            Long index1 = cuckooHash(word, tableSize, 1);
            long H_word = H(word);
            B.get(Math.toIntExact(index0)).add(BigInteger.valueOf(H_word).pow(alpha).mod(BigInteger.valueOf(1000)));

            B.get(Math.toIntExact(index1)).add(BigInteger.valueOf(H_word).pow(alpha).mod(BigInteger.valueOf(1000)));
        }
        return B;
    }

    public static void main(String[] args) {
        SPCEParamsUtils spceParamsUtils = new SPCEParamsUtils();
        long A = (long) Math.pow(spceParamsUtils.g, spceParamsUtils.alpha) % 1000;
        List<List<BigInteger>> B = new ArrayList<>();
        int tableSize = Constants.D.length * 100;
        for (long i = 0; i < tableSize; i++) {
            B.add(new ArrayList<>());
        }
        for (String word : Constants.D) {
            Long index0 = cuckooHash(word, tableSize, 0);
            System.out.println(index0);
            Long index1 = cuckooHash(word, tableSize, 1);
            long H_word = H(word);
            B.get(Math.toIntExact(index0)).add(BigInteger.valueOf(H_word).pow(spceParamsUtils.alpha).mod(BigInteger.valueOf(1000)));

            B.get(Math.toIntExact(index1)).add(BigInteger.valueOf(H_word).pow(spceParamsUtils.alpha).mod(BigInteger.valueOf(1000)));
        }
        // 打印B数组内容
        for (int i = 0; i < tableSize; i++) {
            System.out.println("哈希表位置 " + i + ": " + B.get(i));
        }
        System.out.println(A);
        System.out.println(spceParamsUtils.generateB());
    }
    public static String decrypt(int Q, String encryptedHex, String ivHex, int alpha) throws Exception {
        // 1. 计算 S = Q^alpha mod 1000
        BigInteger s = BigInteger.valueOf(Q)
                .pow(alpha)
                .mod(BigInteger.valueOf(1000));
        String sStr = s.toString();

        // 2. PBKDF2生成AES密钥（参数与前端一致）
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(
                sStr.toCharArray(),
                new byte[16], // Salt全0
                100000,       // 迭代次数
                128           // 密钥长度
        );
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec aesKey = new SecretKeySpec(secretKey.getEncoded(), "AES");

        // 3. 转换IV和加密数据
        byte[] iv = hexToBytes(ivHex);
        byte[] encryptedData = hexToBytes(encryptedHex);

        // 4. AES-CBC解密
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encryptedData);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int val = Integer.parseInt(hex.substring(index, index + 2), 16);
            bytes[i] = (byte) val;
        }
        return bytes;
    }
}
