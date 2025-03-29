package com.easyChat.controller;

import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.query.ChatMessageQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.ChatMessageService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:聊天信息表Controller
 * @author:Shen-An
 * @date:2025/02/07
 */
@RestController
@RequestMapping("/chatMessage")
public class ChatMessageController extends ABaseController {

	@Resource
	private ChatMessageService chatMessageService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(ChatMessageQuery query) {
		return getSuccessResponseVo(chatMessageService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(ChatMessage bean) {
		this.chatMessageService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<ChatMessage> listBean) {
		this.chatMessageService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<ChatMessage> listBean) {
		this.chatMessageService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据MessageId查询
	 */

	@RequestMapping("getChatMessageByMessageId")
	public ResponseVo getChatMessageByMessageId(Long messageId) {
		return getSuccessResponseVo(this.chatMessageService.getChatMessageByMessageId(messageId));
	}

	/**
	 * 根据MessageId更新
	 */

	@RequestMapping("updateChatMessageByMessageId")
	public ResponseVo updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
		this.chatMessageService.updateChatMessageByMessageId(bean,messageId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据MessageId删除
	 */

	@RequestMapping("deleteChatMessageByMessageId")
	public ResponseVo deleteChatMessageByMessageId(Long messageId) {
		this.chatMessageService.deleteChatMessageByMessageId(messageId);
		return getSuccessResponseVo(null);
	}


}