package com.easyChat.controller;

import com.easyChat.entity.vo.ResponseVo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockChainController extends ABaseController {
    @RequestMapping("/test")
    public ResponseVo test() {
      return getSuccessResponseVo(null);
    }
}
