package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttTableComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt");
		parents.add("gantt-schedule");
	}

	public String getName() {
		return "gantt-table";
	}
	
	@Override
	public void initial() {
		addAttribute("enabled", AttributeType.String, null, true, 1, "是否启用列表功能", "是否启用列表功能", null);
		addAttribute("columns", AttributeType.String, null, true, 2, "列内容表达式", "列内容表达式", null);
		addAttribute("headers", AttributeType.String, null, true, 3, "列表表头内容", "列表表头内容", null);
		addAttribute("classes", AttributeType.String, null, true, 4, "定制表头显示样式", "定制表头显示样式", null);
		addAttribute("formatters", AttributeType.String, null, true, 5, "用于格式化表头内容", "用于格式化表头内容", null);
		addAttribute("contents", AttributeType.String, null, true, 6, "列内容表达式", "列内容表达式", null);
		addAttribute("header-contents", AttributeType.String, null, true, 7, "定制表头显示内容", "定制表头显示内容", null);
		super.initial();
	}

}
