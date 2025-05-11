package com.easyChat.constants;

import com.easyChat.enums.UserContactTypeEnum;

public class Constants {
    public static final String REDIS_KEY_CHECK_CODE = "easyChat:checkCode";

    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;
    //验证码有效时间
    public static final Integer REDIS_TIME_1MIN = 60;

    //token有效时间
    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_TIME_1MIN * 60 * 24;
    //token失效时间
    public static final Integer REDIS_KEY_TOKEN_EXPIRES = REDIS_KEY_EXPIRES_DAY * 2;

    //websocket心跳key
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easyChat:ws:user:Heartbeat";

    //存token
    public static final String REDIS_KEY_WS_TOKEN = "easyChat:ws:token:";

    //存token对应的userId
    public static final String REDIS_KEY_WS_TOKEN_USERID = "easyChat:ws:token:userid";

    //系统设置key
    public static final String REDIS_KEY_SYS_SETTING = "easyChat:syssetting";
    //机器人Id
    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";
    //用户id长度
    public static final Integer LENGTH_11 = 11;

    //生成token所需要的长度之一
    public static final Integer LENGTH_20 = 20;

    public static final String FILE_FOLDER_FILE = "/file/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String IMAGE_SUFFIX = ".png";
    public static final String COVER_IMAGE_SUFFIX = "_cover.png";
    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

    //用户联系人列表
    public static final String REDIS_KEY_USER_CONTACT = "easyChat:ws:user:contact";

    public static final Long MILLS_SECONDS_3DAYS_AGO = 3 * 24 * 60 * 60 * 1000L;


    public static final String[] IMAGE_SUFFIX_LIST = new String[]{".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"};

    public static final String[] VIDEO_SUFFIX_LIST = new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"};

    //用于MB转成字节
    public static final Long FILE_SIZE_MB = 1024 * 1024L;
}
