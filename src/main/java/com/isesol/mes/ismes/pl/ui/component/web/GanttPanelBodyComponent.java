package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttPanelBodyComponent extends ContainerComponent {
	private static final long serialVersionUID = -3185323037356069465L;
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt-panel");
	}
	public String getName() {
		return "gantt-panel-body";
	}
	@Override
	public List<String> getParents() {
		return parents;
	}
}
