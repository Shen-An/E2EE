package com.easyChat.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:非法聊天信息表Mapper
 * @author:Shen-An
 * @date:2025/04/14
 */
public interface ChatMessageIllegalMapper<T, P> extends BaseMapper {
	/**
	 * 根据MessageId查询
	 */
	T selectByMessageId(@Param("messageId") Long messageId);

	/**
	 * 根据MessageId更新
	 */
	Integer updateByMessageId(@Param("bean") T t, @Param("messageId") Long messageId);

	/**
	 * 根据MessageId删除
	 */
	Integer deleteByMessageId(@Param("messageId") Long messageId);


}