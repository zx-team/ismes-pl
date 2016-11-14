package com.isesol.mes.ismes.pl.activity;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;

import net.sf.json.JSONArray;

/**
 * 工单批量打印
 * 
 * @author Yang Fan
 *
 */
public class BatchPrintActivity {
	
	//时间格式化
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	//
	private String templateName = "template_gd.pdf";
	private String fileName = "WorkOrder.pdf";
	private String fileType = "pdf";
	
	/**
	 * 显示列表页面
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String showGdlist(Parameters parameters, Bundle bundle) {
		return "pl_gdlb";
	}

	/**
	 * 工单列表数据查询
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String table_gdlb(Parameters parameters, Bundle bundle) {
		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());

		String scrwid = parameters.getString("query_scrwbh");// 生产任务编码 left join sc_scrw on scrwid scrwbh 查询条件修改为scrwid
		String pcid = parameters.getString("query_pcbh");	// 任务批次编码 left join sc_scrwpc  on scrwpcid pcbh 查询条件修改为pcid
		String gdbh = parameters.getString("query_gdbh");	// 工单编号 pl_gdb gdbh
		String sbid = parameters.getString("query_sbbh");	// 设备编号 left join em_sbxxb  on sbid sbbh 查询条件修改为sbid
		
		String czry = parameters.getString("query_czry");	// 操作人员 pl_gdb czry
//		String gdztdm = parameters.getString("query_gdzt");	// 工单状态代码 pl_gdb gdzt 工单状态代码
		String dyzt = parameters.getString("query_gddyzt");	// 打印状态代码 pl_gdb gddyzt 工单打印状态代码

		String con = " 1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		Parameters parts = new Parameters();
		if (StringUtils.isNotBlank(scrwid) || StringUtils.isNotBlank(pcid)) {
			parts.set("scrwid", scrwid);
			parts.set("pcbh", pcid);
			Bundle pcidBundle = Sys.callModuleService("pro", "proServiceScrwByScrwpcbhService", parts);// 查询生产任务id和批次id 
			List scrwpcList = (List) pcidBundle.get("scrwpc");
			if (null == scrwpcList || scrwpcList.isEmpty()) {
				return "json:";
			}
			Iterator<Map<String, Object>> iterator = scrwpcList.iterator();
			con += " and pcid in (";
			while (iterator.hasNext()) {
				Map<String, Object> pcMap = iterator.next();
				con += "'" + pcMap.get("scrwpcid") + "',";// 生产批次代码
			}
			con = con.substring(0, con.length() - 1);
			con += ")";

		}
		
		// 根据设备编号查询设备id  update by yangfan  查询条件由设备编号修改为设备id 
		/*if (StringUtils.isNotBlank(sbbh)) {
			parts = new Parameters();
			parts.set("sbbh", sbbh);
			Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxxInfoBySbbh", parts);// 根据设备编号，查询设备id
			List<Map<String, Object>> sbList = (List<Map<String, Object>>) sbBundle.get("sbxxList");
			if (null == sbList || sbList.isEmpty()) {
				return "json:";
			}
			
			Iterator<Map<String, Object>> iterator = sbList.iterator();
			con += " and sbid in (";
			while (iterator.hasNext()) {
				Map<String, Object> sbMap = iterator.next();
				con += "'" + sbMap.get("sbid") + "',";// 生产批次代码
			}
			con = con.substring(0, con.length() - 1);
			con += ")";
		}*/
		if(StringUtils.isNotBlank(sbid)){
			con += " and sbid = ? ";
			val.add(sbid);
		}
		
		
		if(StringUtils.isNotBlank(gdbh)){
			con += " and gdbh like ? ";
			val.add("%" + gdbh + "%");
		}
		//TODO 2016/08/15 暂时删除调度人员查询条件
		if(StringUtils.isNotBlank(czry)){
			con += " and czry = ? ";
			val.add(czry);
		}
		// 工单状态
		con += " and gdztdm in ('15','20','30','40') ";//修改问题：默认不显示已完成的工单，删除状态“50” add by yangfan 2016/8/31
		
		// 打印状态
		if (StringUtils.isNotBlank(dyzt)) {
			con += " and dyzt = ? ";
			val.add(dyzt);
		}
		
		// 工单id,工单编号,零件id,工序id,工单条码,加工数量,计划开始时间,计划完成时间,操作人员,设备id
		Dataset datasetgdb = Sys.query("pl_gdb", " gdid, gdbh, pcid,  ljid, gxid, jgsl, jhkssj, jhjssj, czry, sbid, dyzt, gdztdm ", con,
				" gdbh desc ", (page - 1) * pageSize, pageSize, val.toArray());

		List<Map<String, Object>> gdList = datasetgdb.getList(true);
		if(null != gdList && !gdList.isEmpty()){
			//生产任务id
			StringBuffer scrwidSb = new StringBuffer();
			//任务批次id
			StringBuffer rwpcidSb = new StringBuffer();
			//设备id
			StringBuffer sbidSb = new StringBuffer();
			Iterator<Map<String, Object>>gdIterator = gdList.iterator();
			scrwidSb.append("(");
			rwpcidSb.append("(");
			sbidSb.append("(");
			Map<String, Object> gdMap = null;
			while (gdIterator.hasNext()){
				gdMap = gdIterator.next();
				rwpcidSb.append(gdMap.get("pcid")).append(",");
				sbidSb.append(gdMap.get("sbid")).append(",");
			}
			rwpcidSb.deleteCharAt(rwpcidSb.length()-1);
			sbidSb.deleteCharAt(sbidSb.length()-1);
			rwpcidSb.append(")");
			sbidSb.append(")");
			
			//查询生产任务编号和任务批次
			parts = new Parameters();
			parts.set("pcid", rwpcidSb.toString());
			Bundle pcbhBundle = Sys.callModuleService("pro", "proServicePcbhByPcidService", parts);//查询生产任务编号和批次编号
			List<Map<String, Object>> pcbhList = (List<Map<String, Object>>) pcbhBundle.get("scrwpc");
			if(null != pcbhList && pcbhList.size()>0){
				Map<String, Object> pcbhMap = null;
				
				//重新循环，赋值生产任务编号和任务批次编号
				gdIterator = gdList.iterator();
				for(int i = 0; i<gdList.size();i++) {
					gdMap = gdList.get(i);
					Iterator<Map<String, Object>> pcIterator = pcbhList.iterator();
					while (pcIterator.hasNext()) {
						pcbhMap = pcIterator.next();
						if(null != gdMap.get("pcid")&& gdMap.get("pcid").equals(pcbhMap.get("scrwpcid"))){
							gdMap.put("scrwpcbh", pcbhMap.get("pcbh")); 
							gdMap.put("scrwbh", pcbhMap.get("scrwbh")); 
							break;
						}
					}
				}
			}
			
			//查询设备信息
			parts = new Parameters();
			parts.set("val_sb", sbidSb.toString());
			Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxx", parts);//查询生产任务编号和批次编号
			List<Map<String, Object>> sbList = (List<Map<String, Object>>) sbBundle.get("sbxx");
			if(null != sbList && sbList.size()>0){
				Map<String, Object> sbMap = null;
				
				//重新循环，赋值编号
				gdIterator = gdList.iterator();
				for(int i = 0; i<gdList.size();i++) {
					gdMap = gdList.get(i);
					Iterator<Map<String, Object>> sbIterator = sbList.iterator();
					String sbidGd = gdMap.get("sbid").toString();
					while (sbIterator.hasNext()) {
						sbMap = sbIterator.next();
						if(null != sbidGd && null != sbMap && sbidGd.equals(sbMap.get("sbid").toString())){
							gdMap.put("sbbh", sbMap.get("sbbh")); 
							break;
						}
					}
				}
			}
		}
		
		bundle.put("rows", gdList);
		int totalPage = datasetgdb.getTotal() % pageSize == 0 ? datasetgdb.getTotal() / pageSize
				: datasetgdb.getTotal() / pageSize + 1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", datasetgdb.getTotal());

		return "json:";
	}

	


	/**
	 * 导出车间生产工单文件
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String exportPDF(Parameters parameters, Bundle bundle) throws Exception {
		JSONArray jsonarray = JSONArray.fromObject(parameters.get("data_list"));
		StringBuffer sb = new StringBuffer();
		String conditions = " 1 = 1 ";
		if (null != jsonarray && jsonarray.size() > 0) {
			sb.append("(");
			for (int i = 0; i < jsonarray.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map = jsonarray.getJSONObject(i);
				sb.append("'").append(map.get("gdid")).append("'").append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			conditions = " gdid in  " + sb.toString();
		}

		// 查询需要打印的工单数据 工单id,工单编号,零件id,工序id,工单条码,加工数量,计划开始时间,计划完成时间,操作人员,设备id,箱号
		Dataset datasetGd = Sys.query("pl_gdb", "gdid, gdbh, pcid, jgsl, jhkssj, jhjssj, ljid, gxid, sbid, xh", conditions, null, new Object[]{});
		List<Map<String, Object>> gdList = datasetGd.getList();

		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < gdList.size(); i++) {
			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Object> gdMap = gdList.get(i);
			String gdbh = gdMap.get("gdbh").toString();
			data.put("gdbh", gdbh);// 工单编号
			data.put("jgsl", gdMap.get("jgsl"));// 加工数量
			
			//工单条码 gdtm
			try {
				File gdtmFile = SimpleCodeUtil.createBarcode(gdbh);
				data.put("gdtm", gdtmFile.getInputStream());
			} catch (Exception e) {
				e.printStackTrace();
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
			Bundle gxBundle = Sys.callModuleService("pm", "queryGxxxByGxid", parts);
			Map<String, Object> gxMap = (Map<String, Object>) gxBundle.get("gxxx");
			if (null != gxMap) {
				if (null !=  gxMap.get("gxbh") && StringUtils.isNotBlank( gxMap.get("gxbh").toString())) {
					data.put("gxbh", gxMap.get("gxbh"));// 工序编号
				}
				if (null != gxMap.get("gxmc") && StringUtils.isNotBlank(gxMap.get("gxmc").toString())) {
					data.put("gxmc", gxMap.get("gxmc"));// 工序名称
				}
			}
			// 查询物料信息（物料编号） 是否可以通过生产任务准备清单 （pl_scrwzbqd）查询物料id，再查询物料编号
			// 分两步，1. 查询物料id，根据工序id查询工序物料关联表。2. 查询物料编号，根据物料id查询物料信息表
			parts = new Parameters();
			parts.set("gxid", gdMap.get("gxid"));
			Bundle wlBundle = Sys.callModuleService("pm", "queryWlidByGxid", parts);
			Map<String, Object> wlMap = (Map<String, Object>) wlBundle.get("wlxx");
			if (null != wlMap && null != wlMap.get("wlid")) {
				String wlid = wlMap.get("wlid").toString();
				
				//第2步查询物料编号
				parts = new Parameters();
				parts.set("wlid", wlid);
				Bundle wlbhBundle = Sys.callModuleService("mm", "materielInfoByWlidService", parts);
				Map<String, Object> wlbhMap  =( Map<String, Object> )wlbhBundle.get("materielInfo");
				 if(wlbhMap != null){
					data.put("wlbh", (String) wlbhMap.get("wlbh"));
				 }
			}
			

			// 查询工步信息
			parts = new Parameters();
			parts.set("gxid", gdMap.get("gxid"));
			Bundle gbBundle = Sys.callModuleService("pm", "pmservice_gbxx", parts);
			List<Map<String, Object>> gbList = (List<Map<String, Object>>) gbBundle.get("gbxx");
			Map<String, Object> table = new HashMap<String, Object>();
			table.put("header", new String[] { "工步号", "工步名称", "刀具名称" });
			
			int gbCount = gbList.size();
			StringBuffer wlsb = new StringBuffer();
			wlsb.append("(");
			String[][] tb = new String[gbCount][3];

			for (int k = 0; k < gbCount; k++) {
				// Map gbmap = gbList.get(k);
				if (gbList.get(k) != null) {
					tb[k][0] =  String.valueOf(gbList.get(k).get("gbxh"));
					tb[k][1] = (String) gbList.get(k).get("jggcms");
					tb[k][2] = String.valueOf( gbList.get(k).get("djid"));
					wlsb.append("'").append(tb[k][2]).append("',");
					// 物料表查询刀具信息（刀具名称）

				}
			}
			wlsb.deleteCharAt(wlsb.length() - 1);
			wlsb.append(")");
			
			// 查询刀具名称
			parts = new Parameters();
			parts.set("val_wl", wlsb.toString());
			Bundle djBundle = Sys.callModuleService("mm", "mmservice_wlxx", parts);
			if (djBundle != null) {
				List<Map<String, Object>> djList = (List<Map<String, Object>>) djBundle.get("wlxx");
				// 将刀具名称更新到table域中
				if (!djList.isEmpty()) {
					for (int k = 0; k < gbCount; k++) {
						// Map gbmap = gbList.get(k);
						Iterator iterator = djList.iterator();
						if (gbList.get(k) != null) {
							while (iterator.hasNext()) {
								Map djMap = (Map) iterator.next();
								if (tb[k][2].equals(djMap.get("wlid").toString())) {
									tb[k][2] = (String) djMap.get("wlmc");
									break;
								}
							}
						}
					}
				}
			}

			table.put("data", tb);
			data.put("table", table);
			
			//查询设备编号
//			parts = new Parameters();
//			parts.set("sbid", gdMap.get("sbid"));// 设备id
//			Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxxInfo", parts);// 根据设备id，查询设备编号
//			Map<String,Object> sbMap = (Map<String, Object>) sbBundle.get("sbxx");
//			if (sbMap != null && !sbMap.isEmpty()) {
//				data.put("sbbh", sbMap.get("sbbh"));// 设置程序名称
//			}
			// 查询加工单元编号
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
			datas.add(data);
			
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
			String templateId = "gddy_tp";
			Map<String, Object> data1 = new HashMap<String, Object>();
			data1.put("scrwbh", data.get("scrwbh"));//生产任务编号
			data1.put("pcmc", data.get("pcmc"));
			data1.put("pcbh", data.get("pcbh"));
			data1.put("gdbh", data.get("xh")); // 箱号
			data1.put("jgsl", data.get("jgsl"));
			data1.put("sbbh", data.get("sbbh"));
			data1.put("ljbh", data.get("ljbh"));
			data1.put("ljmc", data.get("ljmc"));
			data1.put("gxmc", data.get("gxmc"));
			data1.put("jhkssj", ((java.sql.Timestamp)gdMap.get("jhkssj")).getTime());
			data1.put("jhjssj", ((java.sql.Timestamp)gdMap.get("jhjssj")).getTime());
			data1.put("userid", Sys.getUserIdentifier());//操作人
			data1.put("username", Sys.getUserName());//操作人
			data1.put("ljtp", ljtp);//零件图片
			data1.put("scrwjd", scrwjd);//生产任务进度
			sendActivity(activityType, templateId, true, roles, null, null, data1);
		}

		InputStream is = this.getClass().getResourceAsStream("/" + templateName);

		//根据模板生成PDF文件
		InputStream result = Sys.exportFile(fileType, is, datas);
		File file = null;
		try {
			file = new File(fileName, null, result, fileType, Long.valueOf(String.valueOf(result.available())));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			throw new NumberFormatException();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new IOException();
		}
		
		//更新打印状态,将打印状态修改
		Map updateMap = new HashMap<String, Object>();
		updateMap.put("dyzt", "20");//已打印
		conditions += " and dyzt ='10' ";//将未打印的更新为已打印
		Sys.update("pl_gdb", updateMap, conditions, new Object[]{});
		bundle.put("data", file);
		
		return "file:data";
	}

	/**查询生产任务信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String rwxxSelect(Parameters parameters, Bundle bundle) {

		String scrwbh = parameters.getString("query");
		if (StringUtils.isNotBlank(scrwbh)) {

			// 查询物料信息
			Parameters parts = new Parameters();
			parts.set("scrwbh", scrwbh);
			parts.set("page","1");
			parts.set("pageSize","10");
			Bundle scrwBundle = Sys.callModuleService("pro", "proService_scrw", parts);// 查询生产任务信息
			List<Map<String, Object>> rwxx = (List<Map<String, Object>>) scrwBundle.get("rows");
			for (int i = 0; i < rwxx.size(); i++) {
				rwxx.get(i).put("value", rwxx.get(i).get("scrwid"));
				rwxx.get(i).put("label", rwxx.get(i).get("scrwbh"));
				rwxx.get(i).put("title", rwxx.get(i).get("scrwbh"));
			}
			bundle.put("rwxx", rwxx);
		}
		return "json:rwxx";
	}
	
	/**查询生产批次信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String pcxxSelect(Parameters parameters, Bundle bundle) {

		String pcbh = parameters.getString("query");
		if (StringUtils.isNotBlank(pcbh)) {

			// 查询物料信息
			Parameters parts = new Parameters();
			parts.set("pcbh", pcbh);
			Bundle scrwBundle = Sys.callModuleService("pro", "proServiceScrwByScrwpcbhService", parts);// 查询批次信息
			List<Map<String, Object>> pcxx = (List<Map<String, Object>>) scrwBundle.get("scrwpc");
			for (int i = 0; i < pcxx.size(); i++) {
				pcxx.get(i).put("value", pcxx.get(i).get("scrwpcid"));
				pcxx.get(i).put("label", pcxx.get(i).get("pcbh"));
				pcxx.get(i).put("title", pcxx.get(i).get("pcbh"));
			}
			bundle.put("pcxx", pcxx);
		}
		return "json:pcxx";
	}
	
	/**查询设备编号
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String sbxxSelect(Parameters parameters, Bundle bundle) {

		String sbbh = parameters.getString("query");
		if (StringUtils.isNotBlank(sbbh)) {

			// 查询物料信息
			Parameters parts = new Parameters();
			parts.set("sbbh", sbbh);
			Bundle scrwBundle = Sys.callModuleService("em", "emservice_sbxxInfoBySbbh", parts);// 查询批次信息
			List<Map<String, Object>> sbxx = (List<Map<String, Object>>) scrwBundle.get("sbxxList");
			for (int i = 0; i < sbxx.size(); i++) {
				sbxx.get(i).put("value", sbxx.get(i).get("sbid"));
				sbxx.get(i).put("label", sbxx.get(i).get("sbbh"));
				sbxx.get(i).put("title", sbxx.get(i).get("sbbh"));
			}
			bundle.put("sbxx", sbxx);
		}
		return "json:sbxx";
	}
	
	
	/**
	 * 格式化时间
	 * @param date
	 * @return
	 */
	private String dateFormat(Object date) {
		String result = "";
		if (null != date && date instanceof Date) {
			result = sdf.format(date);
		}
		return result;
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
	
	public String gdxfList(Parameters parameters, Bundle bundle) {
		return "pl_gdxf";
	}
	
	/**
	 * 工单列表数据查询
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String table_gdxf(Parameters parameters, Bundle bundle) {
		int page = StringUtils.isBlank(parameters.getString("page")) ? 1 : Integer.parseInt(parameters.get("page").toString());
		int pageSize = StringUtils.isBlank(parameters.getString("pageSize")) ? 10 : Integer.parseInt(parameters.get("pageSize").toString());

		String scrwbh = parameters.getString("query_scrwbh");// 生产任务编码 left join sc_scrw on scrwid scrwbh
		String pcbh = parameters.getString("query_pcbh");	// 任务批次编码 left join sc_scrwpc  on scrwpcid pcbh
		String gdbh = parameters.getString("query_gdbh");	// 工单编号 pl_gdb gdbh
		String sbbh = parameters.getString("query_sbbh");	// 设备编号 left join em_sbxxb  on sbid sbbh
		String czry = parameters.getString("query_czry");	// 操作人员 pl_gdb czry
//		String gdztdm = parameters.getString("query_gdzt");	// 工单状态代码 pl_gdb gdzt 工单状态代码
		String dyzt = parameters.getString("query_gddyzt");	// 打印状态代码 pl_gdb gddyzt 工单打印状态代码

		String con = " 1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		Parameters parts = new Parameters();
		if (StringUtils.isNotBlank(scrwbh) || StringUtils.isNotBlank(pcbh)) {
			parts.set("scrwbh", scrwbh);
			parts.set("pcbh", pcbh);
			Bundle pcidBundle = Sys.callModuleService("pro", "proServiceScrwByScrwpcbhService", parts);// 查询生产任务id和批次id
			List scrwpcList = (List) pcidBundle.get("scrwpc");
			if (null == scrwpcList || scrwpcList.isEmpty()) {
				return "json:";
			}
			Iterator<Map<String, Object>> iterator = scrwpcList.iterator();
			con += " and pcid in (";
			while (iterator.hasNext()) {
				Map<String, Object> pcMap = iterator.next();
				con += "'" + pcMap.get("scrwpcid") + "',";// 生产批次代码
			}
			con = con.substring(0, con.length() - 1);
			con += ")";

		}
		
		// 根据设备编号查询设备id
		if (StringUtils.isNotBlank(sbbh)) {
			parts = new Parameters();
			parts.set("sbbh", sbbh);
			Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxxInfoBySbbh", parts);// 根据设备编号，查询设备id
			List<Map<String, Object>> sbList = (List<Map<String, Object>>) sbBundle.get("sbxxList");
			if (null == sbList || sbList.isEmpty()) {
				return "json:";
			}
			
			Iterator<Map<String, Object>> iterator = sbList.iterator();
			con += " and sbid in (";
			while (iterator.hasNext()) {
				Map<String, Object> sbMap = iterator.next();
				con += "'" + sbMap.get("sbid") + "',";// 生产批次代码
			}
			con = con.substring(0, con.length() - 1);
			con += ")";
			
		}
		
		if(StringUtils.isNotBlank(gdbh)){
			con += " and gdbh like ? ";
			val.add("%" + gdbh + "%");
		}
		//TODO 2016/08/15 暂时删除调度人员查询条件
		if(StringUtils.isNotBlank(czry)){
			con += " and czry = ? ";
			val.add(czry);
		}
		// 工单状态
//		con += " and gdztdm in ('15','20','30','40','50') ";
		con += " and gdztdm in ('20','30') ";
		// 打印状态
		if (StringUtils.isNotBlank(dyzt)) {
			con += " and dyzt = ? ";
			val.add(dyzt);
		}
		
		// 工单id,工单编号,零件id,工序id,工单条码,加工数量,计划开始时间,计划完成时间,操作人员,设备id
		Dataset datasetgdb = Sys.query("pl_gdb", " gdid, gdbh, pcid,  ljid, gxid, jgsl, jhkssj, jhjssj, czry, sbid, dyzt, gdztdm ", con,
				" gdbh desc ", (page - 1) * pageSize, pageSize, val.toArray());

		List<Map<String, Object>> gdList = datasetgdb.getList(true);
		if(null != gdList && !gdList.isEmpty()){
			//生产任务id
			StringBuffer scrwidSb = new StringBuffer();
			//任务批次id
			StringBuffer rwpcidSb = new StringBuffer();
			//设备id
			StringBuffer sbidSb = new StringBuffer();
			Iterator<Map<String, Object>>gdIterator = gdList.iterator();
			scrwidSb.append("(");
			rwpcidSb.append("(");
			sbidSb.append("(");
			Map<String, Object> gdMap = null;
			while (gdIterator.hasNext()){
				gdMap = gdIterator.next();
				rwpcidSb.append(gdMap.get("pcid")).append(",");
				sbidSb.append(gdMap.get("sbid")).append(",");
			}
			rwpcidSb.deleteCharAt(rwpcidSb.length()-1);
			sbidSb.deleteCharAt(sbidSb.length()-1);
			rwpcidSb.append(")");
			sbidSb.append(")");
			
			//查询生产任务编号和任务批次
			parts = new Parameters();
			parts.set("pcid", rwpcidSb.toString());
			Bundle pcbhBundle = Sys.callModuleService("pro", "proServicePcbhByPcidService", parts);//查询生产任务编号和批次编号
			List<Map<String, Object>> pcbhList = (List<Map<String, Object>>) pcbhBundle.get("scrwpc");
			if(null != pcbhList && pcbhList.size()>0){
				Map<String, Object> pcbhMap = null;
				
				//重新循环，赋值生产任务编号和任务批次编号
				gdIterator = gdList.iterator();
				for(int i = 0; i<gdList.size();i++) {
					gdMap = gdList.get(i);
					Iterator<Map<String, Object>> pcIterator = pcbhList.iterator();
					while (pcIterator.hasNext()) {
						pcbhMap = pcIterator.next();
						if(null != gdMap.get("pcid")&& gdMap.get("pcid").equals(pcbhMap.get("scrwpcid"))){
							gdMap.put("scrwpcbh", pcbhMap.get("pcbh")); 
							gdMap.put("scrwbh", pcbhMap.get("scrwbh")); 
							break;
						}
					}
				}
			}
			
			//查询设备信息
			parts = new Parameters();
			parts.set("val_sb", sbidSb.toString());
			Bundle sbBundle = Sys.callModuleService("em", "emservice_sbxx", parts);//查询生产任务编号和批次编号
			List<Map<String, Object>> sbList = (List<Map<String, Object>>) sbBundle.get("sbxx");
			if(null != sbList && sbList.size()>0){
				Map<String, Object> sbMap = null;
				
				//重新循环，赋值编号
				gdIterator = gdList.iterator();
				for(int i = 0; i<gdList.size();i++) {
					gdMap = gdList.get(i);
					Iterator<Map<String, Object>> sbIterator = sbList.iterator();
					String sbidGd = gdMap.get("sbid").toString();
					while (sbIterator.hasNext()) {
						sbMap = sbIterator.next();
						if(null != sbidGd && null != sbMap && sbidGd.equals(sbMap.get("sbid").toString())){
							gdMap.put("sbbh", sbMap.get("sbbh")); 
							break;
						}
					}
				}
			}
		}
		
		bundle.put("rows", gdList);
		int totalPage = datasetgdb.getTotal() % pageSize == 0 ? datasetgdb.getTotal() / pageSize
				: datasetgdb.getTotal() / pageSize + 1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", datasetgdb.getTotal());

		return "json:";
	}
}
