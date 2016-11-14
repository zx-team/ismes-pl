package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttTooltipsComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt");
		parents.add("gantt-schedule");
	}

	public String getName() {
		return "gantt-tooltips";
	}
	
	@Override
	public void initial() {
		addAttribute("content", AttributeType.String, null, true, 1, "tooltip内容", "tooltip内容", null);
		super.initial();
	}

}
