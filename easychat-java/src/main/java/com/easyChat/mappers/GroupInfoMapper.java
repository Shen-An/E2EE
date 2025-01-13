package com.easyChat.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:群组表Mapper
 * @author:Shen-An
 * @date:2025/01/18
 */
public interface GroupInfoMapper<T, P> extends BaseMapper {
	/**
	 * 根据GroupId查询
	 */
	T selectByGroupId(@Param("groupId") String groupId);

	/**
	 * 根据GroupId更新
	 */
	Integer updateByGroupId(@Param("bean") T t, @Param("groupId") String groupId);

	/**
	 * 根据GroupId删除
	 */
	Integer deleteByGroupId(@Param("groupId") String groupId);


}