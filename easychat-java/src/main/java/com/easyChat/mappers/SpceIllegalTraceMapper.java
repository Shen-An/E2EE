package com.easyChat.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:Mapper
 * @author:Shen-An
 * @date:2025/04/06
 */
public interface SpceIllegalTraceMapper<T, P> extends BaseMapper {
	/**
	 * 根据UserId查询
	 */
	T selectByUserId(@Param("userId") String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteByUserId(@Param("userId") String userId);


}