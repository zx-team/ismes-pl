package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttCheckboxComponent extends FieldComponent {
	private static final long serialVersionUID = -4630159795789350665L;

	public String getName() {
		return "gantt-checkbox";
	}
	
	@Override
	public void initial() {
		super.initial();
		addAttribute("value", AttributeType.String, null, true, 0,"值","复选框的值",null);
		addAttribute("label", AttributeType.String, null, true, 1,"文字","复选框的文字",null);
		addAttribute("checked", AttributeType.Boolean, false, true, 2,"默认选中","默认选中状态",null);
		addAttribute("inline", AttributeType.Boolean, false, false, 3,"内联显示","是否内联显示，如果为true,将不独占一行",null);
		
		addAttribute("ng-click", AttributeType.String, null, true, 1,"点击事件","点击事件",null);
		addAttribute("ng-model", AttributeType.String, null, true, 1,"数据绑定","数据绑定",null);
		
//		this.removeAttribute("placeholder");
//		this.removeEvent("focus");
//		this.removeEvent("blur");
//		this.addResource("all", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/all.css", "1.0.0", true);
//		this.addResource("_all", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/_all.css", "1.0.0", true);
//		this.addResource("minimal", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/minimal.css", "1.0.0", true);
//		this.addResource("minimal", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/minimal.png", "1.0.0", true);
//		this.addResource("minimal@2x", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/minimal@2x.png", "1.0.0", true);
//		
//		this.addResource("futurico", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/futurico/futurico.css", "1.0.0", true);
//		this.addResource("futurico", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/futurico/futurico.png", "1.0.0", true);
//		this.addResource("futurico@2x", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/futurico/futurico@2x.png", "1.0.0", true);
//		
//		this.addResource("polaris", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/polaris/polaris.css", "1.0.0", true);
//		this.addResource("polaris", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/polaris/polaris.png", "1.0.0", true);
//		this.addResource("polaris@2x", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/polaris/polaris@2x.png", "1.0.0", true);
//		
//		
//		this.addResource("grey", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/grey.css", "1.0.0", true);
//		this.addResource("grey", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/grey.png", "1.0.0", true);
//		this.addResource("grey@2x", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/icheck/minimal/grey@2x.png", "1.0.0", true);
		
//		this.addResource("icheck", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/icheck.js", "1.0.0", true);
//		this.addResource("ui.checkbox", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/ui.checkbox.js", "1.0.0", true);

	}
}
