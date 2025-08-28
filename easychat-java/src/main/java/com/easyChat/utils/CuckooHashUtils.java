package com.easyChat.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CuckooHashUtils {
    // 布谷鸟哈希函数
    public static Long cuckooHash(String key, int size, int func_num) {
        if (func_num == 0) {
            return H(key)% size;
        } else {
            return (H(key) % size+2)% size;
        }
    }
    // 哈希函数H
    public static long H(String x) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(x.getBytes(StandardCharsets.UTF_8));
            // 使用 BigInteger 处理大整数
            BigInteger hashValue = new BigInteger(1, hash);
            BigInteger modValue = BigInteger.valueOf(1000);
            return hashValue.mod(modValue).longValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
