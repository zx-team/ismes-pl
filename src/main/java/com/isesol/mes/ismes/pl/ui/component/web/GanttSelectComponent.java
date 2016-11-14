package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.ResourceType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttSelectComponent extends FieldComponent {
	private static final long serialVersionUID = 5984150961266851671L;
	private static final List<String> children = new ArrayList<String>();
	static {
		children.add("gantt-option");
	}

	public String getName() {
		return "gantt-select";
	}
	
	@Override
	public void initial() {
		addAttribute("multiple", AttributeType.Boolean, false, false, 0,"可多选","是否可多选，true：多选，false：单选",null);
		addAttribute("dictionary", AttributeType.String, null, true, 0,"代码表","下拉框使用的代码表名称",null);
		addAttribute("parent", AttributeType.String, null, true, 0,"上级","联动的上级下拉框名称",null);
		addAttribute("url", AttributeType.String, null, true, 0,"数据地址","动态加载下拉框内容的url地质",null);
		addAttribute("ng-model", AttributeType.String, null, true, 0,"数据绑定","数据绑定",null);
		this.removeEvent("blur");
		this.removeEvent("focus");
		
		
		
		
		this.addResource("select2", ResourceType.Css,
				"/com/isesol/ismes/pl/ui/component/web/resources/css/select2.css", "1.0.0", true);
		this.addResource("select2-bootstrap.min", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/select2-bootstrap.min.css", "1.0.0", true);
		this.addResource("select2.full", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/select2.full.js", "1.0.0", true);
//		this.addResource("ui.select", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/ui.select.js", "1.0.0", true);
		super.initial();
	}

	@Override
	public List<String> getChildren() {
		return children;
	}
}
