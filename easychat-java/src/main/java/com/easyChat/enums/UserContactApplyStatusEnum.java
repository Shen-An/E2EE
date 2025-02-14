package com.easyChat.enums;

import jodd.util.StringUtil;

public enum UserContactApplyStatusEnum {
    INIT(0,"待处理"),
    PASS(1,"已同意"),
    REJECT(2,"已拒绝"),
    BLACKLIST(3,"已拉黑");

    private Integer status;
    private String desc;

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserContactApplyStatusEnum getByStatus(String  status) {
        try{
            if(StringUtil.isEmpty(status)){
                return null;
            }
            return UserContactApplyStatusEnum.valueOf(status);
        }catch(IllegalArgumentException e){
            return null;
        }
    }

    public static UserContactApplyStatusEnum getByStatus(Integer status) {
        for(UserContactApplyStatusEnum statusEnum : UserContactApplyStatusEnum.values()){
            if(statusEnum.getStatus().equals(status)){
                return statusEnum;
            }
        }
        return null;
    }
}
