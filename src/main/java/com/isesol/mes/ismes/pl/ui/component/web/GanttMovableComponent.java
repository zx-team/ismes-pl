package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttMovableComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt");
		parents.add("gantt-schedule");
	}

	public String getName() {
		return "gantt-movable";
	}
	
	@Override
	public void initial() {
		addAttribute("enabled", AttributeType.String, null, true, 1, "任务是否可被移动", "任务是否可被移动", null);
		addAttribute("allow-row-switching", AttributeType.String, null, true, 1, "是否允许换行移动", "是否允许换行移动", null);
		super.initial();
	}

}
