package com.easyChat.service;

import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.UserContactQuery;

import java.util.List;

/**
 * @Description:联系人Service
 * @author:Shen-An
 * @date:2025/01/18
 */
public interface UserContactService {

	/**
	 * 联系人根据条件查询列表
	 */
	List<UserContact> findListByParam(UserContactQuery query);

	/**
	 * 联系人根据条件查询数量
	 */
	Integer findCountByParam(UserContactQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<UserContact> findListByPage(UserContactQuery query);

	/**
	 * 新增
	 */
	Integer add(UserContact bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserContact> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<UserContact> listBean);

	/**
	 * 根据UserIdAndContactId查询
	 */
	UserContact getUserContactByUserIdAndContactId(String userId, String contactId);

	/**
	 * 根据UserIdAndContactId更新
	 */
	Integer updateUserContactByUserIdAndContactId(UserContact t, String userId, String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteUserContactByUserIdAndContactId(String userId, String contactId);

}