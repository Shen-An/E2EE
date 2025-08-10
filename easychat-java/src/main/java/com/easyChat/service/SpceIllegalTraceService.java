package com.easyChat.service;

import com.easyChat.entity.po.SpceIllegalTrace;
import com.easyChat.entity.query.SpceIllegalTraceQuery;
import com.easyChat.entity.vo.PaginationResultVo;

import java.util.List;

/**
 * @Description:Service
 * @author:Shen-An
 * @date:2025/04/06
 */
public interface SpceIllegalTraceService {

	/**
	 * 根据条件查询列表
	 */
	List<SpceIllegalTrace> findListByParam(SpceIllegalTraceQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(SpceIllegalTraceQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<SpceIllegalTrace> findListByPage(SpceIllegalTraceQuery query);

	/**
	 * 新增
	 */
	Integer add(SpceIllegalTrace bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<SpceIllegalTrace> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<SpceIllegalTrace> listBean);

	/**
	 * 根据UserId查询
	 */
	SpceIllegalTrace getSpceIllegalTraceByUserId(String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateSpceIllegalTraceByUserId(SpceIllegalTrace t, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteSpceIllegalTraceByUserId(String userId);

}