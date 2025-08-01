package com.easyChat.controller;

import com.easyChat.entity.vo.ResponseVo;
import com.ikelin.cuckoofilter.CuckooFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

@RestController
@RequestMapping("/CuckooFilter")
public class CuckooFilterController extends ABaseController {
    // 使用默认参数创建（容量、误报率等自动计算）
    private static CuckooFilter filter = CuckooFilter.create(10000)  // 预期最大容量
            .withFalsePositiveProbability(0.002)  // 可选：设置误报率
            .withEntriesPerBucket(2)  // 可选：每个桶的条目数（必须是2的幂）
            .withBitsPerEntry(8)  // 可选：长度（1-32位）
            .build();

    static {
        //
        String[] D = {"毒品", "钻石", "粉"};
        for (int i = 0; i < D.length; i++) {
            long itemHash = computeHash(D[i]);
            System.out.println(itemHash);
            filter.put(itemHash);
        }
    }

    @RequestMapping("/filter")
    public ResponseVo filter(String[] hashCodeStr) throws NoSuchAlgorithmException {

        Vector<String> bool = new Vector<>();
        for (String str : hashCodeStr) {
            long hashCode = Long.parseLong(str); // 解析为 long
            if (filter.mightContain(hashCode)) {
                bool.add("True");
            } else {
                bool.add("False");
            }
        }
        return getSuccessResponseVo(bool);
    }

    private static long computeHash(String word) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(word.getBytes(StandardCharsets.UTF_8));

            // 确保每个字节转换为无符号 long 后再位移
            return (((hashBytes[0] & 0xFFL) << 56) |
                    ((hashBytes[1] & 0xFFL) << 48) |
                    ((hashBytes[2] & 0xFFL) << 40) |
                    ((hashBytes[3] & 0xFFL) << 32) |
                    ((hashBytes[4] & 0xFFL) << 24) |
                    ((hashBytes[5] & 0xFFL) << 16) |
                    ((hashBytes[6] & 0xFFL) << 8) |
                    (hashBytes[7] & 0xFFL));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("哈希算法不可用", e);
        }
    }
}
//javaScript
//async function computeHash(word) {
//    const encoder = new TextEncoder();
//    const data = encoder.encode(word);
//    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
//    const hashArray = new Uint8Array(hashBuffer);
//
//    // 取前8字节转换为long（大端序）
//    return (hashArray[0] << 56) |
//           (hashArray[1] << 48) |
//           (hashArray[2] << 40) |
//           (hashArray[3] << 32) |
//           (hashArray[4] << 24) |
//           (hashArray[5] << 16) |
//           (hashArray[6] << 8) |
//            hashArray[7];
