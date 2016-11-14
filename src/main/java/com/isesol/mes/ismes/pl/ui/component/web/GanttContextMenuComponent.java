package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.ResourceType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttContextMenuComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;
	
	public String getName() {
		return "gantt-context-menu";
	}
	
	@Override
	public void initial() {
		addAttribute("menuOptions", AttributeType.String, null, true, 1, "数据选项", "数据选项", null);
//		addAttribute("controller", AttributeType.String, null, true, 1, "controller名称", "controller名称", null);
		this.addResource("context.menu", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/menu.css", "1.0.0", true);
		super.initial();
	}

}
