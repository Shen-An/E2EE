package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.query.UserInfoQuery;
import com.easyChat.entity.vo.UserInfoVo;
import com.easyChat.service.UserInfoService;

import com.easyChat.utils.CopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description:用户信息表Controller
 * @author:Shen-An
 * @date:2025/01/17
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController {

	@Resource
	private UserInfoService userInfoService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(UserInfoQuery query) {
		return getSuccessResponseVo(userInfoService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(UserInfo bean) {
		this.userInfoService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<UserInfo> listBean) {
		this.userInfoService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<UserInfo> listBean) {
		this.userInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserId查询
	 */

	@RequestMapping("getUserInfoByUserId")
	public ResponseVo getUserInfoByUserId(String userId) {
		return getSuccessResponseVo(this.userInfoService.getUserInfoByUserId(userId));
	}

	/**
	 * 根据UserId更新
	 */

	@RequestMapping("updateUserInfoByUserId")
	public ResponseVo updateUserInfoByUserId(UserInfo bean, String userId) {
		this.userInfoService.updateUserInfoByUserId(bean,userId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserId删除
	 */

	@RequestMapping("deleteUserInfoByUserId")
	public ResponseVo deleteUserInfoByUserId(String userId) {
		this.userInfoService.deleteUserInfoByUserId(userId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Email查询
	 */

	@RequestMapping("getUserInfoByEmail")
	public ResponseVo getUserInfoByEmail(String email) {
		return getSuccessResponseVo(this.userInfoService.getUserInfoByEmail(email));
	}

	/**
	 * 根据Email更新
	 */

	@RequestMapping("updateUserInfoByEmail")
	public ResponseVo updateUserInfoByEmail(UserInfo bean, String email) {
		this.userInfoService.updateUserInfoByEmail(bean,email);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Email删除
	 */

	@RequestMapping("deleteUserInfoByEmail")
	public ResponseVo deleteUserInfoByEmail(String email) {
		this.userInfoService.deleteUserInfoByEmail(email);
		return getSuccessResponseVo(null);
	}

	@GlobalInterceptor
	@RequestMapping("/getUserInfo")
	public ResponseVo getUserInfo(HttpServletRequest request) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
		UserInfoVo userInfoVo = CopyUtils.copy(userInfo,UserInfoVo.class);
		userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());
		return getSuccessResponseVo(userInfoVo);
	}
}