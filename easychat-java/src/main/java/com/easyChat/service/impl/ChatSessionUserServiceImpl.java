package com.easyChat.service.impl;

import com.easyChat.entity.po.ChatSessionUser;
import com.easyChat.entity.query.ChatSessionUserQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.PageSize;
import com.easyChat.mappers.ChatSessionUserMapper;
import com.easyChat.service.ChatSessionUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:会话用户Service
 * @author:Shen-An
 * @date:2025/02/07
 */
@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService{

	@Resource
	private ChatSessionUserMapper<ChatSessionUser,ChatSessionUserQuery> chatSessionUserMapper;

	/**
	 * 会话用户根据条件查询列表
	 */
	public List<ChatSessionUser> findListByParam(ChatSessionUserQuery query) {
		return this.chatSessionUserMapper.selectList(query);
	}

	/**
	 * 会话用户根据条件查询数量
	 */
	public Integer findCountByParam(ChatSessionUserQuery query) {
		return this.chatSessionUserMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<ChatSessionUser> findListByPage(ChatSessionUserQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize(): query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count,pageSize);
		query.setSimplePage(page);
		List<ChatSessionUser>list = this.findListByParam(query);
		PaginationResultVo<ChatSessionUser> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(ChatSessionUser bean) {
		return this.chatSessionUserMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<ChatSessionUser> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionUserMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<ChatSessionUser> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionUserMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据UserIdAndContactId查询
	 */
	public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId更新
	 */
	public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
		return this.chatSessionUserMapper.updateByUserIdAndContactId(bean,userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
	}


}