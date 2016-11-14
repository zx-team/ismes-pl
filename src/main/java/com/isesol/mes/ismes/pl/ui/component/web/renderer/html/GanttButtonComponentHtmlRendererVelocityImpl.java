package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ComponentRendererVelocityImpl;

public class GanttButtonComponentHtmlRendererVelocityImpl extends ComponentRendererVelocityImpl {

	
	protected String getBodyTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_button.vm";
	}
	protected String getJavascriptTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_button_javascript.vm";
	}

	public String getComponentName() {
		return "gantt-button";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
