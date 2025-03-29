package com.easyChat.websocket;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.WsInitData;
import com.easyChat.entity.po.ChatSessionUser;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.query.ChatSessionUserQuery;
import com.easyChat.entity.query.UserInfoQuery;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.mappers.UserInfoMapper;
import com.easyChat.redis.RedisComponent;

import com.easyChat.service.ChatSessionUserService;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelContextUtils {
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private RedisComponent redisComponent;

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
        List<ChatSessionUser> chatSessionUserList = chatSessionUserService.findListByParam(sessionUserQuery);

        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionUserList(chatSessionUserList);

        /**
         * 2、查询聊天消息
         */

        /**
         * 3、查询好友申请
         */
    }


    //发送消息
    public static void sendMsg(){}

    private void add2Group(String groupId, Channel channel) {
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
}
