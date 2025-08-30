package com.easyChat.utils;

import java.util.*;

public class CyclicGroupGeneratorUtils {
    private static final Random random = new Random();

    /**
     * 生成素数阶循环群 Z_p* 的所有元素
     * @param generator 生成元（原根）
     * @param modulus   素数模
     * @return 包含所有元素的有序列表
     */
    public static List<Integer> generateAllElements(int generator, int modulus) {
        Set<Integer> elements = new HashSet<>();
        int element = 1;
        int exponent = 0;

        while (exponent < modulus - 1) {
            elements.add(element);
            element = (element * generator) % modulus;
            exponent++;
        }

        List<Integer> sortedElements = new ArrayList<>(elements);
        Collections.sort(sortedElements);
        return sortedElements;
    }

    /**
     * 随机选取群中的一个元素
     * @param generator 生成元（原根）
     * @param modulus   素数模
     * @return 随机选取的元素
     */
    public static int getRandomElement(int generator, int modulus) {
        List<Integer> elements = generateAllElements(generator, modulus);
        int randomIndex = random.nextInt(elements.size());
        return elements.get(randomIndex);
    }
}
