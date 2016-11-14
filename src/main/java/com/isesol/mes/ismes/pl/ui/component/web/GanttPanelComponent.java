package com.isesol.mes.ismes.pl.ui.component.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.ComponentRuntime;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttPanelComponent extends ContainerComponent {

	private static final long serialVersionUID = -7445661759891540017L;
	private static final List<String> children = new ArrayList<String>();
	static {
		children.add("gantt-panel-body");
		children.add("gantt-panel-footer");
	}
	public String getName() {
		return "gantt-panel";
	}

	@Override
	public void initial() {
		addAttribute("heading", AttributeType.String, null, true, 1,"标题","",null);
		addAttribute("content", AttributeType.String, null, true, 2,"内容","",null);
		addAttribute("footer", AttributeType.String, null, true, 3,"脚文字","",null);
		addAttribute("context", AttributeType.Enumeration, "default", true,4,"情景","设置显示的情景",CONTEXT_VALUES);
		//addAttribute("border", AttributeType.Boolean, true, true, -1);
		super.initial();
	}

	@Override
	public List<String> getChildren() {
		return children;
	}

	@Override
	public String isValidChild(ComponentRuntime componentRuntime, ComponentRuntime child) {
		if(child.getComponentName().equals("gantt-panel-heading")) {
			List<ComponentRuntime> headings = componentRuntime.getChildren(child.getComponentName());
			if(headings.size()>0) {
				return "panel can't have more than one panel-heading child!";
			} else if(StringUtils.isNotEmpty((String)componentRuntime.getAttribute("heading"))) {
				return "panl can't set heading attribute and panel-heading child at the same time!";
			}
		} else if(child.getComponentName().equals("gantt-panel-body")) {
			List<ComponentRuntime> headings = componentRuntime.getChildren(child.getComponentName());
			if(headings.size()>0) {
				return "panel can't have more than one panel-body child!";
			} else if(StringUtils.isNotEmpty((String)componentRuntime.getAttribute("content"))) {
				return "panl can't set content attribute and panel-body child at the same time!";
			}
		} else if(child.getComponentName().equals("gantt-panel-footer")) {
			List<ComponentRuntime> headings = componentRuntime.getChildren(child.getComponentName());
			if(headings.size()>0) {
				return "panel can't have more than one panel-footer child!";
			} else if(StringUtils.isNotEmpty((String)componentRuntime.getAttribute("footer"))) {
				return "panl can't set footer attribute and panel-footer child at the same time!";
			}
		}
		return null;
	}
}