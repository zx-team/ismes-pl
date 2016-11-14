package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttTreeComponent extends ContainerComponent {
	
	private static final long serialVersionUID = 1L;
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt");
		parents.add("gantt-schedule");
	}

	public String getName() {
		return "gantt-tree";
	}
	
	@Override
	public void initial() {
		addAttribute("enabled", AttributeType.String, null, true, 1, "是否启用树形结构", "是否启用树形结构", null);
		addAttribute("header-content", AttributeType.String, null, true, 2, "定制表头显示内容", "定制表头显示内容", null);
		addAttribute("content", AttributeType.String, null, true, 3, "列内容表达式", "列内容表达式", null);
		addAttribute("keep-ancestor-on-filter-row", AttributeType.String, null, true, 4, "是否继承gantt的filter-row树形", "是否继承gantt的filter-row树形", null);
		super.initial();
	}

}
