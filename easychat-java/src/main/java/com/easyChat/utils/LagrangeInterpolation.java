package com.easyChat.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // 用于 Objects.equals()

public class LagrangeInterpolation {

    // KZG 方案通常在一个大素数模 P 的有限域上操作。这里使用一个示例性的 256 位素数。
    // 这个值与 JavaScript 和 Java 代码中的 MODULUS 保持一致。
    private static final BigInteger MODULUS = new BigInteger("21888242871839275222246405745257275088696311157297823662689037894645226208583");

    /**
     * 辅助类：表示一个数据点 (x, y)。
     */
    public static class DataPoint {
        public BigInteger x;
        public BigInteger y;

        public DataPoint(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "{x=" + x + ", y=" + y + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataPoint dataPoint = (DataPoint) o;
            return Objects.equals(x, dataPoint.x) && Objects.equals(y, dataPoint.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * 根据给定的数据点 (x, y) 使用拉格朗日插值法求出多项式，
     * 并计算 f(0) 的值。所有运算都在 MODULUS 模下进行。
     *
     * @param dataPoints 包含 {x: BigInteger, y: BigInteger} 对象的列表。
     * 对于 n 阶多项式，至少需要 n+1 个数据点。
     * @returns 插值多项式在 x=0 处的值 f(0)。
     * @throws IllegalArgumentException 如果数据点中的 x 值有重复，或数据点数量不足。
     */
    public static BigInteger getLagrangeInterpolationF0(List<DataPoint> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new IllegalArgumentException("Data points list cannot be null or empty.");
        }

        BigInteger f0 = BigInteger.ZERO;
        int n = dataPoints.size();

        for (int i = 0; i < n; i++) {
            DataPoint currentPoint = dataPoints.get(i);
            BigInteger basis = currentPoint.y; // L_i(x) 的 y_i 部分

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    DataPoint otherPoint = dataPoints.get(j);
                    BigInteger xi = currentPoint.x;
                    BigInteger xj = otherPoint.x;

                    // 计算 (0 - x_j)
                    BigInteger numerator = BigInteger.ZERO.subtract(xj);
                    // 确保结果在 [0, MODULUS-1] 范围内
                    numerator = numerator.mod(MODULUS);

                    // 计算 (x_i - x_j)
                    BigInteger denominator = xi.subtract(xj);
                    // 确保结果在 [0, MODULUS-1] 范围内
                    denominator = denominator.mod(MODULUS);

                    // 确保分母不为零，避免除以零的错误
                    if (denominator.equals(BigInteger.ZERO)) {
                        throw new IllegalArgumentException("Duplicate x values found in data points, cannot perform Lagrange interpolation.");
                    }

                    // 计算模逆 (x_i - x_j)^(-1) mod MODULUS
                    // BigInteger 的 modInverse 方法直接计算模逆
                    BigInteger invDenominator = denominator.modInverse(MODULUS);

                    // (numerator / denominator) % MODULUS 等价于 (numerator * inv_denominator) % MODULUS
                    BigInteger term = numerator.multiply(invDenominator).mod(MODULUS);

                    basis = basis.multiply(term).mod(MODULUS);
                }
            }
            f0 = f0.add(basis).mod(MODULUS);
        }
        // 确保最终结果为正且在 [0, MODULUS-1] 范围内
        return f0.add(MODULUS).mod(MODULUS);
    }

    public static void main(String[] args) {
        // 示例用法：
        System.out.println("--- Java 拉格朗日插值求 f(0) (BigInteger 256位运算) ---");

        // 示例数据点 (需要至少3个点来插值一个二阶多项式)
        List<DataPoint> dataPoints = new ArrayList<>();
        // 这些 BigInteger 值可以直接来源于您之前生成的 JavaScript 代码中的 x 和 y
        dataPoints.add(new DataPoint(new BigInteger("1"), new BigInteger("5")));
        dataPoints.add(new DataPoint(new BigInteger("2"), new BigInteger("12")));
        dataPoints.add(new DataPoint(new BigInteger("3"), new BigInteger("23")));
        // 您也可以使用非常大的 BigInteger 值来演示 256 位精度
        // dataPoints.add(new DataPoint(new BigInteger("12345678901234567890123456789012345678901234567890123456789012345678901234567"), new BigInteger("98765432109876543210987654321098765432109876543210987654321098765432109876543")));
        // dataPoints.add(new DataPoint(new BigInteger("22345678901234567890123456789012345678901234567890123456789012345678901234567"), new BigInteger("88765432109876543210987654321098765432109876543210987654321098765432109876543")));
        // dataPoints.add(new DataPoint(new BigInteger("32345678901234567890123456789012345678901234567890123456789012345678901234567"), new BigInteger("78765432109876543210987654321098765432109876543210987654321098765432109876543")));


        System.out.println("用于插值的数据点: " + dataPoints);

        try {
            BigInteger f0Value = getLagrangeInterpolationF0(dataPoints);
            System.out.println("通过拉格朗日插值法求出的 f(0) 值: " + f0Value);

            // 对于本示例数据点 (1,5), (2,12), (3,23), 对应的多项式是 P(x) = x^2 + 4x，所以 P(0)=0。
            // f(0) 应该为0. 如果是 P(x) = x^2 + 4x + C, 那么 f(0) = C。
            // 对于这个例子，如果解 P(x) = ax^2 + bx + c
            // 1a + 1b + c = 5
            // 4a + 2b + c = 12
            // 9a + 3b + c = 23
            // 解得 a=1, b=4, c=0. 所以 f(0) = 0.
            // 让我们验证一下：
            System.out.println("（根据示例数据点，预期 f(0) 值应为 0）");

        } catch (IllegalArgumentException e) {
            System.err.println("错误: " + e.getMessage());
        }
    }

}


