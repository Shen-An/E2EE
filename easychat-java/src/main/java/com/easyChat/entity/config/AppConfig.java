package com.easyChat.entity.config;


import com.easyChat.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {


    /**
     * websocket端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;
    /**
     * 文件目录
     */
    @Value("${D:/java code/Chat/:}")
    private String projectFolder;

    @Value("${lcf2214457971@163.com:}")
    private String adminEmails;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getProjectFolder() {
        if (StringTools.isEmpty(projectFolder) && projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}
