package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttTimeComponent extends FieldComponent {
	private static final long serialVersionUID = -4297628909306862526L;

	@Override
	public void initial() {
		addAttribute("ng-model", AttributeType.String, null, false, 1, "数据绑定", "数据绑定", null);
		addAttribute("ng-blur", AttributeType.String, null, false, 1, "失去焦点事件", "失去焦点事件", null);
		super.initial();
	}
	
	public String getName() {
		return "gantt-time";
	}

}