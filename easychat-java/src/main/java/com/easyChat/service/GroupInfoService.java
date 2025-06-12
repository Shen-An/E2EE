package com.easyChat.service;

import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.query.GroupInfoQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Description:群组表Service
 * @author:Shen-An
 * @date:2025/01/18
 */
public interface GroupInfoService {

    /**
     * 群组表根据条件查询列表
     */
    List<GroupInfo> findListByParam(GroupInfoQuery query);

    /**
     * 群组表根据条件查询数量
     */
    Integer findCountByParam(GroupInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<GroupInfo> findListByPage(GroupInfoQuery query);

    /**
     * 新增
     */
    Integer add(GroupInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<GroupInfo> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<GroupInfo> listBean);

    /**
     * 根据GroupId查询
     */
    GroupInfo getGroupInfoByGroupId(String groupId);

    /**
     * 根据GroupId更新
     */
    Integer updateGroupInfoByGroupId(GroupInfo t, String groupId);

    /**
     * 根据GroupId删除
     */
    Integer deleteGroupInfoByGroupId(String groupId);



    void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCove) throws IOException;


    void dissolutionGroup(String groupOwnerId, String groupId);
}