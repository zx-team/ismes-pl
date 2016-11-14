package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttAppComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	public String getName() {
		return "gantt-app";
	}
	
	@Override
	public void initial() {
		addAttribute("name", AttributeType.String, null, true, 1, "app名称", "app名称", null);
		addAttribute("ng-click", AttributeType.String, null, true, 1, "鼠标单击", "鼠标单击", null);
		addAttribute("controller", AttributeType.String, null, true, 1, "controller名称", "controller名称", null);
		super.initial();
	}

}
