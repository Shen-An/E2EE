package com.easyChat.enums;

public enum GroupStatusEnum {
    NORMAL(1, "正常"),
    DISSOLUTION(0, "解散");
    private Integer status;
    private String desc;

    GroupStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static GroupStatusEnum getGroupStatusEnum(Integer status) {
        for (GroupStatusEnum groupStatusEnum : GroupStatusEnum.values()) {
            if (groupStatusEnum.getStatus().equals(status)) {
                return groupStatusEnum;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }


}
