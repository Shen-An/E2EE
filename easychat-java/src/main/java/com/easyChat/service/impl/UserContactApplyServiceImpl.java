package com.easyChat.service.impl;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.GroupInfo;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.query.*;
import com.easyChat.enums.*;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserContactApply;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.GroupInfoMapper;
import com.easyChat.mappers.UserContactApplyMapper;
import com.easyChat.mappers.UserContactMapper;
import com.easyChat.mappers.UserInfoMapper;
import com.easyChat.redis.RedisComponent;
import com.easyChat.service.UserContactApplyService;
import com.easyChat.service.UserContactService;
import com.easyChat.utils.StringTools;
import com.easyChat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    @Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
	@Resource
	private RedisComponent redisComponent;
	@Resource
	private UserContactService userContactService;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery>groupInfoMapper;
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

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

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
		UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
		if (userContactTypeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		Integer joinType = null;
		String receiveUserId = contactId;

		//申请人ID
		String applyUserId = tokenUserInfoDto.getUserId();
		//如没有填写申请信息-》默认申请信息
		applyInfo = StringTools.isEmpty(applyInfo) ? String.format(Constants.APPLY_INFO_TEMPLATE, tokenUserInfoDto.getNickName()) : applyInfo;

		//查询是否拉黑
		UserContact userContact = userContactMapper.selectByUserIdAndContactId(applyUserId, contactId);
		if (userContact != null &&
				ArrayUtils.contains(new Integer[]{
						UserContactStatusEnum.BLACK_LIST_BE_FIRST.getStatus(),
						UserContactStatusEnum.BLACK_LIST_BE.getStatus()
				}, userContact.getStatus())) {

			throw new BusinessException("对方拒绝接受你的消息");
		}

		//如果是群组
		if (userContactTypeEnum.equals(UserContactTypeEnum.GROUP)) {
			GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
			if (groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
				throw new BusinessException("群聊不存在或已解散");
			}
			receiveUserId = groupInfo.getGroupOwnerId();
			joinType = groupInfo.getJoinType();
		} else {
			//是用户
			UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
			if (userInfo == null) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			joinType = userInfo.getJoinType();
		}
		//直接加入的情况
		if (JoinTypeEnum.JOIN.equals(joinType)) {
			//添加联系人
			userContactService.addContact(applyUserId, receiveUserId, contactId, userContactTypeEnum.getType(), applyInfo);
			return joinType;
		}

		//申请
		UserContactApply dbApply = userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
		//之前没有添加过
		if (dbApply == null) {
			UserContactApply userContactApply = new UserContactApply();
			userContactApply.setApplyUserId(applyUserId);
			userContactApply.setContactType(userContactTypeEnum.getType());
			userContactApply.setReceiveUserId(receiveUserId);
			userContactApply.setLastApplyTime(System.currentTimeMillis());
			userContactApply.setContactId(contactId);
			userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			userContactApply.setApplyInfo(applyInfo);
			userContactApplyMapper.insert(userContactApply);
		} else {
			//之前添加过，可能删除掉了好友//对方拒绝了申请
			//只需要更新状态
			UserContactApply userContactApply = new UserContactApply();
			userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			userContactApply.setLastApplyTime(System.currentTimeMillis());
			userContactApply.setApplyInfo(applyInfo);
			userContactApplyMapper.updateByApplyId(userContactApply, dbApply.getApplyId());
		}
		if (dbApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())) {
			//发送ws消息，通知对方要处理申请了
			MessageSendDto messageSendDto = new MessageSendDto();
			messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
			messageSendDto.setMessageContent(applyInfo);
			messageSendDto.setContactId(receiveUserId);
			messageHandler.sendMessage(messageSendDto);

		}
		return joinType;
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
			// 添加联系人
			userContactService.addContact(userContactApply.getApplyUserId(),userContactApply.getReceiveUserId(),userContactApply.getContactId(),userContactApply.getContactType(),userContactApply.getApplyInfo());

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
			userContact.setStatus(UserContactStatusEnum.BLACK_LIST_BE_FIRST.getStatus());
			userContact.setLastUpdateTime(new Date());
			userContactMapper.insertOrUpdate(userContact);
		}
	}


}