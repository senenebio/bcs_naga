'use strict';


var app = angular.module('mainApp', ['ngRoute', 'ui.bootstrap']); 


// configure routes
app.config(['$routeProvider', function($routeProvider) {
	$routeProvider.
	when('/bus_operation', {
		templateUrl: 'views/bus_operation.html',
		controller: 'MainCtrl'
	}).
	when('/arrival', {
		templateUrl: 'views/arrival.html',
		controller: 'ArrivalCtrl'
	}).
	when('/departure', {
		templateUrl: 'views/departure.html',
		controller: 'DepartureCtrl'
	}).
	when('/assessment', {
		templateUrl: 'views/assessment.html',
		controller: 'AssessmentCtrl'
	}).
	when('/bus_payment', {
		templateUrl: 'views/bus_payment.html',
		controller: 'BusPaymentCtrl'
	}).
	when('/approve', {
		templateUrl: 'views/approve.html',
		controller: 'ApprovalCtrl'
	}).
	when('/vehicle', {
		templateUrl: 'views/vehicle.html',
		controller: 'VehicleCtrl'
	}).
	otherwise({
		redirectTo: '/bus_operation'
	});
}]);

// use bearer token when calling backend
app.config(['$httpProvider', function($httpProvider) {
	//var isExpired = window._keycloak.isTokenExpired();
	//var token = window._keycloak.token;
	
	//if (isExpired) {
	//	window._keycloak.updateToken(5)
	//	.success(function() {
	//		$httpProvider.defaults.headers.common['Authorization'] = 'BEARER ' + token;
	//	})
	//	.error(function() {
	//		console.error('Failed to refresh token');
	//	});
	//}
	
	var token = "demo-security-token";
	$httpProvider.defaults.headers.common['Authorization'] = 'BEARER ' + token;
}]);

//custom directive for bootstrap datetime picker work with angular
app.directive('dateTimePicker',function() {
	return {
		restrict: "A",
		require: "ngModel",
		link: function (scope, element, attrs, ngModelCtrl) {
			var parent = $(element).parent();
			var dtp = parent.datetimepicker({
				format : 'YYYY-MM-DD HH:mm A',
				showTodayButton : true,
				//showClear : true,
				showClose : true,
				//sideBySide : true,
			});
			dtp.on("dp.change", function (e) {
				ngModelCtrl.$setViewValue(moment(e.date).format("YYYY-MM-DD HH:mm A"));
				scope.$apply();
			});
		}
	};
});

//custom directive to change all input to uppercase
app.directive('uppercased', function() {
	return {
		restrict: "A",
		require: "ngModel",
		link: function(scope, element, attrs, ngModelCtrl) {

			//This part of the code manipulates the model
			ngModelCtrl.$parsers.push(function(input) {
				return input ? input.toUpperCase() : "";
			});

			//This part of the code manipulates the viewvalue of the element
			element.css("text-transform","uppercase");
		}
	};
});

app.run(function($rootScope){
	
	$rootScope.tpObjectFactory = function () {
		//json passed in request body
		var tpObject = {
			//common
			id : null,
			
			//arrival
			plateNumber : null,  
			bodyNumber  : null,
			busCompany  : null,
			arrivalTime : null,
			arrivalOrigin      : null,
			arrivalDestination : null,
			arrivalRecorder    : null,
			
			//assessment
			tripType        : null,
			tripCoverage    : null,
			tripOrigin      : null,
			tripDestination : null,
						
			tripUnloadingBay   : null,
			tripUnloadingStart : null, 
			tripUnloadingEnd   : null,
			
			tripLoadingBay   : null,
			tripLoadingStart : null,
			tripLoadingEnd   : null,
						
			tripTerminalFee : null,
			tripParkingFee  : null,			
			tripAssessor    : null,
			
			//payment
			paymentIdNumber : null,
			
			//approval
			approvedBy   : null,
			approvedTime : null,
			
			//common
			status       : null,			
		};
		return tpObject;
	};
	
	$rootScope.paymentObjectFactory = function () {
		var pObject = {
			id: null,
			origReceiptNumber: null,
			origReceiptDate: null,
			paidBy: null,
			paidByAddress: null,
			collectedBy: null,
			etracsObjectId: null,
			etracsPostDate: null,
			paymentItems: [],
		};		
		return pObject;
	};
	
	$rootScope.paymentItemObjectFactory = function () {
		var pItemObject = {			
			id: null,
			itemCode: null,
			itemTitle: null,
			amount: 0,
			paymentId: null,
		};		
		return pItemObject;
	};
	
});

	
/*
// on page load
angular.element(document).ready(() => {
	// manually bootstrap Angular
	angular.bootstrap(document, ['mainApp']); 
	
});
*/


// on page load, because IE can't understand lambda
angular.element(document).ready(function() {
	// manually bootstrap Angular
	angular.bootstrap(document, ['mainApp'])
});
