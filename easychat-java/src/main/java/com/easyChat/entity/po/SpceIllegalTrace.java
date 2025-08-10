package com.easyChat.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * @Description:
 * @author:Shen-An
 * @date:2025/04/06
 */
public class SpceIllegalTrace implements Serializable {
	/**
	 * 用户id
	 */
	@JsonIgnore
	private String userId;

	/**
	 * 用户公钥
	 */
	private String userPk;

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

	@Override
	public String toString() {
		return "用户id:" + (userId == null ? "空" : userId) + ",用户公钥:" + (userPk == null ? "空" : userPk) + ",非法次数:" + (illegalCount == null ? "空" : illegalCount);
	}
}