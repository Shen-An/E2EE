package com.easyChat.entity.po;

import java.io.Serializable;


/**
 * @Description:非法聊天信息表
 * @author:Shen-An
 * @date:2025/04/14
 */
public class ChatMessageIllegal implements Serializable {
	/**
	 * 消息自增ID
	 */
	private Long messageId;

	/**
	 * 消息类型
	 */
	private Integer messageType;

	/**
	 * 非法消息内容
	 */
	private String messageContent;

	/**
	 * 发送人ID
	 */
	private String sendUserId;

	/**
	 * 发送时间
	 */
	private Long sendTime;

	/**
	 * 接收联系人ID
	 */
	private String contactId;

	/**
	 * 联系人类型 0私聊 1群聊
	 */
	private Integer contactType;

	private String e2eeCt;

	public String getE2eeCt() {
		return e2eeCt;
	}

	public void setE2eeCt(String e2eeCt) {
		this.e2eeCt = e2eeCt;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public Long getMessageId() {
		return this.messageId;
	}

	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}

	public Integer getMessageType() {
		return this.messageType;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public String getMessageContent() {
		return this.messageContent;
	}

	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}

	public String getSendUserId() {
		return this.sendUserId;
	}

	public void setSendTime(Long sendTime) {
		this.sendTime = sendTime;
	}

	public Long getSendTime() {
		return this.sendTime;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactId() {
		return this.contactId;
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	public Integer getContactType() {
		return this.contactType;
	}

	@Override
	public String toString() {
		return "消息自增ID:" + (messageId == null ? "空" : messageId) + ",消息类型:" + (messageType == null ? "空" : messageType) + ",非法消息内容:" + (messageContent == null ? "空" : messageContent) + ",发送人ID:" + (sendUserId == null ? "空" : sendUserId) + ",发送时间:" + (sendTime == null ? "空" : sendTime) + ",接收联系人ID:" + (contactId == null ? "空" : contactId) + ",联系人类型 0私聊 1群聊:" + (contactType == null ? "空" : contactType);
	}
}