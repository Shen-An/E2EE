package com.easyChat.entity.po;

import java.io.Serializable;


/**
 * @Description:靓号表
 * @author:Shen-An
 * @date:2025/01/17
 */
public class UserInfoBeauty implements Serializable {
	/**
	 * 自增ID
	 */
	private Integer id;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 微信号,用户ID
	 */
	private String userId;

	/**
	 * 0:未使用1使用
	 */
	private Integer status;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	@Override
	public String toString() {
		return "自增ID:" + (id == null ? "空" : id) + ",邮箱:" + (email == null ? "空" : email) + ",微信号,用户ID:" + (userId == null ? "空" : userId) + ",0:未使用1使用:" + (status == null ? "空" : status);
	}
}