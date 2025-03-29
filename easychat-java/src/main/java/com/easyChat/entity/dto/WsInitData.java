package com.easyChat.entity.dto;

import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.po.ChatSessionUser;

import java.util.List;

public class WsInitData {
    private List<ChatSessionUser> chatSessionUserList;
    private List<ChatMessage>chatMessageList;
    private Integer applyCount;

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public List<ChatSessionUser> getChatSessionUserList() {
        return chatSessionUserList;
    }

    public void setChatSessionUserList(List<ChatSessionUser> chatSessionUserList) {
        this.chatSessionUserList = chatSessionUserList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}
