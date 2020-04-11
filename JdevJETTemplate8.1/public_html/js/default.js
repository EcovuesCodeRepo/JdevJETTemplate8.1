define(['ojs/ojcore', 'knockout', 'configs', 'jquery'], function(oj, ko, configs, $) {
	function defaultValues(roleName) {
		//Declaration Variables
		var self = this;
		self.serviceURL = configs.baseURL + configs.schemaName + 'dashboard/';
		self.performancesTab = ko.observable(false);
		self.reportsPageFlag = ko.observable(false);
		self.setupPageFlag = ko.observable(false);
		self.dashboardPageFlag = ko.observable(false);
		self.myTasksFlag = ko.observable(false);
		self.roleName = ko.observable();
		self.userName = ko.observable();
		self.userDisplayName = ko.observable();
		self.userGroups = ko.observableArray();
		getUser();
		function getUser() {
			console.log('Getting User');
			var x = window.location.href.split('/');
			var baseURL = configs.url + "userroles/getUserName";
			console.log(baseURL);
			$.ajax({
				url: baseURL,
				type: "GET",
				dataType: "json",
				async: false,
				success: function(data) {
					console.log(data.userName);
					if (data.userName == 'null' || data.userName == null) {
						self.userName('pgummaraju');
						self.userDisplayName('Guest');
					}
					else {
						self.userName(data.userName);
						self.userDisplayName(data.userDisplayName);
						for (var i = 0; i < data.role.length; i++) {
							self.userGroups.push(data.role[i].display);
							
						}
					}
				},
				error: function(xhr, textStatus, errorThrown) {
					console.log('Erro while fetching User details - ' + errorThrown);
					self.userName('pgummaraju');
					self.userDisplayName('pgummaraju');
					
					}
				
			});
				}
        }
	return {
		fromUser: defaultValues
	};
});