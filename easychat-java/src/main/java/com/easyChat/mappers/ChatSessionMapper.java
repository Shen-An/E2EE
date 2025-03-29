package com.easyChat.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:Mapper
 * @author:Shen-An
 * @date:2025/02/07
 */
public interface ChatSessionMapper<T, P> extends BaseMapper {
	/**
	 * 根据SessionId查询
	 */
	T selectBySessionId(@Param("sessionId") String sessionId);

	/**
	 * 根据SessionId更新
	 */
	Integer updateBySessionId(@Param("bean") T t, @Param("sessionId") String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteBySessionId(@Param("sessionId") String sessionId);


}