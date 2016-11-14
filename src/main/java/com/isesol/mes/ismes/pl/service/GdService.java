package com.isesol.mes.ismes.pl.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.pl.constant.CustomConstant;

/**
 * 工单的service
 */
public class GdService {
	public void DataA_UpdateGd(Parameters parameters, Bundle bundle) {
		String gdid = (String) parameters.get("gdid");
		Map<String, Object> map = new HashMap<String, Object>();

		Dataset dataset = Sys.query(CustomConstant.工单表, "ncbgsl,gdywcsl,jgsl", " gdid = ? ", null,
				new Object[] { gdid });
		int count = 0;
		if (null != dataset && dataset.getList().size() > 0) {
			if (null != dataset.getMap().get(CustomConstant.工单表_NC自动报工数量)) {
				map.put(CustomConstant.工单表_NC自动报工数量,
						1 + Integer.parseInt(dataset.getMap().get(CustomConstant.工单表_NC自动报工数量).toString()));
			} else {
				map.put(CustomConstant.工单表_NC自动报工数量, 1);
			}
			if (null != dataset.getMap().get(CustomConstant.工单表_报完工数量)) {
				map.put(CustomConstant.工单表_报完工数量,
						1 + Integer.parseInt(dataset.getMap().get(CustomConstant.工单表_报完工数量).toString()));
			} else {
				map.put(CustomConstant.工单表_报完工数量, 1);
			}
			if (Integer.parseInt(map.get(CustomConstant.工单表_报完工数量).toString()) >= Integer
					.parseInt(dataset.getMap().get(CustomConstant.工单表_计划加工数量).toString())) {
				map.put(CustomConstant.工单表_工单状态代码, "50");
				map.put("gdwcsj", new Date());
			}
			count = Sys.update(CustomConstant.工单表, map, " gdid = ? ", new Object[] { gdid });
		}

		bundle.put("count", count);
	}

	public void queryGdxxByGdSbId(Parameters parameters, Bundle bundle) {
		String gdid = (String) parameters.get("gdid");
		String sbid = (String) parameters.get("sbid");
		long shtime = (Long) parameters.get("sjtime");

		Map<String, Object> map = new HashMap<String, Object>();

		Dataset dataset = Sys.query(CustomConstant.工单表, "ncbgsl,jgsl,jhkssj,jhjssj", " gdid = ? and sbid = ? ", null,
				new Object[] { gdid, sbid });
		if (null != dataset && dataset.getMap().size() > 0) {
			long starttime = ((java.sql.Timestamp) dataset.getMap().get("jhkssj")).getTime();
			long endtime = ((java.sql.Timestamp) dataset.getMap().get("jhjssj")).getTime();
			int jgsl = (Integer) dataset.getMap().get("jgsl");
			int ncbgsl = (Integer) dataset.getMap().get("ncbgsl");
			
			long nowtime = new Date().getTime();
			
			//截至到当前  应该完成的数量
			int ygwcsl=  (int) Math.round((nowtime - starttime)  / ( endtime - starttime) * jgsl);
			
			double b = new BigDecimal(ncbgsl).divide(new BigDecimal(ygwcsl), 4,BigDecimal.
					ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();
			String return_str = b + "%";
			
			bundle.put("sfzcjh", return_str);
			
		} else {
			bundle.put("sfzcjh", "");
		}
	}
	public void queryGdxxByScph(Parameters parameters,Bundle bundle){
		String param = " 1=1 ";
		if(null!=parameters.getString("scph")&&!"".equals(parameters.getString("scph"))){
			param+=" and pcid = '"+parameters.getString("scph")+"'";
		}
		if(null!=parameters.getString("rqstart")&&!"".equals(parameters.getString("rqstart"))){
			param+=" and gdscsj > '"+parameters.getString("rqstart")+"'";
		}
		if(null!=parameters.getString("rqend")&&!"".equals(parameters.getString("rqend"))){
			param+=" and gdscsj < '"+parameters.getString("rqend")+"'";
		}
		if(null!=parameters.getString("val_ljid")&&!"".equals(parameters.getString("val_ljid"))){
			param+=" and ljid in "+parameters.getString("val_ljid");
		}else{
			param+=" and ljid in ('000000000000000000000000000000')";
		}
		if(null!=parameters.getString("val_czry")&&!"".equals(parameters.getString("val_czry"))){
			param+=" and czry in "+parameters.getString("val_czry");
		}else{
			param+=" and czry in ('000000000000000000000000000000')";
		}
		Dataset dataset_gdxx = Sys.query("pl_gdb"," zzjgid, zjgkssj, jgkssj, fgdbh, ncbgsl, gdywcsl, gdybgsl, "
				+ "gdid,  bfgf, czry, bflf, gdbh, jhjssj, gxzbs, gdztdm, sbid, dyzt, jgwcsj, kgzt, jgsl, "
				+ "ljid, tssyx, jhkssj, xh, jgjp, gxmc, pcid, gxzxh, gdwcsj, gxid, gdscsj ", 
				param, null, new Object[]{});
		bundle.put("gdxx",  dataset_gdxx.getList());
	}
}
