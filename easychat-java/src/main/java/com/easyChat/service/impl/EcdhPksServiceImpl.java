package com.easyChat.service.impl;

import com.easyChat.entity.po.EcdhPks;
import com.easyChat.entity.query.EcdhPksQuery;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.enums.PageSize;
import com.easyChat.mappers.EcdhPksMapper;
import com.easyChat.service.EcdhPksService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:Service
 * @author:Shen-An
 * @date:2025/03/15
 */
@Service("ecdhPksService")
public class EcdhPksServiceImpl implements EcdhPksService{

	@Resource
	private EcdhPksMapper<EcdhPks,EcdhPksQuery> ecdhPksMapper;

	/**
	 * 根据条件查询列表
	 */
	public List<EcdhPks> findListByParam(EcdhPksQuery query) {
		return this.ecdhPksMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(EcdhPksQuery query) {
		return this.ecdhPksMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<EcdhPks> findListByPage(EcdhPksQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize(): query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count,pageSize);
		query.setSimplePage(page);
		List<EcdhPks>list = this.findListByParam(query);
		PaginationResultVo<EcdhPks> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(),page.getPageTotal(),list);
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(EcdhPks bean) {
		return this.ecdhPksMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<EcdhPks> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.ecdhPksMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<EcdhPks> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.ecdhPksMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据Email查询
	 */
	public EcdhPks getEcdhPksByEmail(String email) {
		return this.ecdhPksMapper.selectByEmail(email);
	}

	/**
	 * 根据Email更新
	 */
	public Integer updateEcdhPksByEmail(EcdhPks bean, String email) {
		return this.ecdhPksMapper.updateByEmail(bean,email);
	}

	/**
	 * 根据Email删除
	 */
	public Integer deleteEcdhPksByEmail(String email) {
		return this.ecdhPksMapper.deleteByEmail(email);
	}

}