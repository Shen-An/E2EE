package com.easyChat.service.impl;

import com.easyChat.constants.Constants;
import com.easyChat.entity.config.AppConfig;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.*;
import com.easyChat.entity.query.*;
import com.easyChat.enums.*;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.*;
import com.easyChat.redis.RedisComponent;
import com.easyChat.service.ChatSessionUserService;
import com.easyChat.service.GroupInfoService;
import com.easyChat.service.UserContactService;
import com.easyChat.utils.CopyUtils;
import com.easyChat.utils.StringTools;
import com.easyChat.websocket.ChannelContextUtils;
import com.easyChat.websocket.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Description:群组表Service
 * @author:Shen-An
 * @date:2025/01/18
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private AppConfig appConfig;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
    @Resource
    private UserContactService userContactService;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private MessageHandler messageHandler;
    @Resource
    private ChannelContextUtils channelContextUtils;
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    @Lazy //重要
    private GroupInfoService groupInfoService;
    /**
     * 群组表根据条件查询列表
     */
    public List<GroupInfo> findListByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectList(query);
    }

    /**
     * 群组表根据条件查询数量
     */
    public Integer findCountByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<GroupInfo> findListByPage(GroupInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(query);
        PaginationResultVo<GroupInfo> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据GroupId查询
     */
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId更新
     */
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    /**
     * @param groupInfo
     * @param avatarFile
     * @param avatarCove
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCove) throws IOException {
        Date curDate = new Date();
        if (null == avatarFile) {
            return;
        }
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFile = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        String filePath = targetFile.getPath() + "/" + groupInfo.getGroupId() + Constants.IMAGE_SUFFIX;
        avatarFile.transferTo(new File(filePath));
        avatarCove.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        //新增群组
        if (StringTools.isEmpty(groupInfo.getGroupId())) {
            GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
            groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
            int count = this.findCountByParam(groupInfoQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupCount()) {
                throw new BusinessException("最多能创建" + sysSettingDto.getMaxGroupCount() + "个群聊");
            }
            if (avatarFile == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            groupInfo.setCreateTime(curDate);
            groupInfo.setGroupId(StringTools.getGroupId());
            this.groupInfoMapper.insert(groupInfo);

            //将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContactMapper.insert(userContact);

            // 创建会话
            String sessionId = StringTools.getChatSessionId4Group(groupInfo.getGroupId());
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(curDate.getTime());
            this.chatSessionMapper.insertOrUpdate(chatSession);

            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(sessionId);
            this.chatSessionUserMapper.insert(chatSessionUser);

            //创建消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageMapper.insert(chatMessage);

            //将群组添加到联系人
            redisComponent.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            //将联系人通道添加到群组通道
            channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

            //发送ws消息
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(curDate.getTime());
            chatSessionUser.setMemberCount(1);

            MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setExtendData(chatSessionUser);
            messageSendDto.setLastMessage(chatSessionUser.getLastMessage());
            messageHandler.sendMessage(messageSendDto);

        } else {
            //修改
            GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            this.groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());

            //更新相关冗余表信息
            String contactNameUpdate = null;
            if (!dbInfo.getGroupName().equals(groupInfo.getGroupName())) {
                contactNameUpdate = groupInfo.getGroupName();
            }
            if (contactNameUpdate == null) {
                return;
            }

            //修改群昵称发送ws消息
            chatSessionUserService.updateRedundanceInfo(contactNameUpdate, groupInfo.getGroupId());

        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void dissolutionGroup(String groupOwnerId, String groupId) {
        GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (dbInfo == null || !dbInfo.getGroupOwnerId().equals(groupOwnerId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //删除群组
        GroupInfo updateInfo = new GroupInfo();
        updateInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        this.groupInfoMapper.updateByGroupId(updateInfo, groupId);

        //更新联系人处群组信息
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());

        UserContact updateUserContact = new UserContact();
        updateUserContact.setStatus(UserContactStatusEnum.DEL.getStatus());
        this.userContactMapper.updateByParam(updateUserContact, userContactQuery);

        //移除相关群员的联系人缓存
        List<UserContact> userContactList = this.userContactMapper.selectList(userContactQuery);
        for (UserContact userContact : userContactList) {
            redisComponent.removeUserContact(userContact.getUserId(), userContact.getContactId());
        }
        // 发消息 1、更新会话信息 2、记录群消息 3、发送解散通知消息
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date curDate = new Date();
        String messageContent = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();//解散群聊消息

        //更新会话表
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        //更新消息表
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(messageContent);
        chatMessageMapper.insert(chatMessage);
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        messageHandler.sendMessage(messageSendDto);

    }

    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String ContactIds, Integer opType) {
        GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (groupInfo == null || !groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String[] contactIdList = ContactIds.split(",");
        for (String contactId : contactIdList) {
            if (Constants.ZERO.equals(opType)) {
                //不能leaveGroup()调用，这样事务会不生效
                groupInfoService.leaveGroup(contactId,groupId,MessageTypeEnum.REMOVE_GROUP);//保证事务生效
            } else {
                userContactService.addContact(contactId, null, groupId, UserContactTypeEnum.GROUP.getType(), null);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //不允许群主退出群聊
        if(userId.equals(groupInfo.getGroupOwnerId())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer count = userContactMapper.deleteByUserIdAndContactId(userId, groupId);
        if(count == 0){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo =  userInfoMapper.selectByUserId(userId);
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date curDate = new Date();
        String messageContent = String.format(messageTypeEnum.getInitMessage(),userInfo.getNickName());

        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(messageContent);
        chatMessageMapper.insert(chatMessage);

        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        Integer memberCount = userContactMapper.selectCount(userContactQuery);
        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setExtendData(userId);
        messageSendDto.setMemberCount(memberCount);
        messageHandler.sendMessage(messageSendDto);
    }
}