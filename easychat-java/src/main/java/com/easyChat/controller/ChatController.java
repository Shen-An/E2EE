package com.easyChat.controller;

import com.alibaba.fastjson.JSON;
import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.constants.Constants;
import com.easyChat.entity.config.AppConfig;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.enums.MessageTypeEnum;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.enums.UserContactStatusEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.service.ChatMessageService;
import com.easyChat.service.ChatSessionUserService;
import com.easyChat.service.UserContactService;
import com.easyChat.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
    @Resource
    private UserContactService userContactService;

    @RequestMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVo sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Max(500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
//        //TODO测试 非好友是否能通过
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(contactId);
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = userContactService.findListByParam(userContactQuery);
        List userIdList = new ArrayList();
        for (UserContact userContact : userContactList) {
            userIdList.add(userContact.getUserId());
        }
        if (!userIdList.contains(tokenUserInfoDto.getUserId())) {
            if(contactId.contains("G")){
                //群
                throw new BusinessException(ResponseCodeEnum.CODE_903);
            }else if(contactId.contains("U")){
                //用户
                throw new BusinessException(ResponseCodeEnum.CODE_902);
            }

        }
//        System.out.println("66" + contactId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        chatMessage.setMessageType(messageType);
        MessageSendDto messageSendDto = chatMessageService.saveMessage(chatMessage, tokenUserInfoDto);
        return getSuccessResponseVo(messageSendDto);
    }

    @RequestMapping("/uploadFile")
    @GlobalInterceptor
    public ResponseVo uploadFile(HttpServletRequest request,
                                 @NotNull Long messageId,
                                 @NotNull MultipartFile file,
                                 @NotEmpty MultipartFile cover) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());

        if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > (new SysSettingDto()).getMaxImageSize() * Constants.FILE_SIZE_MB) {
            //是图片但是大小超出
//            ResponseVo responseVo = new ResponseVo();
//            responseVo.setStatus("error");
//            responseVo.setCode(603);
//            responseVo.setInfo("文件大小超出限制");
//            return responseVo;
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        }
        chatMessageService.saveMessageFile(tokenUserInfoDto.getUserId(), messageId, file, cover);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @NotEmpty String fileId,
                             @NotNull Boolean showCover) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = null;
            //不是全数字，则为头像文件id
            if (!StringTools.isNumber(fileId)) {
                String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
                String avatarPath = appConfig.getProjectFolder() + avatarFolderName + fileId + Constants.IMAGE_SUFFIX;

                //需要展示头像
                if (showCover) {
                    avatarPath = avatarPath + Constants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    response.reset();
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(
                            JSON.toJSONString(
                                    getBusinessErrorResponseVo(null, ResponseCodeEnum.CODE_602.getMsg())
                            )
                    );
                    return; // 立即终止执行
//                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
            } else {
                file = chatMessageService.downLoadFile(tokenUserInfoDto, Long.parseLong(fileId), showCover);
            }
            response.setContentType("application/x-msdownload;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;");
            response.setContentLengthLong(file.length());

            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len;

            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            logger.error("下载文件失败", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }
}
