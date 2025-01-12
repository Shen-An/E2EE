package com.easyChat.service;

import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.query.UserInfoQuery;
import com.easyChat.entity.vo.UserInfoVo;
import com.easyChat.exception.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description:用户信息表Service
 * @author:Shen-An
 * @date:2025/01/17
 */
public interface UserInfoService {

	/**
	 * 用户信息表根据条件查询列表
	 */
	List<UserInfo> findListByParam(UserInfoQuery query);

	/**
	 * 用户信息表根据条件查询数量
	 */
	Integer findCountByParam(UserInfoQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<UserInfo> findListByPage(UserInfoQuery query);

	/**
	 * 新增
	 */
	Integer add(UserInfo bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserInfo> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<UserInfo> listBean);

	/**
	 * 根据UserId查询
	 */
	UserInfo getUserInfoByUserId(String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateUserInfoByUserId(UserInfo t, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoByUserId(String userId);

	/**
	 * 根据Email查询
	 */
	UserInfo getUserInfoByEmail(String email);

	/**
	 * 根据Email更新
	 */
	Integer updateUserInfoByEmail(UserInfo t, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoByEmail(String email);

	/**
	 *	用户注册
	 */
	void register(String email, String nickName, String password);

	UserInfoVo login(String email, String password);
}