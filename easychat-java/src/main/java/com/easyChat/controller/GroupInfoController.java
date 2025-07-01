package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.entity.vo.GroupInfoVo;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.query.GroupInfoQuery;
import com.easyChat.enums.GroupStatusEnum;
import com.easyChat.enums.MessageTypeEnum;
import com.easyChat.enums.UserContactStatusEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.service.GroupInfoService;

import com.easyChat.service.UserContactService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
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
    @Resource
    private UserContactService userContactService;

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
        this.groupInfoService.updateGroupInfoByGroupId(bean, groupId);
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
     * 保存/更新群聊
     *
     * @param request
     * @param groupId
     * @param groupName
     * @param groupNotice
     * @param joinType
     * @param avatarFile
     * @param avatarCover
     * @return
     * @throws IOException
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
    ) throws IOException {
        TokenUserInfoDto TokenUserInfoDto = getTokenUserInfo(request);
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupOwnerId(TokenUserInfoDto.getUserId());

        groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);
        return getSuccessResponseVo(null);
    }

    /**
     * 加载我的群聊
     *
     * @param request
     * @return
     */
    @RequestMapping("/loadMyGroup")
    @GlobalInterceptor
    public ResponseVo loadMyGroup(HttpServletRequest request) {
        TokenUserInfoDto TokenUserInfoDto = getTokenUserInfo(request);
        GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(TokenUserInfoDto.getUserId());
        groupInfoQuery.setOrderBy("create_time asc");
        List<GroupInfo> list = this.groupInfoService.findListByParam(groupInfoQuery);
        return getSuccessResponseVo(list);
    }

    /**
     * 获取群聊详细信息
     *
     * @param request
     * @param groupId
     * @return
     */
    @RequestMapping("/getGroupInfo")
    @GlobalInterceptor
    public ResponseVo getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setOrderBy("create_time asc");
        Integer memberCount = userContactService.findCountByParam(userContactQuery);
        groupInfo.setMemberCount(memberCount);
        return getSuccessResponseVo(groupInfo);
    }

    /**
     * 查群组和联系人的信息
     *
     * @param request
     * @param groupId
     * @return
     */
    @RequestMapping("/getGroupInfo4Chat")
    @GlobalInterceptor
    public ResponseVo getGroupInfo4Chat(HttpServletRequest request, @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setQueryUserInfo(true);
        userContactQuery.setOrderBy("create_time asc");
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        groupInfoVo.setGroupInfo(groupInfo);
        groupInfoVo.setUserContactList(userContactList);
        return getSuccessResponseVo(groupInfoVo);
    }


    private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

        //判断是否是本群组人员的请求
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), groupId);
        if (userContact == null || UserContactStatusEnum.FRIEND.equals(userContact.getStatus())) {
            throw new BusinessException("你不在群聊中/群聊不存在/群聊已解散");
        }

        GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
        //判断是否群聊正常
        if (groupInfo == null || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在/群聊已解散");
        }
        return groupInfo;
    }

    @RequestMapping("/addOrRemoveGroupUser")
    @GlobalInterceptor()
    public ResponseVo addOrRemoveGroupUser(HttpServletRequest request,
                                           @NotEmpty String groupId,
                                           @NotEmpty String selectContacts,
                                           @NotNull Integer opType) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto, groupId, selectContacts, opType);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/leaveGroup")
    @GlobalInterceptor()
    public ResponseVo leaveGroup(HttpServletRequest request,
                                           @NotEmpty String groupId
                                           ) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(),groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor()
    public ResponseVo dissolutionGroup(HttpServletRequest request,
                                           @NotEmpty String groupId
    ) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        groupInfoService.dissolutionGroup(tokenUserInfoDto.getUserId(),groupId);
        return getSuccessResponseVo(null);
    }
}