package com.easyChat.enums;

public enum MessageTypeEnum {
    INIT(0, "", "连接ws获取消息"),
    ADD_FRIEND(1, "", "添加好友打招呼消息"),
    CHAT(2, "", "普通聊天信息"),
    GROUP_CREATE(3, "群组创建完毕，可以开始聊天了", "群创建成功"),
    CONTACT_APPLY(4, "", "好友申请 "),
    MEDIA_CHAT(5, "", "媒体文件"),
    FILE_UPLOAD(6, "", "文件上传完成"),
    FORCE_OFF_LINE(7, "", "强制下线"),
    DISSOLUTION_GROUP(8, "群聊已经解散", "解散群聊"),
    ADD_GROUP(9, "%s加入了群聊", "加入群聊"),
    CONTACT_NAME_UPDATE(10, "", "更新昵称"),
    LEAVE_GROUP(11, "%s退出了群聊", "退出群聊"),
    REMOVE_GROUP(12, "%s被管理员移出了群聊", "被管理员移出了群聊"),
    ADD_FRIEND_SELF(13, "", "添加好友打招呼消息");

    private Integer type;
    private String initMessage;
    private String description;

    MessageTypeEnum(Integer type, String initMessage, String description) {
        this.type = type;
        this.initMessage = initMessage;
        this.description = description;
    }

    public static MessageTypeEnum getByType(Integer type) {
        for (MessageTypeEnum m : MessageTypeEnum.values()) {
            if (m.getType().equals(type)) {
                return m;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }


    public String getInitMessage() {
        return initMessage;
    }


    public String getDescription() {
        return description;
    }

}
