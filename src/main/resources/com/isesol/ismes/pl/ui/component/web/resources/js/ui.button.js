(function(){
	if(ui.components["Button"]){
		return;
	}
	ui.define("Button",{
		extend:"FieldComponent",
		attr:function(name,value){
			var args = arguments.length;
			if(args==0 || args>2) {
				throw new Error("Button::attr() : invalid arguments length!")
			}
			switch(name) {
				case "label":return args==1?this.label():this.label(value);
				case "name":return this.getElement().attr(name);
				case "disabled": return args==1?this.getElement().attr("disabled"):this.getElement().attr("disabled",value);
				default:throw new Error("Button::attr() : invalid attribute name!")
			}
		},
		label:function(label) {
			var tagName = this.getElement().get(0).tagName;
			if("A"==tagName) {
				return arguments.length?this.getElement().text(label):this.getElement().text();
			} else if("INPUT"==tagName) {
				return arguments.length?this.getElement().attr("value",label):this.getElement().attr("value");
			}
		},
		on:function(eventName,func) {
			var btn = this;
			this.getElement().on(eventName,function(){func.call(btn);});
		},
		off:function(eventName,func) {
			this.getElement().off(eventName);
		},
		show:function(){
			this.getElement().removeClass("hidden");
		},
		hide:function(){
			this.getElement().addClass("hidden");
		}
	});
}());

