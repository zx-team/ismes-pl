package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ContainerComponentRendererVelocityImpl;

public class GanttAppComponentHtmlRendererVelocityImpl extends ContainerComponentRendererVelocityImpl{

	protected String getHeadTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_app_head.vm";
	}
	
	protected String getFootTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_app_foot.vm";
	}

	public String getComponentName() {
		return "gantt-app";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
