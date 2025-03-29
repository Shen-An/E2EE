package com.easyChat.websocket.netty;

import com.easyChat.entity.config.AppConfig;
import com.easyChat.redis.RedisUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class NettyWebSocketStarter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HandlerHeardBeat.class);
    private static EventLoopGroup bossGroup=new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup=new NioEventLoopGroup();

    @Resource
    private AppConfig  appConfig;

    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
    @Resource
    private HandlerWebSocket handlerWebSocket;
//    @Async
//    public void startNetty(){
//
//
//    }
    public static void main(String[] args) {

    }

    @Override
    public void run() {
        try{
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).
                    handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline=channel.pipeline();
                            //对http协议的支持，使用http的编码器解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码   httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接受的Htpp的完整性
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            //心跳
                            pipeline.addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));

                            pipeline.addLast(new HandlerHeardBeat());

                            //将http升级为ws
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,64*1024,true,true,10000L));

                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            ChannelFuture channelFuture=serverBootstrap.bind(appConfig.getWsPort()).sync();
            logger.info("netty server started on port {}",appConfig.getWsPort());
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){

            logger.error("启动netty失败",e);
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
