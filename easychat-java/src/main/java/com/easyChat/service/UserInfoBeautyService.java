package com.easyChat.service;

import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserInfoBeauty;
import com.easyChat.entity.query.UserInfoBeautyQuery;

import java.util.List;

/**
 * @Description:靓号表Service
 * @author:Shen-An
 * @date:2025/01/17
 */
public interface UserInfoBeautyService {

	/**
	 * 靓号表根据条件查询列表
	 */
	List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery query);

	/**
	 * 靓号表根据条件查询数量
	 */
	Integer findCountByParam(UserInfoBeautyQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<UserInfoBeauty> findListByPage(UserInfoBeautyQuery query);

	/**
	 * 新增
	 */
	Integer add(UserInfoBeauty bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserInfoBeauty> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<UserInfoBeauty> listBean);

	/**
	 * 根据Id查询
	 */
	UserInfoBeauty getUserInfoBeautyById(Integer id);

	/**
	 * 根据Id更新
	 */
	Integer updateUserInfoBeautyById(UserInfoBeauty t, Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteUserInfoBeautyById(Integer id);

	/**
	 * 根据Email查询
	 */
	UserInfoBeauty getUserInfoBeautyByEmail(String email);

	/**
	 * 根据Email更新
	 */
	Integer updateUserInfoBeautyByEmail(UserInfoBeauty t, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoBeautyByEmail(String email);

	/**
	 * 根据UserId查询
	 */
	UserInfoBeauty getUserInfoBeautyByUserId(String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateUserInfoBeautyByUserId(UserInfoBeauty t, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoBeautyByUserId(String userId);

}