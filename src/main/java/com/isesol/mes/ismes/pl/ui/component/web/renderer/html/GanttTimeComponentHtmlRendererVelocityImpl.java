package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ComponentRendererVelocityImpl;

public class GanttTimeComponentHtmlRendererVelocityImpl extends ComponentRendererVelocityImpl {

	
	protected String getBodyTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_time.vm";
	}

	public String getComponentName() {
		return "gantt-time";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
