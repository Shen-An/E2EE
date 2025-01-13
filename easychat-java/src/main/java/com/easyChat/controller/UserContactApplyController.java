package com.easyChat.controller;

import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.UserContactApply;
import com.easyChat.entity.query.UserContactApplyQuery;
import com.easyChat.service.UserContactApplyService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:联系人申请Controller
 * @author:Shen-An
 * @date:2025/01/18
 */
@RestController
@RequestMapping("/userContactApply")
public class UserContactApplyController extends ABaseController {

	@Resource
	private UserContactApplyService userContactApplyService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(UserContactApplyQuery query) {
		return getSuccessResponseVo(userContactApplyService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(UserContactApply bean) {
		this.userContactApplyService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<UserContactApply> listBean) {
		this.userContactApplyService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<UserContactApply> listBean) {
		this.userContactApplyService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据ApplyId查询
	 */

	@RequestMapping("getUserContactApplyByApplyId")
	public ResponseVo getUserContactApplyByApplyId(Integer applyId) {
		return getSuccessResponseVo(this.userContactApplyService.getUserContactApplyByApplyId(applyId));
	}

	/**
	 * 根据ApplyId更新
	 */

	@RequestMapping("updateUserContactApplyByApplyId")
	public ResponseVo updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
		this.userContactApplyService.updateUserContactApplyByApplyId(bean,applyId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据ApplyId删除
	 */

	@RequestMapping("deleteUserContactApplyByApplyId")
	public ResponseVo deleteUserContactApplyByApplyId(Integer applyId) {
		this.userContactApplyService.deleteUserContactApplyByApplyId(applyId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
	 */

	@RequestMapping("getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId")
	public ResponseVo getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return getSuccessResponseVo(this.userContactApplyService.getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId));
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId更新
	 */

	@RequestMapping("updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId")
	public ResponseVo updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
		this.userContactApplyService.updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(bean,applyUserId, receiveUserId, contactId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */

	@RequestMapping("deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId")
	public ResponseVo deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		this.userContactApplyService.deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
		return getSuccessResponseVo(null);
	}


}