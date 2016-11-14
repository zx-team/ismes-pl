package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.component.web.renderer.ComponentRendererVelocityImpl;

public class GanttNumberComponentHtmlRendererVelocityImpl extends ComponentRendererVelocityImpl {

	
	protected String getBodyTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_number.vm";
	}

	@Override
	protected String getJavascriptTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_bindevents.vm";
	}

	public String getComponentName() {
		return "gantt-number";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
