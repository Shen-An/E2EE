package com.easyChat.entity.query;




/**
 * @Description:查询对象
 * @author:Shen-An
 * @date:2025/04/06
 */
public class SpceIllegalTraceQuery extends BaseQuery {
	/**
	 * 用户id
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 用户公钥
	 */
	private String userPk;

	private String userPkFuzzy;

	/**
	 * 非法次数
	 */
	private Integer illegalCount;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserPk(String userPk) {
		this.userPk = userPk;
	}

	public String getUserPk() {
		return this.userPk;
	}

	public void setIllegalCount(Integer illegalCount) {
		this.illegalCount = illegalCount;
	}

	public Integer getIllegalCount() {
		return this.illegalCount;
	}

	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}

	public void setUserPkFuzzy(String userPkFuzzy) {
		this.userPkFuzzy = userPkFuzzy;
	}

	public String getUserPkFuzzy() {
		return this.userPkFuzzy;
	}

}