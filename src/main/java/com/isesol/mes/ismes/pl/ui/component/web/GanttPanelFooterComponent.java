package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttPanelFooterComponent extends ContainerComponent {
	private static final long serialVersionUID = -8656388471479884372L;
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt-panel");
	}

	@Override
	public List<String> getParents() {
		return parents;
	}

	public String getName() {
		return "gantt-panel-footer";
	}

}
