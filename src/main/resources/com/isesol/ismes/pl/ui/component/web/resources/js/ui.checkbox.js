ui.registerComponent("checkbox",function(name){
	function Checkbox($checkbox) {
		this.$checkbox = $checkbox;
	}
	Checkbox.prototype = {

		attr:function(name,value){
			var args = arguments.length;
			if(args==0 || args>2) {
				throw new Error("Checkbox::attr() : invalid arguments length!")
			}
			switch(name) {
				case "label":return args==1?this.label():this.label(value);
				case "name":return this.$checkbox.attr(name);
				case "disabled": return args==1?this.$checkbox.attr("disabled"):this.$checkbox.attr("disabled",value);
				case "value":return this.val(value);
				case "checked":return args==1?this.$checkbox.attr("checked"):value?this.$checkbox.iCheck('check'):this.$checkbox.iCheck('uncheck');
				default:throw new Error("Checkbox::attr() : invalid attribute name!")
			}
		},
		label:function(label) {
			var $label = this.$text.parents(".form-group").children("label");
			return arguments.length?$label.text(label):$label.text();
		},
		bind:function(eventName,func) {
			var checkbox = this;
			this.$checkbox.bind(eventName,function(){func.call(checkbox);});
			
		},
		unbind:function(eventName,func) {
			this.$checkbox.unbind(eventName);
		},
		val:function(value) {
			return arguments.length?this.$checkbox.val(value):this.$checkbox.val();
		},
		isChecked:function() {
			return this.$checkbox.is(":checked");
		},
		init:function(opts) {
			this.$checkbox.iCheck($.extend({ checkboxClass: 'icheckbox_minimal-grey'},opts));
		}
	}
	var $checkbox = $("#"+name);
	if($checkbox.length!=1) {
		throw new Error("ui.checkbox(name): Can't find valid DOM element by the name!");
	}
	var data = $checkbox.data("ui.checkbox");
	if(!data) {
		data = new Checkbox($checkbox);
		$checkbox.data("ui.checkbox",data);
	}
	return data;
});