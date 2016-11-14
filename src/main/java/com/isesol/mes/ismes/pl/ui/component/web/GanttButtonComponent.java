package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.ResourceType;
import com.isesol.ismes.platform.ui.component.web.ButtonComponent;

public class GanttButtonComponent extends ButtonComponent {

	private static final long serialVersionUID = 1L;

	public String getName() {
		return "gantt-button";
	}

	@Override
	public void initial() {
		addAttribute("type", AttributeType.Enumeration, "button", true,0,"类型","指定按钮的类型",new String[]{"submit","reset","button"});
		addAttribute("icon", AttributeType.String, null, true,0,"图标","系统可用的图标",null);
		addAttribute("href", AttributeType.String, null, true,0,"超链接","超链接地址",null);
		addAttribute("context", AttributeType.Enumeration, "default", true,6,"情景","设置显示的情景",CONTEXT_VALUES);
		addAttribute("tooltip", AttributeType.String, null, true,0,"提示","按钮的提示信息",null);
		addAttribute("ng-click", AttributeType.String, null, true,0,"ng-click","ng-click",null);
		this.addResource("ui.button", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/ui.button.js", "1.0.0", true);
		super.initial();
	}
	
}
