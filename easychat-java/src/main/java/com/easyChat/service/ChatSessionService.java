package com.easyChat.service;

import com.easyChat.entity.po.ChatSession;
import com.easyChat.entity.query.ChatSessionQuery;
import com.easyChat.entity.vo.PaginationResultVo;

import java.util.List;

/**
 * @Description:Service
 * @author:Shen-An
 * @date:2025/02/07
 */
public interface ChatSessionService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatSession> findListByParam(ChatSessionQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(ChatSessionQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<ChatSession> findListByPage(ChatSessionQuery query);

	/**
	 * 新增
	 */
	Integer add(ChatSession bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatSession> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<ChatSession> listBean);

	/**
	 * 根据SessionId查询
	 */
	ChatSession getChatSessionBySessionId(String sessionId);

	/**
	 * 根据SessionId更新
	 */
	Integer updateChatSessionBySessionId(ChatSession t, String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteChatSessionBySessionId(String sessionId);

}