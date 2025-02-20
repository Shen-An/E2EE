package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.dto.UserContactSearchResultDto;
import com.easyChat.entity.po.UserContactApply;
import com.easyChat.entity.query.UserContactApplyQuery;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.enums.PageSize;
import com.easyChat.service.UserContactApplyService;
import com.easyChat.service.UserContactService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
	@Resource
	private UserContactApplyService userContactApplyService;

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

	/**
	 * 搜索人/群
	 * @param request
	 * @param contactId
	 * @return
	 */
	@RequestMapping("/search")
	@GlobalInterceptor
	public ResponseVo search(HttpServletRequest request, @NotEmpty String contactId) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserContactSearchResultDto resultDto = userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
		return getSuccessResponseVo(resultDto);
	}

	/**
	 * 发送添加请求
	 * @param request
	 * @param contactId
	 * @param applyInfo
	 * @param contactType
	 * @return
	 */
	@RequestMapping("/applyAdd")
	@GlobalInterceptor
	public ResponseVo applyAdd(HttpServletRequest request,@NotEmpty String contactId, String applyInfo, String contactType) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		Integer joinType =  userContactService.applyAdd(tokenUserInfoDto,contactId,applyInfo);
		return getSuccessResponseVo(joinType);
	}

	/**
	 * 加载申请列表
	 * @param request
	 * @param pageNo
	 * @return
	 */
	@RequestMapping("loadApply")
	@GlobalInterceptor
	public ResponseVo loadApply(HttpServletRequest request, Integer pageNo) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserContactApplyQuery applyQuery = new UserContactApplyQuery();
		applyQuery.setOrderBy("last_apply_time desc");
		applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
		applyQuery.setPageNo(pageNo);
		applyQuery.setPageSize(PageSize.SIZE15.getSize());
		applyQuery.setQueryContactInfo(true);

		PaginationResultVo resultVo = userContactApplyService.findListByPage(applyQuery);
		return getSuccessResponseVo(resultVo);
	}
	@RequestMapping("/dealWithApply")
	@GlobalInterceptor
	public ResponseVo dealWithApply(HttpServletRequest request, @NotNull Integer applyId,@NotNull Integer status) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(),applyId,status);
//		userContactApplyService.dealWithApply("U77786048081",applyId,status);
		return getSuccessResponseVo(null);
	}

}