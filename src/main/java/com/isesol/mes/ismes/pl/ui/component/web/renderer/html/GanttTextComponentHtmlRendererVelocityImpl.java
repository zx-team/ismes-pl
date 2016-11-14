package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ComponentRendererVelocityImpl;

public class GanttTextComponentHtmlRendererVelocityImpl extends ComponentRendererVelocityImpl {

	
	protected String getBodyTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_text.vm";
	}
	
	public String getComponentName() {
		return "gantt-text";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
