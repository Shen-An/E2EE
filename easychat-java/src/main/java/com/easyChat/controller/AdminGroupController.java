package com.easyChat.controller;


import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController{
    @Resource
    private GroupInfoService groupInfoService;

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo dissolutionGroup(@NotEmpty String groupId) {
        GroupInfo groupInfo = groupInfoService.getGroupInfoByGroupId(groupId);
        if(groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(),groupId);
        return getSuccessResponseVo(null);
    }
}
