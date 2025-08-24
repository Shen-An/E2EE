package com.easyChat.service;

import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessageIllegal;
import com.easyChat.entity.query.ChatMessageIllegalQuery;
import com.easyChat.entity.vo.PaginationResultVo;

import java.util.List;

/**
 * @Description:非法聊天信息表Service
 * @author:Shen-An
 * @date:2025/04/14
 */
public interface ChatMessageIllegalService {

	/**
	 * 非法聊天信息表根据条件查询列表
	 */
	List<ChatMessageIllegal> findListByParam(ChatMessageIllegalQuery query);

	/**
	 * 非法聊天信息表根据条件查询数量
	 */
	Integer findCountByParam(ChatMessageIllegalQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<ChatMessageIllegal> findListByPage(ChatMessageIllegalQuery query);

	/**
	 * 新增
	 */
	Integer add(ChatMessageIllegal bean, TokenUserInfoDto tokenUserInfoDto);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatMessageIllegal> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<ChatMessageIllegal> listBean);

	/**
	 * 根据MessageId查询
	 */
	ChatMessageIllegal getChatMessageIllegalByMessageId(Long messageId);

	/**
	 * 根据MessageId更新
	 */
	Integer updateChatMessageIllegalByMessageId(ChatMessageIllegal t, Long messageId);

	/**
	 * 根据MessageId删除
	 */
	Integer deleteChatMessageIllegalByMessageId(Long messageId);

}