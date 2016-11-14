package com.isesol.mes.ismes.pl.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 计划的service
 */
public class PlanService {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	/**
	 * 通过批次的id 修改工单状态 
	 * @param parameters  pcid 批次id；  gdztdm 工单状态代码 
	 * @param bundle
	 */
	@SuppressWarnings("unchecked")
	public void updateGdStatusByBatchCode(Parameters parameters,Bundle bundle){
		//得到批次编号
		List<Map<String, Object>> pcbhCodeList = (List<Map<String, Object>>) parameters.get("pclist");
		if(CollectionUtils.isEmpty(pcbhCodeList)){
			return;
		}
		String updateStatusCode = parameters.getString("gdzt");
		List<Map<String, Object>> updateList = new ArrayList<Map<String,Object>>();
		List<Object[]> pcbhParamList = new ArrayList<Object[]>();
		for(Map<String, Object> pcbhCodeMap : pcbhCodeList){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("gdztdm", updateStatusCode);
			updateList.add(map);
			pcbhParamList.add(new Integer[]{(Integer) pcbhCodeMap.get("scrwpcid")});
		}
		
		Sys.update("pl_gdb", updateList, "pcid = ? ", pcbhParamList);
	}
	
	/**查询物料配送信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxx(Parameters parameters, Bundle bundle) {
		Date jhkssj_start = parameters.getDate("jhkssj_start");
		Date jhkssj_end = parameters.getDate("jhkssj_end");
		String wllb = parameters.get("wllb").toString();
		String sbid = parameters.get("val_sb").toString();
		String wlid = parameters.get("val_wl").toString();
		Dataset dataset_gdxx = Sys.query( new String[]{"pl_scrwzbqd","pl_gdb"},"pl_scrwzbqd left join pl_gdb on pl_gdb.gdid = pl_scrwzbqd.gdid","pl_gdb.gdid,ljid,sbid,wlid,zbsl,ydwsj,wllb,dwdm,pcid", "ydwsj >= ? and ydwsj <= ? and  wllb in "+wllb+" and  sbid in "+sbid+" and  wlid in "+wlid, null, new Object[]{jhkssj_start,jhkssj_end});
		bundle.put("gdxx", dataset_gdxx.getList());
		
	}
	public void query_gdxxmt(Parameters parameters, Bundle bundle) {
		String jgdyid = parameters.getString("jgdyid");
		Date jhkssj_start = parameters.getDate("jhkssj_start");
		Date jhkssj_end = parameters.getDate("jhkssj_end");
		Dataset dataset_gdxx = Sys.query( "pl_gdb","gdid,xh,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gxzxh,gxzbs,gdywcsl", "jhkssj >= ? and jhkssj <= ?   and sbid = ? ", "jhkssj", new Object[]{jhkssj_start,jhkssj_end,jgdyid});
		bundle.put("gdxx", dataset_gdxx.getList());
	}
	public void query_gdxxnow(Parameters parameters, Bundle bundle) {
		String jgdyid = parameters.getString("jgdyid");
		Dataset dataset_gdxx = Sys.query( "pl_gdb","gdid,xh,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gxzxh,gxzbs,gdywcsl", " kgzt='10' and sbid = ? ", "jhkssj", new Object[]{jgdyid});
		
		Date xjkssj_end = parameters.getDate("xjkssj_end");
		Dataset dataset_hxtz = Sys.query( "pl_hxtz","hxtzid,ljid,gxzid,jgdyid,hxsj", "hxsj >? and hxsj<?  and jgdyid = ? ", "hxsj", new Object[]{new Date(),xjkssj_end,jgdyid});
		bundle.put("gdxx", dataset_gdxx.getList());
		bundle.put("hxtz", dataset_hxtz.getList());
	}
	
	public void query_xjxx(Parameters parameters, Bundle bundle) {
		Map<String, Object> mapxj = new HashMap<String, Object>();
		String jgdyid = parameters.getString("jgdyid");
		mapxj.put("xjzt", "30");
		Sys.update("pl_jgdyxjxx",mapxj,"jgdyid = ? and xjjzsj < ?  and  xjzt ='10' ",new Object[]{jgdyid , new Date()});
		mapxj = new HashMap<String, Object>();
		mapxj.put("zjzt", "30");
		Sys.update("pl_jgdyxjxx",mapxj,"jgdyid = ? and xjjzsj < ?  and  zjzt ='10' ",new Object[]{jgdyid , new Date()});
		Date xjkssj_start = parameters.getDate("xjkssj_start");
		Date xjkssj_end = parameters.getDate("xjkssj_end");
		Dataset dataset_gdxx = Sys.query( "pl_jgdyxjxx","xjxxid,jgdyid,xjkssj,xjjzsj,xjsj,zjry,zjzt,xjry,xjzt", "xjkssj >= ? and xjkssj <= ? and jgdyid = ? ", "xjkssj", new Object[]{xjkssj_start,xjkssj_end,jgdyid});
		bundle.put("xjxx", dataset_gdxx.getList());
	}
	
	/**根据批次ID查询工单信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxByPcid(Parameters parameters, Bundle bundle) {
		String val_pc = parameters.get("val_pc").toString();
		Dataset dataset_gdxx = Sys.query("pl_gdb","gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gdywcsl,xh,dyzt", "pcid in "+ val_pc, null, new Object[]{});
		bundle.put("gdxx", dataset_gdxx.getList());
	}
	/**查询工序的完成数量
	 * @param parameters
	 * @param bundle
	 */
	public void query_gxslByPcid(Parameters parameters, Bundle bundle) {
		String val_pc = parameters.get("val_pc").toString();
		Dataset dataset_gdxx = Sys.query("pl_gdb","gxid,sum(jgsl) jgsl,sum(gdywcsl) wcsl", "pcid in "+ val_pc, "gxid",null, new Object[]{});
		bundle.put("gdxx", dataset_gdxx.getList());
	}
	
	/**根据批次ID分页查询工单进度
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_gdxxfy(Parameters parameters, Bundle bundle) {
		
		String pcid= parameters.getString("pcid"); 
		if(null == pcid)
		{
			return ;
		}
		int page = Integer.parseInt(parameters.get("page").toString()) ;
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString()) ;
		Dataset dataset_gdjd = Sys.query("pl_gdb","gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,xh", "pcid = ?", "gdbh", (page-1)*pageSize, pageSize,new Object[]{pcid});
		List<Map<String, Object>> gdjd = dataset_gdjd.getList();
		bundle.put("rows", gdjd);
		int totalPage = dataset_gdjd.getTotal()%pageSize==0?dataset_gdjd.getTotal()/pageSize:dataset_gdjd.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset_gdjd.getTotal());
	}
	
	/**查询末序已完成的工单，作为可以入库的列表
	 * @param parameters
	 * @param bundle
	 * @return
	 * @author YangFan
	 */
	public void query_ywcgdxx(Parameters parameters, Bundle bundle) {
		
		int page = Integer.parseInt(parameters.get("page").toString()) ;
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString()) ;
		//工单状态代码为50（已完成），同时增加已入库状态55 , gxzbs = 1(工序标识，1标识尾序）
		Dataset dataset_gdjd = Sys.query("pl_gdb","xh,gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gdywcsl", " gdztdm='50' and gxzbs=1 ", "gdbh", (page-1)*pageSize, pageSize,new Object[]{});
		List<Map<String, Object>> gdjd = dataset_gdjd.getList();
		bundle.put("rows", gdjd);
		int totalPage = dataset_gdjd.getTotal()%pageSize==0?dataset_gdjd.getTotal()/pageSize:dataset_gdjd.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset_gdjd.getTotal());
	}
	
	
	/**根据批次ID和工单状态分页查询工单进度
	 * @param parameters
	 * @param bundle
	 * @return
	 * @author YangFan
	 */
	public void query_gdxxByZtdm(Parameters parameters, Bundle bundle) {
		
		String pcid= parameters.getString("pcid"); 
		String gdztdm= parameters.getString("gdztdm"); 
		if(null == pcid)
		{
			return ;
		}
		StringBuffer con = new StringBuffer(" 1=1 ");
		List<Object> val = new ArrayList<Object>();
		
		if(StringUtils.isNotBlank(pcid)){
			con.append( " and pcid = ? ");
			val.add(pcid);
		}
		if(StringUtils.isNotBlank(gdztdm)){
			con.append( " and gdztdm in " + gdztdm);
		}
		
		int page = Integer.parseInt(parameters.get("page").toString()) ;
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString()) ;
		Dataset dataset_gdjd = Sys.query("pl_gdb","gdid,gdbh,xh,tssyx,bfgf,bflf,gxmc,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gdywcsl", con.toString(), "gdbh", (page-1)*pageSize, pageSize, val.toArray());
		List<Map<String, Object>> gdjd = dataset_gdjd.getList();
		bundle.put("rows", gdjd);
		int totalPage = dataset_gdjd.getTotal()%pageSize==0?dataset_gdjd.getTotal()/pageSize:dataset_gdjd.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset_gdjd.getTotal());
	}
	
	/**
	 * 查询工单信息  多个param
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxByParam(Parameters parameters, Bundle bundle){
		StringBuffer conditionSb = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValues = new ArrayList<Object>();
		//批次
		List<String> pcIdList = (List<String>) parameters.get("pcIdList");
		//批次为空的时候 是不加这个参数   还是查询不到
		String pcidNull_returnNull_str = parameters.getString("pcidNull_returnNull");
		Boolean pcidNull_returnNull = StringUtils.isBlank(pcidNull_returnNull_str) ?
				false : Boolean.valueOf(pcidNull_returnNull_str);
		if(CollectionUtils.isEmpty(pcIdList) && pcidNull_returnNull){
			bundle.put("gdxxList",new ArrayList<Map<String,Object>>());
			bundle.put("gdxx", new HashMap<String,Object>());
			return;
		}
		if(!CollectionUtils.isEmpty(pcIdList)){
			StringBuffer pc_condition = new StringBuffer("'");
			for(String pcid : pcIdList){
				pc_condition.append(pcid).append("','");
			}
			pc_condition = pc_condition.delete(pc_condition.length() - 2, pc_condition.length());
			pc_condition = pc_condition.insert(0, " pcid in  (").append(")");
			conditionSb.append(" and ").append(pc_condition);
		}
		//工单编号
		String gdbh =  parameters.getString("gdbh");
		if(StringUtils.isNotBlank(gdbh)){
			conditionSb = conditionSb.append(" and gdbh like ? ");
			conditionValues.add("%" + gdbh.trim() + "%");
		}
		//工单id
		String gdid =  parameters.getString("gdid");
		if(StringUtils.isNotBlank(gdid)){
			conditionSb = conditionSb.append(" and gdid = ? ");
			conditionValues.add(gdid);
		}
		//设备
		List<String> sbIdList = (List<String>) parameters.get("sbIdList");
		Boolean flag = parameters.getString("sbidNullReturnFlag") == null ? false 
				: Boolean.valueOf(parameters.getString("sbidNullReturnFlag"));
		
		if(flag && CollectionUtils.isEmpty(sbIdList)){
			conditionSb = conditionSb.append(" and 1 = 0 ");
		}
		if(!CollectionUtils.isEmpty(sbIdList)){
			StringBuffer sb_condition = new StringBuffer("'");
			for(String sbid : sbIdList){
				sb_condition.append(sbid).append("','");
			}
			sb_condition = sb_condition.delete(sb_condition.length() - 2, sb_condition.length());
			sb_condition = sb_condition.insert(0, " sbid in  (").append(")");
			conditionSb.append(" and ").append(sb_condition);
		}
		//时间阶段   计划开始的时间
		if(parameters.get("jhkssj_start") != null){
			Date jhkssj_start = parameters.getDate("jhkssj_start");
			conditionSb = conditionSb.append(" and jhkssj >= ? ");
			conditionValues.add(jhkssj_start);
		}
		if(parameters.get("jhkssj_end") != null){
			Date jhkssj_end = parameters.getDate("jhkssj_end");
			conditionSb = conditionSb.append(" and jhkssj < ? ");
			conditionValues.add(jhkssj_end);
		}
		//时间阶段   计划结束的时间
		if(parameters.get("jhjssj_start") != null){
			Date jhjssj_start = parameters.getDate("jhjssj_start");
			conditionSb = conditionSb.append(" and jhjssj >= ? ");
			conditionValues.add(jhjssj_start);
		}
		if(parameters.get("jhjssj_end") != null){
			Date jhjssj_end = parameters.getDate("jhjssj_end");
			conditionSb = conditionSb.append(" and jhjssj < ? ");
			conditionValues.add(jhjssj_end);
		}
		
		if(StringUtils.isNotBlank(parameters.getString("jhsj_param"))
				&& parameters.get("jhsj_param_value") != null){
			String param = parameters.getString("jhsj_param");
			List<Object> jhsj_param_value_list = (List<Object>) parameters.get("jhsj_param_value");
			conditionSb = conditionSb.append(param);
			conditionValues.addAll(jhsj_param_value_list);
		}
		
		String gdztdm_s = parameters.getString("gdztdm_s");
		if(StringUtils.isNotBlank(gdztdm_s)){
			if(!gdztdm_s.contains("(")){
				gdztdm_s = "(" + gdztdm_s + ")";
			}
			conditionSb.append(" and gdztdm in " + gdztdm_s );
		}
		
		String page_flag_str = parameters.getString("page_flag_str");
		if(StringUtils.isNotBlank(page_flag_str) && Boolean.valueOf(page_flag_str)){
			
			int page = Integer.parseInt(parameters.get("page").toString()) ;
			int pageSize = Integer.parseInt(parameters.get("pageSize").toString()) ;
			Dataset dataset_gdjd = Sys.query("pl_gdb","gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,"
					+ "gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,ncbgsl,gdybgsl,bfgf,bflf,xh,gxmc", 
					conditionSb.toString(), "gdbh", (page-1)*pageSize, pageSize, conditionValues.toArray());
			bundle.put("rows", dataset_gdjd.getList());
			int totalPage = dataset_gdjd.getTotal()%pageSize==0?dataset_gdjd.getTotal()/pageSize:dataset_gdjd.getTotal()/pageSize+1;
			bundle.put("totalPage", totalPage);
			bundle.put("currentPage", page);
			bundle.put("totalRecord", dataset_gdjd.getTotal());
			bundle.put("gdxxList", dataset_gdjd.getList());
			bundle.put("gdxx", dataset_gdjd.getMap());
		}else{
			Dataset dataset_gdxx = Sys.query("pl_gdb","gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,"
					+ "gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,ncbgsl,gdybgsl,bfgf,bflf,xh,gxmc", 
					conditionSb.toString(), "gdbh", conditionValues.toArray());
			bundle.put("gdxxList", dataset_gdxx.getList());
			bundle.put("gdxx", dataset_gdxx.getMap());
		}
		
		
	}
	/**
	 * 生产任务准备清单
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void query_sczbxxbByParam(Parameters parameters, Bundle bundle) {

		String query_gdid = parameters.getString("gdid"); // 工单ID
		String query_wllb = parameters.getString("wllb"); // 物料类别

		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if (StringUtils.isNotBlank(query_gdid)) {
			con = con + " and gdid = ? ";
			val.add(query_gdid);
		}
		if (StringUtils.isNotBlank(query_wllb)) {
			con = con + " and wllb in " + query_wllb;
		}

		Dataset dataset_sczbxx = Sys.query("pl_scrwzbqd", "qdid,gdid,wlid,wllb,zbsl,ydwsj,dwdm", con,
				null, val.toArray());
		bundle.put("sczbxx", dataset_sczbxx.getList());
	}
	/**
	 * 根据工单编号查询工单信息
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxByGdbh(Parameters parameters, Bundle bundle) {
		String gdbh = parameters.get("gdbh").toString();
		Dataset dataset_gdxx = Sys.query("pl_gdb",
				"gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gdywcsl",
				" gdbh = ? ", null, new Object[] { gdbh });
		bundle.put("gdxx", dataset_gdxx.getList());
	}
	/**根据箱号查询工单
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxByXh(Parameters parameters, Bundle bundle) {
		
		String gdbh = parameters.get("gdbh").toString();
		Dataset dataset_gdxx = Sys.query("pl_gdb",
				"gdid,xh gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gxzxh,gxzbs,gdywcsl",
				" xh = ? and sbid in " + parameters.get("val_sb").toString(), null, new Object[] { gdbh });
		List<Map<String, Object>> gd_list = dataset_gdxx.getList();
		if (gd_list.size()>0) {
			if("0".equals(gd_list.get(0).get("gxzxh")))
			{
				gd_list.get(0).put("ysdsl",gd_list.get(0).get("jgsl"));
			}else{
				Dataset dataset_gdxx1 = Sys.query("pl_gdb",
						"gdid,xh gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gxzxh,gxzbs,gdywcsl",
						" xh = ? and gxzxh = ?" , null, new Object[] { gdbh ,( Integer.parseInt(gd_list.get(0).get("gxzxh").toString())-1 )});
				List<Map<String, Object>> gd_list1 = dataset_gdxx1.getList();
				gd_list.get(0).put("ysdsl",gd_list1.get(0).get("gdywcsl"));
			}
		}
		bundle.put("gdxx", gd_list);
	}
	
	/**
	 * 根据工单ID查询工单信息
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxByGdID(Parameters parameters, Bundle bundle) {
		String gdid = parameters.getString("gdid");
		List<String> gdidList = (List<String>) parameters.get("gdidList");
		StringBuffer sb = new StringBuffer( "1 = 1 ");
		List<Object> values = new ArrayList<Object>();
		if(StringUtils.isNotBlank(gdid)){
			sb = sb.append(" and gdid = ? ");
			values.add(gdid);
		}
			
		if(!CollectionUtils.isEmpty(gdidList)){
			StringBuffer sb_gdids = new StringBuffer();
			for(String gd_id : gdidList){
				sb_gdids = sb_gdids.append("'").append(gd_id).append("',");
			}
			if(sb_gdids.toString().endsWith(",")){
				sb_gdids.deleteCharAt(sb_gdids.lastIndexOf(","));
			}
			sb = sb.append(" and gdid in (").append(sb_gdids).append(")");
		}
		Dataset dataset_gdxx = Sys.query("pl_gdb",
				"gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,tssyx,bfgf,bflf",
				sb.toString(), null,values.toArray());
		bundle.put("gdxx", dataset_gdxx.getList());
	}
	
	/**
	 * 根据工单编号查询工单信息,并且状态不是已完成和已终止的
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxByGdbhIn(Parameters parameters, Bundle bundle) {
		String gdbh = parameters.get("gdbh").toString();
		Dataset dataset_gdxx = Sys.query("pl_gdb",
				"gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gdywcsl",
				" gdbh = ? and gdztdm in ('20','30','40') ", null, new Object[] { gdbh });
		bundle.put("gdxx", dataset_gdxx.getList());
	}
	
	
	private String dateFormat(Object date) {
		String result = "";
		if (null != date && date instanceof Date) {
			result = sdf.format(date);
		}
		return result;
	}
	
	/**保存报工数量
	 * @param parameters
	 * @param bundle
	 */
	public void save_bgsl(Parameters parameters, Bundle bundle) {
		List<Map<String,Object>> bgsl_list = new ArrayList<Map<String,Object>>();
		List<Object[]>  objlist= new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(parameters.get("data_list"));  
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>(); 
			Map<String, Object> map1 = jsonarray.getJSONObject(i);
			// 报完工
			if (null != (map1.get("gdywcsl"))) {
				map.put("gdywcsl", map1.get("gdywcsl"));
				map.put("tssyx", map1.get("tssyx"));
				map.put("bfgf", map1.get("bfgf"));
				map.put("bflf", map1.get("bflf"));
			}
			// 手工报工
			if (null != (map1.get("gdybgsl"))) {
				map.put("gdybgsl", map1.get("gdybgsl"));
				Dataset datasetGd = Sys.query("pl_gdb", "gdid, gdbh, pcid, jgsl, jhkssj, jhjssj, ljid, gxid, sbid, gdztdm, xh, bfgf, bflf", "gdid = ?", null, new Object[]{map1.get("gdid")});

				Map<String, Object> data = new HashMap<String, Object>();
				Map<String, Object> gdMap = datasetGd.getMap();
				String xh = gdMap.get("xh").toString();
				data.put("gdbh", xh);// 箱号
				data.put("jgsl", gdMap.get("jgsl"));// 加工数量
				data.put("bfgf", gdMap.get("bfgf"));// 报废工废
				data.put("bflf", gdMap.get("bflf"));// 报废料废
				
				//手动报工时：如果工单状态是 “已下发” 20，则更新为 “加工中” 30
				if (null != gdMap.get("gdztdm")) {
					String gdztdm = gdMap.get("gdztdm").toString();
					if ("20".equals(gdztdm)) {
						map.put("gdztdm", "30");
					}
				}
				

				data.put("jhkssj", dateFormat(gdMap.get("jhkssj")));// 计划开始时间
				data.put("jhjssj", dateFormat(gdMap.get("jhjssj")));// 计划结束时间

				// 查询零件信息
				String ljid = gdMap.get("ljid").toString();
				Parameters parts = new Parameters();
				parts.set("ljid", ljid);
				Bundle ljBundle = Sys.callModuleService("pm", "partsInfoService", parts);
				if (ljBundle != null) {
					Map<String, Object> ljMap = (Map<String, Object>) ljBundle.get("partsInfo");
					if (ljMap != null) {
						data.put("ljbh", ljMap.get("ljbh"));// 零件编号
						data.put("ljmc", ljMap.get("ljmc"));// 零件名称
					}
				}

				// 查询批次信息（批次号）
				parts = new Parameters();
				parts.set("scrwpcid", gdMap.get("pcid"));
				Bundle pcBundle = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parts);
				Map<String, Object> pcMap = (Map<String, Object>) pcBundle.get("scrwandpc");
				if (pcMap != null) {
					data.put("pcbh", pcMap.get("pcbh"));// 批次编号
					data.put("pcmc", pcMap.get("pcmc"));// 批次名称
					data.put("scrwbh", pcMap.get("scrwbh"));// 生产任务编号
				}

				// 查询工序信息 （工序编号）
				parts = new Parameters();
				parts.set("gxid", gdMap.get("gxid"));
				Bundle gxBundle = Sys.callModuleService("pm", "queryGxzxxByGxid", parts);
				Map<String, Object> gxMap = (Map<String, Object>) gxBundle.get("gxxx");
				if (null != gxMap) {
					if (null !=  gxMap.get("gxzbh") && StringUtils.isNotBlank( gxMap.get("gxzbh").toString())) {
						data.put("gxbh", gxMap.get("gxzbh"));// 工序编号
					}
					if (null != gxMap.get("gxzmc") && StringUtils.isNotBlank(gxMap.get("gxzmc").toString())) {
						data.put("gxmc", gxMap.get("gxzmc"));// 工序名称
					}
				}
				

				
				//查询设备编号
//				parts = new Parameters();
//				parts.set("sbid", gdMap.get("sbid"));// 设备id
//				Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxxInfo", parts);// 根据设备id，查询设备编号
//				Map<String,Object> sbMap = (Map<String, Object>) sbBundle.get("sbxx");
//				if (sbMap != null && !sbMap.isEmpty()) {
//					data.put("sbbh", sbMap.get("sbbh"));// 设置程序名称
//				}
				
				// 查询加工单元信息
				Parameters p1 = new Parameters();
				p1.set("jgdyid", String.valueOf(gdMap.get("sbid")));
				Bundle b1 = Sys.callModuleService("em", "emservice_jgdyById", p1);
				String jgdybh = (String)((Map<String, Object>) b1.get("data")).get("jgdybh");
				data.put("sbbh", jgdybh);
				
				// 查询主程序名称
				parts = new Parameters();
				parts.set("sbid", gdMap.get("sbid"));// 设备id
				parts.set("gxid", gdMap.get("gxid"));// 工序id
				parts.set("zxbz", "1");// 最新标识，1代表最新
				Bundle cxBundle = Sys.callModuleService("pm", "pmservice_cxxxbysbgx", parts);
				List<Map<String, Object>> cxxx = (List<Map<String, Object>>) cxBundle.get("cxxx");
				if (cxxx != null && !cxxx.isEmpty()) {
					Map<String, Object> ccMap = cxxx.get(0);
					data.put("cxmc", ccMap.get("cxmczd"));//设置程序名称
				}
				
				//查询零件图片
				parameters.set("ljid", ljid);
				Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", parameters);
				Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
				//查询任务完成进度
				Parameters progressCondition = new Parameters();
				progressCondition.set("scrwbh", data.get("scrwbh"));
				Bundle resultProgress = Sys.callModuleService("pc", "pcservice_caculateProgress", progressCondition);
				Object scrwjd = resultProgress.get("scrwjd");
				
				String activityType = "0"; //动态任务
				String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
				String templateId = "sdbg_tp";
				Map<String, Object> data1 = new HashMap<String, Object>();
				data1.put("scrwbh", data.get("scrwbh"));//生产任务编号
				data1.put("pcmc", data.get("pcmc"));
				data1.put("pcbh", data.get("pcbh"));
				data1.put("gdbh", data.get("gdbh"));
				data1.put("jgsl", data.get("jgsl"));
				data1.put("sbbh", data.get("sbbh"));
				data1.put("ljbh", data.get("ljbh"));
				data1.put("ljmc", data.get("ljmc"));
				data1.put("gxmc", data.get("gxmc"));
				data1.put("bcbgsl", map1.get("bcbgsl"));
				data1.put("jhkssj", ((java.sql.Timestamp)gdMap.get("jhkssj")).getTime());
				data1.put("jhjssj", ((java.sql.Timestamp)gdMap.get("jhjssj")).getTime());
				data1.put("userid", Sys.getUserIdentifier());//操作人
				data1.put("username", Sys.getUserName());//操作人
				data1.put("ljtp", ljtp);//零件图片
				data1.put("scrwjd", scrwjd);//生产任务进度
				data1.put("bfgf", data.get("bfgf")); // 报废工废
				data1.put("bflf", data.get("bflf")); // 报废料废
				sendActivity(activityType, templateId, true, roles, null, null, data1);
				
				
				// send message
				String message_type = "1";// 待办事项
				String[] message_roles = new String[] { "WORKER_ROLE","MANUFACTURING_MANAGEMENT_ROLE" };
				StringBuffer content = new StringBuffer();
				StringBuffer title = new StringBuffer();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
				Date now = new Date();
				title.append(data.get("ljmc")).append("(编号为").append(data.get("ljbh")).append(") 的生产工单(编号为").append(data.get("gdbh")).append(") 进行报工");
				content.append(sdf.format(now)).append(",");
				content.append(data.get("ljmc")).append("(编号为").append(data.get("ljbh")).append(") 的生产工单(工单编号为").append(data.get("gdbh")).append("，工序为").append(data.get("gxmc")).append(") 进行报工，报工数量");
				content.append(map1.get("gdybgsl")).append(")件，请质检部门检验确认。");
				String bizType = "PRO_PCFPACTIVITY_PCXF";// 生产任务批次下发

				
				String bizId = data.get("pcbh").toString();
				String url = "/pc/zlqr/query_zlqr?_m=quality_confirm" ;
				sendMessage(message_type, title.toString(), null, content.toString(), "系统发送", bizType, bizId,
						"0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, message_roles, null, null,
						"system"/* manual:人工发送，system：系统发送，interface：外部接口 */, null/* 消息来源ID */, null, now, null, null,
						null);
			}
			objlist.add(new Object[]{map1.get("gdid")});
			bgsl_list.add(map); 
		}  
		
		try {
			int i = Sys.update("pl_gdb",bgsl_list,"gdid=?",objlist);
			System.out.println("更新数量"+i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**申请质检
	 * @param parameters
	 * @param bundle
	 */
	@SuppressWarnings({"unchecked" })
	public void save_sqzj(Parameters parameters, Bundle bundle) {
		List<Map<String,Object>> bgsl_list = new ArrayList<Map<String,Object>>();
		List<Object[]>  objlist= new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject(parameters.get("data_list"));  
		Map<String, Object> map_sqzj = new HashMap<String, Object>(); 
		map_sqzj.put("gdztdm", "40");
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>(); 
			map = jsonarray.getJSONObject(i);
			objlist.add(new Object[]{map.get("gdid")});
			bgsl_list.add(map_sqzj); 
		}  
		try {
			int i = Sys.update("pl_gdb",bgsl_list,"gdid=?",objlist);
			System.out.println("更新数量----"+i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**保存质检结果
	 * @param parameters
	 * @param bundle
	 */
	@SuppressWarnings("unchecked")
	public void save_zjjg(Parameters parameters, Bundle bundle) {
		List<Map<String,Object>> zjjg_inlist = new ArrayList<Map<String,Object>>();
		Date now = new Date();
		JSONArray jsonarray = JSONArray.fromObject(parameters.get("data_list"));  
		Object zjjgzbid = null ;
		Map<String, Object> mapxj = new HashMap<String, Object>();
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = (Map<String, Object>) JSONObject.toBean(jsonarray.getJSONObject(i), HashMap.class);
			map.put("jgry", Sys.getUserIdentifier());
			map.put("zjsj", now);
			if(i==0)
			{
				map.put("scrwid", 1);
				try {
					int n = Sys.insert("pl_zjjgzb", map);
					if("10".equals(map.get("sfzj"))&&"20".equals(map.get("zjbz")))
					{
						mapxj.put("xjsj", map.get("zjsj"));
						mapxj.put("jgry", Sys.getUserIdentifier());
						mapxj.put("zjzt", "20");
						Sys.update("pl_jgdyxjxx",mapxj,"jgdyid = ? and xjkssj < ?  and  xjjzsj > ? ",new Object[]{map.get("jgdyid"),map.get("zjsj"),map.get("zjsj")});
					}else if("20".equals(map.get("sfzj"))&&"20".equals(map.get("zjbz"))){
						mapxj.put("xjsj", map.get("zjsj"));
						mapxj.put("xjry", map.get("zjry"));
						mapxj.put("xjzt", "20");
						Sys.update("pl_jgdyxjxx",mapxj,"jgdyid = ? and xjkssj < ?  and  xjjzsj > ? ",new Object[]{map.get("jgdyid"),map.get("zjsj"),map.get("zjsj")});
					}
					zjjgzbid =  map.get("zjjgzbid");
					System.out.println("插入数量"+n);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			map.put("zjjgzbid", zjjgzbid) ;
			zjjg_inlist.add(map); 
		}  
		if (zjjg_inlist.size()>0) {
			try {
				int i = Sys.insert("pl_zjjg", zjjg_inlist);
				System.out.println("插入数量"+i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 根据工单ID更新工单信息
	 * @update 增加工单完成时间，add by yangfan 2016/8/29
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void updategdxxByGdID(Parameters parameters, Bundle bundle) {
		String gdid = parameters.get("gdid").toString();
		String gdzt = parameters.get("gdzt").toString();
		String gdywcsl = parameters.get("gdywcsl") + "";
		String tssyx = parameters.get("tssyx") + "";
		String bfgf = parameters.get("bfgf") + "";
		String bflf = parameters.get("bflf") + "";
		Date gdwcsj = (Date)parameters.get("gdwcsj");
		
		List<Object[]>  objlist= new ArrayList<Object[]>();
		List<Map<String,Object>> bgsl_list = new ArrayList<Map<String,Object>>();
		Map<String, Object> map_gdxx = new HashMap<String, Object>();
		map_gdxx.put("gdztdm", gdzt);
		if(null != gdwcsj){
			map_gdxx.put("gdwcsj", gdwcsj);
			map_gdxx.put("gdywcsl", gdywcsl);
			map_gdxx.put("tssyx", tssyx);
			map_gdxx.put("bfgf", bfgf);
			map_gdxx.put("bflf", bflf);
		}
		objlist.add(new Object[]{gdid});
		bgsl_list.add(map_gdxx);
		
		try {
			int i = Sys.update("pl_gdb",bgsl_list,"gdid=?",objlist);
			System.out.println("更新数量"+i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Bundle sendActivity(String type, String templateId, boolean isPublic, String[] roles, String[] users, String[] group,
			Map<String, Object> data) {
		String PARAMS_TYPE = "type";
		String PARAMS_TEMPLATE_ID = "template_id";
		String PARAMS_PUBLIC = "public";
		String PARAMS_ROLE = "role";
		String PARAMS_USER = "user";
		String PARAMS_GROUP = "group";
		String PARAMS_DATA = "data";
		String SERVICE_NAME = "activity";
		String METHOD_NAME = "send";
		Parameters parameters = new Parameters();
		parameters.set(PARAMS_TYPE, type);
		parameters.set(PARAMS_TEMPLATE_ID, templateId);
		if (isPublic)
			parameters.set(PARAMS_PUBLIC, "1");
		if (roles != null && roles.length > 0)
			parameters.set(PARAMS_ROLE, roles);
		if (users != null && users.length > 0)
			parameters.set(PARAMS_USER, users);
		if (group != null && group.length > 0)
			parameters.set(PARAMS_GROUP, group);
		if (data != null && !data.isEmpty())
			parameters.set(PARAMS_DATA, data);
		return Sys.callModuleService(SERVICE_NAME, METHOD_NAME, parameters);
	}
	private Bundle sendMessage(String type, String title, String abs, String content, String from, String bizType,
			String bizId, String priority, String url, String[] roles, String[] users, String[] group, String sourceType,
			String sourceId, Map<String, Object> data, Date sendTime, String[] fileUri, String[] fileNames,
			long[] filesSize) {
		String PARAMS_TYPE = "message_type";
		String PARAMS_ROLE = "receiver_role";
		String PARAMS_USER = "receiver_user";
		String PARAMS_GROUP = "receiver_group";
		String PARAMS_TITLE = "title";
		String PARAMS_ABSTRACT = "abstract";
		String PARAMS_CONTENT = "content";
		String PARAMS_FROM = "from";
		String PARAMS_DATA = "data";
		String PARAMS_PRIORITY = "priority";
		String PARAMS_SOURCE_TYPE = "source_type";
		String PARAMS_SOURCE_ID = "source_id";
		String PARAMS_URL = "url";
		String PARAMS_FILE_URI = "file_uri";
		String PARAMS_FILE_NAME = "file_name";
		String PARAMS_FILE_SIZE = "file_size";
		String PARAMS_BIZTYPE = "biz_type";
		String PARAMS_BIZID = "biz_id";
		String PARAMS_SEND_TIME = "send_time";
		String SERVICE_NAME = "message";
		String METHOD_NAME = "send";
		Parameters parameters = new Parameters();
		parameters.set(PARAMS_TITLE, title);
		parameters.set(PARAMS_ABSTRACT, abs);
		parameters.set(PARAMS_CONTENT, content);
		parameters.set(PARAMS_FROM, from);
		parameters.set(PARAMS_BIZTYPE, bizType);
		parameters.set(PARAMS_BIZID, bizId);
		parameters.set(PARAMS_TYPE, type);
		parameters.set(PARAMS_PRIORITY, priority);
		parameters.set(PARAMS_USER, users);
		parameters.set(PARAMS_GROUP, group);
		parameters.set(PARAMS_ROLE, roles);
		parameters.set(PARAMS_SOURCE_TYPE, sourceType);
		parameters.set(PARAMS_SOURCE_ID, sourceId);
		parameters.set(PARAMS_URL, url);
		parameters.set(PARAMS_FILE_URI, fileUri);
		parameters.set(PARAMS_FILE_NAME, fileNames);
		parameters.set(PARAMS_FILE_SIZE, filesSize);
		parameters.set(PARAMS_SEND_TIME, sendTime);
		parameters.set(PARAMS_DATA, data);
		return Sys.callModuleService(SERVICE_NAME, METHOD_NAME, parameters);
	}
	
	/**
	 * 通过箱号获取工单结果集，包含在制品数量等信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxsByxh(Parameters parameters, Bundle bundle) {
		
		//生产任务ID
		String scrwid = "('"+parameters.get("scrwid").toString()+"')";
		parameters.set("val_scrw", scrwid);
		
		Bundle pcxx_bundle = Sys.callModuleService("pro", "proService_pcxx", parameters);
		
		List<Map<String, Object>> pcxx_list = (List<Map<String, Object>>)pcxx_bundle.get("pcxx");
		
		StringBuffer pcids = new StringBuffer();
		
		pcids.append("(");
		
		for(int i = 0, len = pcxx_list.size(); i < len; i++){
			pcids.append("'"+pcxx_list.get(i).get("scrwpcid")+"'");
			if(i != (len - 1)){
				pcids.append(",");
			}
		}	
		pcids.append(")");
		
		Dataset dataset_gdxx = Sys.query("pl_gdb",
				"gxid,sum(jgsl) jgsl,sum(gdywcsl) gdywcsl",
				"pcid in " + pcids, "pcid,gxid", null, new Object[] {});
		
		List<Map<String, Object>> gd_list = dataset_gdxx.getList();
		
		StringBuffer gxzids = new StringBuffer();
		gxzids.append("(");
		for(int i = 0, len = gd_list.size(); i < len; i++){
			gxzids.append("\'");
			gxzids.append(gd_list.get(i).get("gxid"));
			gxzids.append("\'");
			
			if(i != (len - 1)){
				gxzids.append(",");
			}
		}
		gxzids.append(")");
		parameters.set("gxzids", gxzids.toString());
		Bundle gxz = Sys.callModuleService("pm", "pmservice_query_gxz_byid", parameters);
		
		//工序组信息
		List<Map<String, Object>> gxzxx_list = (List<Map<String, Object>>)gxz.get("gxzlist");
		
		for(int i = 0, len = gxzxx_list.size(); i < len; i++){
			
			Map<String, Object> value_map = gxzxx_list.get(i);
			Map<String, Object> temp_map = gd_list.get(i);
			
			value_map.put("jgsl", temp_map.get("jgsl"));
			value_map.put("gdywcsl", temp_map.get("gdywcsl"));
			
			//计算在制品数量,如果是第一序,为加工数量-完成数量,其它序为上一序完成数量-当前序完成数量
			if(i == 0){
				
				int _jgsl = Integer.parseInt(temp_map.get("jgsl") + "");
				int _wcsl = Integer.parseInt(temp_map.get("gdywcsl") + "");
				int zzpsl = _jgsl - _wcsl;
				value_map.put("zzpsl", zzpsl);
			} else {
				int _jgsl = Integer.parseInt(gd_list.get(i-1).get("gdywcsl") + "");
				int _wcsl = Integer.parseInt(temp_map.get("gdywcsl") + "");
				int zzpsl = _jgsl - _wcsl;
				value_map.put("zzpsl", zzpsl);
			}
		}
		
		bundle.put("gdxx", gxzxx_list);
	}
	
	public void query_xhxxByPcid(Parameters parameters, Bundle bundle){
		String val_pc = parameters.getString("val_pc"); // 批次ID集合
		
		Dataset xhxx_set = Sys.query("pl_gdb", "xh,max(jgsl) jgsl,max(jgkssj) jgkssj,"
				+ "max(jgwcsj) jgwcsj", "pcid in " + val_pc, "xh", "xh", new Object[]{});
		
		bundle.put("xhxx", xhxx_set.getList());
	}
	
	/**按照箱号计算约当产量，加工进度
	 * @param parameters
	 * 批次ID集合  val_pc  箱号xh 
	 * @param bundle
	 */
	public void query_gdjgjd(Parameters parameters, Bundle bundle) {
		String val_pc = parameters.getString("val_pc"); // 批次ID集合
		String xh = parameters.getString("xh"); // 箱号
		String con = "1 = 1  and gxzbs = '1' ";
		List<Object> val = new ArrayList<Object>();
		if (StringUtils.isNotBlank(val_pc)) {
			con = con + " and pcid in " + val_pc;
		}
		if (StringUtils.isNotBlank(xh)) {
			con = con + " and xh = ? ";
			val.add(xh);
		}
		Dataset dataset_gdxx = Sys.query("pl_gdb",	"gdid,xh,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gxzxh,gxzbs,gdywcsl",
				con,  " xh", val.toArray());
		Dataset dataset_pcgdxx = Sys.query("pl_gdb","pcid,sum(gdywcsl) gdywcsl",	 con, "pcid", null, val.toArray());
		bundle.put("gdxx", dataset_gdxx.getList());
		bundle.put("pcgdxx", dataset_pcgdxx.getList());
	}
	/**查询质检结果主表信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_zjjgzb(Parameters parameters, Bundle bundle) {

		String xh = parameters.get("xh").toString();
		String zjbz = parameters.get("zjbz").toString();
		String zjjgbzw = parameters.get("zjjgbzw").toString();
		String condition = "1=1";
		Object param = new Object[]{};
		if (null != xh && xh.length()>0){
			condition = condition + " and xh like '%"+xh+"%'";
		}
		if (null != zjbz && zjbz.length()>0){
			condition = condition + " and zjbz='"+zjbz+"'";
		}
		if (null != zjjgbzw && zjjgbzw.length()>0){
			condition = condition + " and zjjg='"+zjjgbzw+"'";
		}
		
		
		Dataset dataset_zjjgzb = Sys.query("pl_zjjgzb","zjjgzbid,ljid,gxzid,jgdyid,scrwid,gdid,xh,jgry,zjry,zjsj,zjbz,sfzj", condition, null, new Object[]{});
																		  
		bundle.put("zjjgzb", dataset_zjjgzb.getList());
		
	}
	
	/**查询质检结果明细信息表，查询条件 ：质检项ID集合，质检结果主表ID
	 * @param parameters
	 * @param bundle
	 */
	public void query_zjjgdetail(Parameters parameters, Bundle bundle) {
		String zjxidset = parameters.get("zjxidset").toString();
		int zjjgzbid = parameters.getInteger("zjjgzbid");
		Dataset dataset_zjjg = Sys.query("pl_zjjg","zjjgid,zjjgzbid,gxzjxlrid,zjsj,zjjg", "zjjgzbid=? and gxzjxlrid in ("+zjxidset+")", null, new Object[]{zjjgzbid});
																		  
		bundle.put("zjjgdetail", dataset_zjjg.getList());
		
	}
	
	public void getEquivalentYieldByScrw(Parameters parameters, Bundle bundle){
		
		Map<String, Object> map = getScrwEquivalentYield(parameters);

		bundle.put("map", map);
	}
	
	public void getEquivalentYieldByPc(Parameters parameters, Bundle bundle){
		
		Map<String, Object> map = getPcEquivalentYield(parameters);

		bundle.put("map", map);
	}
	
	public void getEquivalentYieldByXh(Parameters parameters, Bundle bundle){
		
		List<Map<String, Object>> list = getXhEquivalentYield(parameters);

		bundle.put("list", list);
	}
	
	/**
	 * 获取参数生产任务的约当产量
	 */
	public Map<String, Object> getScrwEquivalentYield(Parameters parameters){
		
		String scrwid = parameters.getString("scrwid") + "";
		parameters.set("scrwid", scrwid);
		
		Bundle bundle = Sys.callModuleService("pro", "proService_query_scrwandpic", parameters);
		
		List<Map<String, Object>> scxx_list = (List<Map<String, Object>>)bundle.get("scxx_list");
		
		int jgsl = Integer.parseInt(scxx_list.get(0).get("jgsl") + "");
		
		double scrwydclsl = 0.0;
		int wcsl = 0;
		for(int i = 0, len = scxx_list.size(); i < len; i++){
			parameters.set("pcid", scxx_list.get(i).get("scrwpcid"));
			Map<String, Object> map = getPcEquivalentYield(parameters);
			
			scrwydclsl += new BigDecimal(map.get("pc_ydclsl") + "")
					.setScale(2, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			wcsl += Integer.parseInt(map.get("wcsl") + "");
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("wcsl", wcsl);
		map.put("scrw_ydclsl", scrwydclsl);
		map.put("scrw_ydcljd", new BigDecimal(scrwydclsl)
				.divide(new BigDecimal(jgsl), 2, BigDecimal.ROUND_HALF_UP));
		
		return map;
	}
	
	/**
	 * 获取参数批次ID的约当产量
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public Map<String, Object> getPcEquivalentYield(Parameters parameters){
		String pcid = parameters.getString("pcid") + "";
		
		parameters.set("scrwpcid", pcid);
		
		Bundle pcxx_bundle = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
		
		Map<String, Object> pcxx_map = (Map<String, Object>)pcxx_bundle.get("scrwandpc");
		
		int pcsl = Integer.parseInt(pcxx_map.get("pcsl") +"");
		
		Dataset gdxx_set = Sys.query("pl_gdb", "gdid,ljid,xh", 
				"pcid = ?", "gxzxh", new Object[]{pcid});
		List<Map<String, Object>> gdxx_list = gdxx_set.getList();
		
		double pcydclsl = 0.0;
		int wcsl = 0;
		
		StringBuffer xhs = new StringBuffer();
		
		//获取各个箱的约当产量数量
		for(int i = 0, len = gdxx_list.size(); i < len; i++){
			xhs.append(gdxx_list.get(i).get("xh") + ",");
		}
		
		String _str = xhs.substring(0, xhs.length() - 1);
		parameters.set("xhs", _str);
		
		List<Map<String, Object>> xhydcl_list = getXhEquivalentYield(parameters);
		
		for(int i = 0, len = xhydcl_list.size(); i < len; i++){
			pcydclsl += new BigDecimal(xhydcl_list.get(i).get("xh_ydcl") + "")
					.setScale(2, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			wcsl += Integer.parseInt(xhydcl_list.get(i).get("wcsl") + "");
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("wcsl", wcsl);
		map.put("pc_xhydclxx", xhydcl_list);
		map.put("pc_ydclsl", pcydclsl);
		map.put("pc_ydcljd", new BigDecimal(pcydclsl)
				.divide(new BigDecimal(pcsl), 2, BigDecimal.ROUND_HALF_UP));
		
		return map;
	}
	
	/**
	 * 获取参数箱号的约当产量(多个)
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public List<Map<String, Object>> getXhEquivalentYield(Parameters parameters){
		
		//箱号参数需用逗号分隔,不包含单引号,两边括号
		String xhs = parameters.getString("xhs");
		
		String[] xh_arr = xhs.split(",");
		
		Set<String> arr_set = new HashSet<String>();
		
		for(int i = 0, len = xh_arr.length; i < len; i++){
			arr_set.add(xh_arr[i]);
		}
		
		Object[] obj_arr = arr_set.toArray();
		
		StringBuffer _xhs = new StringBuffer();
		
		for(int i = 0, len = obj_arr.length; i < len; i++){
			_xhs.append("'" + obj_arr[i] + "',");
		}
		String _str = _xhs.substring(0, _xhs.length() - 1);
//		String pcid = parameters.getString("pcid");
		
		Dataset gdxx_set = Sys.query("pl_gdb", "gdid,gxzbs,jgsl,ljid,jgjp,gdywcsl,xh", 
				"xh in (" + _str + ")", "xh,gxzxh", new Object[]{});
		
		List<Map<String, Object>> gdxx_list = gdxx_set.getList();
		
		List<Map<String, Object>> ydclxx_list = new ArrayList<Map<String, Object>>();
		
		for(int i = 0, len = obj_arr.length; i < len; i++){
			List<Map<String, Object>> temp_list = new ArrayList<Map<String, Object>>();
			for(int j = 0, _len = gdxx_list.size(); j < _len; j++){
				if(obj_arr[i].equals(gdxx_list.get(j).get("xh") + "")){
					temp_list.add(gdxx_list.get(j));
				}
			}
			Map<String, Object> value_map = getSingleXhEquivalentYield(temp_list);
			value_map.put("xh", xh_arr[i]);
			ydclxx_list.add(value_map);
		}
		
		return ydclxx_list;
	}
	
	/**
	 * 计算单个箱号的约当产量和进度
	 * @return
	 */
	public Map<String, Object> getSingleXhEquivalentYield(List<Map<String, Object>> gdxx_list){
		
		List<Map<String, Object>> param_list = new ArrayList<Map<String, Object>>();
		
		if(gdxx_list.size() == 0){
			return new HashMap<String, Object>();
		}
		//取其中一个工单的加工数量作为当前箱的加工数量
		int jgsl = Integer.parseInt(gdxx_list.get(0).get("jgsl") + "");
		
		//加工总时间
		int jgzsj = 0;
		//记录末序完成数量作为完成数量
		int ywcsl = 0;
		for(int i = 0, len = gdxx_list.size(); i < len; i++){
			Map<String, Object> temp_map = gdxx_list.get(i);
			Map<String, Object> value_map = new HashMap<String, Object>();
			
			temp_map.put("jgsl", temp_map.get("jgsl"));
			temp_map.put("gdywcsl", temp_map.get("gdywcsl"));
			jgzsj += Integer.parseInt(temp_map.get("jgjp")+"");
			
			//计算完工程度
			Double wgcd = new BigDecimal(temp_map.get("gdywcsl") + "").
					divide(new BigDecimal(temp_map.get("jgsl") + ""), 
							2, BigDecimal.ROUND_HALF_UP).doubleValue();
			value_map.put("wgcd", wgcd);
			
			//获取约当产量数据
			if("-1".equals(temp_map.get("gxzbs") + "")){
				int _jgsl = Integer.parseInt(temp_map.get("jgsl") + "");
				int _wcsl = Integer.parseInt(temp_map.get("gdywcsl") + "");
				//计算在制品数量,如果是第一序,加工数量-完成数量,其它序为上一序完成数量-当前序完成数量
				int zzpsl = Math.abs(_jgsl - _wcsl);
				value_map.put("zzpsl", zzpsl);
				value_map.put("jgsj", temp_map.get("jgjp"));
			} 
			//末序在制品数量为上序完工数
			else if("1".equals(temp_map.get("gxzbs") + "")){
				int _jgsl = 0;
				if(gdxx_list.size() > 1){
					_jgsl = Integer.parseInt(gdxx_list.get(i-1).get("gdywcsl") + "");
				} else {
					_jgsl = Integer.parseInt(gdxx_list.get(i).get("jgsl") + "");
				}
				ywcsl += Integer.parseInt(gdxx_list.get(i).get("gdywcsl") + "");
				value_map.put("zzpsl", _jgsl);
				value_map.put("jgsj", temp_map.get("jgjp"));
			} else {
				int _jgsl = 0;
				if(gdxx_list.size() > 1){
					_jgsl = Integer.parseInt(gdxx_list.get(i-1).get("gdywcsl") + "");
				} else {
					_jgsl = Integer.parseInt(gdxx_list.get(i).get("jgsl") + "");
				}
				int _wcsl = Integer.parseInt(temp_map.get("gdywcsl") + "");
				int zzpsl = _jgsl - _wcsl;
				value_map.put("zzpsl", zzpsl);
				value_map.put("jgsj", temp_map.get("jgjp"));
			}
			param_list.add(value_map);
		}
		double gd_ydcl = 0.0;
		//通过完工程度,加工时间,加工总时间获取当前序完工率 
		//完工率 = (当前序加工时间*当前序完工程度  + 所有上序加工时间之和)/加工总时间
		for(int i = 0, len = param_list.size(); i < len; i++){	
			int jgzsj_temp = 0;
			//获取上序加工时间之和
			for(int j = 0; j < i; j++){
				jgzsj_temp += Integer.parseInt(param_list.get(j).get("jgsj")+"");
			}
			BigDecimal jgsj = new BigDecimal(param_list.get(i).get("jgsj")+"");
			BigDecimal wgcd = new BigDecimal(param_list.get(i).get("wgcd")+"");
			
			//通过公式获取完工率
			BigDecimal wgl = (jgsj.multiply(wgcd).add(new BigDecimal(jgzsj_temp)))
				.divide(new BigDecimal(jgzsj), 3, BigDecimal.ROUND_HALF_UP);
			
			//当前序约当产量
			double ydcl = wgl.multiply(new BigDecimal(param_list.get(i).get("zzpsl") + ""))
					.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			gd_ydcl += ydcl;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("wcsl", ywcsl);
		map.put("xh_ydcl", gd_ydcl);
		map.put("xh_ydcljd", new BigDecimal(gd_ydcl)
				.divide(new BigDecimal(jgsl), 2, BigDecimal.ROUND_HALF_UP));
		
		return map;
	}
}
