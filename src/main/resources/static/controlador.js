var app = angular.module('app', []);

app
		.controller(
				'mainCtl',
				function($scope, $http, $timeout, $sce) {			
					
					obtenerNombresCuadrantes();
					
					
					function obtenerNombresCuadrantes() {
						$http.get('/captura/obtenerNombresCuadrantes').then(
								function(response) {
									if (response) {
										$scope.cuadrantes = response.data;
									}
								});
					}

					$scope.infoTiempoReal = function() {
						$http.get('/pruebas/infoRealTime').then(
								function(response) {
									var jres = response.data;
									$scope.imgTmp = "";
									$scope.res = angular.toJson(jres, true);
									$http.get('/pruebas/obtenerImg').then(
											function(result) {
												var img = result.data;

												var random = (new Date()
														.getTime()).toString();
												$scope.imgTmp = img;
											});
								});
					};

					$scope.accionTiempoReal = function() {
						
						$http.get('/captura/mesaInfo').then(function(response) {
							if (response.data.hand) {
								pintarPantalla(response);
							}
							if ($scope.ciclar) {
								$scope.accionTiempoReal();
							}
						}, function(data) {
							$scope.accionTiempoReal();
						});
					};

					$scope.almacenarImagenCuadrante = function(cuadrante) {
						$http.get('/captura/almacenarImagenCuadrante', {
							params : {
								cuadrante : cuadrante
							}
						});
					};

					$scope.guardarInfo = function() {
						$http.get('/pruebas/guardarInfo').then(
								function(response) {
									if (response) {

									}
								});
					};

					$scope.accionDesdeImagen = function() {
						$http.get('/pruebas/accionDesdeImagen').then(
								function(response) {
									pintarPantalla(response);
								});

					}

					$scope.accionDesdeJson = function() {
						$http.get('/pruebas/accionDesdeJson').then(
								function(response) {
									pintarPantalla(response);
								});

					}

					$scope.iniciarAsesor = function() {
						$scope.ciclar = true;
						if ($scope.ciclar) {
							$scope.accionTiempoReal();
						}

					}

					$scope.dispararShoot = function() {
						$http.get('/captura/mesaInfo').then(function(response) {
							pintarPantalla(response);
							$scope.ciclar = false;
						});
					}

					$scope.detener = function() {
						$scope.ciclar = false;
					}

					$scope.limpiarRangos = function() {
						$scope.rango = "";
						angular.copy($scope.origHands, $scope.allHands);
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
						if (defAccion == "R" || defAccion == "O"
								|| defAccion == "S" || defAccion == "P") {
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

					function pintarPantalla(response) {
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
							$scope.res.hand = pintarHand($scope.res.hand);
							$scope.tiempo = $scope.res.tiempo;

						}
					}

					function pintarHand(hand) {
						var palo1 = hand.substring(1, 2);
						if (palo1 == "s") {
							$scope.palo1 = $sce.trustAsHtml('&spades;');
						} else if (palo1 == "c") {
							$scope.palo1 = $sce.trustAsHtml('&clubs;');
						} else if (palo1 == "d") {
							$scope.palo1 = $sce.trustAsHtml('&diams;');
						} else if (palo1 == "h") {
							$scope.palo1 = $sce.trustAsHtml('&hearts;');
						}

						var palo2 = hand.substring(3, 4);
						if (palo2 == "s") {
							$scope.palo2 = $sce.trustAsHtml('&spades;');
						} else if (palo2 == "c") {
							$scope.palo2 = $sce.trustAsHtml('&clubs;');
						} else if (palo2 == "d") {
							$scope.palo2 = $sce.trustAsHtml('&diams;');
						} else if (palo2 == "h") {
							$scope.palo2 = $sce.trustAsHtml('&hearts;');
						}

						$scope.carta1 = hand.substring(0, 1)
						$scope.carta2 = hand.substring(2, 3)

						return hand;
					}

					$scope.clickHand = function(manod) {
						if (!$scope.rango) {
							$scope.rango = "";
						}

						if (manod.activo) {
							$scope.rango = $scope.rango.replace(manod.value
									+ ", ", "");
							manod.activo = false;
							manod.estilo = manod.estilo.replace(
									" background-color: #ff4d4d; ", "");
						} else {
							$scope.rango = $scope.rango + manod.value + ", ";
							manod.activo = true;
							manod.estilo = manod.estilo
									+ " background-color: #ff4d4d; ";
						}
					}

					$scope.allHands = {
						"1" : [
								{
									"value" : "AA",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "AKs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "AQs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "AJs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "ATs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A9s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A8s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A7s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A6s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A5s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A4s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A3s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "A2s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"2" : [
								{
									"value" : "AKo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "KK",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "KQs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "KJs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "KTs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K9s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K8s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K7s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K6s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K5s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K4s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K3s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K2s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"3" : [
								{
									"value" : "AQo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "KQo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "QQ",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "QJs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "QTs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q9s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q8s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q7s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q6s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q5s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q4s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q3s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q2s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"4" : [
								{
									"value" : "AJo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "KJo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "QJo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "JJ",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "JTs",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J9s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J8s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J7s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J6s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J5s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J4s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J3s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J2s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"5" : [
								{
									"value" : "ATo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "KTo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "QTo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "JTo",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "TT",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "T9s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T8s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T7s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T6s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T5s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T4s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T3s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T2s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"6" : [
								{
									"value" : "A9o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K9o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q9o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J9o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T9o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "99",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "98s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "97s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "96s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "95s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "94s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "93s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "92s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"7" : [
								{
									"value" : "A8o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K8o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q8o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J8o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T8o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "98o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "88",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "87s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "86s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "85s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "84s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "83s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "82s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"8" : [
								{
									"value" : "A7o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K7o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q7o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J7o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T7o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "97o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "87o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "77",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "76s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "75s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "74s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "73s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "72s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"9" : [
								{
									"value" : "A6o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K6o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q6o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J6o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T6o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "96o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "86o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "76o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "66",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "65s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "64s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "63s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "62s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"10" : [
								{
									"value" : "A5o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K5o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q5o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J5o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T5o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "95o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "85o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "75o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "65o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "55",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "54s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "53s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "52s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"11" : [
								{
									"value" : "A4o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K4o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q4o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J4o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T4o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "94o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "84o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "74o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "64o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "54o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "44",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "43s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "42s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"12" : [
								{
									"value" : "A3o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K3o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q3o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J3o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T3o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "93o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "83o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "73o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "63o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "53o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "43o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "33",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								},
								{
									"value" : "32s",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								} ],
						"13" : [
								{
									"value" : "A2o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "K2o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "Q2o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "J2o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "T2o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "92o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "82o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "72o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "62o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "52o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "42o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "32o",
									"estilo" : "padding: 1px; text-align: center; width: 50px;",
									"activo" : false
								},
								{
									"value" : "22",
									"estilo" : "padding: 1px; text-align: center; width: 50px; background-color : aquamarine;",
									"activo" : false
								} ]
					}

					$scope.origHands = angular.copy($scope.allHands);


				});

app.filter('reverse', function() {
	return function(items) {
		if (items) {
			return items.slice().reverse();
		} else {
			return null;
		}
	};
});