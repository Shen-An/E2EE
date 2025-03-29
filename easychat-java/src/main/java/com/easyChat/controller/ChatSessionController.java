package com.easyChat.controller;

import com.easyChat.entity.po.ChatSession;
import com.easyChat.entity.query.ChatSessionQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.ChatSessionService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:Controller
 * @author:Shen-An
 * @date:2025/02/07
 */
@RestController
@RequestMapping("/chatSession")
public class ChatSessionController extends ABaseController {

	@Resource
	private ChatSessionService chatSessionService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(ChatSessionQuery query) {
		return getSuccessResponseVo(chatSessionService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(ChatSession bean) {
		this.chatSessionService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<ChatSession> listBean) {
		this.chatSessionService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<ChatSession> listBean) {
		this.chatSessionService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据SessionId查询
	 */

	@RequestMapping("getChatSessionBySessionId")
	public ResponseVo getChatSessionBySessionId(String sessionId) {
		return getSuccessResponseVo(this.chatSessionService.getChatSessionBySessionId(sessionId));
	}

	/**
	 * 根据SessionId更新
	 */

	@RequestMapping("updateChatSessionBySessionId")
	public ResponseVo updateChatSessionBySessionId(ChatSession bean, String sessionId) {
		this.chatSessionService.updateChatSessionBySessionId(bean,sessionId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据SessionId删除
	 */

	@RequestMapping("deleteChatSessionBySessionId")
	public ResponseVo deleteChatSessionBySessionId(String sessionId) {
		this.chatSessionService.deleteChatSessionBySessionId(sessionId);
		return getSuccessResponseVo(null);
	}


}