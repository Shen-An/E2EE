package com.easyChat.entity.query;




/**
 * @Description:会话用户查询对象
 * @author:Shen-An
 * @date:2025/02/07
 */
public class ChatSessionUserQuery extends BaseQuery {
	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 联系人ID
	 */
	private String contactId;

	private String contactIdFuzzy;

	/**
	 * 会话ID
	 */
	private String sessionId;

	private String sessionIdFuzzy;

	/**
	 * 联系人名称
	 */
	private String contactName;

	private String contactNameFuzzy;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactId() {
		return this.contactId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactName() {
		return this.contactName;
	}

	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}

	public void setContactIdFuzzy(String contactIdFuzzy) {
		this.contactIdFuzzy = contactIdFuzzy;
	}

	public String getContactIdFuzzy() {
		return this.contactIdFuzzy;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy) {
		this.sessionIdFuzzy = sessionIdFuzzy;
	}

	public String getSessionIdFuzzy() {
		return this.sessionIdFuzzy;
	}

	public void setContactNameFuzzy(String contactNameFuzzy) {
		this.contactNameFuzzy = contactNameFuzzy;
	}

	public String getContactNameFuzzy() {
		return this.contactNameFuzzy;
	}

}