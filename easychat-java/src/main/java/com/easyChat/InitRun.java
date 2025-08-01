package com.easyChat;

import com.easyChat.SPCE.SPCEGEN;
import com.easyChat.constants.Constants;
import com.easyChat.controller.AccountController;
import com.easyChat.redis.RedisUtils;
import com.easyChat.utils.FileWriterUtil;
import com.easyChat.websocket.netty.NettyWebSocketStarter;
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
    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            new Thread(nettyWebSocketStarter).start();
            logger.info("服务启动成功");
            String pythonPath = "D:\\Anaconda\\python.exe";
            String scriptPath = "D:\\java code\\Chat\\easychat-java\\src\\main\\java\\com\\easyChat\\SPCE\\SPCE.py";
            String result = SPCEGEN.executePythonScript(pythonPath, scriptPath);
            System.out.println("Python输出：");
            System.out.println(result);

            // 将 Python 输出写入文件
            String filePath = Constants.FILEPATH;
            FileWriterUtil.writeToFile(result, filePath);
        } catch (SQLException e) {
            logger.error("数据库配置错误");
        } catch (RedisConnectionFailureException e) {
            logger.error("redis配置错误");
        } catch (Exception e) {
            logger.error("服务启动异常:{}", e);
        }
    }
}