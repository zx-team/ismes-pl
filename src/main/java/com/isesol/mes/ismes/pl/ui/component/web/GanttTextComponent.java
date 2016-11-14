package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttTextComponent extends FieldComponent {
	private static final long serialVersionUID = -7832837460482667379L;

	public String getName() {
		return "gantt-text";
	}
	
	@Override
	public void initial() {
		addAttribute("ng-readonly", AttributeType.String, null, true, 1, "是否为只读", "是否为只读", null);
		addAttribute("ng-model", AttributeType.String, null, true, -1, "数据绑定", "数据绑定", null);
		super.initial();
	}
}