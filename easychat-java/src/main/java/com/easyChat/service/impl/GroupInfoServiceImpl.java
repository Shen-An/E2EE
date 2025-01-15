package com.easyChat.service.impl;

import com.easyChat.constants.Constants;
import com.easyChat.entity.config.AppConfig;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.enums.PageSize;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.query.GroupInfoQuery;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.enums.UserContactStatusEnum;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.GroupInfoMapper;
import com.easyChat.mappers.UserContactMapper;
import com.easyChat.redis.RedisComponent;
import com.easyChat.service.GroupInfoService;
import com.easyChat.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Description:群组表Service
 * @author:Shen-An
 * @date:2025/01/18
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private AppConfig appConfig;

    /**
     * 群组表根据条件查询列表
     */
    public List<GroupInfo> findListByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectList(query);
    }

    /**
     * 群组表根据条件查询数量
     */
    public Integer findCountByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<GroupInfo> findListByPage(GroupInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(query);
        PaginationResultVo<GroupInfo> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据GroupId查询
     */
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId更新
     */
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    /**
     * @param groupInfo
     * @param avatarFile
     * @param avatarCove
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCove) throws IOException {
        Date curDate = new Date();
        //新增群组
        if (StringTools.isEmpty(groupInfo.getGroupId())) {
            GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
            groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
            int count = this.findCountByParam(groupInfoQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupCount()) {
                throw new BusinessException("最多能创建" + sysSettingDto.getMaxGroupCount() + "个群聊");
            }
            if (avatarFile == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            groupInfo.setCreateTime(curDate);
            groupInfo.setGroupId(StringTools.getGroupId());
            this.groupInfoMapper.insert(groupInfo);

            //将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContactMapper.insert(userContact);

            //TODO 创建会话
            //TODO 发送消息

        } else {
            //修改
            GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            this.updateGroupInfoByGroupId(groupInfo, groupInfo.getGroupId());
            //TODO更新冗余表信息

            //TODO修改群昵称发送ws消息

        }
        if(null == avatarFile){
            return;
        }
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFile = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        String filePath = targetFile.getPath()+"/"+groupInfo.getGroupId()+Constants.IMAGE_SUFFIX;
        avatarFile.transferTo(new File(filePath));
        avatarCove.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));

    }


}