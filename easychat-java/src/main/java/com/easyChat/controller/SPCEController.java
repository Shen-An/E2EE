package com.easyChat.controller;

import com.easyChat.constants.Constants;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.enums.ResponseCodeEnum;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/SPCE")
public class SPCEController extends ABaseController {
    private static final String FILE_PATH = Constants.FILEPATH;

    @RequestMapping("/getPk")
    public ResponseVo downloadPythonOutput() {
        try {
            // 读取文件内容
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return getSuccessResponseVo(ResponseEntity.ok()
                    .headers(headers)
                    .body(content));
        } catch (IOException e) {
            e.printStackTrace();
            return getBusinessErrorResponseVo(null, ResponseCodeEnum.CODE_602.getMsg());
        }
    }

    @RequestMapping("/sendCt")
    public ResponseVo receiveJsonData(@RequestBody Map<String, Object> data) {
        try {
            // 打印接收到的数据
            System.out.println("接收到的 JSON 数据: " + data);

            // 这里简单示例对数据进行处理，实际可根据业务需求存储到数据库等
            // 比如提取 ct 的值进行业务逻辑处理
            if (data.containsKey("ct")) {
                Object ctValue = data.get("ct");
                System.out.println("提取到的 ct 值: " + ctValue);
            }

            return getSuccessResponseVo(null);
        } catch (Exception e) {
            e.printStackTrace();
            return getBusinessErrorResponseVo(null, "数据处理失败");
        }
    }

}