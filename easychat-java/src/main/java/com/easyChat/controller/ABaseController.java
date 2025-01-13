package com.easyChat.controller;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.enums.ResponseCodeEnum;;

import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.exception.BusinessException;
import com.easyChat.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class ABaseController{

    @Resource
    private RedisUtils redisUtils;

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T>ResponseVo getSuccessResponseVo(T t){
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setStatus(STATUC_SUCCESS);
        responseVo.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVo.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVo.setData(t);
        return responseVo;
    }

    protected <T>ResponseVo getServerErrorResponseVo(T t){
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setStatus(STATUC_ERROR);
        responseVo.setCode(ResponseCodeEnum.CODE_500.getCode());
        responseVo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        responseVo.setData(t);
        return responseVo;
    }

    protected <T>ResponseVo getBusinessErrorResponseVo(BusinessException e, T t){
        ResponseVo<T> responseVo = new ResponseVo<>();
        responseVo.setStatus(STATUC_ERROR);
        if(e.getCode() == null){
            responseVo.setCode(ResponseCodeEnum.CODE_600.getCode());
        }else {
            responseVo.setCode(e.getCode());
        }
        responseVo.setInfo(e.getMessage());
        responseVo.setData(t);
        return responseVo;
    }
    protected TokenUserInfoDto getTokenUserInfo(HttpServletRequest request){
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto)redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }
}
