package com.easyChat.websocket;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.WsInitData;
import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.po.ChatSessionUser;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.query.ChatMessageQuery;
import com.easyChat.entity.query.ChatSessionUserQuery;
import com.easyChat.entity.query.UserContactApplyQuery;
import com.easyChat.entity.query.UserInfoQuery;
import com.easyChat.enums.MessageTypeEnum;
import com.easyChat.enums.UserContactApplyStatusEnum;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.mappers.ChatMessageMapper;
import com.easyChat.mappers.ChatSessionUserMapper;
import com.easyChat.mappers.UserContactApplyMapper;
import com.easyChat.mappers.UserInfoMapper;
import com.easyChat.redis.RedisComponent;

import com.easyChat.service.ChatSessionUserService;
import com.easyChat.utils.JsonUtils;
import com.easyChat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChannelContextUtils {
    @Resource
    private ChatSessionUserMapper<ChatSessionUser,ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        AttributeKey attributeKey = null;
        if (!attributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);

        List<String> contactIdList = redisComponent.getUserContactList(userId);
        for (String groupId : contactIdList) {
            if (groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                add2Group(groupId, channel);
            }
        }
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveHeartBeat(userId);

        //更新用户最后链接时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(updateInfo, userId);

        //给用户发送消息
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Long sourceLastOffTime = userInfo.getLastOffTime();
        //只要三天前的
        Long lastOffTime = sourceLastOffTime;
        if (sourceLastOffTime != null && System.currentTimeMillis() - Constants.MILLS_SECONDS_3DAYS_AGO > sourceLastOffTime) {
            lastOffTime = System.currentTimeMillis() - Constants.MILLS_SECONDS_3DAYS_AGO;
        }
        /**
         * 1、查询会话消息，设备更换也可以查出，即会话同步
         */
        ChatSessionUserQuery sessionUserQuery = new ChatSessionUserQuery();
        sessionUserQuery.setUserId(userId);
        sessionUserQuery.setOrderBy("last_receive_time desc");
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.selectList(sessionUserQuery);

        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);

        /**
         * 2、查询聊天消息
         */
        List<String> groupIdList = contactIdList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setContactIdList(groupIdList);
        messageQuery.setLastReceiveTime(lastOffTime);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectList(messageQuery);
        wsInitData.setChatMessageList(chatMessageList);
        /**
         * 3、查询好友申请(几个)
         */
        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setReceiveUserId(userId);
        userContactApplyQuery.setLastApplyTimeStamp(lastOffTime);
        userContactApplyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        Integer applyCount = userContactApplyMapper.selectCount(userContactApplyQuery);
        wsInitData.setApplyCount(applyCount);

        //发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);

        sendMsg(messageSendDto, userId);

    }


    public void add2Group(String groupId, Channel channel) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (channel == null) {
            return;
        }
        group.add(channel);
    }


    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (StringTools.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeHeartBeat(userId);

        //更新用户最后离线时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(updateInfo, userId);
    }
//    public void send2Group(String message) {
//        ChannelGroup group = GROUP_CONTEXT_MAP.get("10000");
//        group.writeAndFlush(new TextWebSocketFrame(message));
//    }

    public void sendMessage(MessageSendDto messageSendDto) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (contactTypeEnum) {
            case USER:
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;
        }
    }

    private void send2User(MessageSendDto messageSendDto) {
        if (StringTools.isEmpty(messageSendDto.getContactId())) {
            return;
        }
        String contactId = messageSendDto.getContactId();
        sendMsg(messageSendDto, contactId);
        //强制下线的消息
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())) {
            closeContext(contactId);
        }
    }

    private void send2Group(MessageSendDto messageSendDto) {
        if (StringTools.isEmpty(messageSendDto.getContactId())) {
            return;
        }
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if (channelGroup == null) {
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObjectToJson(messageSendDto)));

        //移出群聊
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        if(MessageTypeEnum.LEAVE_GROUP == messageTypeEnum || MessageTypeEnum.REMOVE_GROUP == messageTypeEnum) {
            String userId = (String) messageSendDto.getExtendData();
            redisComponent.removeUserContact(userId, messageSendDto.getContactId());
            Channel channel = USER_CONTEXT_MAP.get(userId);
            if (channel == null) {
                return;
            }
            channelGroup.remove(channel);
        }
        if(MessageTypeEnum.DISSOLUTION_GROUP == messageTypeEnum) {
            GROUP_CONTEXT_MAP.remove(messageSendDto.getContactId());
            channelGroup.close();
        }

    }

    //发送消息
    public void sendMsg(MessageSendDto messageSendDto, String receiverId) {
        Channel sendChannel = USER_CONTEXT_MAP.get(receiverId);
        if (sendChannel == null) {
            return;
        }
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
            UserInfo userInfo = (UserInfo) messageSendDto.getExtendData();
            messageSendDto.setContactId(userInfo.getUserId());
            messageSendDto.setContactName(userInfo.getNickName());
            messageSendDto.setExtendData(null);
        }else{
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        }
        sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObjectToJson(messageSendDto)));

    }

    public void closeContext(String userId) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        redisComponent.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            return;
        }
        channel.close();
    }
    public void addUser2Group(String userId, String groupId) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        add2Group(groupId, channel);
    }


}
