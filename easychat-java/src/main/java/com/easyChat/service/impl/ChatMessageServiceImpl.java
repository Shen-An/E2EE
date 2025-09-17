package com.easyChat.service.impl;

import com.easyChat.AI.AIExec;
import com.easyChat.constants.Constants;
import com.easyChat.controller.CuckooFilterController;
import com.easyChat.entity.config.AppConfig;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.po.ChatSession;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.ChatMessageQuery;
import com.easyChat.entity.query.ChatSessionQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.*;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.ChatMessageMapper;
import com.easyChat.mappers.ChatSessionMapper;
import com.easyChat.mappers.UserContactMapper;
import com.easyChat.redis.RedisComponent;
import com.easyChat.service.ChatMessageService;
import com.easyChat.utils.CopyUtils;
import com.easyChat.utils.DateUtils;
import com.easyChat.utils.StringTools;
import com.easyChat.websocket.MessageHandler;
import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @Description:聊天信息表Service
 * @author:Shen-An
 * @date:2025/02/07
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    private static Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
    @Resource
    private MessageHandler messageHandler;
    @Resource
    private AppConfig appConfig;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private CuckooFilterController cuckooFilterController;

    /**
     * 聊天信息表根据条件查询列表
     */
    public List<ChatMessage> findListByParam(ChatMessageQuery query) {
        return this.chatMessageMapper.selectList(query);
    }

    /**
     * 聊天信息表根据条件查询数量
     */
    public Integer findCountByParam(ChatMessageQuery query) {
        return this.chatMessageMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<ChatMessage> findListByPage(ChatMessageQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(query);
        PaginationResultVo<ChatMessage> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据MessageId查询
     */
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId更新
     */
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    public Integer deleteChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        //不是机器人回复，判断好友状态
        if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
            List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
//            for (String contact : contactList) {
////                System.out.println(contact);
//            }
//            System.out.println(chatMessage.getContactId());
            if (!contactList.contains(chatMessage.getContactId())) {

                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (userContactTypeEnum == UserContactTypeEnum.USER) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        String sessionId = null;
        String sendUserId = tokenUserInfoDto.getUserId();
        String contactId = chatMessage.getContactId();

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (UserContactTypeEnum.USER == contactTypeEnum) {
            sessionId = StringTools.getChatSessionId4User(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringTools.getChatSessionId4Group(contactId);
        }
        chatMessage.setSessionId(sessionId);
        Long curTime = System.currentTimeMillis();
        chatMessage.setSendTime(curTime);

        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (messageTypeEnum == null || !ArrayUtils.contains(new Integer[]{
                        MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()},
                chatMessage.getMessageType()
        )) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        chatMessage.setStatus(status);

        String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);

        //更新会话
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            chatSession.setLastMessage(tokenUserInfoDto.getNickName() + ": " + messageContent);
        }
        chatSession.setLastReceiveTime(curTime);
        chatSessionMapper.updateBySessionId(chatSession, sessionId);


        //记录消息表
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setContactType(contactTypeEnum.getType());

        chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);

        if (Constants.ROBOT_UID.equals(contactId)) {
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            TokenUserInfoDto robot = new TokenUserInfoDto();
            robot.setUserId(sysSettingDto.getRobotUid());
            robot.setNickName(sysSettingDto.getRobotNickName());
            ChatMessage robotChatMessage = new ChatMessage();
            robotChatMessage.setContactId(sendUserId);
            try {
                JiebaSegmenter js = new JiebaSegmenter();
                List<String> segement = js.sentenceProcess(messageContent);
                System.out.println(segement.toString());
                Boolean flag = false;
                for (Boolean bool : cuckooFilterController.isIllegal(segement)) {
                    if (bool != null && bool) {
                        flag = true;
                    }
                }

                if (flag == false) {
                    //这里对接AI，实现聊天
                    String output = AIExec.execute(messageContent);
                    segement = js.sentenceProcess(output);
                    flag = false;
                    for (Boolean bool : cuckooFilterController.isIllegal(segement)) {
                        if (bool != null && bool) {
                            flag = true;
                        }


                    }
                    if (flag==false) {
                        robotChatMessage.setMessageContent(output);
                    } else {
                        robotChatMessage.setMessageContent("很抱歉，服务器回答有误。");
                    }


                } else {
                    robotChatMessage.setMessageContent("很抱歉，您的问题我无法回答。");
                }
//                String output = AIExec.execute(messageContent);
//                robotChatMessage.setMessageContent(output);
                robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());

            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            saveMessage(robotChatMessage, robot);
        } else {
            messageHandler.sendMessage(messageSendDto);
        }
        System.out.println("messageSendDto = " + messageSendDto.getMessageContent());
        return messageSendDto;
    }

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        //防止通过url绕行客户端访问
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());

        if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSettingDto.getMaxImageSize() * Constants.FILE_SIZE_MB) {
            //是图片但是大小超出
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        } else if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB
        ) {
            //是视频但是大小超出
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringTools.isEmpty(fileSuffix)
                && !ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && !ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB
        ) {
            //除图片和视频之外的文件大小超出
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String fileName = file.getOriginalFilename();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);


        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);

        try {
            file.transferTo(uploadFile);
            cover.transferTo(new File(uploadFile.getPath() + Constants.COVER_IMAGE_SUFFIX));

        } catch (IOException e) {
            logger.error("上传文件失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ChatMessage uploadInfo = new ChatMessage();
        uploadInfo.setStatus(MessageStatusEnum.SENDED.getStatus());
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setMessageId(messageId);
        chatMessageMapper.updateByParam(uploadInfo, messageQuery);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public File downLoadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover) {
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        //接收者contactId不同
        if (UserContactTypeEnum.USER == contactTypeEnum && !tokenUserInfoDto.getUserId().equals(contactId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            //不在群聊中
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setUserId(tokenUserInfoDto.getUserId());
            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = userContactMapper.selectCount(userContactQuery);
            if (contactCount == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = chatMessage.getFileName();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;

        if (showCover != null && showCover) {
            fileRealName = fileRealName + Constants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileRealName);
        if (!file.exists()) {
//            file.mkdirs();
            logger.info("文件不存在{}", messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }

        return file;
    }

    @Override
    public List<ChatMessage> findListByDate(Long date) {
        return this.chatMessageMapper.selectListByDate(date);
    }

    @Override
    public Integer markMessagesAsUpChain(List<String> msgArr) {
        if (msgArr == null || msgArr.isEmpty()) {
            return 0;
        }
        // 调用Mapper更新消息状态
        return chatMessageMapper.updateMessagesAsUpChain(msgArr);
    }
}