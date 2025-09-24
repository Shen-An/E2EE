package com.easyChat.controller;

import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessageIllegal;
import com.easyChat.entity.query.ChatMessageIllegalQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.ChatMessageIllegalService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController extends ABaseController{
    @Resource
    private ChatMessageIllegalService chatMessageIllegalService;

    @RequestMapping("/selectIllegalInformation")
    public ResponseVo selectIllegalInformation(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        ChatMessageIllegalQuery chatMessageIllegalQuery = new ChatMessageIllegalQuery();
        List<ChatMessageIllegal> list= chatMessageIllegalService.findListByParam(chatMessageIllegalQuery);
        if(tokenUserInfoDto == null ||tokenUserInfoDto.getAdmin()==null || !tokenUserInfoDto.getAdmin()){
            for (ChatMessageIllegal chatMessageIllegal : list) {
                chatMessageIllegal.setMessageContent("*********");
            }
        }
        return getSuccessResponseVo(list);

    }
}
