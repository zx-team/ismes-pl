package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttNumberComponent extends FieldComponent {
	private static final long serialVersionUID = -8694483281826054166L;

	public String getName() {
		return "gantt-number";
	}

	@Override
	public void initial() {
		addAttribute("format", AttributeType.String, null, true, -1, "数字格式", "数字的格式，如：###，##.00", null);
		addAttribute("ng-model", AttributeType.String, null, true, -1, "数据绑定", "数据绑定", null);
		// addAttribute("footer", AttributeType.String, null, true, -1);
		super.initial();
	}
}