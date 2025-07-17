package com.easyChat.entity.query;




/**
 * @Description:查询对象
 * @author:Shen-An
 * @date:2025/03/15
 */
public class EcdhPksQuery extends BaseQuery {
	/**
	 * 邮箱
	 */
	private String email;

	private String emailFuzzy;

	/**
	 * 对应的公钥
	 */
	private String ecdhPublicKey;

	private String ecdhPublicKeyFuzzy;

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

	public void setEmailFuzzy(String emailFuzzy) {
		this.emailFuzzy = emailFuzzy;
	}

	public String getEmailFuzzy() {
		return this.emailFuzzy;
	}

	public void setEcdhPublicKeyFuzzy(String ecdhPublicKeyFuzzy) {
		this.ecdhPublicKeyFuzzy = ecdhPublicKeyFuzzy;
	}

	public String getEcdhPublicKeyFuzzy() {
		return this.ecdhPublicKeyFuzzy;
	}

}