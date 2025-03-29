package com.easyChat.controller;

import com.easyChat.entity.po.ChatSessionUser;
import com.easyChat.entity.query.ChatSessionUserQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.ChatSessionUserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:会话用户Controller
 * @author:Shen-An
 * @date:2025/02/07
 */
@RestController
@RequestMapping("/chatSessionUser")
public class ChatSessionUserController extends ABaseController {

	@Resource
	private ChatSessionUserService chatSessionUserService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(ChatSessionUserQuery query) {
		return getSuccessResponseVo(chatSessionUserService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(ChatSessionUser bean) {
		this.chatSessionUserService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<ChatSessionUser> listBean) {
		this.chatSessionUserService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<ChatSessionUser> listBean) {
		this.chatSessionUserService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserIdAndContactId查询
	 */

	@RequestMapping("getChatSessionUserByUserIdAndContactId")
	public ResponseVo getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return getSuccessResponseVo(this.chatSessionUserService.getChatSessionUserByUserIdAndContactId(userId, contactId));
	}

	/**
	 * 根据UserIdAndContactId更新
	 */

	@RequestMapping("updateChatSessionUserByUserIdAndContactId")
	public ResponseVo updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
		this.chatSessionUserService.updateChatSessionUserByUserIdAndContactId(bean,userId, contactId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */

	@RequestMapping("deleteChatSessionUserByUserIdAndContactId")
	public ResponseVo deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		this.chatSessionUserService.deleteChatSessionUserByUserIdAndContactId(userId, contactId);
		return getSuccessResponseVo(null);
	}


}