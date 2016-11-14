package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.component.web.FieldComponent;

public class GanttOptionComponent extends FieldComponent {
	private static final long serialVersionUID = -3505275720349042874L;
	private static final List<String> parents = new ArrayList<String>();
	static {
		parents.add("gantt-select");
	}

	public String getName() {
		return "gantt-option";
	}
	
	@Override
	public void initial() {
		this.removeAllAttributes();
		addAttribute("value", AttributeType.String, null, false, 1,"值","选项值",null);
		addAttribute("label", AttributeType.String, null, false, 2,"标签","显示文本",null);
		addAttribute("selected", AttributeType.Boolean, false, true, 3,"默认选中","是否选中，true：选中，false：不选中",null);
		addAttribute("ng-repeat", AttributeType.String, null, false, 4, "重复标签", "重复标签", null);
		super.initial();
	}

	@Override
	public List<String> getParents() {
		return parents;
	}
}
