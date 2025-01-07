package com.easyChat.enums;

public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");
    private int status;

    private String desc;

    UserStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer status) {
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.getStatus() == status) {
                return userStatusEnum;
            }
        }
        return null;
    }

    public String getDesc() {
        return desc;
    }

    public int getStatus() {
        return status;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }
}
