package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ContainerComponentRendererVelocityImpl;

public class GanttPanelComponentHtmlRendererVelocityImpl extends ContainerComponentRendererVelocityImpl{

	protected String getHeadTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_panel_head.vm";
	}

	
	protected String getFootTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_panel_foot.vm";
	}


	public String getComponentName() {
		return "gantt-panel";
	}


	public RendererType getType() {
		return RendererType.Html;
	}
	
}
