package com.easyChat.controller;


import com.easyChat.constants.Constants;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.redis.RedisUtils;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController{

    @Resource
    RedisUtils redisUtils;

    private static Logger logger = LoggerFactory.getLogger(AccountController.class);

    @RequestMapping("/checkCode")
    public ResponseVo checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,43);//与前端适配
        String code = captcha.text();

        //设置验证码有效时间=5min
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE,code,Constants.REDIS_TIME_1MIN*5);

        String checkCodeKey = UUID.randomUUID().toString();
        logger.info("验证码是：{}",code);
        String checkCodeBase64 = captcha.toBase64();
        Map<String,String> resultMap = new HashMap<String,String>();
        resultMap.put("checkCode",checkCodeBase64);
        resultMap.put("checkCodeKey",checkCodeKey);
        return getSuccessResponseVo(resultMap);

    }
}
