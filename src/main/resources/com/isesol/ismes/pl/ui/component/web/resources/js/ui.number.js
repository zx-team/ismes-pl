ui.registerComponent("number",function(name){
	function Number($number) {
		this.$number = $number;
	}
	Number.prototype = {

		attr:function(name,value){
			var args = arguments.length;
			if(args==0 || args>2) {
				throw new Error("Number::attr() : invalid arguments length!")
			}
			switch(name) {
				case "label":return args==1?this.label():this.label(value);
				case "name":return this.$number.attr(name);
				case "disabled": return args==1?this.$number.attr("disabled"):this.$number.attr("disabled",value);
				case "value":return this.val(value);
				default:throw new Error("Number::attr() : invalid attribute name!")
			}
		},
		label:function(label) {
			var $label = this.$number.parents(".form-group").children("label");
			return arguments.length?$label.text(label):$label.text();
		},
		getLabel:function() {
			return this.$number.parents(".form-group").children("label").text();
		},
		bind:function(eventName,func) {
			var number = this;
			this.$number.bind(eventName,function(){func.call(number);});
		},
		unbind:function(eventName,func) {
			this.$number.bind(eventName);
		},
		val:function(value) {
			return arguments.length?this.$number.val(value):this.$number.val();
		}
	}
	var $number = $("#"+name);
	if($number.length!=1) {
		throw new Error("ui.number(name): Can't find valid DOM element by the name!");
	}
	var data = $number.data("ui.number");
	if(!data) {
		data = new Number($number);
		$number.data("ui.number",data);
	}
	return data;
});