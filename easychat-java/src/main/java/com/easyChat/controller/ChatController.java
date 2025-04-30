package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.config.AppConfig;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.enums.MessageTypeEnum;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.service.ChatMessageService;
import com.easyChat.service.ChatSessionUserService;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/chat")
public class ChatController extends ABaseController {
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Resource
    private ChatMessageService chatMessageService;
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private AppConfig appConfig;

    @RequestMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVo sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Max(500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageType);
        if(messageTypeEnum == null || ArrayUtils.contains(new Integer[]{
                MessageTypeEnum.CHAT.getType(),MessageTypeEnum.MEDIA_CHAT.getType()},messageType
        )){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        MessageSendDto messageSendDto =chatMessageService.saveMessage(chatMessage,tokenUserInfoDto);
        return getSuccessResponseVo(messageSendDto);
    }
}
