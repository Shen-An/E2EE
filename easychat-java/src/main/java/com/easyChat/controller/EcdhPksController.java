package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.entity.po.EcdhPks;
import com.easyChat.entity.query.EcdhPksQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.EcdhPksService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:Controller
 * @author:Shen-An
 * @date:2025/03/15
 */
@RestController
@RequestMapping("/ecdhPks")
public class EcdhPksController extends ABaseController {

	@Resource
	private EcdhPksService ecdhPksService;
	@GlobalInterceptor
	@RequestMapping("loadPkDataList")
	public ResponseVo loadPkDataList(EcdhPksQuery query) {
		return getSuccessResponseVo(ecdhPksService.findListByPage(query));
	}
	/**
	 * 新增
	 */
	@GlobalInterceptor
	@RequestMapping("addPk")
	public ResponseVo addPk(EcdhPks bean) {
		this.ecdhPksService.add(bean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增
	 */
	@GlobalInterceptor
	@RequestMapping("addBatch")
	public ResponseVo addBatch(@RequestBody List<EcdhPks> listBean) {
		this.ecdhPksService.addBatch(listBean);
		return getSuccessResponseVo(null);
	}

	/**
	 * 批量新增或修改
	 */
	@GlobalInterceptor
	@RequestMapping("addOrUpdateBatch")
	public ResponseVo addOrUpdateBatch(@RequestBody List<EcdhPks> listBean) {
		this.ecdhPksService.addOrUpdateBatch(listBean);
		return getSuccessResponseVo(null);
	}






	/**
	 * 根据Email查询
	 */

	@RequestMapping("getEcdhPksByEmail")
	public ResponseVo getEcdhPksByEmail(String email) {
		return getSuccessResponseVo(this.ecdhPksService.getEcdhPksByEmail(email));
	}

	/**
	 * 根据Email更新
	 */

	@RequestMapping("updateEcdhPksByEmail")
	public ResponseVo updateEcdhPksByEmail(EcdhPks bean, String email) {
		this.ecdhPksService.updateEcdhPksByEmail(bean,email);
		return getSuccessResponseVo(null);
	}

	/**
	 * 根据Email删除
	 */

	@RequestMapping("deleteEcdhPksByEmail")
	public ResponseVo deleteEcdhPksByEmail(String email) {
		this.ecdhPksService.deleteEcdhPksByEmail(email);
		return getSuccessResponseVo(null);
	}


}