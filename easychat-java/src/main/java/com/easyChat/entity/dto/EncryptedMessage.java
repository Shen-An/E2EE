package com.easyChat.entity.dto;

// 用于表示 encryptedMessages0 和 encryptedMessages1 中的单个元素
class EncryptedMessage {
    private String encryptedMessage;
    private String key;
    private String iv;

    // Getter 和 Setter 方法
    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(String encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}