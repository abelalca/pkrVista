var app = angular.module('app', []);



app.controller('mainCtl', function($scope, $http) {

	$scope.infoTiempoReal = function() {
		$http.get('/pruebas/infoRealTime').then(function(response) {
			var jres = response.data;
			$scope.imgTmp = "";
			$scope.res = angular.toJson(jres, true);
			$http.get('/pruebas/obtenerImg').then(function(result) {
				var img = result.data;

				var random = (new Date().getTime()).toString();
				$scope.imgTmp = img;
			});
		});
	};

	$scope.guardarInfo = function() {
		$http.get('/pruebas/guardarInfo').then(function(response) {
			if (response) {

			}
		});
	};

	$scope.pruebaVista = function() {
		$http.get('/pruebas/pruebaVista').then(function(response) {
			if (response) {
				$scope.res = response.data;
				acciones = response.data.accionVsPlayer;
				var tipJug = [];
				var i = 0;
				var attb = [];
				for ( var index in acciones) {
					tipJug.push({});
					tipJug[i].tipo = index;
					tipJug[i].color = colorTipJug(index);
					attb[i] = acciones[index];
					i++;
				}
				$scope.tipJug = tipJug;
				$scope.anchoColum = tipJug.length;
				$scope.acc = attb;
				$scope.defAcc = defaultAccion($scope.res.defAccion);
			}

		});

	}

	function colorTipJug(tipo) {
		if (tipo == "DEF") {
			return "orange";
		}
		if (tipo == "FISH") {
			return "blue";
		}
		if (tipo == "REG") {
			return "red";
		}
		if (tipo == "GTO") {
			return "purple";
		}
		return "black";
	}

	function defaultAccion(defAccion) {
		if (defAccion == "R") {
			return "green";
		}
		if (defAccion == "L") {
			return "orange";
		}
		if (defAccion == "S") {
			return "red";
		}
		if (defAccion == "F") {
			return "gray";
		}
		return "black";
	}

});


app.filter('reverse', function() {
	return function(items) {
		return items.slice().reverse();
	};
});