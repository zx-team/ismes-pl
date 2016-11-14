package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttGroupsComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt");
		parents.add("gantt-schedule");
	}

	public String getName() {
		return "gantt-groups";
	}
	
	@Override
	public void initial() {
		addAttribute("enabled", AttributeType.String, null, true, 1, "是否启用分组", "是否启用分组", null);
		addAttribute("display", AttributeType.String, null, true, 2, "分组内容显示形式", "分组内容显示形式", null);
		super.initial();
	}

}
