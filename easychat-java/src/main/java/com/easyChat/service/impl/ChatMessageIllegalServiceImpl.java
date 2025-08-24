package com.easyChat.service.impl;

import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.ChatMessageIllegal;
import com.easyChat.entity.query.ChatMessageIllegalQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.MessageTypeEnum;
import com.easyChat.enums.PageSize;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.mappers.ChatMessageIllegalMapper;
import com.easyChat.service.ChatMessageIllegalService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:非法聊天信息表Service
 * @author:Shen-An
 * @date:2025/04/14
 */
@Service("chatMessageIllegalService")
public class ChatMessageIllegalServiceImpl implements ChatMessageIllegalService {

    @Resource
    private ChatMessageIllegalMapper<ChatMessageIllegal, ChatMessageIllegalQuery> chatMessageIllegalMapper;

    /**
     * 非法聊天信息表根据条件查询列表
     */
    public List<ChatMessageIllegal> findListByParam(ChatMessageIllegalQuery query) {
        return this.chatMessageIllegalMapper.selectList(query);
    }

    /**
     * 非法聊天信息表根据条件查询数量
     */
    public Integer findCountByParam(ChatMessageIllegalQuery query) {
        return this.chatMessageIllegalMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<ChatMessageIllegal> findListByPage(ChatMessageIllegalQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<ChatMessageIllegal> list = this.findListByParam(query);
        PaginationResultVo<ChatMessageIllegal> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(ChatMessageIllegal bean, TokenUserInfoDto tokenUserInfoDto) {
        Long curTime = System.currentTimeMillis();
        String userId = tokenUserInfoDto.getUserId();

        bean.setSendTime(curTime);
        bean.setMessageType(MessageTypeEnum.CHAT.getType());
        bean.setSendUserId(userId);
        if (userId.contains("G") || bean.getContactId().contains("G")) {
            bean.setContactType(UserContactTypeEnum.GROUP.getType());
        } else {
            bean.setContactType(UserContactTypeEnum.USER.getType());
        }
        return this.chatMessageIllegalMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<ChatMessageIllegal> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageIllegalMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<ChatMessageIllegal> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageIllegalMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据MessageId查询
     */
    public ChatMessageIllegal getChatMessageIllegalByMessageId(Long messageId) {
        return this.chatMessageIllegalMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId更新
     */
    public Integer updateChatMessageIllegalByMessageId(ChatMessageIllegal bean, Long messageId) {
        return this.chatMessageIllegalMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    public Integer deleteChatMessageIllegalByMessageId(Long messageId) {
        return this.chatMessageIllegalMapper.deleteByMessageId(messageId);
    }


}