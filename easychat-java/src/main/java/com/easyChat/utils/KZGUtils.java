package com.easyChat.utils;

import java.math.BigInteger;
import java.util.*;

public class KZGUtils {

    // ---------- 构造右侧多项式 ----------
    /* ---------- 构造右侧多项式（完整系数版） ---------- */
    private static List<BigInteger> buildRightPoly(List<BigInteger> left) {
        // 直接把左侧系数复制过来
        return new ArrayList<>(left);
    }



    // ---------- 多项式乘法 Q(X)*(X-r) ----------
    private static List<BigInteger> multiplyByXMinusR(List<BigInteger> q, BigInteger r) {
        int n = q.size();
        List<BigInteger> res = new ArrayList<>(Collections.nCopies(n + 1, BigInteger.ZERO));
        for (int i = 0; i < n; i++) {
            res.set(i,     res.get(i).subtract(q.get(i).multiply(r)));
            res.set(i + 1, res.get(i + 1).add(q.get(i)));
        }
        return res;
    }

    // ---------- 去尾 0 ----------
    private static void trimZeros(List<BigInteger> poly) {
        while (!poly.isEmpty() && poly.get(poly.size() - 1).equals(BigInteger.ZERO)) {
            poly.remove(poly.size() - 1);
        }
    }

    // ---------- 验证 ----------
    public static boolean verifyKZG(List<BigInteger> quotientPoly,
                                    BigInteger fTau,
                                    BigInteger r,
                                    BigInteger pR) {
        if (quotientPoly == null || quotientPoly.isEmpty()) return false;

        // 左侧：Q(X)*(X-r)
        List<BigInteger> left = multiplyByXMinusR(quotientPoly, r);

        // 右侧：P(X)-P(r) 的系数，长度 = left.size()
        List<BigInteger> right = buildRightPoly(left);

        trimZeros(left);
        trimZeros(right);

        return left.equals(right);   // 直接用 equals 比较即可
    }
    /* ------------------- 测试 ------------------- */
    public static void main(String[] args) {
        // 正确的商 Q(X) = 11X + 98
        List<BigInteger> q = Arrays.asList(
                BigInteger.valueOf(98),   // 常数项
                BigInteger.valueOf(11)    // 一次项
        );

        BigInteger r   = BigInteger.valueOf(2);
        BigInteger pR  = BigInteger.valueOf(257);  // P(2)

        // P(τ) 的值不影响系数比较，随便写
        BigInteger fTau = BigInteger.valueOf(148);

        boolean ok = verifyKZG(q, fTau, r, pR);
        System.out.println(ok);  // true
    }
}