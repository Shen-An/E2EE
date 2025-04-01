package com.easyChat.enums;

public enum MessageStatusEnum {
    SENDING(0,"发送中"),
    SENDED(1,"已发送");

    private Integer status;
    private String description;

    MessageStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public static MessageStatusEnum getEnum(Integer status) {
        for (MessageStatusEnum e : MessageStatusEnum.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }
    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
