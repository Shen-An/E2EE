package com.easyChat.enums;

import com.easyChat.utils.StringTools;

public enum JoinTypeEnum {
    JOIN(0,"直接加入"),
    APPLY(1,"需要审核");

    private Integer type;
    private String description;
    JoinTypeEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }
    public Integer getType() {
        return type;
    }
    public String getDescription() {
        return description;
    }
    public static JoinTypeEnum getByName(String name) {
        try{
            if(StringTools.isEmpty(name)){
                return null;
            }
            return JoinTypeEnum.valueOf(name.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }
    public static JoinTypeEnum getByType(Integer joinType) {
        for(JoinTypeEnum joinTypeEnum : JoinTypeEnum.values()){
            if(joinTypeEnum.getType().equals(joinType)){
                return joinTypeEnum;
            }
        }
        return null;
    }
}
