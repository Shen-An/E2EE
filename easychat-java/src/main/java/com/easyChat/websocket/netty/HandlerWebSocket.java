package com.easyChat.websocket.netty;

import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.redis.RedisComponent;
import com.easyChat.utils.StringTools;
import com.easyChat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger = LoggerFactory.getLogger(HandlerHeardBeat.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelContextUtils channelContextUtils;
    /**
     * 通道就绪后，调用，一般用户来做初始化
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新链接加入...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有链接断开...");
        channelContextUtils.removeContext(ctx.channel());
    }

    /**
     * 收信息
     *
     * @param ctx
     * @param textWebSocketFrame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
//        logger.info("收到消息userId{}的信息{}",userId, textWebSocketFrame.text());
        redisComponent.saveHeartBeat(userId);
//        channelContextUtils.send2Group(textWebSocketFrame.text());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            if (token == null) {
                ctx.channel().close();
                return;
            }
//            logger.info("url:{}", url);
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if (tokenUserInfoDto == null) {
                ctx.channel().close();
                return;
            }
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(),ctx.channel());
        }
    }

    private String getToken(String url) {
        if (StringTools.isEmpty(url) || url.indexOf("?") == -1) {
            return null;
        }
        String[] query = url.split("\\?");
        if (query.length != 2) {
            return null;
        }
        String[] params = query[1].split("=");
        if (params.length != 2) {
            return null;
        }
        return params[1];

    }
}
