package com.isesol.mes.ismes.pl.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.isesol.ismes.aps.model.CalendarSource;
import com.isesol.ismes.aps.model.Device;
import com.isesol.ismes.aps.model.DeviceUnavai;
import com.isesol.ismes.aps.model.WorkOrder;
import com.isesol.ismes.aps.model.page.Gantt;
import com.isesol.ismes.aps.model.page.JhpcConverter;
import com.isesol.ismes.aps.simple.NewAPS;
import com.isesol.ismes.aps.simple.SimpleProcess;
import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

/**
 * 计划排产activity
 * @author wangxu
 *
 */
public class JhpcActivity {
	
	private final String 已保存 = "10";
	private final String 已发布 = "15";
	
	public String initAdjustScrw(Parameters parameters, Bundle bundle) {
		String jgdyids = parameters.getString("jgdyids");
		String pcid = parameters.getString("pcid");
		String pcjhksrq = parameters.getString("pcjhksrq");
		bundle.put("jgdyids", jgdyids);
		bundle.put("pcid", pcid);
		bundle.put("pcjhksrq", pcjhksrq);
		return "tzjgdyrw";
	}
	
	public String scrwTable(Parameters parameters, Bundle bundle) throws ParseException {
		bundle.put("rows", getTableContent(parameters));
		return "json:";
	}
	
	public String releaseTable(Parameters parameters, Bundle bundle) throws ParseException {
		List<Map<String, Object>> rows = getTableContent(parameters);
		if (rows.isEmpty()) {
			return "json:";
		}
		for (Map<String, Object> row : rows) {
			if (Integer.parseInt(String.valueOf(row.get("scrwztdm"))) >= Integer.parseInt(已发布)) {
				// 不处理已发布及以后流程状态的小生产任务
				continue;
			}
			Map<String, Object> updated = Maps.newHashMap();
			updated.put("gdztdm", 已发布);
			// 更改小任务状态
			Sys.update("pl_gdpcb", updated, "gdid=?", row.get("gdid"));
			// 生成工艺流程卡表中的数据
			insertGdb(row);
			if (this.isHxGd(String.valueOf(row.get("jgdyid")), String.valueOf(row.get("ljid")), (Date) row.get("jgksrq"))) {
				this.insertHxtz(String.valueOf(row.get("ljid")), String.valueOf(row.get("jgdyid")), String.valueOf(row.get("gxzid")), (Date) row.get("jgksrq"));
			}
		}
		return "json:";
	}
	
	/**
	 * 根据箱号的表达式还原箱号
	 * @param scph 生产批号
	 * @param xhPattern
	 * @return
	 */
	private static Map<Integer, List<String>> restoreXhs(String scph, String xhPattern) {
		Map<Integer, List<String>> slXhs = Maps.newHashMap();
		for (String pattern : xhPattern.split(";")) {
			int sl = Integer.parseInt(pattern.substring(1, pattern.indexOf(")")));
			List<String> xhs = slXhs.get(sl);
			if (xhs == null) {
				xhs = Lists.newArrayList();
				slXhs.put(sl, xhs);
			}
			String subPattern = pattern.substring(pattern.indexOf(")") + 1);
			for (String s : subPattern.split(",")) {
				if (s.indexOf("-") != -1) {
					// 范围
					for (int i = Integer.parseInt(s.split("-")[0]); i <= Integer.parseInt(s.split("-")[1]); i++) {
						xhs.add(scph + "-" + StringUtils.leftPad(i + "", 2, "0"));
					}
				} else {
					xhs.add(scph + "-" + StringUtils.leftPad(s + "", 2, "0"));
				}
			}
		}
		return slXhs;
	}
	
	private void insertGdb(Map<String, Object> row) {
		// 计算箱号信息
		String xhPattern = String.valueOf(row.get("xh"));
		Map<Integer, List<String>> slXhs = restoreXhs(String.valueOf(row.get("scph")), xhPattern);
		
		for (Entry<Integer, List<String>> entry : slXhs.entrySet()) {
			int jgsl = entry.getKey();
			List<String> xhs = entry.getValue();
			for (String xh : xhs) {
				Map<String, Object> gdData = new HashMap<String, Object>();
				gdData.put("gdbh", row.get("gdid"));
				gdData.put("jgjp", row.get("jgjp"));
				gdData.put("pcid", row.get("pcid"));// 批次ID
				gdData.put("ljid", row.get("ljid"));// 零件ID
				gdData.put("gxid", row.get("gxzid"));// 工序ID
				gdData.put("sbid", row.get("jgdyid"));// 设备ID
				gdData.put("xh", xh);    // 箱号
				gdData.put("jgsl", jgsl);// 加工数量
				gdData.put("gxzxh", row.get("gxzxh"));    // 工序组序号
				gdData.put("gxzbs", row.get("gxzbs"));    // 工序组标识
				gdData.put("gdztdm", 已发布);
				gdData.put("gdscsj", new Date()); // 工单生成时间
				gdData.put("jhkssj", row.get("jgksrq"));// 计划开始时间
				gdData.put("jhjssj", row.get("jgwcrq"));// 计划结束时间
				gdData.put("gdywcsl", 0); // 报完工数量
				gdData.put("ncbgsl", 0); // NC自动报工数量
				gdData.put("gdybgsl", 0); // 工人报工数量
				gdData.put("czry", Sys.getUserIdentifier()); // 操作人员
				gdData.put("dyzt", "10"); // 打印状态 未打印
				gdData.put("zzjgid", row.get("zzjgid")); // 组织机构ID
				gdData.put("bfgf", "0"); // 打印状态 未打印
				gdData.put("bflf", "0"); // 组织机构ID
				// 父工单编号
				gdData.put("fgdbh", "");
				// 判断是否为换线工单
				Sys.insert("pl_gdb", gdData);
				this.insertScrwzbqd(String.valueOf(gdData.get("gdid")), (Date) row.get("jgksrq"), 0, String.valueOf(row.get("gxzid")), jgsl);
			}
		}
	}
	
	private List<Map<String, Object>> getTableContent(Parameters parameters) throws ParseException {
		String jgdyid = parameters.getString("jgdyid");
		String pcjhksrq = parameters.getString("pcjhksrq");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date kssj = sdf.parse(pcjhksrq);
		Date date = new Date();
		Date dateCondition = kssj.compareTo(date) <= 0 ? kssj : date;
		String gdidSeqMapping = parameters.getString("gdidSeqMapping"); // 保存原序号的关系
		Map<String, String> gdidSeqMap = null;
		if (gdidSeqMapping != null) {
			gdidSeqMap = Maps.newHashMap();
			String[] temp = gdidSeqMapping.split(",");
			for (int i = 0; i < temp.length; i++) {
				gdidSeqMap.put(temp[i].split("-")[0], temp[i].split("-")[1]);
			}
		}
		// 根据加工单元获取任务信息
		Dataset dataset = Sys.query("pl_gdpcb", "gdid,ljid,gxid,jgsl,jhkssj,jhjssj,gdztdm,pcid,jgjp,xh,zxsl,gxzbs,gxzxh,zzjgid", "sbid = ? and jhkssj > ?", null, "jhkssj asc", new Object[] {jgdyid, dateCondition});
		List<Map<String, Object>> retRows = Lists.newArrayList();
		List<Map<String, Object>> rows = dataset.getList();
		for (int i = 0; i < rows.size(); i++) {
			Map<String, Object> row = rows.get(i);
			Map<String, Object> retRow = Maps.newHashMap();
			String gdid = String.valueOf(row.get("gdid")); // 工单id
			String ljid = String.valueOf(row.get("ljid")); // 零件id
			String gxid = String.valueOf(row.get("gxid")); // 工序组id
			String jgsl = String.valueOf(row.get("jgsl")); // 加工数量
			Date jhkssj = (Date)row.get("jhkssj"); // 计划开始时间
			Date jhjssj = (Date)row.get("jhjssj"); // 计划结束时间
			String scrwzt = getGdztmc(String.valueOf(row.get("gdztdm"))); // 工单状态代
			String pcid = String.valueOf(row.get("pcid")); // 批次id
			String jgjp = String.valueOf(row.get("jgjp"));
			String xh = String.valueOf(row.get("xh"));
			String zxsl = String.valueOf(row.get("zxsl"));
			String gxzxh = String.valueOf(row.get("gxzxh"));
			String gxzbs = String.valueOf(row.get("gxzbs"));
			String zzjgid = String.valueOf(row.get("zzjgid"));
			
			// 取工单对应的生产任务信息
			Parameters p1 = new Parameters();
			p1.set("pcid", pcid);
			Bundle b1 = Sys.callModuleService("pro", "proService_scrwInfoByPcid", p1);
			Map<String, Object> scrwxx = (Map<String, Object>) b1.get("scrw");
			
			// 取加工单元信息
			p1 = new Parameters();
			p1.set("jgdyid", jgdyid);
			b1 = Sys.callModuleService("em", "emservice_jgdyById", p1);
			Map<String,Object> jgdyxx = (Map<String, Object>) b1.get("data");
			
			// 取零件信息
			p1 = new Parameters();
			p1.set("ljid", ljid);
			b1 = Sys.callModuleService("pm", "partsInfoService", p1);
			Map<String,Object> ljxx = (Map<String, Object>) b1.get("partsInfo");
			
			// 获取工序组信息
			p1 = new Parameters();
			p1.set("gxzid", gxid);
			b1 = Sys.callModuleService("pm", "queryGxzxxByGxid_new", p1);
			Map<String,Object> gxxx = (Map<String, Object>) b1.get("gxxx");
			
			// 对返回结果赋值
			retRow.put("gdid", gdid);
			retRow.put("jgdymc", jgdyxx.get("jgdymc"));
			retRow.put("scph", scrwxx.get("scph"));
			retRow.put("lh", scrwxx.get("mplh"));
			retRow.put("th", ljxx.get("ljbh"));
			retRow.put("jhjgsl", jgsl);
			retRow.put("jgksrq", jhkssj);
			retRow.put("jgwcrq", jhjssj);
			retRow.put("gxzmc", gxxx.get("gxzmc"));
			retRow.put("scrwzt", scrwzt);
			retRow.put("scrwztdm", String.valueOf(row.get("gdztdm")));
			retRow.put("ljid", ljid);
			retRow.put("jgdyid", jgdyid);
			retRow.put("gxzid", gxid);
			retRow.put("jgjp", jgjp);
			retRow.put("xh", xh);
			retRow.put("zxsl", zxsl);
			retRow.put("gxzxh", gxzxh);
			retRow.put("gxzbs", gxzbs);
			retRow.put("zzjgid", zzjgid);
			retRow.put("pcid", pcid);
			
			// 通过零件id取零件信息
			if (gdidSeqMapping != null) {
				// 通过上移或者下移的按钮
				retRow.put("ysxh", gdidSeqMap.get(gdid));
			} else {
				// 初始化查询
				retRow.put("ysxh", i);
			}
			retRows.add(retRow);
		}
		return retRows;
	}
	
	/**
	 * 检查零件是否满足排产要求
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String checkPc(Parameters parameters, Bundle bundle) {
		String ljid = parameters.getString("ljid");
		String pcid = parameters.getString("pcid");
		// 根据批次ID查询加工状态
		Parameters p1 = new Parameters();
		p1.set("pcid", pcid);
		Bundle b1 = Sys.callModuleService("pro", "proService_scrwInfoByPcid", p1);
		Map<String, Object> scrw = (Map<String, Object>) b1.get("scrw");
		String jgzt = String.valueOf(scrw.get("jgzt"));
		parameters.set("jgzt", jgzt);
		String jgztDesc = "10".equals(jgzt) ? "-0" : "-2";
		Bundle b = Sys.callModuleService("pm", "pmservice_query_gxzList_by_ljid_jgzt", parameters);
		List<Map<String, Object>> gxzList = (List<Map<String, Object>>) b.get("gxzList");
		if (CollectionUtils.isEmpty(gxzList)) {
			bundle.put("error", "零件没有加工状态为" + jgztDesc + "生产过程信息"); 
			return "json:";
		}
		for (Map<String, Object> gxz : gxzList) {
			// 工序组名称
			String gxzmc = String.valueOf(gxz.get("gxzmc"));
			String gxids = String.valueOf(gxz.get("gxids"));
			String jgdyids = String.valueOf(gxz.get("jgdyids"));
			if (StringUtils.isEmpty(gxids)) {
				bundle.put("error", "工序组" + gxzmc + "没有关联工序"); 
				return "json:";
			}
			if (StringUtils.isEmpty(jgdyids) || "null".equals(jgdyids)) {
				bundle.put("error", "工序组" + gxzmc + "没有关联加工单元"); 
				return "json:";
			}
		}
		return "json:";
	}
	
	/**
	 * 加载甘特图数据
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws ParseException 
	 */
	public String loadSchedule(Parameters parameters, Bundle bundle) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String pcid = String.valueOf(parameters.getString("pcid"));
		String ljid = String.valueOf(parameters.getString("ljid"));
		Date fromDate = sdf.parse(String.valueOf(parameters.getString("fromDate")));
		Date toDate = sdf.parse(String.valueOf(parameters.getString("toDate")));
		String scale = parameters.getString("scale");
		String jgzt = parameters.getString("jgzt");
		// 本次选中的[gxid-sbid]
		List<String> gxsbids = Lists.newArrayList();
		String[] ids = String.valueOf(parameters.getString("sbids")).split(",");
		for (String id : ids) {
			if (!id.trim().equals("")) {
				gxsbids.add(id);
			}
		}
		Parameters p = new Parameters();
		p.set("ljid", ljid);
		Bundle b1 = Sys.callModuleService("pm", "partsInfoService", p);
		Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
		String ljmc = String.valueOf(partsInfoMap.get("ljmc"));
		
		// 获取批次信息
		p = new Parameters();
		p.set("val_pc", "(" + pcid + ")");
		Bundle b = Sys.callModuleService("pro", "proService_pcxxbyid", p);
		Map<String, Object> pcxx = ((List<Map<String, Object>>)b.get("pcxx")).get(0);
		// 批次计划状态
		int pczt = Integer.parseInt(String.valueOf(pcxx.get("pcjhztdm")));
		String scph = String.valueOf(pcxx.get("pcmc"));
		
		// 工序组ID，加工单元ID关联关系
		Map<String, List<DeviceUnavai>> sbMaintenance = Maps.newHashMap();
		List<SimpleProcess> processes = getExistingData(scph, ljid, ljmc, pcid, false, fromDate, toDate, sbMaintenance, jgzt);
		JhpcConverter gc = new JhpcConverter(processes, gxsbids, scale);
		List<Gantt> gantts = gc.get(pcid, pczt, ljid, ljmc, sbMaintenance, null);
		bundle.put("result", gantts);
		return "json:result";
	}
	
	/**
	 * 工序试排
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws ParseException 
	 */
	public String trySchedule(Parameters parameters, Bundle bundle) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date fromDate = sdf.parse(String.valueOf(parameters.getString("fromDate")));
		Date toDate = sdf.parse(String.valueOf(parameters.getString("toDate")));
		String pcid = String.valueOf(parameters.getString("pcid"));
		String pcbh = String.valueOf(parameters.getString("pcbh"));
		String scph = String.valueOf(parameters.getString("scph"));
		String ljid = String.valueOf(parameters.getString("ljid"));
		Calendar kssj = Calendar.getInstance();
		kssj.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(String.valueOf(parameters.getString("kssj"))));
		String scale = parameters.getString("scale");
		String jgzt = parameters.getString("jgzt");
		
		// 本次选中的[gxid-sbid]
		List<String> gxsbids = Lists.newArrayList();
		String[] ids = String.valueOf(parameters.getString("sbids")).split(",");
		for (String id : ids) {
			if (!id.trim().equals("")) {
				gxsbids.add(id);
			}
		}
		
		Parameters p = new Parameters();
		p.set("ljid", ljid);
		Bundle b1 = Sys.callModuleService("pm", "partsInfoService", p);
		Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
		String ljmc = String.valueOf(partsInfoMap.get("ljmc"));
		// 装箱数量
//		int zxsl = Integer.parseInt(String.valueOf(partsInfoMap.get("zxsl")));
		
		// 生产数量
		int quantity = Integer.parseInt(parameters.getString("pcsl"));
		// 工单最小单位数量
		int min = 1;
		// 单位数量， 正祥的位装箱数量
		int per = Integer.parseInt(parameters.getString("per"));
		// 工单最大单位数量，正祥为批次数量/装箱数量 向上取整
		int max = new Double(Math.ceil((double)quantity / per)).intValue();
		
		// 整单or流水
		boolean whole = true;
		
		Map<String, List<DeviceUnavai>> sbMaintenance = Maps.newHashMap();
		List<SimpleProcess> processes = getExistingData(scph, ljid, ljmc, pcid, true, fromDate, toDate, sbMaintenance, jgzt);
		Calendar from = Calendar.getInstance();
		if (kssj.after(from)) {
			// 如果计划开始时间要晚于当前时间的话，使用计划的开始时间作为排产的开始时间
			from = (Calendar) kssj.clone();
		}
		Map<String, List<WorkOrder>> sbWorkOrder = Maps.newHashMap();
		long consuming = new NewAPS(gxsbids, sbMaintenance).schedule(scph, ljid, pcid, pcbh, processes.get(0), quantity, from, per, min, max, whole, sbWorkOrder);
		if (checkResult(sbWorkOrder, toDate)) {
			JhpcConverter gc = new JhpcConverter(processes, gxsbids, scale);
			List<Gantt> gantts = gc.get(pcid, 30, ljid, ljmc, sbMaintenance, sbWorkOrder);
			bundle.put("result", gantts);
		} else {
			bundle.put("error", "没有在指定时间范围内试排成功，请扩大时间范围重试");
		}
		return "json:";
	}
	
	/**
	 * 检查试排的工单是否超出了计划排产的结束日期
	 * @param sbWorkOrder
	 * @param toDate
	 * @return
	 */
	private boolean checkResult(Map<String, List<WorkOrder>> sbWorkOrder, Date toDate) {
		boolean result = true;
		for (Entry<String, List<WorkOrder>> entry : sbWorkOrder.entrySet()) {
			List<WorkOrder> workOrders = entry.getValue();
			if (CollectionUtils.isEmpty(workOrders)) {
				continue;
			}
			Collections.sort(workOrders);
			WorkOrder last = workOrders.get(workOrders.size() - 1);
			if (last.completeAfter(toDate.getTime())) {
				return false;
			}
		}
		return result;
	}
	
	private String getGdztmc(String gdztdm) {
		String mc = "";
		switch (Integer.parseInt(gdztdm)) {
		case 10:
			mc = "已保存";
			break;
		case 15:
			mc = "未下发";
			break;
		case 20:
			mc = "已下发";
			break;
		case 30:
			mc = "加工中";
			break;
		case 40:
			mc = "质检中";
			break;
		case 50:
			mc = "加工完成";
			break;
		case 60:
			mc = "已终止";
			break;
		}
		return mc;
	}
	
	private List<SimpleProcess> getExistingData(String scph, String ljid, String ljmc, String pcid, boolean trySchedule, Date fromDate, Date toDate, Map<String, List<DeviceUnavai>> sbUnavai, String jgzt) throws ParseException {
		
		Calendar fromDay = Calendar.getInstance();
		fromDay.setTime(fromDate);
		fromDay.add(Calendar.DAY_OF_YEAR, -2);
		Calendar toDay = Calendar.getInstance();
		toDay.setTime(toDate);
		toDay.add(Calendar.DAY_OF_YEAR, 2);
		
		// 工序
		List<SimpleProcess> processes = Lists.newArrayList();
		
		// 根据零件id和加工状态获取工序组信息
		Parameters p = new Parameters();
		p.set("ljid", ljid);
		p.set("jgzt", jgzt);
		Bundle bundle = Sys.callModuleService("pm", "pmservice_query_gxzList_by_ljid_jgzt", p);
		List<Map<String, Object>> gxzList = (List<Map<String, Object>>) bundle.get("gxzList");
		Map<String, Device> idSb = Maps.newHashMap();
		if (CollectionUtils.isNotEmpty(gxzList)) {
			for (Map<String, Object> gx : gxzList) {
				// 工序组ID
				String gxzid = String.valueOf(gx.get("gxzid"));
				// 工序组名称
				String gxzmc = String.valueOf(gx.get("gxzmc"));
				// 加工单元IDs
				String jgdyids = String.valueOf(gx.get("jgdyids"));
				SimpleProcess process = new SimpleProcess(gxzid, gxzmc);
				
				// 通过加工单元IDs获取加工单元信息
				for (String jgdyid : jgdyids.split(",")) {
					Device device = idSb.get(jgdyid);
					if (device == null) {
						// 根据加工单元ID和工序组ID获取加工节拍和准备时间
						Parameters p1 = new Parameters();
						p1.set("jgdyid", jgdyid);
						Bundle b1 = Sys.callModuleService("em", "emservice_jgdyById", p1);
						String jgdymc = (String)((Map<String, Object>) b1.get("data")).get("jgdymc");
						String zzjgid = (String)((Map<String, Object>) b1.get("data")).get("zzjgid");
						
						// 根据加工单元ID和工序组ID获取加工节拍和准备时间
						p1 = new Parameters();
						p1.set("gxzid", gxzid);
						p1.set("jgdyid", jgdyid);
						b1 = Sys.callModuleService("pm", "pmservice_query_time_by_gxzid_jgdyid", p1);
						// 加工节拍
						int jgjp = (Integer)b1.get("jgjp");
						// 准备时间
						int zbsj = (Integer)b1.get("zbsj");
						// 加工时间
						int jgsj = (Integer)b1.get("jgsj");
						device = new Device(jgdyid, jgdymc, zzjgid, jgjp, zbsj, jgsj);
						idSb.put(jgdyid, device);
						
						// 获取设备上的工单
						String conditions = "";
						Object[] params = null;
						if (trySchedule) {
							// 试排时不加载加工单元上本批次工单
							conditions = "sbid=? and pcid!=? and jhkssj >= ? and jhjssj <= ?";
							params = new Object[] { jgdyid, pcid, fromDay.getTime(), toDay.getTime() };
						} else {
							// 初始化加载时显示加工单元上已有工单
							conditions = "sbid=? and jhkssj >= ? and jhjssj <= ?";
							params = new Object[] { jgdyid, fromDay.getTime(), toDay.getTime() };
						}
						Dataset dataset = Sys.query("pl_gdpcb", "gdid,gdbh,jgsl,jhkssj,jhjssj,gxid,ljid,pcid,gdztdm,fgdbh,xh", conditions, null, params);
						List<Map<String, Object>> workOrders = dataset.getList();
						for (Map<String, Object> workOrder : workOrders) {
							Calendar from = Calendar.getInstance();
							Calendar to = Calendar.getInstance();
							from.setTime((Date)workOrder.get("jhkssj"));
							to.setTime((Date)workOrder.get("jhjssj"));
							WorkOrder wo = new WorkOrder(String.valueOf(workOrder.get("gdid")), String.valueOf(workOrder.get("xh")), from.getTimeInMillis(), to.getTimeInMillis());
							wo.setGxid(String.valueOf(workOrder.get("gxid")));
							wo.setQuantity((Integer)workOrder.get("jgsl"));
							wo.setPartId(String.valueOf(workOrder.get("ljid")));
							wo.setPcid(String.valueOf(workOrder.get("pcid")));
							wo.setScph(scph);
							wo.setGdbh(String.valueOf(workOrder.get("gdbh")));
							wo.setGdztdm(String.valueOf(workOrder.get("gdztdm")));
							wo.setGdztmc(this.getGdztmc(String.valueOf(workOrder.get("gdztdm"))));
							wo.setFgdbh(String.valueOf(workOrder.get("fgdbh")));
							// 设置批次编号
							Parameters p4 = new Parameters();
							p4.set("val_pc", "(" + wo.getPcid() + ")");
							Bundle bd = Sys.callModuleService("pro", "proService_pcxxbyid", p4);
							Map<String, Object> pcxx = ((List<Map<String, Object>>)bd.get("pcxx")).get(0);
							wo.setPcbh(String.valueOf(pcxx.get("pcbh")));
							device.addWorkOrders(wo);
						}
						// 通过加工单元取设备
						Parameters p2 = new Parameters();
						p2.set("jgdyid", jgdyid);
						Bundle bd1 = Sys.callModuleService("em", "emservice_jgdysbglb", p2);
						List<Map<String, Object>> jgdysbglList = (List<Map<String, Object>>) bd1.get("sbidList");
						// 暂时只对应工厂日期，因为工厂日历对于每台设备来说都是一样的，所以只取加工单元内的一台设备
						String sbId = String.valueOf(jgdysbglList.get(0).get("sbid"));
						// 获取设备上的日历信息
						Parameters p4 = new Parameters();
						p4.set("sbid", sbId);
						p4.set("begin", fromDay.getTime());
						p4.set("end", toDay.getTime());
						Bundle b = Sys.callModuleService("em", "query_sb_cannotJobTime_service", p4);
						if (b != null && b.get("list") != null) {
							List<Map<String, Object>> list = (List<Map<String, Object>>) b.get("list");
							List<DeviceUnavai> mts = sbUnavai.get(device.getId()); // 此处是device.getId出来的是加工单元ID
							if (mts == null) {
								mts = Lists.newArrayList();
								sbUnavai.put(device.getId(), mts);
							}
							for (Map<String, Object> map : list) {
								String reason = String.valueOf(map.get("reason"));
								Date begin = (Date) map.get("begin");
								Date end = (Date) map.get("end");
								Calendar beginDay = Calendar.getInstance();
								beginDay.setTime(begin);
								Calendar endDay = Calendar.getInstance();
								endDay.setTime(end);
								// 日历来源对于非整天的设备不可用没有实际意义
								DeviceUnavai m = new DeviceUnavai(reason, CalendarSource.FACTORY, false,
										beginDay.getTimeInMillis(), endDay.getTimeInMillis());
								mts.add(m);
							}
						}
						if (b != null && b.get("allDaylist") != null) {
							List<DeviceUnavai> unavais = sbUnavai.get(device.getId());
							if (unavais == null) {
								unavais = Lists.newArrayList();
								sbUnavai.put(device.getId(), unavais);
							}
							List<Map<String, Object>> list = (List<Map<String, Object>>) b.get("allDaylist");
							for (Map<String, Object> map : list) {
								String reason = String.valueOf(map.get("reason"));
								CalendarSource source = CalendarSource.from(String.valueOf(map.get("source")));
								Date begin = (Date) map.get("date");
								Calendar beginDay = Calendar.getInstance();
								beginDay.setTime(begin);
								Calendar endDay = Calendar.getInstance();
								endDay.setTime(begin);
								endDay.add(Calendar.MINUTE, 1440);
								DeviceUnavai m = new DeviceUnavai(reason, source, true,
										beginDay.getTimeInMillis(), endDay.getTimeInMillis());
								unavais.add(m);
							}
						}
						
					}
					process.addDevice(device);
				}
				processes.add(process);
			}
		}
		
		// 工序间关联
		for (int i = 0; i < processes.size(); i++) {
			if (i == 0) {
				processes.get(i).setPrevious(null);
			} else {
				processes.get(i).setPrevious(processes.get(i - 1));
			}
			if (i == processes.size() - 1) {
				processes.get(processes.size() - 1).setNext(null);
			} else {
				processes.get(i).setNext(processes.get(i + 1));
			}
		}
		return processes;
	}
	
	private String getPczt(String dm) {
		String zt = "";
		switch (Integer.parseInt(dm)) {
		case 10:
			zt = "未下发";
			break;
		case 20:
			zt = "已下发";
			break;
		case 30:
			zt = "计划制定中";
			break;
		case 40:
			zt = "工单已生成";
			break;
		case 50:
			zt = "工单已下发";
			break;
		case 70:
			zt = "加工中";
			break;
		case 80:
			zt = "加工完成";
			break;
		case 85:
			zt = "已入库";
			break;
		case 90:
			zt = "已终止";
			break;
		}
		return zt;
	}
	
	/**
	 * 查看计划排产信息（页面初始化）
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String listSchedule(Parameters parameters, Bundle bundle) {
		// 批次id
		String pcid = parameters.getString("pcid");
		Parameters p = new Parameters();
		p.set("val_pc", "(" + pcid + ")");
		Bundle b = Sys.callModuleService("pro", "proService_pcxxbyid", p);
		Map<String, Object> pcxx = ((List<Map<String, Object>>)b.get("pcxx")).get(0);
		
		// 批次计划状态
		pcxx.put("pcjhztmc", getPczt(String.valueOf(pcxx.get("pcjhztdm"))));
		
		// 批次计划开始日期
		pcxx.put("wholekssj", String.valueOf(pcxx.get("pcjhksrq")).substring(0, 16));
		pcxx.put("pcjhksrq", String.valueOf(pcxx.get("pcjhksrq")).substring(0, 10));
		// 批次计划结束日期
		pcxx.put("pcjhwcrq", String.valueOf(pcxx.get("pcjhwcrq")).substring(0, 10));
		
		// 获取生产任务编号
		p = new Parameters();
		p.set("scrwid", pcxx.get("scrwid"));
		b = Sys.callModuleService("pro", "proService_scrwByScrwid", p);
		Map<String, Object> scrw = (Map<String, Object>) b.get("scrw");
		pcxx.put("scph", scrw.get("scph")); // 生产批号
		
		// 获取零件加工状态
		Bundle b2 = Sys.callModuleService("pro", "proService_queryJgztByPcid", parameters);
		String jgzt = String.valueOf(b2.get("jgzt"));
		
		String ljid = pcxx.get("ljid").toString();
		p = new Parameters();
		p.set("ljid", ljid);
		Bundle b1 = Sys.callModuleService("pm", "partsInfoService", p);
		Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
		pcxx.put("ljmc", partsInfoMap.get("ljmc"));
		pcxx.put("ljbh", partsInfoMap.get("ljbh"));
		pcxx.put("url", partsInfoMap.get("url"));
		pcxx.put("zxsl", partsInfoMap.get("zxsl"));
		pcxx.put("jgzt", jgzt);
		bundle.put("pcxx", pcxx);
		
		return "pl_jhpc";
	}
	
	/**
	 * 清除批次下已保存和已发布的工单
	 * @param pcid
	 */
	private void cleanGds(String pcid) {
		// 获取该零件批次的所有保存的工单
		Dataset existingDataset = Sys.query("pl_gdpcb", "gdid", "pcid=? and gdztdm=?", null, new Object[] { pcid, 已保存 });
		List<String> deleteableGdids = Lists.newArrayList();
		for (Map<String, Object> row : existingDataset.getList()) {
			deleteableGdids.add(String.valueOf(row.get("gdid")));
		}
		if (deleteableGdids.isEmpty()) {
			return;
		}
		String inCondition = Joiner.on(",").join(deleteableGdids);
		// 先清空同零件同批次的所有"已保存"的工单
		Sys.delete("pl_gdpcb", "gdid in(" + inCondition + ")");
	}
	
	/**
	 * 保存计划排产信息
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws ParseException 
	 */
	public String saveSchedule(Parameters parameters, Bundle bundle) throws ParseException {
		String data = parameters.getNames().next();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		JSONObject obj = JSONObject.parseObject(data);
		// 批次ID
		String pcid = obj.getString("pcid");
		// 批次编号
		String pcbh = obj.getString("pcbh");
		// 零件ID
		String ljid = obj.getString("ljid");
		// 零件编号
		String ljbh = obj.getString("ljbh");
		// 零件名称
		String ljmc = obj.getString("ljmc");
		// 遍历排产
		JSONArray gds = obj.getJSONArray("gd");
		// 是保存还是发布
		String type = obj.getString("type");
		// 装箱数量
		String zxsl = obj.getString("zxsl");
		
		Parameters p2 = new Parameters();
		p2.set("ljid", ljid);
		Bundle b1 = Sys.callModuleService("pm", "partsInfoService", p2);
		Map<String,Object> partsInfoMap = (Map<String, Object>) b1.get("partsInfo");
		String ljUrl = (String) partsInfoMap.get("url");
		
		// 清除已保存的工单
		this.cleanGds(pcid);
		
		// 保存提交的工单信息
		for (int i = 0; i < gds.size(); i++) {
			JSONObject gd = gds.getJSONObject(i);
			String gdbh = gd.getString("gdbh");// 工单编号
			String gxzid = gd.getString("gxid");// 工序组ID
			String sbid = gd.getString("sbid");// 设备ID
			String xh = gd.getString("xh");    // 箱号
			String gxzxh = gd.getString("gxzxh");    // 工序组序号
			String gxzbs = gd.getString("gxzbs");    // 工序组标识
			String zzjgid = gd.getString("zzjgid");// 组织机构ID
			int jgsl = gd.getInteger("jgsl");// 工单加工数量
			int jgsj = gd.getInteger("jgsj"); // 加工时间
			String hxgd = gd.getString("hxgd");// 是不是换线工单  yes:是，no:不是，unknown:不确定。出现unknown的原因是页面加载工单不完全无法通过页面来确定是否该工单前是否还有其他工单
			// 工单编号
			Map<String, Object> gdData = new HashMap<String, Object>();
			if (StringUtils.isEmpty(gdbh)) {
				gdData.put("gdbh", pcbh + StringUtils.leftPad(String.valueOf(Sys.getNextSequence("gd")), 4, "0")); // 工单编号
			} else {
				gdData.put("gdbh", gdbh);
			}
			gdData.put("jgjp", jgsj);
			gdData.put("pcid", pcid);// 批次ID
			gdData.put("ljid", ljid);// 零件ID
			gdData.put("gxid", gxzid);// 工序ID
			gdData.put("jgsl", jgsl);// 加工数量
			gdData.put("sbid", sbid);// 设备ID
			gdData.put("xh", xh);    // 箱号
			gdData.put("gxzxh", gxzxh);    // 工序组序号
			gdData.put("gxzbs", gxzbs);    // 工序组标识
			if ("release".equals(type)) {
				gdData.put("gdztdm", 已发布);
				if ("unknown".equals(hxgd)) {
					hxgd = this.isHxGd(sbid, ljid, sdf.parse(gd.getString("jhkssj"))) ? "yes" : "no";
				}
			} else { // 工单状态代码
				gdData.put("gdztdm", 已保存);
			}
			gdData.put("gdscsj", new Date()); // 工单生成时间
			gdData.put("jhkssj", sdf.parse(gd.getString("jhkssj")));// 计划开始时间
			gdData.put("jhjssj", sdf.parse(gd.getString("jhjssj")));// 计划结束时间
			gdData.put("gdywcsl", 0); // 报完工数量
			gdData.put("ncbgsl", 0); // NC自动报工数量
			gdData.put("gdybgsl", 0); // 工人报工数量
			gdData.put("czry", Sys.getUserIdentifier()); // 操作人员
			gdData.put("dyzt", "10"); // 打印状态 未打印
			gdData.put("zzjgid", zzjgid); // 组织机构ID
			gdData.put("bfgf", "0"); // 打印状态 未打印
			gdData.put("bflf", "0"); // 组织机构ID
			gdData.put("zxsl", zxsl); // 装箱数量
			
			// 父工单编号
			String fgdbh = gd.getString("fgdbh");
			if (fgdbh != null && !"".equals(fgdbh) && !"null".equals(fgdbh)) {
				gdData.put("fgdbh", fgdbh);
			}
			Sys.insert("pl_gdpcb", gdData);
			if ("release".equals(type)) {
				if ("yes".equals(hxgd))
				{
					// 换线通知
					insertHxtz(ljid, sbid, gxzid, sdf.parse(gd.getString("jhkssj")));
				}
				// 生产任务准备清单
				insertScrwzbqd(String.valueOf(gdData.get("gdid")), (Date) gdData.get("jhkssj"),
						gd.getInteger("zbsj") == null ? 0 : gd.getInteger("zbsj"), gxzid, jgsl);
			}
		}
		
		String ztdm = "";
		String ztmc = "";
		if ("release".equals(type)) {
			ztdm = "40";
			ztmc = "工单已生成";
			
			// activity
			Parameters p1 = new Parameters();
			p1.set("scrwpcid", pcid);
			Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", p1);
			Map<String, Object> map = (Map<String, Object>) b_scrwxx.get("scrwandpc");
			String activityType = "0"; //动态任务
			String[] roles = new String[] { "PLAN_MANAGEMENT_ROLE", "MANUFACTURING_MANAGEMENT_ROLE" };//关注该动态的角色
			String templateId = "pcfb_tp";
			Map<String, Object> data1 = new HashMap<String, Object>();
			data1.put("title", map.get("scrwbh") + "生产任务" + pcbh + "批次已排程");
			data1.put("ljUrl", ljUrl);
			data1.put("scrwbh", map.get("scrwbh"));//生产任务编号
			data1.put("pcmc", map.get("pcmc"));    //生产批号
			data1.put("ljmc", ljmc);//零件名称
			data1.put("ljbh", ljbh);//零件图号
			data1.put("gdgs", gds.size()); // 工单个数
			data1.put("scrwjgsl", map.get("jgsl")); // 生产任务加工数量
			data1.put("jgwcrq", ((java.sql.Timestamp)map.get("jgwcrq")).getTime()); // 生产任务加工完成日期
			data1.put("pcjhksrq", ((java.sql.Timestamp)map.get("pcjhksrq")).getTime()); // 批次计划开始日期
			data1.put("pcjhwcrq", ((java.sql.Timestamp)map.get("pcjhwcrq")).getTime()); // 批次计划完成日期
			data1.put("pcsl", map.get("pcsl")); // 批次数量
			data1.put("pcbh", pcbh);//批次编号
			data1.put("pcid", pcid);//批次编号
			data1.put("username", Sys.getUserName());//操作人
			// 生产任务进度
			Bundle b = new Bundle();
			Parameters p = new Parameters();
			p.set("query_scrwbh", map.get("scrwbh"));
			p.set("query_pcbh", pcbh);
			p.set("query_ljbh", ljbh);
			// TODO 等待郭皓完成
//			ProgressUtil util = new ProgressUtil();
//			util.caculateProgress(p, b);
			data1.put("scrwjd", 40); //生产任务进度
			sendActivity(activityType, templateId, true, roles, null, null, data1);
			
			// message
			String message_type = "1";// 待办事项
			String[] message_roles = new String[] { "PLAN_MANAGEMENT_ROLE", "MANUFACTURING_MANAGEMENT_ROLE" };
			StringBuffer content = new StringBuffer();
			StringBuffer title = new StringBuffer();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
			Date now = new Date();
			title.append(ljmc).append("(编号为").append(ljbh).append(")的批次生产计划(编号为").append(pcbh).append(")已经发布");
			content.append(sdf1.format(now)).append(",");
			content.append(ljmc).append("(编号为").append(ljbh).append(")的批次生产计划(批次号为");
			content.append(pcbh).append("),已经发布，该批次计划生产").append(map.get("pcsl")).append("件，请为该批次下发工单。");
			String bizType = "PL_BATCH_PLAN_RELEASE";// 批次计划发布
			String bizId = pcbh;
			String url = "/pl/gdyl/gdPreviewIndex?scrwpcid=" + map.get("scrwpcid");
			sendMessage(message_type, title.toString(), null, content.toString(), "系统发送", bizType, bizId,
					"0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, message_roles, null, null,
					"system"/* manual:人工发送，system：系统发送，interface：外部接口 */, null/* 消息来源ID */, null, now, null, null,
					null);
		} else {
			ztdm = "30";
			ztmc = "计划制作中";
		}
		// 点击发布按钮 修改生产批次状态为 “工单已生成”
		Parameters p = new Parameters();
		p.set("scrwpcid", pcid);
		p.set("pcjhztdm", ztdm);
		Sys.callModuleService("pro", "proService_updateScrwpczt", p);
		
		Map<String, Object> result = Maps.newHashMap();
		result.put("pcjhztmc", ztmc);
		bundle.put("result", result);
		return "json:result";
	}
	
	/**
	 * 判断是不是换线工单
	 * @return
	 */
	private boolean isHxGd(String jgdyid, String ljid, Date jhkssj)
	{
		Object[] params = new Object[]{ jgdyid, jhkssj };
		// 找到这个工单开始时间前的第一条工单数据的零件ID
		Dataset dataset = Sys.query("pl_gdpcb", "ljid", "sbid= ? and jhjssj < ?", null, 0, 1, params);
		List<Map<String, Object>> rows = dataset.getList();
		if (rows.size() == 0) {
			// 该工单为这个加工单元在系统内的第一个工单
			return true;
		}
		return !ljid.equals(String.valueOf(rows.get(0).get("ljid")));
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
	
	private void insertHxtz(String ljid, String jgdyid, String gxzid, Date hxsj) {
		Map<String, Object> data = Maps.newHashMap();
		data.put("ljid", ljid);
		data.put("jgdyid", jgdyid);
		data.put("gxzid", gxzid);
		data.put("hxsj", hxsj);
		Sys.insert("pl_hxtz", data);
	}
	
	/**
	 * 生成生产任务准备清单
	 */
	private void insertScrwzbqd(String gdid, Date gdkssj, int sczbsj, String gxzid, int jgsl) {
		Calendar zbsj = Calendar.getInstance();
		zbsj.setTime(gdkssj);
		zbsj.add(Calendar.SECOND, -sczbsj);
		List<Map<String, Object>> list = Lists.newArrayList();
		
		// 根据工序组id获得工序id
		Parameters p = new Parameters();
		p.set("gxid", gxzid);
		Bundle b = Sys.callModuleService("pm", "queryGxzxxByGxid", p);
		String gxids = String.valueOf(((Map<String, Object>)b.get("gxxx")).get("gxids"));
		gxids = gxids.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "");
		for (String gxid : gxids.split(","))
		{
			// 获取工步信息（工序的刀具）
			p = new Parameters();
			p.set("gxid", gxid);
			b = Sys.callModuleService("pm", "pmservice_gbxx", p);
			List<Map<String, Object>> gbList = (List<Map<String, Object>>) b.get("gbxx");
			for (Map<String, Object> gb : gbList) {
				// 获取物料信息
				p = new Parameters();
				p.set("wlid", String.valueOf(gb.get("djid")));
				b = Sys.callModuleService("mm", "materielInfoByWlidService", p);
				if (b != null && b.get("materielInfo") != null) {
					Map<String, Object> m_wl = (Map<String, Object>) b.get("materielInfo");
					Map<String, Object> data = Maps.newHashMap();
					data.put("gdid", gdid);  // 工单ID
					data.put("wlid", gb.get("djid"));  // 物料ID
					data.put("wllb", "10");  // 物料类别
					data.put("zbsl", 1);  // 准备数量
					data.put("ydwsj", zbsj.getTime()); // 应到位时间
					data.put("dwdm", m_wl.get("wldwdm"));  // 单位代码
					list.add(data);
				}
			}
			
			// 获取关联物料
			p = new Parameters();
			p.set("gxid", gxid);
			b = Sys.callModuleService("pm", "pmservice_wllist_by_gxid", p);
			List<Map<String, Object>> wlglList = (List<Map<String, Object>>) b.get("wllist");
			if (CollectionUtils.isNotEmpty(wlglList)) {
				for (Map<String, Object> wlgl : wlglList) {
					if ("40".equals(String.valueOf(wlgl.get("wlqfdm")))) {
						// 过滤掉生成物料
						continue;
					}
					// 获取物料信息
					p = new Parameters();
					p.set("wlid", String.valueOf(wlgl.get("wlid")));
					b = Sys.callModuleService("mm", "materielInfoByWlidService", p);
					if (b != null && b.get("materielInfo") != null) {
						Map<String, Object> wl = (Map<String, Object>) b.get("materielInfo");
						Map<String, Object> data = Maps.newHashMap();
						data.put("gdid", gdid);            // 工单ID
						data.put("wlid", wlgl.get("wlid"));  // 物料ID
						String wllb = String.valueOf(wl.get("wllbdm"));
						if ("20".equals(wllb) || "30".equals(wllb)) {
							// 夹具、量具
							data.put("zbsl", Float.parseFloat(String.valueOf(wlgl.get("wlsl"))));
						} else if ("50".equals(wllb)) {
							// 原材料， 零件定额重量为kg单位 进生产准备表需要转为g的单位
							data.put("zbsl", Float.parseFloat(String.valueOf(wlgl.get("wlsl"))) * jgsl * 1000);
						}else {
							data.put("zbsl", Float.parseFloat(String.valueOf(wlgl.get("wlsl"))) * jgsl);
						}
						data.put("ydwsj", zbsj.getTime()); // 应到位时间
						data.put("wllb", wllb);// 物料类别
						data.put("dwdm", wl.get("wldwdm"));  // 单位代码
						list.add(data);
					}
				}
			}
			Sys.insert("pl_scrwzbqd", list);
		}
	}
	
	/**
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String pic_display(Parameters parameters, Bundle bundle) {
		Parameters p = new Parameters();
		p.set("wjid", parameters.getString("wjid"));
		Bundle b = Sys.callModuleService("pm", "picDisplay", p);
		bundle.put("file_display", b.get("file_display"));
		return "file:file_display";
	}
}
