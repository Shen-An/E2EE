package com.easyChat.enums;

import com.easyChat.utils.StringTools;

public enum UserContactStatusEnum {
    NOT_FRIEND(0,"不是好友"),
    FRIEND(1,"好友"),
    DEL(2,"已删除好友"),
    DEL_BE(3,"被好友删除"),
    BLACK_LIST(4,"拉黑好友"),
    BLACK_LIST_BE(5,"被好友拉黑");

    private Integer status;
    private String desc;

    UserContactStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public static UserContactStatusEnum getByStatus(String  status) {
        try{
            if(StringTools.isEmpty(status)){
                return null;
            }
            return UserContactStatusEnum.valueOf(status.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }
    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum userContactStatusEnum : UserContactStatusEnum.values()) {
            if (userContactStatusEnum.getStatus().equals(status)) {
                return userContactStatusEnum;
            }
        }
        return null;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getStatus() {
        return status;
    }
}
