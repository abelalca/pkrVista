var app = angular.module('app', []);

app.controller('mainCtl', function($scope, $http) {

	$scope.infoTiempoReal = function() {
		$http.get('/pruebas/infoRealTime').then(function(response) {			
			var jres = response.data;
			$scope.imgTmp = "";
			$scope.res = angular.toJson(jres, true);
			$http.get('/pruebas/obtenerImg').then(function(result) {
				var img= result.data;				

				var random = (new Date().getTime()).toString();
				$scope.imgTmp = img;			
			});
		});
	};

	$scope.guardarInfo = function() {
		 $http.get('/pruebas/guardarInfo').then(function(response) {
			 if(response){
				 
			 }
		});
	};

});