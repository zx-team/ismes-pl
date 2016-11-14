package com.isesol.mes.ismes.pl.activity;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.pl.constant.WorkOrderStatus;

/**
 * 任务批次列表
 */
public class TaskBatchPlanActivity {

	/**
	 * 任务批次列表 初始化页面
	 * @param parameters 
	 * @param bundle
	 * @return
	 */
	public String index(Parameters parameters, Bundle bundle){
		return "rwjglb";
	}
	
	
	/**
	 * 任务批次列表 外层表格
	 * @param parameters  包含  orderBy  begin   size 这几个可选字段
	 * @param bundle
	 * @return
	 */
	public String taskBatchLevelOneTable(Parameters parameters, Bundle bundle){
		// 生产任务状态：执行中
		parameters.set("scrwztdm", "20");
		parameters.set("sortOrder", "desc");
		Bundle b = Sys.callModuleService("pro", "scrwListByScrwztService", parameters);
		List<Map<String,Object>> rows = (List<Map<String, Object>>) b.get("rows");
		if(CollectionUtils.isNotEmpty(rows)){
			for(Map<String,Object> map : rows){
				map.put("scrwztdm", scrwzt(map.get("scrwztdm")));
				String ljid = map.get("ljid").toString();
				parameters.set("ljid", ljid);
				Bundle b1 = Sys.callModuleService("pm", "partsInfoService", parameters);
				if (b1 != null) {
					Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
					map.put("ljmc", partsInfoMap.get("ljmc"));
					map.put("ljbh", partsInfoMap.get("ljbh"));
				} else {
					map.put("ljmc", "找不到的零件");
				}
			}
		}
		bundle.put("rows", rows);
		bundle.put("totalPage", b.get("totalPage"));
		bundle.put("currentPage", b.get("currentPage"));
		bundle.put("totalRecord", b.get("totalRecord"));
		return "json:";
	}
	
	/**
	 * 任务批次列表  二级下拉数据
	 * @param parameters  scrwid 必传字段
	 * @param bundle
	 * @return
	 */
	public String taskBatchLevelTwoTable(Parameters parameters, Bundle bundle){
		parameters.set("pcjhztdm", "20,30,40,50,70,80,85");
		String scrwid = parameters.getString("parentRowid");
		parameters.set("scrwid", scrwid);
		Bundle b = Sys.callModuleService("pro", "scrwpcListByRwidService", parameters);
		bundle.put("rows", b.get("rows"));
		bundle.put("totalPage", b.get("totalPage"));
		bundle.put("currentPage", b.get("currentPage"));
		bundle.put("totalRecord", b.get("totalRecord"));
		return "json:";
	}
	
	
	/**
	 * 临时 --修改 批次状态
	 * @param parameters
	 * @param bundle
	 */
	public void updatePcStatus(Parameters parameters, Bundle bundle){
		Sys.callModuleService("pro", "proService_updateScrwpczt", parameters);
	}
	
	/**
	 * 批次计划 初始化页面
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String batchPlanIndex(Parameters parameters, Bundle bundle){
		return "pcjhlb";
	}
	
	
	/**
	 * 工单下发 外层表格
	 * @param parameters  包含  orderBy  begin   size 这几个可选字段
	 * @param bundle
	 * @return
	 */
	public String batchPlanLevelOneTable(Parameters parameters, Bundle bundle){
		// 生产任务状态：执行中
		parameters.set("scrwztdm", "20");
		parameters.set("sortOrder", "desc");
		Bundle b = Sys.callModuleService("pro", "scrwListByScrwztService", parameters);
		List<Map<String,Object>> rows = (List<Map<String, Object>>) b.get("rows");
		if(CollectionUtils.isNotEmpty(rows)){
			for(Map<String,Object> map : rows){
				map.put("scrwztdm", scrwzt(map.get("scrwztdm")));
				String ljid = map.get("ljid").toString();
				parameters.set("ljid", ljid);
				Bundle b1 = Sys.callModuleService("pm", "partsInfoService", parameters);
				if(b1 != null){
					Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
					map.put("ljmc", partsInfoMap.get("ljmc"));
					map.put("ljbh", partsInfoMap.get("ljbh"));
				}				
			}
		}
		bundle.put("rows", rows);
		bundle.put("totalPage", b.get("totalPage"));
		bundle.put("currentPage", b.get("currentPage"));
		bundle.put("totalRecord", b.get("totalRecord"));
		return "json:";
	}
	
	/**
	 * 批次计划列表  二级下拉数据
	 * @param parameters  scrwid 必传字段
	 * @param bundle
	 * @return
	 */
	public String batchPlanLevelTwoTable(Parameters parameters, Bundle bundle) {
		parameters.set("pcjhztdm", "40,50,70,80,85");
		String scrwid = parameters.getString("parentRowid");
		parameters.set("scrwid", scrwid);
		Bundle b = Sys.callModuleService("pro", "scrwpcListByRwidService", parameters);
		if (null==b) {
			return "json:";
		}
		String undoStatus = new StringBuffer()
				.append("'").append(WorkOrderStatus.未下发).append("'").toString();
		String doneStatus = new StringBuffer()
				.append("'").append(WorkOrderStatus.已下发).append("'").append(",")
				.append("'").append(WorkOrderStatus.加工中).append("'").append(",")
				.append("'").append(WorkOrderStatus.质检中).append("'").append(",")
				.append("'").append(WorkOrderStatus.加工完成).append("'").append(",")
				.append("'").append(WorkOrderStatus.已终止).append("'").toString();
		List<Map<String,Object>> rows = (List<Map<String, Object>>) b.get("rows");
		for (int i = 0; i < rows.size(); i++) {
			rows.get(i).put("pcjhzt", pcjhzt(rows.get(i).get("pcjhztdm")));
			Dataset dataset1 = Sys.query("pl_gdb", "gdid,gdztdm",
					"pcid = ?  and gdztdm not in ("+undoStatus+")", null ,new Object[]{rows.get(i).get("scrwpcid")});
			int undo = dataset1.getCount();
			
			Dataset dataset2 = Sys.query("pl_gdb", "gdid,gdztdm",
					"pcid = ?  ", null ,new Object[]{rows.get(i).get("scrwpcid")});
			int all = dataset2.getCount();
			rows.get(i).put("ratio", undo + "/" + all);
			
		}
		
		
		bundle.put("rows", b.get("rows"));
		bundle.put("totalPage", b.get("totalPage"));
		bundle.put("currentPage", b.get("currentPage"));
		bundle.put("totalRecord", b.get("totalRecord"));
		return "json:";
	}
	
	/**
	 * 根据生产任务状态代码获取状态名称
	 * @param dm
	 * @return
	 */
	private String scrwzt(Object dm) {
		String name = "";
		if (dm != null) {
			switch (Integer.parseInt(String.valueOf(dm))) {
			case 10:
				name = "未执行";
				break;
			case 20:
				name = "执行中";
				break;
			case 30:
				name = "已完成";
				break;
			case 40:
				name = "已终止";
				break;
			}
		}
		return name;
	}
	
	private String pcjhzt(Object dm) {
		String name = "";
		if (dm != null) {
			switch (Integer.parseInt(String.valueOf(dm))) {
			case 10:
				name = "未下发";
				break;
			case 20:
				name = "已下发";
				break;
			case 30:
				name = "计划制定中";
				break;
			case 40:
				name = "工单已生成";
				break;
			case 50:
				name = "工单已下发";
				break;
			case 70:
				name = "加工中";
				break;
			case 80:
				name = "加工完成";
				break;
			case 85:
				name = "已入库";
				break;
			case 90:
				name = "已终止";
				break;
			}
		}
		return name;
	}
}
