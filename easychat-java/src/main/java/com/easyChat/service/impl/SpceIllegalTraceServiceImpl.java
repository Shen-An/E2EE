package com.easyChat.service.impl;

import com.easyChat.entity.po.SpceIllegalTrace;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.query.SpceIllegalTraceQuery;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.PageSize;
import com.easyChat.mappers.SpceIllegalTraceMapper;
import com.easyChat.service.SpceIllegalTraceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:Service
 * @author:Shen-An
 * @date:2025/04/06
 */
@Service("spceIllegalTraceService")
public class SpceIllegalTraceServiceImpl implements SpceIllegalTraceService{

	@Resource
	private SpceIllegalTraceMapper<SpceIllegalTrace,SpceIllegalTraceQuery> spceIllegalTraceMapper;

	/**
	 * 根据条件查询列表
	 */
	public List<SpceIllegalTrace> findListByParam(SpceIllegalTraceQuery query) {
		return this.spceIllegalTraceMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(SpceIllegalTraceQuery query) {
		return this.spceIllegalTraceMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<SpceIllegalTrace> findListByPage(SpceIllegalTraceQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize(): query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count,pageSize);
		query.setSimplePage(page);
		List<SpceIllegalTrace>list = this.findListByParam(query);
		PaginationResultVo<SpceIllegalTrace> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(SpceIllegalTrace bean) {
		return this.spceIllegalTraceMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<SpceIllegalTrace> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.spceIllegalTraceMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<SpceIllegalTrace> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.spceIllegalTraceMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据UserId查询
	 */
	public SpceIllegalTrace getSpceIllegalTraceByUserId(String userId) {
		return this.spceIllegalTraceMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId更新
	 */
	public Integer updateSpceIllegalTraceByUserId(SpceIllegalTrace bean, String userId) {
		return this.spceIllegalTraceMapper.updateByUserId(bean,userId);
	}

	/**
	 * 根据UserId删除
	 */
	public Integer deleteSpceIllegalTraceByUserId(String userId) {
		return this.spceIllegalTraceMapper.deleteByUserId(userId);
	}



}