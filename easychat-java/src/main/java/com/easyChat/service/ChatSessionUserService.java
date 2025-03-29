package com.easyChat.service;

import com.easyChat.entity.po.ChatSessionUser;
import com.easyChat.entity.query.ChatSessionUserQuery;
import com.easyChat.entity.vo.PaginationResultVo;

import java.util.List;

/**
 * @Description:会话用户Service
 * @author:Shen-An
 * @date:2025/02/07
 */
public interface ChatSessionUserService {

	/**
	 * 会话用户根据条件查询列表
	 */
	List<ChatSessionUser> findListByParam(ChatSessionUserQuery query);

	/**
	 * 会话用户根据条件查询数量
	 */
	Integer findCountByParam(ChatSessionUserQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<ChatSessionUser> findListByPage(ChatSessionUserQuery query);

	/**
	 * 新增
	 */
	Integer add(ChatSessionUser bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatSessionUser> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<ChatSessionUser> listBean);

	/**
	 * 根据UserIdAndContactId查询
	 */
	ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId);

	/**
	 * 根据UserIdAndContactId更新
	 */
	Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser t, String userId, String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId);

}