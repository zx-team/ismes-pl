package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttDateComponent extends FieldComponent {
	private static final long serialVersionUID = -4297628909306862526L;

	@Override
	public void initial() {
		addAttribute("ng-model", AttributeType.String, null, false, 1, "数据绑定", "数据绑定", null);
//		addAttribute("min-date", AttributeType.String, null, false, 1, "最小日期", "最小日期", null);
//		addAttribute("max-date", AttributeType.String, null, false, 1, "最大日期", "最大日期", null);
		addAttribute("start-date", AttributeType.String, null, true, 1, "开始日期", "开始日期", null);
		addAttribute("start-week", AttributeType.String, null, true, 1, "周几为第一天", "周几为第一天", null);
		addAttribute("placeholder", AttributeType.String, null, false, 1, "提示信息", "提示信息", null);
		addAttribute("ng-blur", AttributeType.String, null, false, 1, "失去焦点事件", "失去焦点事件", null);
		super.initial();
	}
	
	public String getName() {
		return "gantt-date";
	}

}