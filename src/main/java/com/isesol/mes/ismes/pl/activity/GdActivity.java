package com.isesol.mes.ismes.pl.activity;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Handler;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.pl.constant.CustomConstant;
import com.isesol.mes.ismes.pl.constant.WorkOrderStatus;
import com.isesol.mes.ismes.pl.util.FileUtil;
import com.isesol.mes.ismes.pl.util.MD5BigFileUtil;

/**
 * 工单activity
 */
public class GdActivity {
	
//	private static final String IP = "http://10.24.11.246:8444";
//	private static final String IP = "http://10.24.11.236:9080";
	private static final String IP = "http://223.223.1.20:9999";
	
	private Logger log4j = Logger.getLogger(GdActivity.class);
	private SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 工单预览页面初始化
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@SuppressWarnings("unchecked")
	public String gdPreviewIndex(Parameters parameters, Bundle bundle) throws UnsupportedEncodingException{
		
		//任务批次
		Bundle b = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
		if(b == null ||  b.get("scrwandpc") == null){
			log4j.info("根据批次id === " + parameters.getString("scrwpcid") +" 查询任务批次信息为空");
			return "gdyl";
		}
		Map<String,Object> map = (Map<String, Object>) b.get("scrwandpc");
		parameters.set("wlid", map.get("wlid"));
		Bundle wlxx = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
		
		Map<String, Object> wlxx_map = (Map<String, Object>)wlxx.get("materielInfo");
		String wlgg = "";
		if(wlxx_map != null){
			wlgg = wlxx_map.get("wlgg")+"";
		}
		map.put("wlgg", wlgg);
		bundle.put("scrwandpc", map);
		//零件信息
		parameters.set("ljid", map.get("ljid"));
		Bundle b1 = Sys.callModuleService("pm", "partsInfoService", parameters);
		Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
		if(b1 == null ||  partsInfoMap == null){
			log4j.info("根据零件id === " +map.get("ljid") +" 查询零件信息为空");
			return "gdyl";
		}
		bundle.put("partsInfo", partsInfoMap);
		return "gdyl";
	}
	
	/**
	 * 跳转到工单查询界面
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String gdcx(Parameters parameters, Bundle bundle){
		return "gdcx";
	}
	
	/**
	 * 初始化工单查询表格数据
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String table_gdcx(Parameters parameters, Bundle bundle){
		
		String query_ljbh = parameters.get("query_ljbh")==null?"" : parameters.get("query_ljbh").toString();
		String query_ljmc = parameters.get("query_ljmc")==null?"" : parameters.get("query_ljmc").toString();
		
		StringBuffer ljids = new StringBuffer();
		
		parameters.set("ljbh", query_ljbh);
		parameters.set("ljmc", query_ljmc);
		Bundle ljxx_bundle = Sys.callModuleService("pm", "queryLjxxByparam", parameters);
		
		List<Map<String, Object>> ljxx_list = (List<Map<String, Object>>)ljxx_bundle.get("ljxx");
		
		if(ljxx_list.size() != 0){
			ljids.append("(");
			for(int i = 0, len = ljxx_list.size(); i < len; i++){
				ljids.append("'" + ljxx_list.get(i).get("ljid") + "'");
				if(i != (len - 1)){
					ljids.append(",");
				}
			}
			ljids.append(")");
			parameters.set("ljids", ljids.toString());
		}
		
		if(!StringUtils.isEmpty(query_ljbh) || !StringUtils.isEmpty(query_ljmc)){
			if(ljxx_list.size() == 0){
				parameters.set("ljids", "('-1')");	
			}
		}
		
		Bundle scrw_bundle = Sys.callModuleService("pro", "proService_queryProcessingScrw", parameters);
		
		int total = Integer.parseInt(scrw_bundle.get("totalPage") + "");
		int currentPage = Integer.parseInt(scrw_bundle.get("currentPage") + "");
		int totalRecord = Integer.parseInt(scrw_bundle.get("totalRecord") + "");
		List<Map<String, Object>> rows = (List<Map<String, Object>>)scrw_bundle.get("rows");
		
		
		bundle.put("totalPage", total);
		bundle.put("currentPage", currentPage);
		bundle.put("totalRecord", totalRecord);
		
		List<Map<String, Object>> val_List = new ArrayList<Map<String, Object>>();
		
		//通过零件ID获取零件信息
		for(int i = 0, len = rows.size(); i < len; i++){
			parameters.set("ljid", rows.get(i).get("ljid"));
			Bundle scrwxx = Sys.callModuleService("pm", "partsInfoService", parameters);			
			Map<String, Object> map_temp = (Map<String, Object>)scrwxx.get("partsInfo");
			val_List.add(map_temp);
		}
		
		//整合数据
		for(int i = 0, len = rows.size(); i < len; i++){
			for(int j = 0, _len = val_List.size(); j < _len; j++){
				if(rows.get(i).get("ljid").equals(val_List.get(j).get("ljid"))){
					rows.get(i).put("ljmc", val_List.get(j).get("ljmc"));//零件名称
					rows.get(i).put("ljbh", val_List.get(j).get("ljbh"));//零件编号
				}
			}
		}
		
		bundle.put("rows", rows);
		return "json:";
	}
	
	/**
	 * 获取工单信息和在制品数量等信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String table_queryInfo(Parameters parameters, Bundle bundle){
		
		String scrwid = parameters.get("parentRowid").toString();
		
		parameters.set("scrwid", scrwid);
		
		Bundle gdxx = Sys.callModuleService("pl", "plservice_query_gdxxsByxh", parameters);
		if(gdxx == null){
			return "json:";
		}
		List<Map<String, Object>> gdxxList = (List<Map<String, Object>>)gdxx.get("gdxx");		
		bundle.put("rows", gdxxList);
		return "json:";
	}
	
	/**
	 * 通过批次id获取工单信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getGdxxByPcid(Parameters parameters, Bundle bundle){
		String pcid = parameters.getString("scrwpcid");
		if(StringUtils.isBlank(pcid)){
			log4j.info("生产任务批次id为空");
			return "json:";
		}
		parameters.set("val_pc", "( "+pcid+" )");
		Bundle gdxx = Sys.callModuleService("pl", "plservice_gdxxByPcid", parameters);
		
		List<Map<String, Object>> list = (List<Map<String, Object>>)gdxx.get("gdxx");
		
		List<Map<String, Object>> list_temp = new ArrayList<Map<String, Object>>();
		
		//去除重复箱号的数据
		for(int i = 0 , len = list.size(); i < len; i++){			
			boolean flag = true;
			for(int j = 0, _len = list_temp.size(); j < _len; j++){
				if(list.get(i).get("xh").equals(list_temp.get(j).get("xh"))){
					flag = false;
					break;
				}
			}			
			if(flag == true){
				list_temp.add(list.get(i));
			}
		}
		
		bundle.put("rows", list_temp);
		return "json:";
	}
	
	/**
	 * 下发工单,判断库存材料是否足够,如果不够,给出提示
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String xfgd(Parameters parameters, Bundle bundle){
		
		String xhs = parameters.getString("xhs");
		
		String pcid = parameters.getString("pcid");
		parameters.set("scrwpcid", pcid);
		
		Bundle scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
		
		List<Map<String, Object>> scrw_list = (List<Map<String, Object>>)scrwxx.get("scrwandpcList");
		
		String mplh = "";
		
		mplh = scrw_list.get(0).get("mplh") + "";
		
		//通过箱号获取对应的工单ID
		Dataset gdidset = Sys.query("pl_gdb", "gdid,gxid,xh,sbid,jhkssj,jhjssj,pcid,jgsl,gdbh,ljid",
				"xh in ("+xhs+") and pcid = " + pcid, null ,new Object[]{});
		
		List<Map<String, Object>> gdis_list = gdidset.getList();
		
		StringBuffer gdidsb = new StringBuffer();
		
		for(int i = 0, len = gdis_list.size(); i < len; i++) {
			gdidsb.append(gdis_list.get(i).get("gdid"));
			if(i != (len -1 )){
				gdidsb.append(",");
			}		
		}
		
		String gdids = gdidsb.toString();
		
		//获取生产任务准备清单工单物料ID,物料数量
		Dataset dataset = Sys.query("pl_scrwzbqd", "wlid,sum(zbsl) total",
				"gdid in ("+gdids+") and wllb in ('40', '50')", "wlid", null ,new Object[]{});
		
		List<Map<String,Object>> gdwlList = dataset.getList();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("(");
		for(int i = 0, len = gdwlList.size(); i < len; i++){
			sb.append(gdwlList.get(i).get("wlid"));
			if(i != (len-1)){
				sb.append(",");
			}
		}
		sb.append(")");
		parameters.set("wlids", sb.toString());
		
		parameters.set("mplh", mplh);
		
		//获取物料实际库存数量
		Bundle sjwlkcxx = Sys.callModuleService("wm", "wmService_queryWlkcByWlidandMplh", parameters);
		
		//记录物料不足的id和数量
		List<Map<String, Object>> wl_insufficient_list = new ArrayList<Map<String, Object>>();
		
		List<Map<String, Object>> sjwlkclist = null;
		
		if(sjwlkcxx != null && sjwlkcxx.get("sjwlkc") != null){
			sjwlkclist = (List<Map<String, Object>>)sjwlkcxx.get("sjwlkc");
		} else {
			sjwlkclist = gdwlList;
		}
				
		//循环实际物料库存和需要物料,判断物料是否充足
		for(int i = 0, len = gdwlList.size(); i < len; i++){
			for(int j = 0, _len = sjwlkclist.size(); j < _len; j++){
				//比较实际物料库存数量和需要的物料数量
				if(gdwlList.get(i).get("wlid").equals(sjwlkclist.get(j).get("wlid"))){
					BigDecimal wlsl = new BigDecimal(gdwlList.get(i).get("total")+"");
					BigDecimal sjwlsl = new BigDecimal(sjwlkclist.get(j).get("total")+"");
					//实际物料数量减去需要物料数量
					int temp = sjwlsl.subtract(wlsl).compareTo(BigDecimal.ZERO);
					//如果实际物料数量小于需要物料数量
					if(temp < 0){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("wlid", gdwlList.get(i).get("wlid"));
						map.put("total", wlsl.subtract(sjwlsl));
						wl_insufficient_list.add(map);
					}
					break;
				} 
				//实际物料库存表不存在该种物料
				else if(j == (_len -1)){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("wlid", gdwlList.get(i).get("wlid"));
					map.put("total", gdwlList.get(i).get("total"));
					wl_insufficient_list.add(map);
				}
			}
		}
		//物料实际库存不足
		if(wl_insufficient_list.size() > 0){
			
			List<Map<String, Object>> wlid_list = new ArrayList<Map<String, Object>>();
			//查询不足的物料信息
			for(int i = 0, len = wl_insufficient_list.size(); i <len; i++){
				Map<String, Object> wlid_map = new HashMap<String, Object>();
				wlid_map.put("wlid", wl_insufficient_list.get(i).get("wlid"));
				wlid_list.add(wlid_map);
			}
			parameters.set("wlids", wlid_list);
			//获取不足的物料信息
			Bundle wlxxs = Sys.callModuleService("mm", "materielInfoByWlidService", parameters);
			List<Map<String, Object>> wlxx_list = (List<Map<String, Object>>)wlxxs.get("materielInfoList");
			for(int i = 0, len = wlxx_list.size(); i < len; i++){
				for(int j = 0, _len = wl_insufficient_list.size(); j <_len; j++){
					if(wlxx_list.get(i).get("wlid").equals(wl_insufficient_list.get(j).get("wlid"))){
						wlxx_list.get(i).put("total", wl_insufficient_list.get(j).get("total"));
					}
				}
			}
			bundle.put("data", wlxx_list);
		} 
		//物料充足,进行下发工单操作
		else{
			Map<String, Object> param_map = new HashMap<String, Object>();
			param_map.put("gdztdm", 20);
			@SuppressWarnings("unused")
			int number = Sys.update("pl_gdb", param_map, "gdid in ("+gdids+")", new Object[]{});
			bundle.put("data", "success");
			xfgdActivity(gdis_list);
		}
		return "json:";
	}
	
	private void xfgdActivity(List<Map<String, Object>> gdList) {
		String activityType = "0"; //动态任务
		String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
		String templateId = "gdxf_tp";
		
		for (int j = 0; j < gdList.size(); j++ ) {
			Map<String, Object> data1 = new HashMap<String, Object>();
			//查询工序信息
			Parameters gxCondition = new Parameters();
			gxCondition.set("gxzid", gdList.get(j).get("gxid"));
			Bundle resultGxz = Sys.callModuleService("pm", "queryGxzxxByGxid_new", gxCondition);
			Object gxmc = ((Map<String,Object>)resultGxz.get("gxxx")).get("gxzmc");
			data1.put("gxmc", gxmc);
			data1.put("jgsl", gdList.get(j).get("jgsl"));
			data1.put("jhkssj", ((Timestamp)gdList.get(j).get("jhkssj")).getTime());//计划开始时间
			data1.put("jhjssj", ((Timestamp)gdList.get(j).get("jhjssj")).getTime());//计划结束时间
			data1.put("gdbh", gdList.get(j).get("xh"));
			String pcid = String.valueOf(gdList.get(j).get("pcid"));
			Parameters p = new Parameters();
			p.set("val_pc", "(" + pcid + ")");
			Bundle b = Sys.callModuleService("pro", "proService_pcxxbyid", p);
			Map<String, Object> pcxx = ((List<Map<String, Object>>)b.get("pcxx")).get(0);
			data1.put("pcmc", pcxx.get("pcmc")); // 生产批号
			
			// 查询加工单元信息
			Parameters p1 = new Parameters();
			p1.set("jgdyid", String.valueOf(gdList.get(j).get("sbid")));
			Bundle b1 = Sys.callModuleService("em", "emservice_jgdyById", p1);
			String jgdybh = (String)((Map<String, Object>) b1.get("data")).get("jgdybh");
			data1.put("sbbh", jgdybh);
			
			p = new Parameters();
			p.set("ljid", String.valueOf(gdList.get(j).get("ljid")));
			b1 = Sys.callModuleService("pm", "partsInfoService", p);
			Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
			String ljmc = String.valueOf(partsInfoMap.get("ljmc"));
			String ljbh = String.valueOf(partsInfoMap.get("ljbh"));
			data1.put("ljbh", ljbh);
			data1.put("ljmc", ljmc);
			data1.put("userid", Sys.getUserIdentifier());//操作人
			data1.put("username", Sys.getUserName());//操作人
			data1.put("scrwjd", 11);//TODO生产任务进度 
			data1.put("ljtp", String.valueOf(partsInfoMap.get("url")));//零件图片
			sendActivity("0", templateId, true, roles, null, null, data1);
		}
	}
	
	/**
	 * 打印工艺流程卡
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String dygylck(Parameters parameters, Bundle bundle){
		
		//箱号集合
		String xhs = parameters.getString("xhs");
		if(xhs == null){
			return "json:";
		}
		
		String[] xh_arr = xhs.split(",");
		
		//通过箱号获取工序组ID信息
		Dataset dataset = Sys.query("pl_gdb", "gxid,gdid,xh",
				"xh = ?", null ,new Object[]{xh_arr[0]});
		
		List<Map<String, Object>> gxzid_list = dataset.getList();
		
		StringBuffer gxzids = new StringBuffer();
		
		for(int i = 0, len = gxzid_list.size(); i < len; i++){
			gxzids.append(gxzid_list.get(i).get("gxid"));
			if(i != (len - 1)){
				gxzids.append(",");
			}
		}
		//工序流程集合
		List<Map<String, Object>> table_list = new ArrayList<Map<String, Object>>();
		//通过工序组ID获取工序信息
		for(int i = 0, len = gxzid_list.size(); i < len; i++){
			parameters.set("gxzid", gxzid_list.get(i).get("gxid"));
			Bundle gxxx = Sys.callModuleService("pm", "pmservice_querygxxxbygxzid", parameters);
			List<Map<String, Object>> list = (List<Map<String, Object>>)gxxx.get("gxxxlist");
			Map<String, Object> temp_map = new HashMap<String, Object>();
			String _gxxh = "";
			for(int j = 0, _len = list.size(); j < _len; j++){
				temp_map.put("gxzmc", list.get(j).get("gxzmc"));
				temp_map.put("gxxh", list.get(j).get("gxxh"));
				temp_map.put("gxmc", list.get(j).get("gxmc"));
				_gxxh += list.get(j).get("gxxh")+"";
				if( j != (_len -1)){
					_gxxh += "-";
				}
				if(_len > 1){
					temp_map.put("gxmc", list.get(j).get("gxzmc"));
				}
			}
			temp_map.put("gxxh", _gxxh);
			table_list.add(temp_map);
		}
		
		//名称
		String mc = parameters.getString("mc");
		//图号
		String th = parameters.getString("th");
		//材料
		String cl = parameters.getString("cl");
		//炉号
		String lh = parameters.getString("lh");
		//批号
		String ph = parameters.getString("ph");
		//编号
		String bh = parameters.getString("bh");
		
		InputStream is = this.getClass().getResourceAsStream("/" + "template_gylckv2.pdf");
		
		Map<String, Object> data_map = new HashMap<String, Object>();
		data_map.put("mc", mc);
		data_map.put("th", th);
		data_map.put("cl", cl);
		data_map.put("lh", lh);
		data_map.put("ph", ph);
		data_map.put("bh", bh);
		
		Map<String, Object> table = new HashMap<String, Object>();
		table.put("header", new String[] { "工序", "工序名称", "完工数", "操作者", "日期", "周转箱号"});
		
		int gbCount = table_list.size();
		String[][] tb = new String[gbCount][6];

		for (int k = 0; k < gbCount; k++) {
			// Map gbmap = gbList.get(k);
			if (table_list.get(k) != null) {
				tb[k][0] = table_list.get(k).get("gxxh") + "";
				tb[k][1] = table_list.get(k).get("gxmc") + "";
				tb[k][2] = "";
				tb[k][3] = "";
				tb[k][4] = "";
				tb[k][5] = "";
			}
		}
		try {
			File gdtmFile = SimpleCodeUtil.createBarcode(bh);
			data_map.put("bhtm", gdtmFile.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		table.put("data", tb);
//		data_map.put("table", table);
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		
		StringBuffer param_xh = new StringBuffer();
		
		//同一批次下的工艺流程卡是相同的,所以有几个箱号就打印几个相同的工艺流程卡
		for(int i = 0, len = xh_arr.length; i < len; i++){
			datas.add(data_map);
			param_xh.append("\'");
			param_xh.append(xh_arr[i]);
			param_xh.append("\'");
			if(i != (len -1)){
				param_xh.append(",");
			}
		}
		
		//根据模板生成PDF文件
		InputStream result = Sys.exportFile("pdf", is, datas);
		@SuppressWarnings("unused")
		File file = null;
		try {
			file = new File("gylck.pdf", null, result, "pdf", Long.valueOf(String.valueOf(result.available())));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			throw new NumberFormatException();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//更新打印状态,将打印状态修改
		Map<String, Object> param_map = new HashMap<String, Object>();
		param_map.put("dyzt", "20");
		@SuppressWarnings("unused")
		int number = Sys.update("pl_gdb", param_map, "xh in ("+param_xh.toString()+")", new Object[]{});
		
//		bundle.put("data", file);
		return "json:";
	}
	
	/**
	 * 根据零件编号查询零件的工序信息
	 * @param parameters 需要有ljid
	 * @param bundle
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public String workOrderPreviewLevelOneTable(Parameters parameters, Bundle bundle){
		String pcid = parameters.getString("scrwpcid");
		if(StringUtils.isBlank(pcid)){
			log4j.info("生产任务批次id为空");
			return "json:";
		}
		if(StringUtils.isBlank(parameters.getString("ljid"))){
			log4j.info("零件id为空");
			return "json:";
		}
		Bundle b = Sys.callModuleService("pm", "query_gxxx_by_ljid_service", parameters);
		List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("gxList");
		bundle.put("gxList", list);
		//批次    为每个工序 增加该批次下工单的完成比。   未派工/其余
		String undoStatus = new StringBuffer()
				.append("'").append(WorkOrderStatus.未下发).append("'").toString();
		String doneStatus = new StringBuffer()
				.append("'").append(WorkOrderStatus.已下发).append("'").append(",")
				.append("'").append(WorkOrderStatus.加工中).append("'").append(",")
				.append("'").append(WorkOrderStatus.质检中).append("'").append(",")
				.append("'").append(WorkOrderStatus.加工完成).append("'").append(",")
				.append("'").append(WorkOrderStatus.已终止).append("'").toString();
		if(CollectionUtils.isNotEmpty(list)){
			for(Map<String,Object> map : list){
				//工单状态     比例
				if(map.get("gxid") == null){
					continue;
				}
				String gxid = map.get("gxid").toString();
				if(StringUtils.isBlank(gxid)){
					continue;
				}
				Dataset dataset1 = Sys.query("pl_gdb", "gdid,gdztdm",
						"pcid = ? and gxid = ? and gdztdm not in ("+undoStatus+")", null ,new Object[]{pcid,gxid});
				int undo = dataset1.getCount();
				
//				Dataset dataset2 = Sys.query("pl_gdb", "gdid,gdztdm",
//						"pcid = ? and gxid = ? and gdztdm in ("+doneStatus+")", null ,new Object[]{pcid,gxid});
//				int done = dataset2.getCount();
//				map.put("ratio", undo + "/" + done);
				
				Dataset dataset2 = Sys.query("pl_gdb", "gdid,gdztdm",
						"pcid = ? and gxid = ? ", null ,new Object[]{pcid,gxid});
				int all = dataset2.getCount();
				map.put("ratio", undo + "/" + all);
				
				//工序说明   工步说明的集合  加序号 回车
				StringBuffer gxsm = new StringBuffer();
				parameters.set("gxid", gxid);
				Bundle b_pm = Sys.callModuleService("pm", "pmservice_gbxx", parameters);
				List<Map<String,Object>> gbList = (List<Map<String, Object>>) b_pm.get("gbxx");
				if(CollectionUtils.isNotEmpty(gbList)){
					for(Map<String,Object> m : gbList){
						gxsm = gxsm.append(m.get("gbxh").toString()).append(" ").append(m.get("jggcms")).append("</br>");
					}
					gxsm = gxsm.delete(gxsm.lastIndexOf("</br>"), gxsm.length()) ;
				}
				map.put("gxsm", gxsm);
			}
		}
		
		bundle.put("rows", list);
		return "json:";
	}
	
	/***
	 * 根据批次、工序  得到工单信息
	 * @param parameters
	 * @param bundle
	 */
	public String workOrderPreviewLevelTwoTable(Parameters parameters, Bundle bundle){
		String pcid = parameters.getString("scrwpcid");
		String gxid = parameters.getString("parentRowid");
		if(StringUtils.isBlank(pcid)){
			log4j.info("生产任务批次id为空");
			return "json:";
		}
		if(StringUtils.isBlank(gxid)){
			log4j.info("工序id为空");
			return "json:";
		}
		
		String groupBy = null;
		String orderBy = StringUtils.isBlank(parameters.getString("sortName")) 
				? " jgkssj " : parameters.getString("sortName");
		String asc =  StringUtils.isBlank(parameters.getString("sortOrder")) 
				? " asc " : parameters.getString("sortOrder");
		
		int page = parameters.get("page") == null ? 1 : parameters.getInteger("page");
		int pageSize = parameters.get("pageSize") == null ? 100 : parameters.getInteger("pageSize");
		Dataset dataset = Sys.query("pl_gdb", "gdid,gdbh,pcid,ljid,gxid,jgsl,jgkssj,jgwcsj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,jhkssj,jhjssj,gdybgsl,ncbgsl,gdywcsl",
				"pcid = ? and gxid = ? ",groupBy, orderBy + asc ,(page-1)*pageSize, 
				pageSize,new Object[]{pcid,gxid});
		List<Map<String,Object>> gdList = dataset.getList();
		
		String val_gd = "(";
		for (int i = 0; i < gdList.size(); i++) {
			if(i!=0)
			{
				val_gd = val_gd +",";
			}
			val_gd += gdList.get(i).get("gdid");
		} 
		val_gd = val_gd +")";
		
		Dataset datasetzb = Sys.query("pl_scrwzbqd", "qdid,gdid,wlid,wllb,zbsl,ydwsj,dwdm", "gdid in "+val_gd, null, new Object[]{});
		
		for (int i = 0; i < gdList.size(); i++) {
			for (int j = 0; j < datasetzb.getList().size(); j++) {
				if (gdList.get(i).get("gdid").toString().equals(datasetzb.getList().get(j).get("gdid").toString())) {
					gdList.get(i).put("ydwsj", datasetzb.getList().get(j).get("ydwsj"));
					break;
				}
			}
		} 
		bundle.put("rows", gdList);
		int totalPage = dataset.getTotal() % pageSize== 0 ? dataset.getTotal()/pageSize:
			dataset.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset);
		bundle.put("records", dataset.getCount());
		
		if(CollectionUtils.isNotEmpty(gdList)){
			//设备编号
			for(Map<String,Object> map : gdList){
				map.put("gxid", gxid);
				if(map == null || map.get("sbid") == null){
					continue;
				}
				String sbid = map.get("sbid").toString();
				parameters.set("sbid", sbid);
				Bundle b_em = Sys.callModuleService("em", "emservice_sbxxInfo", parameters);
				if(b_em == null){
					continue;
				}
				map.put("sbxx",  b_em.get("sbxx"));
			}
		}
		
		return "json:";
	}
	
	/**
	 * 基于工序下发全部工单
	 * @param parameters
	 * @param bundle
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void xfgdAll(Parameters parameters, Bundle bundle){
		String pcid = parameters.getString("pcid");
		String gxids = parameters.getString("gxids");
		if(StringUtils.isBlank(pcid)){
			log4j.info("批次id为空，不能修改");
			return;
		}
		if(StringUtils.isBlank(gxids)){
			log4j.info("工序id为空，不能修改");
			return;
		}
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		List<Object[]> conditionValues = new ArrayList<Object[]>();
		String val_gx = "(";
		int i = 0;
		for(String gxid : gxids.split(",")){
			if(StringUtils.isBlank(gxid)){
				continue;
			}
			Map<String,Object> mapDate = new HashMap<String, Object>();
			mapDate.put("gdztdm", WorkOrderStatus.已下发);
			data.add(mapDate);
			
			Object[] conditionValue = {pcid,gxid};
			conditionValues.add(conditionValue);
			if(i!=0)
			{
				val_gx = val_gx +",";
			}
			val_gx += "'" +gxid+"'";
			i++;
		}
		val_gx = val_gx +")";
		Sys.update("pl_gdb", data, " pcid = ? and gxid = ? and gdztdm = '"
				+ WorkOrderStatus.未下发 + "'", conditionValues);
		//修改批次状态
		
		for(Object[] o : conditionValues){
			Dataset dataset = Sys.query("pl_gdb", "gdid,gdbh,ljid,gxid,sbid",  " pcid = ? and gxid = ? ", null, new Object[]{o[0],o[1]});
			//获得程序  打包下发
			List<String> flagList = new ArrayList<String>();
			for(Map<String,Object> m : dataset.getList()){
				String sbid = m.get("sbid").toString();
				String gxid = m.get("gxid").toString();
				@SuppressWarnings("unused")
				String gdbh = m.get("gdbh").toString();
				if(flagList.contains(sbid + "_" + gxid)){
					continue;
				}
				xfcx(sbid, gxid,false);
				flagList.add(sbid + "_" + gxid);			
			}
		}
		
		parameters.set("scrwpcid", pcid);
		Dataset dataset = Sys.query("pl_gdb", "gdbh,jgsl,jhkssj,jhjssj,gxid,sbid", " pcid = ? and gxid in "+val_gx, null, null, new Object[]{pcid});
		List<Map<String,Object>> gdList = dataset.getList();
		
		Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
		Map<String, Object> maprw = (Map<String, Object>) b_scrwxx.get("scrwandpc");
		String activityType = "0"; //动态任务
		String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
		String templateId = "gdxf_tp";
		//查询任务完成进度
		Parameters progressCondition = new Parameters();
		progressCondition.set("scrwbh", maprw.get("scrwbh"));
		Bundle resultProgress = Sys.callModuleService("pc", "pcservice_caculateProgress", progressCondition);
		Object scrwjd = resultProgress.get("scrwjd");
		//查询零件图片
		Object ljid =  maprw.get("ljid");
		parameters.set("ljid", ljid);
		Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", parameters);
		Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
		for (int j = 0; j < gdList.size(); j++) {
			Map<String, Object> data1 = new HashMap<String, Object>();
			data1.put("scrwbh", maprw.get("scrwbh"));//生产任务编号
			data1.put("pcmc", maprw.get("pcmc"));
			data1.put("pcbh", maprw.get("pcbh"));
			data1.put("gdbh", gdList.get(j).get("gdbh"));
			data1.put("jgsl", gdList.get(j).get("jgsl"));
			data1.put("jhkssj", ((Timestamp)gdList.get(j).get("jhkssj")).getTime());//计划开始时间
			data1.put("jhjssj", ((Timestamp)gdList.get(j).get("jhjssj")).getTime());//计划结束时间
			data1.put("ljbh", parameters.get("ljbh"));
			data1.put("ljmc", parameters.get("ljmc"));
			data1.put("userid", Sys.getUserIdentifier());//操作人
			data1.put("username", Sys.getUserName());//操作人
			data1.put("scrwjd", scrwjd);//生产任务进度
			data1.put("ljtp", ljtp);
			Parameters gxCondition = new Parameters();
			gxCondition.set("gxid", gdList.get(j).get("gxid"));
			Bundle resultGx = Sys.callModuleService("pm", "queryGxxxByGxid", gxCondition);
			Object gxmc = ((Map<String,Object>)resultGx.get("gxxx")).get("gxmc");
			data1.put("gxmc", gxmc);
			Object sbbh = parameters.get("sbbh");
			Object sbid = gdList.get(j).get("sbid");
			if(sbbh == null || "".equals(sbbh)){
				parameters.set("sbid", sbid);
				Bundle b_em = Sys.callModuleService("em", "emservice_sbxxInfo", parameters);
				if(b_em != null){
					sbbh = ((Map<String,Object>)b_em.get("sbxx")).get("sbbh");
				}
			}
			data1.put("sbbh", sbbh);
			sendActivity(activityType, templateId, true, roles, null, null, data1);
		}
		
	}
	
	public String xfcx(Parameters parameters, Bundle bundle){
		String sbid = parameters.getString("sbid");
		String gxid = parameters.getString("gxid");
		boolean success = xfcx(sbid,gxid,true);
		bundle.put("success", success);
		return "json:success";
	}
	
	@SuppressWarnings("unchecked")
	private boolean xfcx(String sbid,String gxid,boolean synchronization ){
		Parameters p = new Parameters();
		p.set("sbid", sbid);
		p.set("gxid", gxid);
		p.set("zxbz", "1");
		Bundle b = Sys.callModuleService("pm", "pmservice_cxgl", p);
		List<Map<String, Object>> cxxx = (List<Map<String, Object>>) b.get("cxxx");
		List<File> fileList = new ArrayList<File>();
		if(CollectionUtils.isNotEmpty(cxxx)){
			for(Map<String, Object> m_file : cxxx){
				fileList.add((File) m_file.get("cxwj"));
			}
		}
		Map<String,Object> fileMap = FileUtil.fileToZip(fileList, "/cxgl/zip", sbid+"_"+gxid);
		
		if(MapUtils.isNotEmpty(fileMap)){
			Map<String, Object> map = new HashMap<String, Object>(); 
			map.put("wjlj", "/cxgl/zip/" + sbid+"_"+gxid + ".zip");
			map.put("wjmc", sbid+"_"+gxid+".zip");
			map.put("wjdx",((java.io.File) fileMap.get("zip_file")).length());
			map.put("wjlb", "zip");
			p.set("infoMap", map); 
			Bundle b_file = Sys.callModuleService("pm", "pmservice_insertFile", p);
			String file_id = b_file.get("wjid").toString();
			
			String md5Str = MD5BigFileUtil.md5((java.io.File) fileMap.get("zip_file"));
			
			Parameters p_em = new Parameters();
			p_em.set("sbid", sbid);
			Bundle b_sb = Sys.callModuleService("em", "emservice_sbxxInfo", p_em);
			Map<String,Object> sbinfoMap = (Map<String, Object>) b_sb.get("sbxx");
			
			Parameters p_if = new Parameters();
			//p_if.set("destination", sbinfoMap.get("sbbh"));
			p_if.set("sbbh", sbinfoMap.get("sbbh"));
			//http://10.24.11.246:8333/ismes-web/file/download/pm/nc/downloadFiles/8247.zip
			
			String url = IP + "/ismes-web/file/download/pl/gdyl/downloadFiles/" + file_id +".zip";
			log4j.info("工单下发URL:" + url);
			System.out.println("工单下发URL:" + url);
			p_if.set("orderData", "{\"content\":\"\",\"md5\":\"" + md5Str + "\","
					+ "\"downloadUrl\":\""+ url + "\",\"id\":\"\",\"msgType\":\"1\"}");
			
			if(synchronization){
				Bundle bundle = Sys.callModuleService("interf", "ifService_pushMsg", p_if);
				return (Boolean) bundle.get("success");
			}else{
				Handler handler = new Handler() {
					public void handle(Bundle bundle) {
						if(bundle.getStatus() != null ){
							log4j.error("#################-ERROR-################");
							log4j.error(bundle.getStatus() .getMessage());
						}
						@SuppressWarnings("unused")
						boolean success = (Boolean) bundle.get("success");
					}
				};
				Sys.callModuleService("interf", "ifService_pushMsg", p_if,handler);
				
				return true;
			}
		}
		return false;
	}
	
	public String getImage(Parameters parameters, Bundle bundle) throws NumberFormatException, IOException{
		InputStream is = this.getClass().getResourceAsStream("/" + "gylcksm.png");
		File file = new File("gylck.png", null, is, "png", Long.valueOf(String.valueOf(is.available())));
		bundle.put("file", file);
		return "file:file";
	}
	
	@SuppressWarnings("unchecked")
	public String downloadFiles(Parameters parameters, Bundle bundle) {
		String wjid = parameters.getString("wjid");
		String fid = parameters.getString("fid").replace(".zip", "");
		if(StringUtils.isBlank(wjid) && StringUtils.isNotBlank(fid)){
			wjid = fid;
			parameters.set("wjid", wjid);
		}
		Bundle b_sb = Sys.callModuleService("pm", "fileInfoService", parameters);
		Map<String, Object> map =(Map<String,Object>) b_sb.get("fileInfoMap");
		if (map != null) {
			File file_display = new File((String) map.get("wjmc"), null, Sys.readFile((String) map.get("wjlj")),
					(String) map.get("wjlb"), Long.valueOf(map.get("wjdx").toString()));
			bundle.put("file_display", file_display);
		} else {
			bundle.put("file_display", null);
		}
		return "file:file_display";
	}
	
	/**
	 * 下发单个工单
	 * @param parameters
	 * @param bundle
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void xfgdOne(Parameters parameters, Bundle bundle){
		String gdid = parameters.getString("gdid");
		String pcid = parameters.getString("pcid");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("gdztdm", WorkOrderStatus.已下发);
		Sys.update("pl_gdb", map, " gdid = ? ", gdid);

		
		parameters.set("scrwpcid", pcid);
		Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
		Map<String, Object> maprw = (Map<String, Object>) b_scrwxx.get("scrwandpc");
		String activityType = "0"; //动态任务
		String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
		String templateId = "gdxf_tp";
		Map<String, Object> data = new HashMap<String, Object>();
		
		//查询任务完成进度
		Parameters progressCondition = new Parameters();
		progressCondition.set("scrwbh", maprw.get("scrwbh"));
		Bundle resultProgress = Sys.callModuleService("pc", "pcservice_caculateProgress", progressCondition);
		Object scrwjd = resultProgress.get("scrwjd");
		data.put("scrwjd", scrwjd);//生产任务进度
		//查询零件图片
		Object ljid =  maprw.get("ljid");
		parameters.set("ljid", ljid);
		Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", parameters);
		Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
		data.put("ljtp", ljtp);//零件图片
		//查询工单信息
		Dataset dataset = Sys.query("pl_gdb", "gdbh,jgsl,jhkssj,jhjssj,gxid", " gdid = ? ", null, null, new Object[]{gdid});
		List<Map<String,Object>> gdList = dataset.getList();
		//查询工序信息
		Parameters gxCondition = new Parameters();
		gxCondition.set("gxid", gdList.get(0).get("gxid"));
		Bundle resultGx = Sys.callModuleService("pm", "queryGxxxByGxid", gxCondition);
		Object gxmc = ((Map<String,Object>)resultGx.get("gxxx")).get("gxmc");
		data.put("gxmc", gxmc);//工序名称
		data.put("jhkssj", ((Timestamp)gdList.get(0).get("jhkssj")).getTime());//计划开始时间
		data.put("jhjssj", ((Timestamp)gdList.get(0).get("jhjssj")).getTime());//计划结束时间
		data.put("scrwbh", maprw.get("scrwbh"));//生产任务编号
		data.put("pcmc", maprw.get("pcmc"));
		data.put("pcbh", maprw.get("pcbh"));
		data.put("gdbh", parameters.get("gdbh"));
		data.put("jgsl", parameters.get("jgsl"));
		data.put("sbbh", parameters.get("sbbh"));
		data.put("ljbh", parameters.get("ljbh"));
		data.put("ljmc", parameters.get("ljmc"));
		data.put("userid", Sys.getUserIdentifier());//操作人
		data.put("username", Sys.getUserName());//操作人
		sendActivity(activityType, templateId, true, roles, null, null, data);
	}
	
	/**查询刀具夹具量具
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String table_djzb(Parameters parameters, Bundle bundle) throws Exception {
		String gdid = parameters.getString("gdid");
		if(StringUtils.isBlank(gdid))
		{
			return "json:";
		}
		String wllb = parameters.getString("wllb");
		Dataset dataset_gdxx = Sys.query("pl_scrwzbqd", "qdid,gdid,wlid,wllb,zbsl,ydwsj,dwdm", "gdid = ? and wllb in "+wllb, null, new Object[]{Integer.parseInt(gdid)});
		List<Map<String, Object>> gdxx = dataset_gdxx.getList();
		if (gdxx.size()<=0) {
			return "json:";
		}
		//物料ID  
		String val_wl = "(";
		for (int i = 0; i < gdxx.size(); i++) {
			if(i!=0)
			{
				val_wl = val_wl +",";
			}
			val_wl += gdxx.get(i).get("wlid");
		} 
		val_wl = val_wl +")";
		parameters.set("val_wl", val_wl);
//		Dataset dataset_glxx = Sys.query("pl_sbwlglb", "wlid,count(wlid) yzbsl", "sbid = ?  ","wlid", null, new Object[]{Integer.parseInt(sbid)});
//		List<Map<String, Object>> glxx = dataset_glxx.getList();
//		for (int i = 0; i < gdxx.size(); i++) {
//			for (int j = 0; j < glxx.size(); j++) {
//				if (gdxx.get(i).get("wlid").toString().equals(glxx.get(j).get("wlid").toString())) {
//					gdxx.get(i).put("yzbsl", glxx.get(j).get("yzbsl"));
//					break;
//				}
//			}
//		}
		
		
		//查询物料信息
		Bundle b_wlxx = Sys.callModuleService("mm", "mmservice_wlxx", parameters);
		List<Map<String, Object>> wlxx = (List<Map<String, Object>>) b_wlxx.get("wlxx");
		for (int i = 0; i < gdxx.size(); i++) {
			for (int j = 0; j < wlxx.size(); j++) {
				if (gdxx.get(i).get("wlid").toString().equals(wlxx.get(j).get("wlid").toString())) {
					gdxx.get(i).put("wlmc", wlxx.get(j).get("wlmc"));
					gdxx.get(i).put("wlgg", wlxx.get(j).get("wlgg"));
					gdxx.get(i).put("wldw", wlxx.get(j).get("wllxdm"));
					break;
				}
			}
		}
		bundle.put("rows", gdxx);
		return "json:";
	}
	
	/**查询物料准备信息
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String table_wlzb(Parameters parameters, Bundle bundle) throws Exception {
		String gdid = parameters.getString("gdid");
//		String sbid = parameters.getString("sbid");
		if(StringUtils.isBlank(gdid))
		{
			return "json:";
		}
		Dataset dataset_gdxx = Sys.query("pl_scrwzbqd", "qdid,gdid,wlid,wllb,zbsl,ydwsj,dwdm", "gdid = ? and wllb in ('40','50')", null, new Object[]{Integer.parseInt(gdid)});
		List<Map<String, Object>> gdxx = dataset_gdxx.getList();
		if (gdxx.size()<=0) {
			return "json:";
		}
		//物料ID  
		String val_wl = "(";
		for (int i = 0; i < gdxx.size(); i++) {
			if(i!=0)
			{
				val_wl = val_wl +",";
			}
			val_wl += gdxx.get(i).get("wlid");
		} 
		val_wl = val_wl +")";
		parameters.set("val_wl", val_wl);
		
		//查询物料信息
		Bundle b_kcxx = Sys.callModuleService("wm", "wmService_kcxx", parameters);
		List<Map<String, Object>> kcxx = (List<Map<String, Object>>) b_kcxx.get("kcxx");

		//查询物料信息
		Bundle b_wlxx = Sys.callModuleService("mm", "mmservice_wlxx", parameters);
		List<Map<String, Object>> wlxx = (List<Map<String, Object>>) b_wlxx.get("wlxx");
		List<Map<String, Object>> wlxx1 = new ArrayList<Map<String,Object>>(); 
		for (int i = 0; i < wlxx.size(); i++) {
			wlxx.get(i).put("isexists", "0");
			for (int j = 0; j < kcxx.size(); j++) {
				if (wlxx.get(i).get("wlid").toString().equals(kcxx.get(j).get("wlid").toString())) {
					Map<String, Object> a = new HashMap<String, Object>();
					a.put("kfmc", kcxx.get(j).get("kfmc"));
					a.put("kcsl", kcxx.get(j).get("kcsl"));
					a.put("wlbh", wlxx.get(i).get("wlbh"));
					a.put("wlmc", wlxx.get(i).get("wlmc"));
					a.put("wlgg", wlxx.get(i).get("wlgg"));
					a.put("wldwdm", wlxx.get(i).get("wldwdm"));
					wlxx.get(i).put("isexists", "1");
					wlxx1.add(a);
				}
			}
		}
		for (int i = 0; i < wlxx.size(); i++) {
			if ("0".equals(wlxx.get(i).get("isexists").toString())) {
				Map<String, Object> a = new HashMap<String, Object>();
				a.put("wlbh", wlxx.get(i).get("wlbh"));
				a.put("wlmc", wlxx.get(i).get("wlmc"));
				a.put("wlgg", wlxx.get(i).get("wlgg"));
				a.put("wldwdm", wlxx.get(i).get("wldwdm"));
				wlxx1.add(a);
			}
		}
		bundle.put("rows", wlxx1);
		return "json:";
	}
	
	/**程序准备
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String table_cxzb(Parameters parameters, Bundle bundle) throws Exception {
		String gxid = parameters.getString("gxid");
		String sbid = parameters.getString("sbid");
		if(StringUtils.isBlank(gxid)&&StringUtils.isBlank(sbid))
		{
			return "json:";
		}
		Bundle b_cxxx = Sys.callModuleService("pm", "pmservice_cxxxbysbgx", parameters);
		if(null == b_cxxx)
		{
			return "json:";
		}
		List<Map<String, Object>> cxxx = (List<Map<String, Object>>) b_cxxx.get("cxxx");
		bundle.put("rows", cxxx);
		return "json:";
	}
	
	@SuppressWarnings("unchecked")
	public String table_cxzbzxbz(Parameters parameters, Bundle bundle) throws Exception {
		String gxid = parameters.getString("gxid");
		String sbid = parameters.getString("sbid");
		parameters.set("zxbz", "1"); 
		if(StringUtils.isBlank(gxid)&&StringUtils.isBlank(sbid))
		{
			return "json:";
		}
		Bundle b_cxxx = Sys.callModuleService("pm", "pmservice_cxxxbysbgx", parameters);
		if(null == b_cxxx)
		{
			return "json:";
		}
		List<Map<String, Object>> cxxx = (List<Map<String, Object>>) b_cxxx.get("cxxx");
		if(cxxx.size()>0)
		{
			bundle.put("cxxx", cxxx.get(0));
		}
		return "json:cxxx";
	}
	
	
	
	/**
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void saveAupdate_Gdb(Parameters parameters, Bundle bundle) {
		String gdid = (String) parameters.get("gdid");
		String bgsl = (String) parameters.get("bgsl");
		String bfgf = (String) parameters.get("bfgf");
		String bflf = (String) parameters.get("bflf");
		if("".equals(bgsl)){
			bgsl = "0";
		}
		if("".equals(bfgf)){
			bfgf = "0";
		}
		if("".equals(bflf)){
			bflf = "0";
		}
		Map<String, Object> map = new HashMap<String, Object>();

		Dataset dataset = Sys.query(CustomConstant.工单表, CustomConstant.工单表_工人报工数量+",bfgf,bflf",
				" gdid = ? ", null, new Object[] { gdid });
		int count = 0;
		if (null != dataset && dataset.getList().size() > 0) {
			int sl_sql = 0;
			int gf_sql = 0;
			int lf_sql = 0;
			if(null != dataset.getMap().get(CustomConstant.工单表_工人报工数量)){
				sl_sql=Integer.parseInt(dataset.getMap().get(CustomConstant.工单表_工人报工数量).toString());
				gf_sql=Integer.parseInt(dataset.getMap().get("bfgf").toString());
				lf_sql=Integer.parseInt(dataset.getMap().get("bflf").toString());
			}
			map.put(CustomConstant.工单表_工人报工数量,	Integer.parseInt(bgsl) + sl_sql);
			map.put("bfgf",	Integer.parseInt(bfgf) + gf_sql);
			map.put("bflf",	Integer.parseInt(bflf) + lf_sql);
			map.put("kgzt",	"20");
			
			map.put(CustomConstant.工单表_操作人员,Sys.getUserIdentifier());
			count = Sys.update(CustomConstant.工单表, map, " gdid = ? ",
					new Object[] { gdid });
		} 

		bundle.put("count", count);
	}
	
	
	
	/**
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("deprecation")
	public void updateGdbTime(Parameters parameters, Bundle bundle) throws Exception {
		String gdid = (String) parameters.get("gdid");
		String sbid = (String) parameters.get("sbid");//加工单元ID
		Date sj = (Date) parameters.get("sj");
		List<Map<String,Object>> xjlist = new ArrayList<Map<String,Object>>();
		Date xjkssj_start = sdf_time.parse(sdf_time.format(new Date())+" 00:00:00");
		Date xjkssj_end = sdf_time.parse(sdf_time.format(new Date())+" 23:59:59");
		Dataset dataset_xj = Sys.query( "pl_jgdyxjxx","xjxxid", "xjkssj >= ? and xjkssj <= ? and jgdyid = ? ", "xjkssj", new Object[]{xjkssj_start,xjkssj_end,sbid});

		Map<String, Object> mapxj = new HashMap<String, Object>();
		if (null == dataset_xj || dataset_xj.getList().size() <= 0) {
			Date now = new Date();
			Calendar ca=Calendar.getInstance();
			for (int i =  now.getHours(); i < 24; i=i+2) {
				mapxj = new HashMap<String, Object>();
				mapxj.put("jgdyid", sbid);
				mapxj.put("xjkssj", now);
				ca.setTime(now);
				ca.add(Calendar.HOUR_OF_DAY, 2);
				now = ca.getTime();
				mapxj.put("xjjzsj", now);
				
				mapxj.put("zjzt", "10");
				mapxj.put("xjzt", "10");
				xjlist.add(mapxj);
			}
			if (xjlist.size()>0) {
				try {
					int i = Sys.insert("pl_jgdyxjxx", xjlist);
					System.out.println("插入数量"+i);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Map<String, Object> mapzt = new HashMap<String, Object>();
		mapzt.put("kgzt", "20");//开工状态
		Sys.update(CustomConstant.工单表, mapzt, " sbid = ? and kgzt = '10' ",new Object[] { sbid });//加工单元更新成不开工状态
		
		Map<String, Object> map = new HashMap<String, Object>();

		Dataset dataset = Sys.query(CustomConstant.工单表, CustomConstant.工单表_加工开始时间,
				" gdid = ? ", null, new Object[] { gdid });
		int count = 0;
		map.put("kgzt", "10");//开工状态
		if (null != dataset && dataset.getList().size() > 0) {
			if(null != dataset.getMap().get(CustomConstant.工单表_加工开始时间)){
				map.put(CustomConstant.工单表_再加工开始时间,
						sj);
				map.put(CustomConstant.工单表_操作人员,
						Sys.getUserIdentifier());
				count = Sys.update(CustomConstant.工单表, map, " gdid = ? ",
						new Object[] { gdid });
			} else {
				map.put(CustomConstant.工单表_再加工开始时间,
						sj);
				map.put(CustomConstant.工单表_加工开始时间,
						sj);
				map.put(CustomConstant.工单表_操作人员,
						Sys.getUserIdentifier());
				count = Sys.update(CustomConstant.工单表, map, " gdid = ? ",
						new Object[] { gdid });
			}
		} 
		bundle.put("count", count);
	}
	
	
	/**
	 * 
	 * @param parameters
	 * @param bundle
	 */
	public void query_gdxxBysbbh(Parameters parameters, Bundle bundle) {
		String sbid = parameters.get("sbid").toString();
		Dataset dataset = Sys.query("pl_gdb", "gdid,gxid", " sbid=? ", null, null, new Object[] { sbid });
		bundle.put("gdxxList", dataset.getList());

	}
	public void query_gdxxBysbid(Parameters parameters, Bundle bundle) {
		String sbid = parameters.get("sbid").toString();
		Dataset dataset_gdxx= Sys.query("pl_gdb","max(zjgkssj)", "sbid = ? " , " sbid ",null, new Object[] { sbid });
		List<Map<String, Object>> gdxx = new ArrayList<Map<String,Object>>();
		if(dataset_gdxx.getList().size()>0)
		{
			Dataset dataset_gd= Sys.query("pl_gdb","gdid,pcid,ljid,gdbh,gxid,jgsl,gdybgsl,ncbgsl,jgkssj,jgwcsj,jhkssj,jhjssj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj,xh", "sbid = ?  and zjgkssj = ?" , null,  new Object[]{sbid , dataset_gdxx.getList().get(0).get("zjgkssj")});
			bundle.put("gdxxList", dataset_gd.getList());
		}else{
			bundle.put("gdxxList", gdxx);
		}
	}
	
	public void updateNcbgsl(Parameters parameters, Bundle bundle) {
		String gdid = (String) parameters.get("gdid");
		Map<String, Object> map = new HashMap<String, Object>();

		Dataset dataset = Sys.query(CustomConstant.工单表, CustomConstant.工单表_NC自动报工数量,
				" gdid = ? ", null, new Object[] { gdid });
		int count = 0;
		if (null != dataset && dataset.getList().size() > 0) {
			if(null != dataset.getMap().get(CustomConstant.工单表_NC自动报工数量)){
				map.put(CustomConstant.工单表_NC自动报工数量,
						1 + Integer.parseInt(dataset.getMap().get(CustomConstant.工单表_NC自动报工数量).toString()));
			} else {
				map.put(CustomConstant.工单表_NC自动报工数量,
						1);
			}
			count = Sys.update(CustomConstant.工单表, map, " gdid = ? ",
					new Object[] { gdid });
		} 

		bundle.put("count", count);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void update_xfgd(Parameters parameters, Bundle bundle) {
		Map<String, Object> gdzt = new HashMap<String, Object>();
		if (StringUtils.isBlank(parameters.getString("xfgdid"))) {
			bundle.setError("下发工单ID为空");
		}
		gdzt.put("gdztdm", "20");//工单状态
		try {
			int i = Sys.update("pl_gdb",gdzt,"gdid = ? ",new Object[]{parameters.get("xfgdid")});
			System.out.println("更新数量"+i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Dataset dataset = Sys.query("pl_gdb", "sbid,gxid,gdbh,jgsl,jhkssj,jhjssj,sbid", " gdid = ? ", null, new Object[]{parameters.get("xfgdid")});
		if(dataset != null && MapUtils.isNotEmpty( dataset.getMap())){
			String sbid = dataset.getMap().get("sbid").toString();
			String gxid = dataset.getMap().get("gxid").toString();
			xfcx(sbid, gxid,false);
		}
		
		String pcid = parameters.getString("pcid");
		//更新生产任务批次状态，当有工单已下发时，则更新生产任务批次状态为“部分工单已下发”,add by yangfan 2016/8/29
		Parameters parts = new Parameters();
		parts.set("val_pc", "("+pcid+")");
		Bundle pcxxBundle = Sys.callModuleService("pro", "proService_pcxxbyid", parts);
		if (null != pcxxBundle) {
			List<Map<String, Object>> pcList = (List<Map<String, Object>>) pcxxBundle.get("pcxx");
			if (null != pcList && !pcList.isEmpty()) {
				Map<String, Object> pcMap = (Map<String, Object>) pcList.get(0);
				// 如果 批次状态是“工单已生成”，则更新批次状态为：“部分工单已下发”
				if (null != pcMap && null != pcMap.get("pcjhztdm") && "40".equals(pcMap.get("pcjhztdm").toString())) {
					parts.set("scrwpcid", pcid);
					parts.set("pcjhztdm", "50");
					Sys.callModuleService("pro", "proService_updateScrwpczt", parts);
				}
			}
		}
		
		parameters.set("scrwpcid", pcid);
		List<Map<String,Object>> gdList = dataset.getList();
		
		Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
		Map<String, Object> maprw = (Map<String, Object>) b_scrwxx.get("scrwandpc");
		String activityType = "0"; //动态任务
		String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
		String templateId = "gdxf_tp";
		//查询任务完成进度
		Parameters progressCondition = new Parameters();
		progressCondition.set("scrwbh", maprw.get("scrwbh"));
		Bundle resultProgress = Sys.callModuleService("pc", "pcservice_caculateProgress", progressCondition);
		Object scrwjd = resultProgress.get("scrwjd");
		//查询零件图片
		Object ljid =  maprw.get("ljid");
		parameters.set("ljid", ljid);
		Bundle resultLjUrl = Sys.callModuleService("pm", "partsInfoService", parameters);
		Object ljtp = ((Map)resultLjUrl.get("partsInfo")).get("url");
		
		for (int j = 0; j < gdList.size(); j++) {
			Map<String, Object> data1 = new HashMap<String, Object>();
			//查询工序信息
			Parameters gxCondition = new Parameters();
			gxCondition.set("gxid", gdList.get(j).get("gxid"));
			Bundle resultGx = Sys.callModuleService("pm", "queryGxxxByGxid", gxCondition);
			Object gxmc = ((Map<String,Object>)resultGx.get("gxxx")).get("gxmc");
			data1.put("gxmc", gxmc);
			data1.put("jhkssj", ((Timestamp)gdList.get(j).get("jhkssj")).getTime());//计划开始时间
			data1.put("jhjssj", ((Timestamp)gdList.get(j).get("jhjssj")).getTime());//计划结束时间
			data1.put("scrwbh", maprw.get("scrwbh"));//生产任务编号
			data1.put("pcmc", maprw.get("pcmc"));
			data1.put("pcbh", maprw.get("pcbh"));
			data1.put("gdbh", gdList.get(j).get("gdbh"));
			data1.put("jgsl", gdList.get(j).get("jgsl"));
			Object sbbh = parameters.get("sbbh");
			Object sbid = gdList.get(j).get("sbid");
			if(sbbh == null || "".equals(sbbh)){
				parameters.set("sbid", sbid);
				Bundle b_em = Sys.callModuleService("em", "emservice_sbxxInfo", parameters);
				if(b_em != null){
					sbbh = ((Map<String,Object>)b_em.get("sbxx")).get("sbbh");
				}
			}
			data1.put("sbbh", sbbh);
			data1.put("ljbh", parameters.get("ljbh"));
			data1.put("ljmc", parameters.get("ljmc"));
			data1.put("userid", Sys.getUserIdentifier());//操作人
			data1.put("username", Sys.getUserName());//操作人
			data1.put("scrwjd", scrwjd);//生产任务进度
			data1.put("ljtp", ljtp);//零件图片
			sendActivity(activityType, templateId, true, roles, null, null, data1);
		}
	}
	
	public String pic_cjscgd(Parameters parameters, Bundle bundle) throws Exception {
		if(null == parameters.get("gdbh")||"".equals(parameters.get("gdbh").toString()))
		{
			return null;
		}
		bundle.put("file_cxxx", SimpleCodeUtil.createBarcode(parameters.get("gdbh").toString()));
		return "file:file_cxxx";
	}
	
	@SuppressWarnings("unchecked")
	public String table_cjscgd(Parameters parameters, Bundle bundle) {
		//按照工单ID查询工单信息，取工序信息
//		parameters.set("gdid", "1");
		if(null==parameters.get("gdid"))
		{
			return "json:";
		}
		Dataset dataset_gdjd = Sys.query("pl_gdb","gdid,pcid,ljid,gdbh,gxid,jgsl,gdybgsl,ncbgsl,jgkssj,jgwcsj,jhkssj,jhjssj,sbid,gdztdm,gdscsj,gdywcsl,gdwcsj", "gdid = ? ", null, 0, 10,new Object[]{parameters.get("gdid").toString()});
		List<Map<String, Object>> gdjd = dataset_gdjd.getList();
		if (gdjd.size()!=1) {
			return "json:";
		}
		parameters.set("gxid", gdjd.get(0).get("gxid")); 
		
		//根据工序ID查询工歩信息
		Bundle b_gbxx = Sys.callModuleService("pm", "pmservice_gbxx", parameters);
		if (null == b_gbxx) {
			return "json:";
		}
		List<Map<String, Object>> gbxx = (List<Map<String, Object>>) b_gbxx.get("gbxx");
		if (gbxx.size()<=0) {
			return "json:";
		}
		String val_dj = "(";
		for (int i = 0; i < gbxx.size(); i++) {
			if(i!=0)
			{
				val_dj = val_dj +",";
			}
			val_dj += gbxx.get(i).get("djid");
		} 
		val_dj = val_dj +")";
		parameters.set("val_wl", val_dj);
		
		//根据刀具ID查询物料信息
		Bundle b_wlxx = Sys.callModuleService("mm", "mmservice_wlxx", parameters);
		List<Map<String, Object>> wlxx = (List<Map<String, Object>>) b_wlxx.get("wlxx");
		for (int i = 0; i < gbxx.size(); i++) {
			for (int j = 0; j < wlxx.size(); j++) {
				if (gbxx.get(i).get("djid").toString().equals(wlxx.get(j).get("wlid").toString())) {
					gbxx.get(i).put("wlgg", wlxx.get(j).get("wlgg"));
					gbxx.get(i).put("wlmc", wlxx.get(j).get("wlmc"));
					break;
				}
			}
		}
		bundle.put("rows", gbxx);
		return "json:";
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
}
