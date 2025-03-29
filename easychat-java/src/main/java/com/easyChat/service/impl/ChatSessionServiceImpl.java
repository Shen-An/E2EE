package com.easyChat.service.impl;

import com.easyChat.entity.po.ChatSession;
import com.easyChat.entity.query.ChatSessionQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.PageSize;
import com.easyChat.mappers.ChatSessionMapper;
import com.easyChat.service.ChatSessionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:Service
 * @author:Shen-An
 * @date:2025/02/07
 */
@Service("chatSessionService")
public class ChatSessionServiceImpl implements ChatSessionService{

	@Resource
	private ChatSessionMapper<ChatSession,ChatSessionQuery> chatSessionMapper;

	/**
	 * 根据条件查询列表
	 */
	public List<ChatSession> findListByParam(ChatSessionQuery query) {
		return this.chatSessionMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(ChatSessionQuery query) {
		return this.chatSessionMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<ChatSession> findListByPage(ChatSessionQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize(): query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count,pageSize);
		query.setSimplePage(page);
		List<ChatSession>list = this.findListByParam(query);
		PaginationResultVo<ChatSession> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(ChatSession bean) {
		return this.chatSessionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<ChatSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<ChatSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据SessionId查询
	 */
	public ChatSession getChatSessionBySessionId(String sessionId) {
		return this.chatSessionMapper.selectBySessionId(sessionId);
	}

	/**
	 * 根据SessionId更新
	 */
	public Integer updateChatSessionBySessionId(ChatSession bean, String sessionId) {
		return this.chatSessionMapper.updateBySessionId(bean,sessionId);
	}

	/**
	 * 根据SessionId删除
	 */
	public Integer deleteChatSessionBySessionId(String sessionId) {
		return this.chatSessionMapper.deleteBySessionId(sessionId);
	}


}