package com.easyChat.controller;

import com.easyChat.enums.ResponseCodeEnum;;

import com.easyChat.entity.vo.ResponseVo;

public class ABaseController{
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
}
