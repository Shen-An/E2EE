package com.easyChat.enums;

import com.easyChat.utils.StringTools;

public enum UserContactTypeEnum {
    USER(0,"U","好友"),
    GROUP(1,"G","群");


    private Integer type;
    private String prefix;
    private String description;
    UserContactTypeEnum(Integer type, String prefix, String description) {
        this.type = type;
        this.prefix = prefix;
        this.description = description;
    }


    public Integer getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDescription() {
        return description;
    }

    public static UserContactTypeEnum getByName(String name) {
      try {
          if(StringTools.isEmpty(name)){
            return null;
          }
          return UserContactTypeEnum.valueOf(name.toUpperCase());
      }catch (Exception e) {
          e.printStackTrace();
          return null;
      }
    }

    public static UserContactTypeEnum getByPrefix(String prefix) {
        try {
            if(StringTools.isEmpty(prefix)||prefix.trim().length()==0){
                return null;
            }
            //传入的可能是id U11111111111，只取第一个U
            prefix = prefix.substring(0,1);

            for(UserContactTypeEnum typeEnum:UserContactTypeEnum.values()) {
                if(typeEnum.getPrefix().equals(prefix)){
                    return typeEnum;
                }
            }
            return null;

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
