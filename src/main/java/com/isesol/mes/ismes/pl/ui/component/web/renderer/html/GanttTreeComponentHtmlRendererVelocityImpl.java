package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ContainerComponentRendererVelocityImpl;

public class GanttTreeComponentHtmlRendererVelocityImpl extends ContainerComponentRendererVelocityImpl{

	protected String getHeadTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_tree_head.vm";
	}
	
	protected String getFootTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_tree_foot.vm";
	}

	public String getComponentName() {
		return "gantt-tree";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
