package com.isesol.mes.ismes.pl.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

public class ProgressUtil {
	
	private DecimalFormat fnum = new DecimalFormat("##0.00"); 
	
	@SuppressWarnings("unchecked")
	public void caculateProgress(Parameters parameters, Bundle bundle) {
		String scrwbh = parameters.getString("query_scrwbh");
		String pcbh = parameters.getString("query_pcbh");
		String ljbh = parameters.getString("query_ljbh");
		parameters.set("query_sign", "query");
		parameters.set("page", 1);
		parameters.set("pageSize", 10);
		//查询零件信息
		Bundle b_ljxx = Sys.callModuleService("pm", "pmservice_ljxxbybhmc", parameters);
		if (null==b_ljxx) {
			return;
		}
		List<Map<String, Object>> ljxx = (List<Map<String, Object>>) b_ljxx.get("ljxx");
		if (ljxx.size()<=0) {
			return;
		}
		String val_lj = "(";
		for (int i = 0; i < ljxx.size(); i++) {
			if(i!=0)
			{
				val_lj = val_lj +",";
			}
			val_lj += "'" +ljxx.get(i).get("ljid")+"'";
		} 
		val_lj = val_lj +")";
		parameters.set("val_lj", val_lj);
		
		//查询生产任务
		Bundle b_scrw = Sys.callModuleService("pro", "proService_scrw", parameters);
		List<Map<String, Object>> scrw = (List<Map<String, Object>>) b_scrw.get("rows");
		if (scrw.size()<=0) {
			return;
		}
		String val_scrw = "(";
		for (int i = 0; i < scrw.size(); i++) {
			for (int j = 0; j < ljxx.size(); j++) {
				if (scrw.get(i).get("ljid").toString().equals(ljxx.get(j).get("ljid").toString())) {
					scrw.get(i).put("ljbh", ljxx.get(j).get("ljbh"));
					scrw.get(i).put("ljmc", ljxx.get(j).get("ljmc"));
					break;
				}
			}
			//初始化完成数量
			scrw.get(i).put("ywcsl", 0);
			scrw.get(i).put("wxwcsl", 0);
			scrw.get(i).put("wcjd", 0.0);
			scrw.get(i).put("wxwcjd", 0.0);
			
			if(i!=0)
			{
				val_scrw = val_scrw +",";
			}
			val_scrw += scrw.get(i).get("scrwid");
		}
		val_scrw = val_scrw +")";
		parameters.set("val_scrw", val_scrw);
		
		//根据生产任务ID，查询生产任务批次信息
		Bundle b_pcxx = Sys.callModuleService("pro", "proService_pcxx", parameters);
		List<Map<String, Object>> pcxx = (List<Map<String, Object>>) b_pcxx.get("pcxx");
		if (pcxx.size()>0) {
			for (int n = 0; n < pcxx.size(); n++) {
				pcxx.get(n).put("pcjhzt", pcjhzt(pcxx.get(n).get("pcjhztdm")));
				//初始化完成数量
				pcxx.get(n).put("ywcsl", 0);
				pcxx.get(n).put("wcjd",0);
				pcxx.get(n).put("wxwcsl", 0.0);
				pcxx.get(n).put("wxwcjd", 0.0);
				String val_pc = "('"+pcxx.get(n).get("scrwpcid")+"')";
				parameters.set("val_pc", val_pc);
				Bundle b_gdxx = Sys.callModuleService("pl", "plservice_gxslByPcid", parameters);
				if(null != b_gdxx)
				{
					List<Map<String, Object>> gdxx = (List<Map<String, Object>>) b_gdxx.get("gdxx");
					if (gdxx.size()>0) {
						
						String val_gx = "(";
						for (int i = 0; i < gdxx.size(); i++) {
							if(i!=0)
							{
								val_gx = val_gx +",";
							}
							val_gx += "'" +gdxx.get(i).get("gxid")+"'";
						}
						val_gx = val_gx +")";
						parameters.set("val_gx", val_gx);
						Bundle b_gxxx = Sys.callModuleService("pm", "pmservice_query_gygx", parameters);
						if (null!= b_gxxx) {
							List<Map<String, Object>> gxxx = (List<Map<String, Object>>) b_gxxx.get("gxxx");
							for (int i = 0; i < gxxx.size(); i++) {
								for (int j = 0; j < gdxx.size(); j++) {
									if (gxxx.get(i).get("gxid").toString().equals(gdxx.get(j).get("gxid").toString())) {
										gxxx.get(i).put("jgsl",gdxx.get(j).get("jgsl"));
										gxxx.get(i).put("wcsl",gdxx.get(j).get("wcsl"));
									}
								}
							}
							Map<String, Object> gxxxMap = new HashMap<String, Object>();
							for (int i = 0; i < gxxx.size(); i++) {
								gxxx.get(i).put("zzpsl", 0);
								gxxx.get(i).put("ydcl", 0);
								if(null==gxxx.get(i).get("qxid")||StringUtils.isBlank(gxxx.get(i).get("qxid").toString()))
								{
									gxxx.get(i).put("qxjgsj", Integer.parseInt(gxxx.get(i).get("jgfs").toString()));
									gxxxMap = gx_ydcl(gxxx,gxxx.get(i));
									if("1".equals(gxxxMap.get("wxbz")))
									{
										gxxx.get(i).put("zzpsl", Integer.parseInt(gxxx.get(i).get("wcsl").toString()));
										pcxx.get(n).put("wxwcsl", gxxx.get(i).get("wcsl"));
										pcxx.get(n).put("ywcsl",fnum.format(Float.parseFloat(gxxxMap.get("ydcl").toString())+Float.parseFloat(gxxx.get(i).get("wcsl").toString())));
										pcxx.get(n).put("wcjd",""+(Math.round((Float.parseFloat(pcxx.get(n).get("ywcsl").toString())*10000)/Float.parseFloat(pcxx.get(n).get("pcsl").toString()))/100.0));
										pcxx.get(n).put("wxwcjd",""+(Math.round((Float.parseFloat(pcxx.get(n).get("wxwcsl").toString())*10000)/Float.parseFloat(pcxx.get(n).get("pcsl").toString()))/100.0));
									}else{
										gxxx.get(i).put("zzpsl", Float.parseFloat(gxxx.get(i).get("jgsl").toString())- Float.parseFloat(gxxx.get(i).get("wcsl").toString()));
										if(0!=Integer.parseInt(gxxx.get(i).get("jgsl").toString()))
										{
											pcxx.get(n).put("ywcsl",  fnum.format(Float.parseFloat(gxxxMap.get("ydcl").toString())+((Float.parseFloat(gxxx.get(i).get("zzpsl").toString())*(Float.parseFloat(gxxx.get(i).get("wcsl").toString())/Float.parseFloat(gxxx.get(i).get("jgsl").toString()))*(Float.parseFloat(gxxx.get(i).get("jgfs").toString())*100/Float.parseFloat(gxxx.get(i).get("zjgsj").toString())))/100.0)));
											pcxx.get(n).put("wxwcsl", gxxxMap.get("wxwcsl"));
											pcxx.get(n).put("wcjd",""+(Math.round((Float.parseFloat(pcxx.get(n).get("ywcsl").toString())*10000)/Float.parseFloat(pcxx.get(n).get("pcsl").toString()))/100.0));
											pcxx.get(n).put("wxwcjd",""+(Math.round((Float.parseFloat(pcxx.get(n).get("wxwcsl").toString())*10000)/Float.parseFloat(pcxx.get(n).get("pcsl").toString()))/100.0));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		Object pcjd = null;
		//计算生产任务已完成数量
		for (int i = 0; i < scrw.size(); i++) {
			for (int j = 0; j < pcxx.size(); j++) {
				if(StringUtils.isNotEmpty(pcbh) && pcbh.equals(pcxx.get(j).get("pcbh"))){
					pcjd = pcxx.get(j).get("wcjd");
				}
				if (scrw.get(i).get("scrwid").toString().equals(pcxx.get(j).get("scrwid").toString())) {
					scrw.get(i).put("ywcsl",Float.parseFloat(scrw.get(i).get("ywcsl").toString())+ Float.parseFloat(pcxx.get(j).get("ywcsl").toString()));
					scrw.get(i).put("wxwcsl",Float.parseFloat(scrw.get(i).get("wxwcsl").toString())+ Float.parseFloat(pcxx.get(j).get("wxwcsl").toString()));
				}
			}
		}
		//计算生产任务百分比
		for (int i = 0; i < scrw.size(); i++) {
			scrw.get(i).put("wcjd",""+(Math.round((Float.parseFloat(scrw.get(i).get("ywcsl").toString())*10000)/Float.parseFloat(scrw.get(i).get("jgsl").toString()))/100.0));
			scrw.get(i).put("wxwcjd",""+(Math.round((Float.parseFloat(scrw.get(i).get("wxwcsl").toString())*10000)/Float.parseFloat(scrw.get(i).get("jgsl").toString()))/100.0));
		}
		
		bundle.put("scrwjd", scrw.get(0).get("wcjd"));
		if(pcjd == null)pcjd = "0";
		bundle.put("pcjd", pcjd);
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
	
	/**工单约当产量的计算,递归
	 * @param parameters
	 * @param bundle
	 */
	private Map<String, Object> gx_ydcl(List<Map<String, Object>> gxxx ,Map<String, Object> gxMap) {
		Map<String, Object> gxxxMap = new HashMap<String, Object>();
		gxxxMap.put("ydcl", 0);//约当产量
		gxxxMap.put("wxbz", "1");//尾序标识
		for (int i = 0; i < gxxx.size(); i++) {
			if(gxMap.get("gxid").equals(gxxx.get(i).get("qxid")))
			{
				
				gxxx.get(i).put("qxjgsj", Integer.parseInt(gxxx.get(i).get("jgfs").toString()) + Integer.parseInt(gxMap.get("qxjgsj").toString()));
				gxxxMap = gx_ydcl(gxxx,gxxx.get(i));   
				
				if("1".equals(gxxxMap.get("wxbz")))
				{
					gxxxMap.put("wxwcsl", gxxx.get(i).get("wcsl"));
					gxxx.get(i).put("zzpsl", Integer.parseInt(gxMap.get("wcsl").toString()));
				}else{
					gxxxMap.put("wxwcsl", gxxxMap.get("wxwcsl"));
					gxxx.get(i).put("zzpsl", Integer.parseInt(gxMap.get("wcsl").toString()) - Integer.parseInt(gxxx.get(i).get("wcsl").toString()));
				}
				if(0!=Integer.parseInt(gxxx.get(i).get("jgsl").toString()))
				{
					float zzpsl = Float.parseFloat(gxxx.get(i).get("zzpsl").toString());
					float wcsl = Float.parseFloat(gxxx.get(i).get("wcsl").toString());
					float jgsl = Float.parseFloat(gxxx.get(i).get("jgsl").toString());
					float jgfs = Float.parseFloat(gxxx.get(i).get("jgfs").toString());
					float zjgsj = Float.parseFloat(gxxx.get(i).get("zjgsj").toString());
					float qxjgsj = Float.parseFloat(gxMap.get("qxjgsj").toString());
					gxxxMap.put("ydcl", Float.parseFloat(gxxxMap.get("ydcl").toString())+(zzpsl*((wcsl*100/jgsl)*jgfs+qxjgsj*100)/zjgsj)/100.0);
				}
				gxxxMap.put("wxbz", "0");
				break;
			}
		}
		
		return gxxxMap;
	}

}
