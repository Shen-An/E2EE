package com.easyChat.service;

import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserContactApply;
import com.easyChat.entity.query.UserContactApplyQuery;

import java.util.List;

/**
 * @Description:联系人申请Service
 * @author:Shen-An
 * @date:2025/01/18
 */
public interface UserContactApplyService {

    /**
     * 联系人申请根据条件查询列表
     */
    List<UserContactApply> findListByParam(UserContactApplyQuery query);

    /**
     * 联系人申请根据条件查询数量
     */
    Integer findCountByParam(UserContactApplyQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<UserContactApply> findListByPage(UserContactApplyQuery query);

    /**
     * 新增
     */
    Integer add(UserContactApply bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserContactApply> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<UserContactApply> listBean);

    /**
     * 根据ApplyId查询
     */
    UserContactApply getUserContactApplyByApplyId(Integer applyId);

    /**
     * 根据ApplyId更新
     */
    Integer updateUserContactApplyByApplyId(UserContactApply t, Integer applyId);

    /**
     * 根据ApplyId删除
     */
    Integer deleteUserContactApplyByApplyId(Integer applyId);

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
     */
    UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId);

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId更新
     */
    Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply t, String applyUserId, String receiveUserId, String contactId);

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
     */
    Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId);

    /**
     * 处理申请
     */
    void dealWithApply(String userId, Integer applyId, Integer status);


}