var exec = require('cordova/exec');

exports.start = function(success,error){
	exec(success, error, 'MapBox', 'start', [""]);	
};

exports.stop = function(success,error){
	exec(success, error, 'MapBox', 'stop', [""]);	
};

exports.getMetaData = function(success,error){
	exec(success, error, 'MapBox', 'getMetaData', [""]);	
};

exports.test = function(success,error){
	exec(success, error, 'MapBox', 'test', [""]);	
};