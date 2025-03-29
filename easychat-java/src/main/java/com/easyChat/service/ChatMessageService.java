package com.easyChat.service;

import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.query.ChatMessageQuery;
import com.easyChat.entity.vo.PaginationResultVo;

import java.util.List;

/**
 * @Description:聊天信息表Service
 * @author:Shen-An
 * @date:2025/02/07
 */
public interface ChatMessageService {

	/**
	 * 聊天信息表根据条件查询列表
	 */
	List<ChatMessage> findListByParam(ChatMessageQuery query);

	/**
	 * 聊天信息表根据条件查询数量
	 */
	Integer findCountByParam(ChatMessageQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<ChatMessage> findListByPage(ChatMessageQuery query);

	/**
	 * 新增
	 */
	Integer add(ChatMessage bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatMessage> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<ChatMessage> listBean);

	/**
	 * 根据MessageId查询
	 */
	ChatMessage getChatMessageByMessageId(Long messageId);

	/**
	 * 根据MessageId更新
	 */
	Integer updateChatMessageByMessageId(ChatMessage t, Long messageId);

	/**
	 * 根据MessageId删除
	 */
	Integer deleteChatMessageByMessageId(Long messageId);

}