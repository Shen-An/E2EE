package com.easyChat.service;

import com.easyChat.entity.po.EcdhPks;
import com.easyChat.entity.query.EcdhPksQuery;
import com.easyChat.entity.vo.PaginationResultVo;

import java.util.List;

/**
 * @Description:Service
 * @author:Shen-An
 * @date:2025/03/15
 */
public interface EcdhPksService {

	/**
	 * 根据条件查询列表
	 */
	List<EcdhPks> findListByParam(EcdhPksQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(EcdhPksQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<EcdhPks> findListByPage(EcdhPksQuery query);

	/**
	 * 新增
	 */
	Integer add(EcdhPks bean);
	/**
	 * 批量新增
	 */
	Integer addBatch(List<EcdhPks> listBean);

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<EcdhPks> listBean);

	/**
	 * 根据Email查询
	 */
	EcdhPks getEcdhPksByEmail(String email);

	/**
	 * 根据Email更新
	 */
	Integer updateEcdhPksByEmail(EcdhPks t, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteEcdhPksByEmail(String email);



}