package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.utils.KZGUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;


@RestController
@RequestMapping("/SPCESwTT")
public class SPCESwTTController extends ABaseController {


    @GlobalInterceptor
    @RequestMapping("/checkKZG")
    public ResponseVo checkKZG(String isValid) {
        System.out.println(isValid);
        return getSuccessResponseVo(null);
    }
}
