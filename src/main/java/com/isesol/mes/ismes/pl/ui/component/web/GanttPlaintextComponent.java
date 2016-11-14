package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttPlaintextComponent extends FieldComponent {
	
	private static final long serialVersionUID = -5347766607902834816L;

	public String getName() {
		return "gantt-plaintext";
	}

	@Override
	public void initial() {
		addAttribute("text", AttributeType.String, null, true, 1,"文本","显示的文本内容",null);
		addAttribute("bind", AttributeType.String, null, true, 2,"绑定","控件绑定信息指定，依据该信息动态获取控件值，如：user.name",null);
		addAttribute("format", AttributeType.String, null, true, 3,"显示格式","文字的显示格式如：YYYY-MM-dd 、####，##.00",null);
		addAttribute("lead", AttributeType.Boolean, false, true, 4,"突出显示","是否突出显示",null);
		addAttribute("type", AttributeType.Enumeration, null, true, 5,"显示类型","文本的显示类型，如：strong等",new String[] {"strong", "em", "u", "del","ins","mark","s","small","p"});//
		addAttribute("align", AttributeType.Enumeration, null, true, 6,"排列","排列方式，如：left",new String[] {"left","right","center","justify","nowrap"});//left right center justify nowrap
		addAttribute("context", AttributeType.Enumeration, "default", true,7,"情景","设置显示的情景",CONTEXT_VALUES);
		addAttribute("ng-bind", AttributeType.String, null, true, 1,"数据绑定","数据绑定",null);
		//this.addResource("ui.label", ResourceType.Javascript, "/com/isesol/ismes/platform/ui/component/web/resources/js/ui.label.js", "1.0.0", true);
		super.initial();
	}
}
