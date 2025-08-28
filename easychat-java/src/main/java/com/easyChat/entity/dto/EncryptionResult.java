package com.easyChat.entity.dto;

import java.util.List;

// 主类，用于接收整个对象
public class EncryptionResult {
    private List<Integer> QS0;
    private List<Integer> QS1;
    private List<EncryptedMessage> encryptedMessages0;
    private List<EncryptedMessage> encryptedMessages1;

    // Getter 和 Setter 方法
    public List<Integer> getQS0() {
        return QS0;
    }

    public void setQS0(List<Integer> QS0) {
        this.QS0 = QS0;
    }

    public List<Integer> getQS1() {
        return QS1;
    }

    public void setQS1(List<Integer> QS1) {
        this.QS1 = QS1;
    }

    public List<EncryptedMessage> getEncryptedMessages0() {
        return encryptedMessages0;
    }

    public void setEncryptedMessages0(List<EncryptedMessage> encryptedMessages0) {
        this.encryptedMessages0 = encryptedMessages0;
    }

    public List<EncryptedMessage> getEncryptedMessages1() {
        return encryptedMessages1;
    }

    public void setEncryptedMessages1(List<EncryptedMessage> encryptedMessages1) {
        this.encryptedMessages1 = encryptedMessages1;
    }
}
