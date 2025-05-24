package com.easyChat.controller;


import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.vo.UserInfoVo;
import com.easyChat.exception.BusinessException;
import com.easyChat.redis.RedisComponent;
import com.easyChat.redis.RedisUtils;
import com.easyChat.service.UserInfoService;

import com.easyChat.utils.CopyUtils;
import com.easyChat.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    RedisUtils redisUtils;

    private static Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private MessageHandler messageHandler;

    @RequestMapping("/checkCode")
    public ResponseVo checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);//与前端适配
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        //设置验证码有效时间=5min
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_1MIN * 5);


        logger.info("验证码是：{}", code);
        String checkCodeBase64 = captcha.toBase64();
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("checkCode", checkCodeBase64);
        resultMap.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVo(resultMap);
    }

    @RequestMapping("/register")
    public ResponseVo register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode) throws BusinessException {
//        System.out.println(password);
        try {
            //失败
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, password);
            return getSuccessResponseVo(null);
        } finally {
            //无论是否成功，在redis中删除相应的key和value
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    @RequestMapping("/login")
    public ResponseVo login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCode) throws BusinessException {
        try {
            //失败
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }

            UserInfoVo userInfoVo = userInfoService.login(email, password);
            return getSuccessResponseVo(userInfoVo);
        } finally {
            //无论是否成功，在redis中删除相应的key和value
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }

    }

    @RequestMapping("/getSysSetting")
    public ResponseVo getSysSetting() {
        return getSuccessResponseVo(redisComponent.getSysSetting());
    }

    @RequestMapping("/getSysSetting1")
    public ResponseVo getSysSetting1() {
        Map<String,Integer> map = new HashMap<>();
        map.put("0",(new SysSettingDto()).getMaxImageSize());
        map.put("1",(new SysSettingDto()).getMaxVideoSize());
        map.put("2",(new SysSettingDto()).getMaxFileSize());

        return getSuccessResponseVo(map);
    }


    @RequestMapping("test")
    public ResponseVo test() {
        MessageSendDto sendDto = new MessageSendDto();
        sendDto.setMessageContent("hhhtest"+System.currentTimeMillis());
        messageHandler.sendMessage(sendDto);
        return getSuccessResponseVo(null);
    }

}
