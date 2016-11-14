package com.isesol.mes.ismes.pl.constant;

public class WorkOrderStatus {

	//计划排产页面，点击计划下发按钮，生成工单后
	public static final String 未下发 = "15";
	//工单预览页面，点击下发按钮后
	public static final String 已下发 = "20";
	//扫描枪扫码之后
	public static final String 加工中 = "30";
	//质量派工页面，申请质检后/ 在工序中 设置为必检时 工人手工报工后
	public static final String 质检中 = "40";
	//报完工后/质检人员 录入质检结果后
	public static final String 加工完成 = "50";
	//任务终止
	public static final String 已终止 = "60";
}
