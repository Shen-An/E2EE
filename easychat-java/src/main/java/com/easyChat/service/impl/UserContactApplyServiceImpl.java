package com.easyChat.service.impl;

import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.enums.PageSize;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserContactApply;
import com.easyChat.entity.query.UserContactApplyQuery;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.enums.UserContactApplyStatusEnum;
import com.easyChat.enums.UserContactStatusEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.UserContactApplyMapper;
import com.easyChat.mappers.UserContactMapper;
import com.easyChat.service.UserContactApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Description:联系人申请Service
 * @author:Shen-An
 * @date:2025/01/18
 */
@Service("userContactApplyService")
public class UserContactApplyServiceImpl implements UserContactApplyService{

	@Resource
	private UserContactApplyMapper<UserContactApply,UserContactApplyQuery> userContactApplyMapper;
    @Autowired
    private UserContactMapper userContactMapper;

	/**
	 * 联系人申请根据条件查询列表
	 */
	public List<UserContactApply> findListByParam(UserContactApplyQuery query) {
		return this.userContactApplyMapper.selectList(query);
	}

	/**
	 * 联系人申请根据条件查询数量
	 */
	public Integer findCountByParam(UserContactApplyQuery query) {
		return this.userContactApplyMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<UserContactApply> findListByPage(UserContactApplyQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize(): query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count,pageSize);
		query.setSimplePage(page);
		List<UserContactApply>list = this.findListByParam(query);
		PaginationResultVo<UserContactApply> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(UserContactApply bean) {
		return this.userContactApplyMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<UserContactApply> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactApplyMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<UserContactApply> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactApplyMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据ApplyId查询
	 */
	public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
		return this.userContactApplyMapper.selectByApplyId(applyId);
	}

	/**
	 * 根据ApplyId更新
	 */
	public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
		return this.userContactApplyMapper.updateByApplyId(bean,applyId);
	}

	/**
	 * 根据ApplyId删除
	 */
	public Integer deleteUserContactApplyByApplyId(Integer applyId) {
		return this.userContactApplyMapper.deleteByApplyId(applyId);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
	 */
	public UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId更新
	 */
	public Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
		return this.userContactApplyMapper.updateByApplyUserIdAndReceiveUserIdAndContactId(bean,applyUserId, receiveUserId, contactId);
	}

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	public Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return this.userContactApplyMapper.deleteByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void dealWithApply(String userId, Integer applyId, Integer status) {
		UserContactApplyStatusEnum userContactApplyStatusEnum = UserContactApplyStatusEnum.getByStatus(status);
		if(userContactApplyStatusEnum == null || userContactApplyStatusEnum.equals(UserContactApplyStatusEnum.INIT)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		UserContactApply userContactApply = userContactApplyMapper.selectByApplyId(applyId);
		if(userContactApply == null &&!userId.equals(userContactApply.getReceiveUserId())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//防止并发，只能从状态INIT(0)改为其他的状态如1，不能从其他的状态到其他的状态
		UserContactApply updateInfo = new UserContactApply();
		updateInfo.setStatus(userContactApplyStatusEnum.getStatus());
		updateInfo.setLastApplyTime(System.currentTimeMillis());

		UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
		userContactApplyQuery.setApplyId(applyId);
		userContactApplyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());

		Integer count = userContactApplyMapper.updateByParam(updateInfo,userContactApplyQuery);
		if(count == 0){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//通过
		if(UserContactApplyStatusEnum.PASS.getStatus().equals(status)){
			//TODO 添加联系人
			return;
		}
		//拒绝不做处理

		//拉黑
		if(UserContactApplyStatusEnum.BLACKLIST.equals(userContactApplyStatusEnum)){
			UserContact userContact = new UserContact();
			userContact.setUserId(userContactApply.getApplyUserId());
			userContact.setContactId(userContactApply.getContactId());
			userContact.setContactType(userContactApply.getContactType());
			userContact.setCreateTime(new Date());
			userContact.setStatus(UserContactStatusEnum.BLACK_LIST_BE.getStatus());
			userContact.setLastUpdateTime(new Date());
			userContactMapper.insertOrUpdate(userContact);
		}
	}
}