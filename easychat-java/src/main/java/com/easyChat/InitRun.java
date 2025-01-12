package com.easyChat;

import com.easyChat.controller.AccountController;
import com.easyChat.redis.RedisUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component
public class InitRun implements ApplicationRunner {
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private DataSource dataSource;
    @Resource
    private RedisUtils redisUtils;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try{
            dataSource.getConnection();
            redisUtils.get("test");
            logger.info("服务启动成功");
        }catch (SQLException e){
            logger.error("数据库配置错误");
        }catch (RedisConnectionFailureException e){
            logger.error("redis配置错误");
        }catch (Exception e){
            logger.error("服务启动异常:{}",e);
        }
    }
}
