package com.isesol.mes.ismes.pl.ui.component.web.renderer.html;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.ui.ComponentFactory;
import com.isesol.ismes.platform.ui.ComponentRuntime;
import com.isesol.ismes.platform.ui.Context;
import com.isesol.ismes.platform.ui.Optgroup;
import com.isesol.ismes.platform.ui.Option;
import com.isesol.ismes.platform.ui.RendererType;
import com.isesol.ismes.platform.ui.UIException;
import com.isesol.ismes.platform.ui.component.web.renderer.ComponentRendererVelocityImpl;
import com.isesol.ismes.platform.ui.component.web.renderer.UITools;

public class GanttSelectComponentHtmlRendererVelocityImpl extends ComponentRendererVelocityImpl {

	
	@Override
	protected String getHeadTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_select_head.vm";
	}

	@Override
	protected String getFootTemplatePath() {
		return "/com/isesol/ismes/pl/ui/component/web/renderer/html/gantt_select_foot.vm";
	}
	
	private void genrateOptions(List<Option> options,ComponentRuntime componentRuntime) throws UIException {
		for(Option option:options) {
			if(option instanceof Optgroup) {
				ComponentRuntime optgroupComponent = new ComponentRuntime(ComponentFactory.getInstance().getComponent("optgroup"));
				optgroupComponent.addAttribute("value", option.getValue());
				optgroupComponent.addAttribute("label", option.getLabel());
				componentRuntime.addChild(optgroupComponent);
				this.genrateOptions(((Optgroup) option).getOptions(), optgroupComponent);
			} else {
				ComponentRuntime optionComponent = new ComponentRuntime(ComponentFactory.getInstance().getComponent("option"));
				optionComponent.addAttribute("value", option.getValue());
				optionComponent.addAttribute("label", option.getLabel());
				componentRuntime.addChild(optionComponent);
			}
		}
	}
	
	@Override
	protected String renderBody(Context context, ComponentRuntime componentRuntime) throws UIException {
		StringBuffer sb = new StringBuffer();
		Object value = componentRuntime.getAttribute("value");
		String bind = (String)componentRuntime.getAttribute("bind");
		if(StringUtils.isNotEmpty(bind)) {
			Object bindvalue = (new UITools(context)).getBindValue(bind);
			value = bindvalue==null?value:bindvalue;
		}
		String dictionary = (String)componentRuntime.getAttribute("dictionary");
//		if(StringUtils.isNotEmpty(dictionary)) {
//			DictionaryService dictionaryService = DictionaryServiceFactory.getInstance().getDictionaryService();
//			if(dictionaryService == null) {
//				throw new IllegalStateException("Can't find option service !");
//			}
//			List<Option> options = dictionaryService.getOptions(dictionary);
//			componentRuntime.removeChildren();
//			this.genrateOptions(options, componentRuntime);
//		}
		if(value!=null) {
			if(componentRuntime.getAttribute("multiple").toString().equalsIgnoreCase("true")) {
				Object[] values = null;
				if(value instanceof List) {
					values = ((List<?>)value).toArray();
				} else if( value.getClass().isArray() ) {
					values = (Object[])value;
				} else {
					values = value.toString().split(",");
				}
				Map<String,String> valuesMap = new HashMap<String,String>(values.length);
				for(Object v:values) {
					valuesMap.put(v.toString(), null);
				}
				for(ComponentRuntime child:componentRuntime.getChildren()) {
					if(child.getComponentName().equals("optgroup")) {
						for(ComponentRuntime c:child.getChildren()) {
							c.addAttribute("selected", false);
							if(valuesMap.containsKey(c.getAttribute("value"))) {
								c.addAttribute("selected", true);
							}
						}
					} else if(child.getComponentName().equals("option")) {
						child.addAttribute("selected", false);
						if(valuesMap.containsKey(child.getAttribute("value"))) {
							child.addAttribute("selected", true);
						}
					}
				}
			} else {
				String v = value.toString();
				for(ComponentRuntime child:componentRuntime.getChildren()) {
					if(child.getComponentName().equals("optgroup")) {
						for(ComponentRuntime c:child.getChildren()) {
							c.addAttribute("selected", false);
							if(c.getAttribute("value").equals(v)) {
								c.addAttribute("selected", true);
							}
						}
					} else if(child.getComponentName().equals("option")) {
						child.addAttribute("selected", false);
						if(child.getAttribute("value").equals(v)) {
							child.addAttribute("selected", true);
						}
					}
				}
			}
		}
		for(ComponentRuntime child:componentRuntime.getChildren()) {
			sb.append(child.render(this.getType(), context));
		}
		return sb.toString();
	}

	public String getComponentName() {
		return "gantt-select";
	}

	public RendererType getType() {
		return RendererType.Html;
	}
}
