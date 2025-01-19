package com.easyChat.controller;

import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.dto.UserContactSearchResultDto;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.service.UserContactService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @Description:联系人Controller
 * @author:Shen-An
 * @date:2025/01/18
 */
@RestController
@RequestMapping("/userContact")
public class UserContactController extends ABaseController {

	@Resource
	private UserContactService userContactService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(UserContactQuery query) {
		return getSuccessResponseVo(userContactService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(UserContact bean) {
		this.userContactService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<UserContact> listBean) {
		this.userContactService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<UserContact> listBean) {
		this.userContactService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserIdAndContactId查询
	 */

	@RequestMapping("getUserContactByUserIdAndContactId")
	public ResponseVo getUserContactByUserIdAndContactId(String userId, String contactId) {
		return getSuccessResponseVo(this.userContactService.getUserContactByUserIdAndContactId(userId, contactId));
	}

	/**
	 * 根据UserIdAndContactId更新
	 */

	@RequestMapping("updateUserContactByUserIdAndContactId")
	public ResponseVo updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
		this.userContactService.updateUserContactByUserIdAndContactId(bean,userId, contactId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */

	@RequestMapping("deleteUserContactByUserIdAndContactId")
	public ResponseVo deleteUserContactByUserIdAndContactId(String userId, String contactId) {
		this.userContactService.deleteUserContactByUserIdAndContactId(userId, contactId);
		return getSuccessResponseVo(null);
	}


	@RequestMapping("/search")
	public ResponseVo search(HttpServletRequest request, @NotEmpty String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserContactSearchResultDto resultDto = userContactService.searchContact("U77786048081", contactId);
		return getSuccessResponseVo(resultDto);
	}
}