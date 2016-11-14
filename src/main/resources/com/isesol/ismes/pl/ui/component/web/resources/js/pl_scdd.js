'use strict';

angular.module('angularGanttDemoApp', [
    'gantt', // angular-gantt.
    'gantt.movable',
    'gantt.drawtask',
    'gantt.tooltips',
    'gantt.bounds',
    'gantt.progress',
    'gantt.table',
    'gantt.tree',
    'gantt.groups',
    'gantt.resizeSensor',
    'ngAnimate',
    'mgcrea.ngStrap'
]).config(['$compileProvider', function($compileProvider) {
    $compileProvider.debugInfoEnabled(false); // Remove debug info (angularJS >= 1.3)
}]);

'use strict';
angular.module('angularGanttDemoApp')
.directive('onlyDigits', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attr, ctrl) {
          function inputValue(val) {
            if (val) {
              var digits = val.replace(/[^0-9]/g, '');

              if (digits !== val) {
                ctrl.$setViewValue(digits);
                ctrl.$render();
              }
              return parseInt(digits,10);
            }
            return undefined;
          }            
          ctrl.$parsers.push(inputValue);
        }
      }});


'use strict';

angular.module('angularGanttDemoApp')
    .controller('MainCtrl', ['$scope', '$timeout', '$log', '$filter', 'ganttUtils', 'ganttMouseOffset', 'ganttDebounce', 'moment', '$http', function($scope, $timeout, $log, $filter, utils, mouseOffset, debounce, moment, $http) {
    	// 零件图片
    	ui.image("image_lj").attr("src", ui.hidden('pic_url').val());
    	ui.image("image_lj").init({});
    	$scope.pcjhztmc = ui.hidden('pcjhztmc').val();
    	
    	var dataToRemove;
        
        // 任务窗口遮罩是否显示
        $scope.editedTask = '';
        var backupTasks = {};
        var backupGx = [];
        var savings = {};
        $scope.minNum = 3;
        $scope.maxNum = 50;
        // 排产后的开始日期和结束日期
        $scope.quantity = '';
        $scope.minFrom = undefined;
        $scope.maxTo = undefined;
        // 是否为整单输出
        $scope.whole = 'true';
        $scope.copiedGdbh = '';
        $scope.changeSplitBill = function() {
        	if ($scope.editedTask.model.splitBill) {
        		$scope.editedTask.model.splitBillId = $scope.copiedGdbh;
        	} else {
        		$scope.editedTask.model.splitBillId = '';
        	}
        };
        
        $scope.confirmDisplay = function() {
        	$scope.api.tree.expandAll();
        	if ($scope.filterRowModel == "1") {
        		$scope.options.filterRow = "";
        	} else if ($scope.filterRowModel == "2") {
        		$scope.api.tree.collapseAll();
        	} else if ($scope.filterRowModel == "3") {
        		var selected = false;
        		for (var i = 0; i < $scope.data.length; i++) {
        			if ($scope.data[i].ck && $scope.data[i].drawTask) {
        				selected = true;
        				break;
        			}
        		}
        		if (selected) {
        			$scope.options.filterRow = true;
        		} else {
        			ui.alert('无选中设备');
        			return;
        		}
        	}
        	if ($scope.fromDayTemp === undefined || $scope.fromDayTemp == null) {
        		ui.alert('查询条件中的开始日期格式错误');
        		return;
        	}
        	if ($scope.toDayTemp === undefined || $scope.toDayTemp == null) {
        		ui.alert('查询条件中的结束日期格式错误');
        		return;
        	}
        	if (moment($scope.fromDayTemp).isAfter($scope.toDayTemp)) {
        		ui.alert('查询条件中的开始日期不能大于完成日期');
        		return;
        	}
        	var hasChange = false;
        	if (!moment($scope.options.fromDate).isSame($scope.fromDayTemp)) {
        		$scope.options.fromDate = $scope.fromDayTemp;
        		hasChange = true;
        	}
        	if (!moment($scope.options.toDate).isSame($scope.toDayTemp)) {
        		$scope.options.toDate = $scope.toDayTemp;
        		hasChange = true;
        	}
        	if (hasChange) {
        		$scope.load();
        	} else {
        		$scope.options.scale = $scope.options.scale_temp;
        	}
        }
        
        // 初始化页面选项
        // 弹出菜单
        $scope.menuOptions = [
                              {label : "修改", value : "modify"},
                              {label : "删除", value : "delete"},
                              {label : "复制工单编号", value : "copy"}
                          ];
        $scope.timeScale = [
                             {label : "小时", value : "hour"},
                             {label : "天", value : "day"},
                             {label : "周", value : "week"},
                             {label : "月", value : "month"},
                             {label : "季度", value : "quarter"},
                             {label : "半年", value : "6 months"},
                             {label : "年", value : "year"}
                         ];
        $scope.filterRowTemp = [
                            {label : "全部显示", value : "1"},
                            {label : "全部隐藏", value : "2"},
                            {label : "仅显示选中", value : "3"},
                        ];
        $scope.filterRowModel = "1";
        $scope.workOrderTransfer = [
                            {label : "整单", value : true},
                            {label : "流水", value : false}
                        ];
        // Event handler
        var handleTaskEvent = function(eventName, task) {
        	$scope.hideMenu();
        	console.log(eventName);
            if (eventName == 'tasks.on.resizeEnd' || eventName == 'tasks.on.moveEnd') {
            	$scope.editedTask = task.clone();
            	if(!checkTask(task)) {
            		// 删除编辑的task, 不刷新备份数据
            		deleteEditedTask(false);
            		if (!task.model.isNew) {
            			// 得到备份的task(用于还原task)
            			var foundBackups = backupTasks[task.model.id];
            			var backup = undefined;
            			for (var i = 0; i < foundBackups.length; i++) {
            				if (foundBackups[i].rowid == task.row.model.gxid + "-" + task.row.model.name) {
            					// 找到备份
            					backup = foundBackups[i];
            					break;
            				}
            			}
            			for (var i = 0; i < $scope.data.length; i++) {
            				// 设备所在的工序相同并且设备所在的能力组相同
            				if ($scope.data[i].sbid == task.row.model.sbid) {
            					var newData = angular.copy(backup);
            					// 还原到原设备
            					if (newData.task.gxid == $scope.data[i].gxid) {
            						$scope.data[i].tasks.push(newData.task);
            					} else {
            						newData.task.disabled = true;
            						newData.task.color = "#d6d6d6";
            						$scope.data[i].tasks.push(newData.task);
            					}
            				}
            			}
            		}
            	} else {
            		calculateQuantity(task.row.model, task.model);
            		if (task.model.isNew) {
            			task.model.gxid = task.row.model.gxid;
            		}
            		var currentDeviceTask = $scope.editedTask.model;
            		currentDeviceTask.disabled = true;
            		currentDeviceTask.color = "#d6d6d6";
            		currentDeviceTask.gxid = task.row.model.gxid;
            		currentDeviceTask.num = task.model.num;
            		currentDeviceTask.name = task.model.name;
            		
            		// 处理相同设备在不同工序的场景的设备数据同步
            		for (var i = 0; i < $scope.data.length; i++) {
        				if (task.row.model.sbid == $scope.data[i].sbid && $scope.data[i].name != task.row.model.name) {
        					if (task.model.isNew) {
        						$scope.data[i].tasks.push(currentDeviceTask);
        					} else {
        						if ($scope.data[i].drawTask) {
        							// 设备
        							for (var j = 0; j < $scope.data[i].tasks.length; j++) {
            							if ($scope.data[i].tasks[j].id == task.model.id) {
            								$scope.data[i].tasks[j] = currentDeviceTask;
            							}
            						}
        						}
        					}
        				}
        			}
            		// 将task标记为不是新创建的
                	task.model.isNew = false;
            	}
        		// 刷新备份的tasks
        		recalculate();
            }
        };
        
        // 在修改工单弹出窗口中校验工单时间
        $scope.checkTaskOnModal = function(dest) {
        	if (!checkTask($scope.editedTask)) {
        		for (var i=0; i<$scope.data.length; i++) {
        			if ($scope.data[i].id == $scope.editedTask.row.model.id) {
        				for (var j = 0; j < $scope.data[i].tasks.length; j++) {
        					if ($scope.data[i].tasks[j].id == $scope.editedTask.model.id) {
        						// 还原task数据
        						if (dest == 'from') {
            						$scope.editedTask.model.from = $scope.data[i].tasks[j].from;
        						} else {
        							$scope.editedTask.model.to = $scope.data[i].tasks[j].to;
        						}
        						break;
        					}
        				}
        			}
        		}
        	} else {
        		// 校验通过就重新计算加工数量
        		calculateQuantity($scope.editedTask.row.model, $scope.editedTask.model);
        	}
        }
        
        var calculateQuantity = function(rowModel, taskModel) {
        	var jgjp = rowModel.jgjp;
        	for (var i = 0; i < rowModel.tasks.length; i++) {
        		if (!rowModel.tasks[i].overlap) {
        			// 过滤掉工单
        			continue;
        		}
        		// 设备占用
        		if (moment(taskModel.from).isBetween(rowModel.tasks[i].from, rowModel.tasks[i].to) 
        				&& moment(taskModel.to).isBetween(rowModel.tasks[i].from, rowModel.tasks[i].to)) {
        			taskModel.num = 0;
        			taskModel.name = 0;
        			return;
        		}
        	}
        	var milliseconds = moment(taskModel.to).diff(moment(taskModel.from));
        	var mtMs = getMaintenanceMS(rowModel.tasks, taskModel.from, taskModel.to);
        	taskModel.num = parseInt((milliseconds - mtMs) / 1000 / jgjp);
        	taskModel.name = taskModel.num;
        }
        
        var getMaintenanceMS = function(mts, from, to) {
    		if (mts === undefined || mts.length == 0) {
    			return 0;
    		}
    		var suitables = [];
    		for (var i = 0; i < mts.length; i++) {
    			if (moment(mts[i].to).isBefore(from) 
    					|| moment(mts[i].to).isSame(from) 
    					|| moment(mts[i].from).isAfter(to) 
    					|| moment(mts[i].from).isSame(to) || !mts[i].overlap) {
    				// 略过不在指定时间段的工单占用
    				continue;
    			}
    			suitables.push(mts[i]);
    		}
    		if (suitables.length == 0) {
    			return 0;
    		}
    		var result = 0;
    		for (var i = 0; i < suitables.length; i++) {
    			var mt = suitables[i];
    			if (moment(from).isBetween(mt.from, mt.to)) {
    				result += moment(mt.to).diff(from);
    				continue;
    			}
    			if (moment(to).isBetween(mt.from, mt.to)) {
    				result += moment(to).diff(mt.from);
    				continue;
    			}
    			result += moment(mt.to).diff(mt.from);
    		}
    		return result;
    	}
        
        // 检查task是否合法
        var checkTask = function(task) {
        	if (task.model.from === undefined || task.model.from == null) {
        		ui.alert('加工开始时间格式错误');
        		return false;
        	}
        	if (task.model.to === undefined || task.model.to == null) {
        		ui.alert('加工完成时间格式错误');
        		return false;
        	}
        	if (moment(task.model.from).isAfter(task.model.to)) {
        		ui.alert('加工开始时间不能大于加工完成时间');
        		return false;
        	}
        	var tasks = task.row.model.tasks;
        	for (var i = 0; i < tasks.length; i++) {
        		if (tasks[i].id == task.model.id || tasks[i].overlap) {
        			// 如果是当前判断的task
        			continue;
        		}
        		// 判断当前task是否与其他task有时间上的交集
        		if (moment(tasks[i].from).isBetween(task.model.from, task.model.to)) {
        			ui.alert('与其他工单时间有重叠');
            		return false;
            	}
            	if (moment(tasks[i].to).isBetween(task.model.from, task.model.to)) {
            		ui.alert('与其他工单时间有重叠');
            		return false;
            	}
            	if (moment(task.model.from).isBetween(tasks[i].from, tasks[i].to)) {
            		ui.alert('与其他工单时间有重叠');
            		return false;
            	}
            	if (moment(task.model.to).isBetween(tasks[i].from, tasks[i].to)) {
            		ui.alert('与其他工单时间有重叠');
            		return false;
            	}
        	}
        	return true;
        }

        // Event handler
        var logReadyEvent = function() {
            $log.info('[Event] core.on.ready');
        };

        // Event utility function
        var addEventName = function(eventName, func) {
            return function(data) {
                return func(eventName, data);
            };
        };
        
        var hasDeletedOrder = false;
        
        var deleteEditedTask = function (flag) {
    		// 删除点击的Task
        	dataToRemove = [];
        	for (var i = 0; i < $scope.data.length; i++) {
        		if ($scope.data[i].drawTask && $scope.data[i].tasks.length > 0) {
        			// 设备
        			for (var j = 0; j < $scope.data[i].tasks.length; j++) {
        				if ($scope.data[i].tasks[j].id == $scope.editedTask.model.id) {
        					var rowid = $scope.data[i].id;
        					var taskid = $scope.data[i].tasks[j].id;
        					dataToRemove.push({'id':rowid, 'tasks': [{'id': taskid}]});
        					hasDeletedOrder = true;
        				}
        			}
        		}
        	}
        	$scope.api.data.remove(dataToRemove);
        	dataToRemove = undefined;
        	$scope.api.groups.refresh();
        	if (flag) {
        		recalculate();
        	}
        }
        
        // 用来调整默认显示时间段的，如果没有他并且没有task的话，甘特图日期头不显示且不能新拖拽
        var ksrq  = moment(ui.hidden('pcjhksrq').val() + " 00:00");
        var jsrq = moment(ui.hidden('pcjhwcrq').val() + " 00:00");
        
        var today = new Date();
        var fromDay = ksrq;
        var toDay = jsrq;
        
        $scope.fromDayTemp = ksrq;
        $scope.toDayTemp = jsrq;
        
        // angular-gantt options
        $scope.options = {
            mode: 'custom',
            scale: 'day',
            scale_temp: 'day',
            sideMode: 'TreeTable',
            daily: false,
            maxHeight: 610,
            zoom: 1,
            tooltip:'<small>零件名称:  {{task.model.part}}<br/>批次编号:  {{task.model.pcbh}}<br/>工单编号:  {{task.model.gdbh}}<br/>工单状态:  {{task.model.gdztmc}}<br/>加工数量:  {{task.model.num}}<br/>开始时间:  {{task.model.from.format("MM-DD HH:mm")}}<br/>结束时间:  {{task.model.to.format("MM-DD HH:mm")}}</small>',
            columnsContents:{'model.gx': '{{getValue()}}'},
            treeContent:'<input type="checkbox" ng-model="row.model.ck" ng-click="scope.checkCb(row.model.gxid, row.model.name, row.model.children, row.model.ck)"/>  {{row.model.label}} {{row.model.useRatio}}',
            treeTableColumns: ['model.gx'],
            columnsHeaders: {'model.gx': '工序'},
            treeHeaderContent: '能力组设备',
            autoExpand: 'none',
            taskOutOfRange: 'truncate',
            fromDate: fromDay,
            toDate: toDay,
            allowSideResizing: false,
            labelsEnabled: true,
            currentDate: 'line',
            currentDateValue: today,
            draw: true,
            movable: function(event, task) {
            	if (task.model.gxid != task.row.model.gxid) {
            		// 相同设备挂在不同的工序下
            		return false;
            	}
            	if (task.model.disabled === undefined) {
            		return true;
            	}
            	if (task.model.disabled) {
            		return false;
            	}
            	return true;
            },
            groupDisplayMode: 'Disabled',
            filterTask: '',
            filterRow: '',
            timeFrames: {
                'day': {
                    start: moment('8:00', 'HH:mm'),
                    end: moment('20:00', 'HH:mm'),
                    color: '#ACFFA3',
                    working: true,
                    default: true
                },
                'noon': {
                    start: moment('12:00', 'HH:mm'),
                    end: moment('13:30', 'HH:mm'),
                    working: false,
                    default: true
                },
                'closed': {
                    working: false,
                    default: true
                },
                'weekend': {
                    working: false
                },
                'holiday': {
                    working: false,
                    color: 'red',
                    classes: ['gantt-timeframe-holiday']
                }
            },
            dateFrames: {
                'weekend': {
                    evaluator: function(date) {
                        return date.isoWeekday() === 6 || date.isoWeekday() === 7;
                    },
                    targets: ['weekend']
                },
                '11-november': {
                    evaluator: function(date) {
                        return (date.month() === 10 && date.date() === 11) || date.month() === 6 && date.date() === 6;
                    },
                    targets: ['holiday']
                }
            },
            // 工作日时间显示样式
            timeFramesWorkingMode: 'hidden',
            timeFramesNonWorkingMode: 'hidden',
            columnMagnet: '15 minutes',
            timeFramesMagnet: true,
            canDraw: function(event) {
                var isLeftMouseButton = event.button === 0 || event.button === 1;
                return $scope.options.draw && isLeftMouseButton;
            },
            drawTaskFactory: function() {
                return {
                    id: utils.randomUuid(),  // Unique id of the task.
                    name: '手工工单', // Name shown on top of each task.
                    color: '#F5C24F', // Color of the task in HEX format (Optional).
                    part: ui.hidden('ljmc').val(),
                    pcid:ui.hidden('pcid').val(),
                    isNew: true,
                    zindex:50, // 设备占用40， 工单50
                    num:0
                };
            },
            api: function(api) {
                // API Object is used to control methods and events from angular-gantt.
                $scope.api = api;

                api.core.on.ready($scope, function() {
                    // Log various events to console
                    api.core.on.ready($scope, logReadyEvent);

                    if (api.tasks.on.moveBegin) {
                    	api.tasks.on.moveBegin($scope, addEventName('tasks.on.moveBegin', handleTaskEvent));
                        api.tasks.on.moveEnd($scope, addEventName('tasks.on.moveEnd', handleTaskEvent));
                        api.tasks.on.moveBegin($scope, addEventName('tasks.on.resizeBegin', handleTaskEvent));
                        api.tasks.on.resizeEnd($scope, addEventName('tasks.on.resizeEnd', handleTaskEvent));
                    }

                    $scope.load();

                    // Add some DOM events
                    api.directives.on.new($scope, function(directiveName, directiveScope, element) {
                        if (directiveName === 'ganttTask') {
                            element.bind('click', function(event) {
                            	event.preventDefault();
                            });
                            element.bind('contextmenu', function(event) {
                            	event.preventDefault();
                            	if (!directiveScope.task.model.disabled) {
                            		popMenu(event, directiveScope.task);
                            	}
                        	});
                            element.bind('dblclick', function(event) {
                            	event.preventDefault();
                        	});
                        } else if (directiveName === 'ganttRowLabel') {
                            element.bind('click', function() {
                                logRowEvent('row-label-click', directiveScope.row);
                            });
                        }
                    });
                });
            }
        };
        
        $scope.hideMenu = function() {
        	document.getElementById("contextMenu").style.display='none';
        }
        
        $scope.showTaskModal = function() {
        	ui.modal("modifyWorkOrderModal").show();
        }
        
        $scope.hideTaskModal = function(key) {
        	if (key == 'confirm') {
        		// 找到相应的task
        		for (var i=0; i<$scope.data.length; i++) {
        			if ($scope.data[i].drawTask && $scope.editedTask.row.model.sbid == $scope.data[i].sbid) {
        				for (var j = 0; j<$scope.data[i].tasks.length; j++) {
        					if ($scope.editedTask.row.model.sbid != $scope.data[i].sbid) {
        						continue;
        					}
        					if ($scope.data[i].tasks[j].id == $scope.editedTask.model.id) {
        						var tempTask = angular.copy($scope.editedTask.model);
        						// 标签显示加工数量
        						tempTask.name = tempTask.num;
            					if (tempTask.gxid != $scope.data[i].gxid) {
            						tempTask.disabled = true;
            						tempTask.color = "#d6d6d6";
            					}
            					$scope.data[i].tasks[j] = tempTask;
        					}
        				}
        			}
        		}
        		recalculate();
        	}
        	ui.modal("modifyWorkOrderModal").hide();
        }
        
        var popMenu = function(event, task) {
        	$scope.editedTask = task.clone();
        	document.getElementById("contextMenu").style.top = (event.pageY - $(".container")[2].offsetTop - 150)+ 'px';
        	document.getElementById("contextMenu").style.left = (event.pageX - $(".container")[2].offsetLeft) + 'px';
        	document.getElementById("contextMenu").style.display='block';
        }
        
        $scope.handleMenuClick = function(value) {
        	if (value == 'delete') {
        		deleteEditedTask(true);
        	}
        	if (value == 'modify') {
        		$scope.showTaskModal();
        	}
        	if (value == 'copy') {
        		if ($scope.editedTask.model.gdbh === undefined || $scope.editedTask.model.gdbh == '') {
        			ui.alert('该工单尚未保存，无法复制');
        		} else {
        			$scope.copiedGdbh = $scope.editedTask.model.gdbh;
        		}
        	}
        	$scope.hideMenu();
        }
        
        // Event handler
        var logRowEvent = function(eventName, row) {
            $log.info('[Event] ' + eventName + ': ' + row.model.name);
        };
        
        $scope.checkCb = function(gxid, name, children, ck) {
        	if(children !== undefined) {
        		// 点击能力组
        		for (var i=0; i<$scope.data.length; i++) {
        			if ($scope.data[i].children === undefined) {
        				// 设备
        				for (var j=0; j < children.length; j++) {
        					if ($scope.data[i].name == children[j] && gxid == $scope.data[i].gxid) {
        						$scope.data[i].ck = ck;
        					}
        				}
        			}
        		}
        	} else {
        		// 点击设备
        		var foundedChildren = [];
        		var childrenCk = [];
        		var index;
        		// 一定是先找到该设备相应的能力组
        		for (var i=0; i<$scope.data.length; i++) {
        			if ($scope.data[i].children !== undefined) {
        				if (foundedChildren.length != 0) {
        					// 遍历到新的能力组，关心的能力组和能力组的设备已经处理过了，跳出循环
        					break;
        				}
        				for (var j=0; j< $scope.data[i].children.length; j++) {
        					if (name == $scope.data[i].children[j] && gxid == $scope.data[i].gxid) {
        						// 匹配到能力组
        						foundedChildren = $scope.data[i].children;
        						index = i;
        						break;
        					}
        				}
        			} else {
        				if (foundedChildren.length != 0) {
        					// 匹配到能力组后，接着遍历的是该能立组下的所有设备
        					childrenCk.push($scope.data[i].ck);
        				}
        			}
        		}
        		var result = childrenCk[0];
        		for (var i=1; i < childrenCk.length; i++) {
        			// 根据能力组下所有设备的状态决定能力组的状态
        			result = result || childrenCk[i];
        		}
        		$scope.data[index].ck = result;
        	}
        	// 根据都选框状态改变试排按钮文字
        	if (ck) {
        		ui.button("trySchedule").attr("label",'试排选中');
        	} else {
        		var found = false;
        		for (var i=0; i < $scope.data.length; i++) {
        			if ($scope.data[i].ck) {
        				found = true;
        				break;
        			}
        		}
        		if (found) {
        			ui.button("trySchedule").attr("label",'试排选中');
        		} else {
        			ui.button("trySchedule").attr("label",'全部试排');
        		}
        	}
        }

        $scope.canAutoWidth = function(scale) {
            if (scale.match(/.*?hour.*?/) || scale.match(/.*?minute.*?/)) {
                return false;
            }
            return true;
        };
        
        $scope.getColumnWidth = function(widthEnabled, scale, zoom) {
            if (!widthEnabled && $scope.canAutoWidth(scale)) {
                return undefined;
            }

            if (scale.match(/.*?week.*?/)) {
                return 150 * zoom;
            }

            if (scale.match(/.*?month.*?/)) {
                return 300 * zoom;
            }

            if (scale.match(/.*?quarter.*?/)) {
                return 500 * zoom;
            }

            if (scale.match(/.*?year.*?/)) {
                return 800 * zoom;
            }

            return 40 * zoom;
        };

        // Reload data action
        $scope.load = function() {
        	var loading = new ol.loading({id:"ganttAppDiv"});
    		loading.show();
        	 $http({
				method:'GET',
				url:'loadSchedule',
				params:{
				'pcid':ui.hidden('pcid').val(),
				'ljid':ui.hidden('ljid').val(),
				'ljmc':ui.hidden('ljmc').val(),
				'fromDate':moment($scope.options.fromDate).format("YYYY-MM-DD"),
				'toDate':moment($scope.options.toDate).format("YYYY-MM-DD"),
				'sbids':getSelectedSbids(),
				'scale':$scope.options.scale_temp
				}})
             .then(function successCallback(response) {
            	 $timeout(function() {
                  	$scope.api.side.setWidth(undefined);
                  });
            	 $scope.data = response.data;
        		 // 备份数据
                 recalculate();
                 dataToRemove = undefined;
                 hasDeletedOrder = false;
                 $scope.options.scale = $scope.options.scale_temp;
                 calDeviceUseRatio();
                 loading.hide();
             	}, function errorCallback(response) {
             		loading.hide();
             		ui.error('数据加载失败');
             	}
             );
        };
        
        var getSelectedSbids = function() {
        	if ($scope.data === undefined) {
        		return '';
        	}
    		 var sbids = '';
	       	 // 得到选中的设备
	       	 for (var i = 0; i < $scope.data.length; i++) {
	       		 if (!$scope.data[i].drawTask) {
	       			 // 略过能力组
	       			 continue;
	       		 }
	       		 if ($scope.data[i].ck) {
	       			 sbids += "," + $scope.data[i].gxid + "-" + $scope.data[i].sbid;
	       		 }
	       		 
	       	 }
	       	 if (sbids != '') {
	       		sbids = sbids.substring(1);
	       	 }
	       	 return sbids;
        }
        
        /**
         * 检查是否有所有工序都有至少一个设备选中
         */
        var checkGx = function(gxsbids) {
        	for (var i = 0; i < backupGx.length; i++) {
        		var gxid = backupGx[i].gxid;
        		var found = false;
        		var gxsbidArr = gxsbids.split(",");
        		for (var j = 0; j < gxsbidArr.length; j++) {
        			if (gxid == gxsbidArr[j].substring(0, gxsbidArr[j].indexOf("-"))) {
        				found = true;
        				break;
        			}
        		}
        		if (!found) {
        			ui.alert(backupGx[i].gxmc + "下至少需要有一台设备被选中");
        			return false;
        		}
        	}
        	return true;
        }
        
        $scope.goBack = function() {
        	// 跳转到计划排产页面
			location.href="../taskbatch/index";
        }
        
        $scope.trySchedule = function() {
        	var gxsbids = getSelectedSbids();
        	if (gxsbids != '') {
        		// 有选中的设备
        		if (!checkGx(gxsbids)) {return;}
        	}
        	 $http({
				method:'GET',
				url:'trySchedule',
				params:{
				'pcid':ui.hidden('pcid').val(),
				'pcbh':ui.hidden('pcbh').val(),
				'pcsl':ui.hidden('pcsl').val(),
				'ljid':ui.hidden('ljid').val(),
				'ljmc':ui.hidden('ljmc').val(),
				'kssj':ui.hidden('kssj').val(),
				'fromDate':moment($scope.options.fromDate).format("YYYY-MM-DD"),
				'toDate':moment($scope.options.toDate).format("YYYY-MM-DD"),
				'minNum':$scope.minNum,
				'maxNum':$scope.maxNum,
				'whole':$scope.whole,
				'sbids':gxsbids,
				'scale':$scope.options.scale_temp
				}})
             .then(function successCallback(response) {
            	 $timeout(function() {
                  	$scope.api.side.setWidth(undefined);
                  });
            	 $scope.data = response.data;
        		 // 备份数据
                 recalculate();
                 dataToRemove = undefined;
                 ui.success("试排成功！");
             	}, function errorCallback(response) {
             		ui.error("试排失败！");
             	}
             );
        };
        
        $scope.save = function(type) {
        	// 准备提交的数据
        	savings = {};
        	savings["pcid"] = ui.hidden('pcid').val();
        	savings["pcbh"] = ui.hidden('pcbh').val();
        	savings["ljid"] = ui.hidden('ljid').val();
        	savings["ljmc"] = ui.hidden('ljmc').val();
        	savings["ljbh"] = ui.hidden('ljbh').val();
        	savings["gd"] = [];
        	
        	var dealtSbids = {};
        	
        	for (var i = 0; i < $scope.data.length; i++) {
        		var tasks = $scope.data[i].tasks;
        		if ($scope.data[i].drawTask && tasks !== undefined) {
        			if (dealtSbids[$scope.data[i].sbid] === undefined) {
        				dealtSbids[$scope.data[i].sbid] = true;
        			} else {
        				continue;
        			}
        			for (var j = 0; j < tasks.length; j++) {
        				if(tasks[j].pcid !== undefined && tasks[j].pcid == ui.hidden('pcid').val()) {
        					var gdid = tasks[j].gdId;
        					var gdbh = tasks[j].gdbh;
        					var gxid = tasks[j].gxid;
        					var sbid =  $scope.data[i].sbid;
        					var zzjgid =  $scope.data[i].zzjgid;
        					var jgsl = tasks[j].num;
        					var gdztdm = tasks[j].gdztdm;
        					if (parseInt(jgsl) == 0){
        						ui.error('本批次有加工数量为0的工单，无法保存');
        						return;
        					}
        					var fgdbh = tasks[j].splitBillId; // 父工单编号
        					var jhkssj = moment(tasks[j].from).format('YYYY-MM-DD HH:mm');
        					var jhjssj = moment(tasks[j].to).format('YYYY-MM-DD HH:mm');
        					var zbsj = tasks[j].zbsj;
        					savings["gd"].push({"gxid":gxid, "sbid":sbid, "zzjgid":zzjgid, "jgsl":jgsl, "jhkssj":jhkssj, "jhjssj":jhjssj, "fgdbh":fgdbh, "gdid":gdid, "gdbh":gdbh , "zbsj":zbsj, "gdztdm":gdztdm});
        				}
        			}
        		}
        	}
        	
        	if (savings["gd"].length == 0 && !hasDeletedOrder) {
        		ui.info("无可提交的变更");
        		return;
        	}
        	
        	$http({
				method:'POST',
				url:'saveSchedule',
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},
				data : savings
				})
             .then(function successCallback(response) {
            	 ui.success('工单保存成功');
            	 // 重新加载甘特图数据
            	 $scope.load();
             	}, function errorCallback(response) {
             		 ui.error('工单保存失败');
             	}
             );
        }
        
        var recalculate = function(isFromDelete) {
        	$scope.minFrom = undefined;
        	$scope.maxTo = undefined;
        	backupTasks = {};
        	backupGx = [];
        	for (var i = 0; i < $scope.data.length; i++) {
        		// 刷新工序信息
        		if ($scope.data[i].gx !== undefined && $scope.data[i].gx != '') {
        			backupGx.push({"gxid":$scope.data[i].gxid, "gxmc":$scope.data[i].gx});
        		}
        		var tasks = $scope.data[i].tasks;
        		if ($scope.data[i].drawTask && tasks !== undefined) {
        			for (var j = 0; j < tasks.length; j++) {
        				if (backupTasks[tasks[j].id] === undefined) {
        					backupTasks[tasks[j].id] = [];
        				}
        				// 可能出现多个相同id的工单，所以使用数组存储。rowid 工序id+能力组id+设备id
        				backupTasks[tasks[j].id].push({"rowid":$scope.data[i].gxid+"-"+ $scope.data[i].name, "task":angular.copy(tasks[j])});
        				if(tasks[j].pcid !== undefined && tasks[j].pcid == ui.hidden('pcid').val() && !tasks[j].overlap) {
        					// 计算本批次的所有工单的最小开始时间和最大结束时间
        					if ($scope.minFrom === undefined) {
        						$scope.minFrom = moment(tasks[j].from);
        						$scope.maxTo = moment(tasks[j].to);
        						continue;
        					}
        					if (moment(tasks[j].from).isBefore($scope.minFrom)) {
        						$scope.minFrom = moment(tasks[j].from);
        					}
        					if (moment(tasks[j].to).isAfter($scope.maxTo)) {
        						$scope.maxTo = moment(tasks[j].to);
        					}
        				}
        			}
        		}
        	}
        	
        	// 计算本批次数量
        	$scope.quantity = '';
        	var tempQuantity = {};
        	var dealtSbids = {};
        	for (var i = 0; i < $scope.data.length; i++) {
        		var tasks = $scope.data[i].tasks;
        		if ($scope.data[i].drawTask && tasks !== undefined) {
        			// 设备
        			if (dealtSbids[$scope.data[i].sbid] === undefined) {
        				dealtSbids[$scope.data[i].sbid] = true;
        			} else {
        				continue;
        			}
        			for (var j = 0; j < tasks.length; j++) {
        				if(tasks[j].pcid !== undefined && tasks[j].pcid == ui.hidden('pcid').val()) {
        					var gxid = tasks[j].gxid;
        					var jgsl = tasks[j].num;
        					if(tempQuantity[gxid] === undefined) {
        						tempQuantity[gxid] = parseInt(jgsl);
        					} else {
        						tempQuantity[gxid] += parseInt(jgsl);
        					}
        				}
        			}
        		}
        	}
        	for (var i = 0; i < backupGx.length; i++) {
        		if(tempQuantity[backupGx[i].gxid] === undefined) {
        			$scope.quantity += backupGx[i].gxmc + ':0 ';
        		} else {
        			$scope.quantity += backupGx[i].gxmc + ':' + tempQuantity[backupGx[i].gxid] + ' ';
        		}
        	}
        }
        
        function calDeviceUseRatio() {
        	var totalTime = jsrq.diff(ksrq);
        	// 计算设备利用率
        	for (var i = 0; i < $scope.data.length; i++) {
        		var tasks = $scope.data[i].tasks;
        		if ($scope.data[i].drawTask && tasks !== undefined) {
        			// 设备
        			var occupyTime = 0;
        			for (var j = 0; j < tasks.length; j++) {
        				if (tasks[j].overlap || tasks[j].gdId == '') {
        					continue;
        				}
        				var from = moment(tasks[j].from);
        				var to = moment(tasks[j].to);
        				var fromStr = from.format('YYYY-MM-DD HH:mm');
        				var toStr = to.format('YYYY-MM-DD HH:mm');
        				if (ksrq.isBetween(fromStr, toStr) && jsrq.isBetween(fromStr, toStr) ) {
        					occupyTime = totalTime;
        					break;
        				}
        				if (to.isBefore(ksrq) || to.isSame(ksrq)) {
        					continue;
        				}
        				if (from.isAfter(jsrq) || from.isSame(jsrq)) {
        					continue;
        				}
        				if (ksrq.isBetween(fromStr, toStr)) {
        					occupyTime += to.diff(ksrq);
        					continue;
        				}
        				if (jsrq.isBetween(fromStr, toStr)) {
        					occupyTime += jsrq.diff(from);
        					continue;
        				}
        				occupyTime += moment(tasks[j].to).diff(moment(tasks[j].from));
        			}
        			if (totalTime == 0) {
        				$scope.data[i].useRatio = "(0%)";
        			} else {
        				$scope.data[i].useRatio = ((occupyTime / totalTime).toFixed(2) * 100).toFixed(0) + '%';
        				$scope.data[i].useRatio = '(' + $scope.data[i].useRatio + ')';
        			}
        		}
        	}
        }
        
        function switchScale(newValue, oldValue, scope) {
        	if (newValue !== undefined && newValue == 'hour') {
            	//alert('把所有隐藏的设备占用转为显示');
            	for (var i = 0; i < $scope.data.length; i++) {
            		if ($scope.data[i].tasks === undefined) {
            			continue;
            		}
            		var tasks = $scope.data[i].tasks;
            		for (var j = 0; j < tasks.length; j++) {
            			if (tasks[j].classes == 'hidden') {
            				tasks[j].classes = 'device-occupy';
            			}
            		}
            	}
            }
            if (oldValue !== undefined && oldValue == 'hour') {
            	//alert('把所有的非整天的设备占用转为不显示');
            	for (var i = 0; i < $scope.data.length; i++) {
            		if ($scope.data[i].tasks === undefined) {
            			continue;
            		}
            		var tasks = $scope.data[i].tasks;
            		for (var j = 0; j < tasks.length; j++) {
            			if (tasks[j].classes == 'device-occupy') {
            				tasks[j].classes = 'hidden';
            			}
            		}
            	}
            }
        }
        
        // 监控时间维度的变化
        $scope.$watch('options.scale', switchScale);
//        $timeout(function() {
//        	$scope.api.tree.collapseAll();
//        }, 100);
    }]);
