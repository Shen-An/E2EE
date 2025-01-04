package com.easyChat.controller;

import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.UserInfoBeauty;
import com.easyChat.entity.query.UserInfoBeautyQuery;
import com.easyChat.service.UserInfoBeautyService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:靓号表Controller
 * @author:Shen-An
 * @date:2025/01/17
 */
@RestController
@RequestMapping("/userInfoBeauty")
public class UserInfoBeautyController extends ABaseController {

	@Resource
	private UserInfoBeautyService userInfoBeautyService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(UserInfoBeautyQuery query) {
		return getSuccessResponseVo(userInfoBeautyService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(UserInfoBeauty bean) {
		this.userInfoBeautyService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<UserInfoBeauty> listBean) {
		this.userInfoBeautyService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<UserInfoBeauty> listBean) {
		this.userInfoBeautyService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Id查询
	 */

	@RequestMapping("getUserInfoBeautyById")
	public ResponseVo getUserInfoBeautyById(Integer id) {
		return getSuccessResponseVo(this.userInfoBeautyService.getUserInfoBeautyById(id));
	}

	/**
	 * 根据Id更新
	 */

	@RequestMapping("updateUserInfoBeautyById")
	public ResponseVo updateUserInfoBeautyById(UserInfoBeauty bean, Integer id) {
		this.userInfoBeautyService.updateUserInfoBeautyById(bean,id);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Id删除
	 */

	@RequestMapping("deleteUserInfoBeautyById")
	public ResponseVo deleteUserInfoBeautyById(Integer id) {
		this.userInfoBeautyService.deleteUserInfoBeautyById(id);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Email查询
	 */

	@RequestMapping("getUserInfoBeautyByEmail")
	public ResponseVo getUserInfoBeautyByEmail(String email) {
		return getSuccessResponseVo(this.userInfoBeautyService.getUserInfoBeautyByEmail(email));
	}

	/**
	 * 根据Email更新
	 */

	@RequestMapping("updateUserInfoBeautyByEmail")
	public ResponseVo updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email) {
		this.userInfoBeautyService.updateUserInfoBeautyByEmail(bean,email);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Email删除
	 */

	@RequestMapping("deleteUserInfoBeautyByEmail")
	public ResponseVo deleteUserInfoBeautyByEmail(String email) {
		this.userInfoBeautyService.deleteUserInfoBeautyByEmail(email);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserId查询
	 */

	@RequestMapping("getUserInfoBeautyByUserId")
	public ResponseVo getUserInfoBeautyByUserId(String userId) {
		return getSuccessResponseVo(this.userInfoBeautyService.getUserInfoBeautyByUserId(userId));
	}

	/**
	 * 根据UserId更新
	 */

	@RequestMapping("updateUserInfoBeautyByUserId")
	public ResponseVo updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId) {
		this.userInfoBeautyService.updateUserInfoBeautyByUserId(bean,userId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据UserId删除
	 */

	@RequestMapping("deleteUserInfoBeautyByUserId")
	public ResponseVo deleteUserInfoBeautyByUserId(String userId) {
		this.userInfoBeautyService.deleteUserInfoBeautyByUserId(userId);
		return getSuccessResponseVo(null);
	}


}