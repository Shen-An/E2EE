package com.easyChat.service.impl;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.MessageSendDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.query.ChatMessageQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.PageSize;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.ChatMessageMapper;
import com.easyChat.redis.RedisComponent;
import com.easyChat.service.ChatMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:聊天信息表Service
 * @author:Shen-An
 * @date:2025/02/07
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService{

	@Resource
	private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;
	@Resource
	private RedisComponent redisComponent;

	/**
	 * 聊天信息表根据条件查询列表
	 */
	public List<ChatMessage> findListByParam(ChatMessageQuery query) {
		return this.chatMessageMapper.selectList(query);
	}

	/**
	 * 聊天信息表根据条件查询数量
	 */
	public Integer findCountByParam(ChatMessageQuery query) {
		return this.chatMessageMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<ChatMessage> findListByPage(ChatMessageQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize(): query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count,pageSize);
		query.setSimplePage(page);
		List<ChatMessage>list = this.findListByParam(query);
		PaginationResultVo<ChatMessage> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(ChatMessage bean) {
		return this.chatMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<ChatMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据MessageId查询
	 */
	public ChatMessage getChatMessageByMessageId(Long messageId) {
		return this.chatMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId更新
	 */
	public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
		return this.chatMessageMapper.updateByMessageId(bean,messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	public Integer deleteChatMessageByMessageId(Long messageId) {
		return this.chatMessageMapper.deleteByMessageId(messageId);
	}

	@Override
	public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
		//不是机器人回复，判断好友状态
		if(!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())){
			List<String> contactList =redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
			if(!contactList.contains(tokenUserInfoDto.getUserId())){
				UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
				if(userContactTypeEnum==UserContactTypeEnum.USER){
					throw  new BusinessException(ResponseCodeEnum.CODE_902);
				}else {
					throw  new BusinessException(ResponseCodeEnum.CODE_903);
				}
			}
		}
		return null;
	}
}