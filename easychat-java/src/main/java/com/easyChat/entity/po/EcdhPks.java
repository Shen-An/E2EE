package com.easyChat.entity.po;

import java.io.Serializable;


/**
 * @Description:
 * @author:Shen-An
 * @date:2025/03/15
 */
public class EcdhPks implements Serializable {
	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 对应的公钥
	 */
	private String ecdhPublicKey;

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEcdhPublicKey(String ecdhPublicKey) {
		this.ecdhPublicKey = ecdhPublicKey;
	}

	public String getEcdhPublicKey() {
		return this.ecdhPublicKey;
	}

	@Override
	public String toString() {
		return "邮箱:" + (email == null ? "空" : email) + ",对应的公钥:" + (ecdhPublicKey == null ? "空" : ecdhPublicKey);
	}
}