define(['ojs/ojcore', 'jquery'], function(oj, $) {
	var configs = {};
	//console.log('Getting ords url: ' + window.location.protocol + "//" + window.location.host);
	var ordsUrl;
	var clientCredentials;
	requirejs.config({
		waitSeconds: 0
	});
	configs.contextUrl = window.location.protocol + "//" + window.location.host + "/imagingapptest/";
	//configs.contextUrl =  window.location.protocol + "//" + window.location.host +"/EcovueDashboardApp-EcovueDashboardProj-context-root/";
	configs.url = configs.contextUrl + "resources/";
	console.log("configs url: " + configs.url + 'userroles/configs');
	$.ajax({
		url: configs.url + 'userroles/configs',
		type: 'GET',
		dataType: 'json',
		async: false,
		success: function(data2) {
			//console.log('Success in configs: '+JSON.stringify(data2.items));
			var fields = data2.items;
			for (var i = 0; i < fields.length; i++) {
				configs[fields[i].configs_name] = fields[i].configs_value;
			}
		},
		error: function(data, e) {
			//window.location.href = configs.weblogicServerUrl+"dashboard/logout";
			console.log("Error: Configs Failed");
		}
	});
	
	return configs;
});