package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.query.GroupInfoQuery;
import com.easyChat.service.GroupInfoService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:群组表Controller
 * @author:Shen-An
 * @date:2025/01/18
 */
@RestController
@RequestMapping("/groupInfo")
public class GroupInfoController extends ABaseController {

	@Resource
	private GroupInfoService groupInfoService;

	@RequestMapping("loadDataList")
	public ResponseVo loadDataList(GroupInfoQuery query) {
		return getSuccessResponseVo(groupInfoService.findListByPage(query));
	}
	/**
	 * 新增
	 */

	@RequestMapping("add")
	public ResponseVo add(GroupInfo bean) {
		this.groupInfoService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */

	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<GroupInfo> listBean) {
		this.groupInfoService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */

	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<GroupInfo> listBean) {
		this.groupInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据GroupId查询
	 */

	@RequestMapping("getGroupInfoByGroupId")
	public ResponseVo getGroupInfoByGroupId(String groupId) {
		return getSuccessResponseVo(this.groupInfoService.getGroupInfoByGroupId(groupId));
	}

	/**
	 * 根据GroupId更新
	 */

	@RequestMapping("updateGroupInfoByGroupId")
	public ResponseVo updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
		this.groupInfoService.updateGroupInfoByGroupId(bean,groupId);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据GroupId删除
	 */

	@RequestMapping("deleteGroupInfoByGroupId")
	public ResponseVo deleteGroupInfoByGroupId(String groupId) {
		this.groupInfoService.deleteGroupInfoByGroupId(groupId);
		return getSuccessResponseVo(null);
	}

	/**
	 *
	 * @param request
	 * @param groupId
	 * @param groupName
	 * @param groupNotice
	 * @param joinType
	 * @param avatarFile 初始的封面
	 * @param avatarCover 经过Electronic处理的封面
	 * @return
	 */
	@GlobalInterceptor
	@RequestMapping("/saveGroup")
	public ResponseVo saveGroup(HttpServletRequest request,
								String groupId,
								@NotEmpty String groupName,
								String groupNotice,
								@NotNull Integer joinType,
								MultipartFile avatarFile,
								MultipartFile avatarCover
								) {
		TokenUserInfoDto TokenUserInfoDto = getTokenUserInfo(request);
		return getSuccessResponseVo(null);
	}
}