package com.easyChat.entity.query;




/**
 * @Description:聊天信息表查询对象
 * @author:Shen-An
 * @date:2025/02/07
 */
public class ChatMessageQuery extends BaseQuery {
	/**
	 * 消息自增ID
	 */
	private Long messageId;

	/**
	 * 会话ID
	 */
	private String sessionId;

	private String sessionIdFuzzy;

	/**
	 * 消息类型
	 */
	private Integer messageType;

	/**
	 * 消息内容
	 */
	private String messageContent;

	private String messageContentFuzzy;

	/**
	 * 发送人ID
	 */
	private String sendUserId;

	private String sendUserIdFuzzy;

	/**
	 * 发送人昵称
	 */
	private String sendUserNickName;

	private String sendUserNickNameFuzzy;

	/**
	 * 发送时间
	 */
	private Long sendTime;

	/**
	 * 接收联系人ID
	 */
	private String contactId;

	private String contactIdFuzzy;

	/**
	 * 联系人类型 0私聊 1群聊
	 */
	private Integer contactType;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件名
	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
	 * 文件类型
	 */
	private Integer fileType;

	/**
	 * 状态0 正在发送 1已经发送
	 */
	private Integer status;

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public Long getMessageId() {
		return this.messageId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return this.sessionId;
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

	public void setSendUserNickName(String sendUserNickName) {
		this.sendUserNickName = sendUserNickName;
	}

	public String getSendUserNickName() {
		return this.sendUserNickName;
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

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Long getFileSize() {
		return this.fileSize;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}

	public Integer getFileType() {
		return this.fileType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy) {
		this.sessionIdFuzzy = sessionIdFuzzy;
	}

	public String getSessionIdFuzzy() {
		return this.sessionIdFuzzy;
	}

	public void setMessageContentFuzzy(String messageContentFuzzy) {
		this.messageContentFuzzy = messageContentFuzzy;
	}

	public String getMessageContentFuzzy() {
		return this.messageContentFuzzy;
	}

	public void setSendUserIdFuzzy(String sendUserIdFuzzy) {
		this.sendUserIdFuzzy = sendUserIdFuzzy;
	}

	public String getSendUserIdFuzzy() {
		return this.sendUserIdFuzzy;
	}

	public void setSendUserNickNameFuzzy(String sendUserNickNameFuzzy) {
		this.sendUserNickNameFuzzy = sendUserNickNameFuzzy;
	}

	public String getSendUserNickNameFuzzy() {
		return this.sendUserNickNameFuzzy;
	}

	public void setContactIdFuzzy(String contactIdFuzzy) {
		this.contactIdFuzzy = contactIdFuzzy;
	}

	public String getContactIdFuzzy() {
		return this.contactIdFuzzy;
	}

	public void setFileNameFuzzy(String fileNameFuzzy) {
		this.fileNameFuzzy = fileNameFuzzy;
	}

	public String getFileNameFuzzy() {
		return this.fileNameFuzzy;
	}

}