package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessageIllegal;
import com.easyChat.entity.query.ChatMessageIllegalQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.ChatMessageIllegalService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description:非法聊天信息表Controller
 * @author:Shen-An
 * @date:2025/04/14
 */
@RestController
@RequestMapping("/chatMessageIllegal")
public class ChatMessageIllegalController extends ABaseController {

	@Resource
	private ChatMessageIllegalService chatMessageIllegalService;

//	@RequestMapping("loadDataList")
//	public ResponseVo loadDataList(ChatMessageIllegalQuery query) {
//		return getSuccessResponseVo(chatMessageIllegalService.findListByPage(query));
//	}
//	/**
//	 * 新增
//	 */
	@GlobalInterceptor
	@RequestMapping("addIllegalMessage")
	public ResponseVo add(ChatMessageIllegal bean, HttpServletRequest request) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		this.chatMessageIllegalService.add(bean,tokenUserInfoDto);
		return getSuccessResponseVo(null);
	}

//	/**
//	 * 批量新增
//	 */
//
//	@RequestMapping("addBatch")
//	public ResponseVo addBatch(@RequestBody List<ChatMessageIllegal> listBean) {
//		this.chatMessageIllegalService.addBatch(listBean);
//		return getSuccessResponseVo(null);
//	}
//
//	/**
//	 * 批量新增或修改
//	 */
//
//	@RequestMapping("addOrUpdateBatch")
//	public ResponseVo addOrUpdateBatch(@RequestBody List<ChatMessageIllegal> listBean) {
//		this.chatMessageIllegalService.addOrUpdateBatch(listBean);
//		return getSuccessResponseVo(null);
//	}
//
//	/**
//	 * 根据MessageId查询
//	 */
//
//	@RequestMapping("getChatMessageIllegalByMessageId")
//	public ResponseVo getChatMessageIllegalByMessageId(Long messageId) {
//		return getSuccessResponseVo(this.chatMessageIllegalService.getChatMessageIllegalByMessageId(messageId));
//	}
//
//	/**
//	 * 根据MessageId更新
//	 */
//
//	@RequestMapping("updateChatMessageIllegalByMessageId")
//	public ResponseVo updateChatMessageIllegalByMessageId(ChatMessageIllegal bean, Long messageId) {
//		this.chatMessageIllegalService.updateChatMessageIllegalByMessageId(bean,messageId);
//		return getSuccessResponseVo(null);
//	}
//
//	/**
//	 * 根据MessageId删除
//	 */
//
//	@RequestMapping("deleteChatMessageIllegalByMessageId")
//	public ResponseVo deleteChatMessageIllegalByMessageId(Long messageId) {
//		this.chatMessageIllegalService.deleteChatMessageIllegalByMessageId(messageId);
//		return getSuccessResponseVo(null);
//	}


}