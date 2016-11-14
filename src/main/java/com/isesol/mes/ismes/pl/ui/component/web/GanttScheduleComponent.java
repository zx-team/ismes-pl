package com.isesol.mes.ismes.pl.ui.component.web;

import com.isesol.ismes.platform.ui.AttributeType;
import com.isesol.ismes.platform.ui.ResourceType;
import com.isesol.ismes.platform.ui.component.web.ContainerComponent;

public class GanttScheduleComponent extends ContainerComponent {

	private static final long serialVersionUID = 1L;

	public String getName() {
		return "gantt-schedule";
	}
	
	@Override
	public void initial() {
		addAttribute("data", AttributeType.String, null, true, 1, "数据", "甘特图数据", null);
		addAttribute("timespans", AttributeType.String, null, true, 1, "不工作时间", "不工作时间", null);
		addAttribute("show-side", AttributeType.String, null, true, 2, "列表侧是否显示", "列表侧是否显示", null);
		addAttribute("daily", AttributeType.String, null, true, 3, "任务显示按照天对其", "任务显示按照天对其", null);
		addAttribute("filter-task", AttributeType.String, null, true, 4, "过滤任务的表达式", "过滤任务的表达式", null);
		addAttribute("filter-row", AttributeType.String, null, true, 5, "过滤显示行的表达式", "过滤显示行的表达式", null);
		addAttribute("view-scale", AttributeType.String, null, true, 6, "甘特图显示内容的时间维度", "甘特图显示内容的时间维度", null);
		addAttribute("column-width", AttributeType.String, null, true, 7, "甘特图中的列宽", "甘特图中的列宽", null);
		addAttribute("auto-expand", AttributeType.String, null, true, 8, "数据区域是否可以被扩展当用户使用滚动条从左到右滚动", "数据区域是否可以被扩展当用户使用滚动条从左到右滚动", null);
		addAttribute("task-out-of-range", AttributeType.String, null, true, 9, "当任务不在视图范围内的处理方式", "当任务不在视图范围内的处理方式", null);
		addAttribute("from-date", AttributeType.String, null, true, 10, "视图的开始日期", "视图的开始日期", null);
		addAttribute("to-date", AttributeType.String, null, true, 11, "视图的结束日期", "视图的结束日期", null);
		addAttribute("allow-side-resizing", AttributeType.String, null, true, 12, "左侧表格区域是否支持拖拽改变大小", "左侧表格区域是否支持拖拽改变大小", null);
		addAttribute("task-content", AttributeType.String, null, true, 13, "任务上显示的内容", "任务上显示的内容", null);
		addAttribute("row-content", AttributeType.String, null, true, 14, "行上显示的内容", "行上显示的内容", null);
		addAttribute("current-date", AttributeType.String, null, true, 15, "当前日期展现形式", "当前日期展现形式", null);
		addAttribute("current-date-value", AttributeType.String, null, true, 16, "当前日期", "当前日期", null);
		addAttribute("headers", AttributeType.String, null, true, 17, "甘特图的头信息", "甘特图的头信息", null);
		addAttribute("max-height", AttributeType.String, null, true, 18, "甘特图的最大高度", "如果内容超过最大高度将出现纵向滚动条", null);
		addAttribute("time-frames", AttributeType.String, null, true, 19, "工作时间", "工作时间", null);
		addAttribute("date-frames", AttributeType.String, null, true, 20, "工作日期", "工作日期", null);
		addAttribute("time-frames-working-mode", AttributeType.String, null, true, 21, "工作时间显示方式", "工作时间显示方式", null);
		addAttribute("time-frames-non-working-mode", AttributeType.String, null, true, 22, "非工作时间显示方式", "非工作时间显示方式", null);
		addAttribute("time-frames-magnet", AttributeType.String, null, true, 23, "Make timeFrame borders snap", "Make timeFrame borders snap", null);
		addAttribute("column-magnet", AttributeType.String, null, true, 24, "Precision of the column. All date and time computation will be rounded using this precision", "Precision of the column. All date and time computation will be rounded using this precision", null);
		addAttribute("api", AttributeType.String, null, true, 25, "Registers an API Object to call methods of the component and listen or raise events.", "Registers an API Object to call methods of the component and listen or raise events.", null);
		this.addResource("ui.angular", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/vendor.js", "1.0.0", true);
		this.addResource("ui.loading.bgiframe", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/loading/jquery.bgiframe.min.js", "1.0.0", true);
		this.addResource("ui.loading", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/loading/loading.js", "1.0.0", true);
		this.addResource("ui.scripts", ResourceType.Javascript, "/com/isesol/ismes/pl/ui/component/web/resources/js/pl_scdd.js", "1.0.0", true);
		this.addResource("ui.main.css", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/main.css", "1.0.0", true);
		this.addResource("ui.menu.css", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/menu.css", "1.0.0", true);
		this.addResource("fontawesome-webfont-eot", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/fontawesome-webfont.eot", "1.0.0", true);
		this.addResource("fontawesome-webfont-svg", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/fontawesome-webfont.svg", "1.0.0", true);
		this.addResource("fontawesome-webfont-ttf", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/fontawesome-webfont.ttf", "1.0.0", true);
		this.addResource("fontawesome-webfont-woff", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/fontawesome-webfont.woff", "1.0.0", true);
		this.addResource("fontawesome-webfont-woff2", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/fontawesome-webfont.woff2", "1.0.0", true);
		this.addResource("FontAwesome-otf", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/FontAwesome.otf", "1.0.0", true);
		
		this.addResource("glyphicons-halflings-regular-eot", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/glyphicons-halflings-regular.eot", "1.0.0", true);
		this.addResource("glyphicons-halflings-regular-svg", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/glyphicons-halflings-regular.svg", "1.0.0", true);
		this.addResource("glyphicons-halflings-regular-ttf", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/glyphicons-halflings-regular.ttf", "1.0.0", true);
		this.addResource("glyphicons-halflings-regular-woff", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/glyphicons-halflings-regular.woff", "1.0.0", true);
		this.addResource("glyphicons-halflings-regular-woff2", ResourceType.Font, "/com/isesol/ismes/pl/ui/component/web/resources/fonts/glyphicons-halflings-regular.woff2", "1.0.0", true);
		
		this.addResource("ui.vendor.css", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/vendor.css", "1.0.0", true);
		this.addResource("ui.loading.css", ResourceType.Css, "/com/isesol/ismes/pl/ui/component/web/resources/css/loading/loading.css", "1.0.0", true);
		this.addResource("ui.loading.img", ResourceType.Image, "/com/isesol/ismes/pl/ui/component/web/resources/css/loading/loading.gif", "1.0.0", true);
		
//		this.addResource("fontawesome-webfont.eot", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/fontawesome-webfont.eot", "1.0.0", false);
//		this.addResource("fontawesome-webfont.svg", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/fontawesome-webfont.svg", "1.0.0", false);
//		this.addResource("fontawesome-webfont.ttf", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/fontawesome-webfont.ttf", "1.0.0", false);
//		this.addResource("fontawesome-webfont.woff", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/fontawesome-webfont.woff", "1.0.0", false);
//		this.addResource("fontawesome-webfont.woff2", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/fontawesome-webfont.woff2", "1.0.0", false);
//		this.addResource("FontAwesome.otf", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/FontAwesome.otf", "1.0.0", false);
		
//		this.addResource("glyphicons-halflings-regular.eot", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/bootstrap/glyphicons-halflings-regular.eot", "3.3.5", false);
//		this.addResource("glyphicons-halflings-regular.svg", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/bootstrap/glyphicons-halflings-regular.svg", "3.3.5", false);
//		this.addResource("glyphicons-halflings-regular.ttf", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/bootstrap/glyphicons-halflings-regular.ttf", "3.3.5", false);
//		this.addResource("glyphicons-halflings-regular.woff", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/bootstrap/glyphicons-halflings-regular.woff", "3.3.5", false);
//		this.addResource("glyphicons-halflings-regular.woff2", ResourceType.Font, "/com/isesol/ismes/platform/ui/component/web/resources/fonts/bootstrap/glyphicons-halflings-regular.woff2", "3.3.5", false);
		super.initial();
	}
}
