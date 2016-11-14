package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttDrawTaskComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt");
		parents.add("gantt-schedule");
	}

	public String getName() {
		return "gantt-draw-task";
	}
	
	@Override
	public void initial() {
		addAttribute("enabled", AttributeType.String, null, true, 1, "是否允许画任务", "是否允许画任务", null);
		addAttribute("move-threshold", AttributeType.String, null, true, 1, "Threshold of how many pixel the user must move the mouse before the drawing begins. ", "Threshold of how many pixel the user must move the mouse before the drawing begins. ", null);
		addAttribute("task-factory", AttributeType.String, null, true, 1, "创建任务的工厂方法（任务模板）", "创建任务的工厂方法（任务模板）", null);
		super.initial();
	}
}
