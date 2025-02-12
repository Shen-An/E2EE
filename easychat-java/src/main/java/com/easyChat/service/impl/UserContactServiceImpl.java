package com.easyChat.service.impl;

import com.easyChat.entity.dto.UserContactSearchResultDto;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.po.UserInfoBeauty;
import com.easyChat.entity.query.GroupInfoQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.enums.PageSize;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.enums.UserContactStatusEnum;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.mappers.GroupInfoMapper;
import com.easyChat.mappers.UserContactMapper;
import com.easyChat.mappers.UserInfoMapper;
import com.easyChat.service.UserContactService;
import com.easyChat.utils.CopyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:联系人Service
 * @author:Shen-An
 * @date:2025/01/18
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoBeauty> userInfoMapper;
    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    /**
     * 联系人根据条件查询列表
     */
    public List<UserContact> findListByParam(UserContactQuery query) {
        return this.userContactMapper.selectList(query);
    }

    /**
     * 联系人根据条件查询数量
     */
    public Integer findCountByParam(UserContactQuery query) {
        return this.userContactMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserContact> findListByPage(UserContactQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserContact> list = this.findListByParam(query);
        PaginationResultVo<UserContact> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserContact bean) {
        return this.userContactMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserIdAndContactId查询
     */
    public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId更新
     */
    public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
        return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据userId查contactId（查人或者群组）
     * @param userId
     * @param contactId
     * @return
     */
    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
		/*得到  USER(0,"U","好友"),
		  GROUP(1,"G","群")
		  中的一种
		  即根据contactId搜人还是搜群组
		 */
        UserContactSearchResultDto result = new UserContactSearchResultDto();
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (userContactTypeEnum == null) {
            return null;
        }
        switch (userContactTypeEnum) {
            case USER:
                UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
                if (userInfo == null) {
                    return null;
                }
                result = CopyUtils.copy(userInfo,UserContactSearchResultDto.class);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
                if (groupInfo == null) {
                    return null;
                }
                result.setNickName(groupInfo.getGroupName());
                break;
        }
        result.setContactType(userContactTypeEnum.toString());
        result.setContactId(contactId);
        //如果userId查自己
        if(userId.equals(contactId)) {
            result.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return result;
        }
        //是否是好友
        UserContact userContact = userContactMapper.selectByUserIdAndContactId(userId, contactId);
        result.setStatus(userContact == null ? null : userContact.getStatus());
        return result;
    }


}