package com.easyChat.enums;

public enum BeautyAccountStatusEnum {
    NO_USE(0,"未使用"),
    USEED(1,"使用");

    private Integer status;
    private String description;

    BeautyAccountStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public static BeautyAccountStatusEnum getByStatus(Integer status) {
        for (BeautyAccountStatusEnum accountStatus : BeautyAccountStatusEnum.values()) {
            if (accountStatus.getStatus() == status) {
                return accountStatus;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
