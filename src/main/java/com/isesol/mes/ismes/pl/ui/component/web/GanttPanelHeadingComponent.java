package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttPanelHeadingComponent extends ContainerComponent {
	private static final long serialVersionUID = 4770501410616746748L;
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt-panel");
	}

	public String getName() {
		return "gantt-panel-heading";
	}

	@Override
	public List<String> getParents() {
		return parents;
	}

}
