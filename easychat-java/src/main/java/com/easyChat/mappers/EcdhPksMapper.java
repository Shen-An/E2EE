package com.easyChat.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:Mapper
 * @author:Shen-An
 * @date:2025/03/15
 */
public interface EcdhPksMapper<T, P> extends BaseMapper {
	/**
	 * 根据Email查询
	 */
	T selectByEmail(@Param("email") String email);

	/**
	 * 根据Email更新
	 */
	Integer updateByEmail(@Param("bean") T t, @Param("email") String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteByEmail(@Param("email") String email);




}