package com.easyChat.service.impl;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.dto.UserContactSearchResultDto;
import com.easyChat.entity.po.*;
import com.easyChat.entity.query.*;

import com.easyChat.enums.*;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.*;

import com.easyChat.redis.RedisComponent;
import com.easyChat.service.UserContactApplyService;
import com.easyChat.service.UserContactService;
import com.easyChat.utils.CopyUtils;
import com.easyChat.utils.StringTools;

import com.easyChat.websocket.ChannelContextUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description:联系人Service
 * @author:Shen-An
 * @date:2025/01/18
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoBeauty> userInfoMapper;
    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private RedisComponent  redisComponent;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private ChannelContextUtils channelContextUtils;

    /**
     * 联系人根据条件查询列表
     */
    public List<UserContact> findListByParam(UserContactQuery query) {
        return this.userContactMapper.selectList(query);
    }

    /**
     * 联系人根据条件查询数量
     */
    public Integer findCountByParam(UserContactQuery query) {
        return this.userContactMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserContact> findListByPage(UserContactQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserContact> list = this.findListByParam(query);
        PaginationResultVo<UserContact> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserContact bean) {
        return this.userContactMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserIdAndContactId查询
     */
    public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId更新
     */
    public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
        return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据userId查contactId（查人或者群组）
     *
     * @param userId
     * @param contactId
     * @return
     */
    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
		/*得到  USER(0,"U","好友"),
		  GROUP(1,"G","群")
		  中的一种
		  即根据contactId搜人还是搜群组
		 */
        UserContactSearchResultDto result = new UserContactSearchResultDto();
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (userContactTypeEnum == null) {
            return null;
        }
        switch (userContactTypeEnum) {
            case USER:
                UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
                if (userInfo == null) {
                    return null;
                }
                result = CopyUtils.copy(userInfo, UserContactSearchResultDto.class);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
                if (groupInfo == null) {
                    return null;
                }
                result.setNickName(groupInfo.getGroupName());
                break;
        }
        result.setContactType(userContactTypeEnum.toString());
        result.setContactId(contactId);
        //如果userId查自己
        if (userId.equals(contactId)) {
            result.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return result;
        }
        //是否是好友
        UserContact userContact = userContactMapper.selectByUserIdAndContactId(userId, contactId);
        result.setStatus(userContact == null ? null : userContact.getStatus());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (userContactTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        Integer joinType = null;
        String receiveUserId = contactId;

        //申请人ID
        String applyUserId = tokenUserInfoDto.getUserId();
        //如没有填写申请信息-》默认申请信息
        applyInfo = StringTools.isEmpty(applyInfo) ? String.format(Constants.APPLY_INFO_TEMPLATE, tokenUserInfoDto.getNickName()) : applyInfo;

        //查询是否拉黑
        UserContact userContact = userContactMapper.selectByUserIdAndContactId(applyUserId, contactId);
        if (userContact != null &&
                ArrayUtils.contains(new Integer[]{
                        UserContactStatusEnum.BLACK_LIST_BE_FIRST.getStatus(),
                        UserContactStatusEnum.BLACK_LIST_BE.getStatus()
                }, userContact.getStatus())) {

            throw new BusinessException("对方拒绝接受你的消息");
        }

        //如果是群组
        if (userContactTypeEnum.equals(UserContactTypeEnum.GROUP)) {
            GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
            if (groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
                throw new BusinessException("群聊不存在或已解散");
            }
            receiveUserId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();
        } else {
            //是用户
            UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
            if (userInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            joinType = userInfo.getJoinType();
        }
        //直接加入的情况
        if (JoinTypeEnum.JOIN.equals(joinType)) {
            //添加联系人
            this.addContact(applyUserId, receiveUserId, contactId, userContactTypeEnum.getType(), applyInfo);
            return joinType;
        }

        //申请
        UserContactApply dbApply = userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
        //之前没有添加过
        if (dbApply == null) {
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyUserId(applyUserId);
            userContactApply.setContactType(userContactTypeEnum.getType());
            userContactApply.setReceiveUserId(receiveUserId);
            userContactApply.setLastApplyTime(System.currentTimeMillis());
            userContactApply.setContactId(contactId);
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setApplyInfo(applyInfo);
            userContactApplyMapper.insert(userContactApply);
        } else {
            //之前添加过，可能删除掉了好友//对方拒绝了申请
            //只需要更新状态
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setLastApplyTime(System.currentTimeMillis());
            userContactApply.setApplyInfo(applyInfo);
            userContactApplyMapper.updateByApplyId(userContactApply, dbApply.getApplyId());
        }
        if (dbApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())) {
            //发送ws消息，通知对方要处理申请了
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDto.setMessageContent(applyInfo);
            messageSendDto.setContactId(receiveUserId);
            channelContextUtils.sendMsg(messageSendDto,receiveUserId);

        }
        return joinType;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum status) {
        //我移除好友
        UserContact userContact =new UserContact();
        userContact.setStatus(status.getStatus());

        userContactMapper.updateByUserIdAndContactId(userContact, userId, contactId);

        //移除好友列表中的我
        UserContact oldFriend = new UserContact();
        if(UserContactStatusEnum.DEL.getStatus().equals(status)){
            oldFriend.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
        }else if(UserContactStatusEnum.BLACK_LIST.getStatus().equals(status)){
            oldFriend.setStatus(UserContactStatusEnum.BLACK_LIST_BE.getStatus());
        }
        userContactMapper.updateByUserIdAndContactId(oldFriend, contactId, userId);

        //TODO 从我的好友列表缓存中删除好友
        //TODO 从好友列表缓存删除我
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        //群聊人数
        if(UserContactTypeEnum.GROUP.getType().equals(contactType)){
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = userContactMapper.selectCount(userContactQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count>= sysSettingDto.getMaxGroupCount()){
                throw new BusinessException("群聊人数已满");
            }
        }
        Date curDate = new Date();
        //同意
        List<UserContact> list = new ArrayList<>();
        //申请人添加对方
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        list.add(userContact);

        //如果是好友，接收者也添加，如果是群组，群组不必添加
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact.setUserId(receiveUserId);
            userContact.setContactId(applyUserId);
            userContact.setContactType(contactType);
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            list.add(userContact);
        }
        //批量插入

        userContactMapper.insertOrUpdateBatch(list);

        //TODO如果是好友，接收者也添加申请人为好友，添加redis缓存

        //TODO创建会话
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String userId) {
        Date date =new Date();
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String contactId = sysSettingDto.getRobotUid();
        String contactName=sysSettingDto.getRobotNickName();
        String sendMsg = sysSettingDto.getRobotWelcome();
        sendMsg=StringTools.cleanHtmlTag(sendMsg);
        //增加机器人为好友
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactId(contactId);
        userContact.setContactType(UserContactTypeEnum.USER.getType());
        userContact.setCreateTime(date);
        userContact.setLastUpdateTime(date);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContactMapper.insert(userContact);

        //增加会话信息
        String sessionId = StringTools.getChatSessionId4User(new String[]{userId,contactId});
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(sendMsg);
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(date.getTime());
        this.chatSessionMapper.insert(chatSession);

        //增加会话人信息
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(userId);
        chatSessionUser.setContactId(contactId);
        chatSessionUser.setContactName(contactName);
        chatSessionUser.setSessionId(sessionId);
        this.chatSessionUserMapper.insert(chatSessionUser);

        //增加聊天信息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessage.setMessageContent(sendMsg);
        chatMessage.setSendUserId(contactId);
        chatMessage.setSendUserNickName(contactName);
        chatMessage.setSendTime(date.getTime());
        chatMessage.setContactId(userId);
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);
    }
}