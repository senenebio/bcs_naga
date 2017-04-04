'use strict';

//Running Controller (change to something more fancy perhaps)
app.controller('TimeCtrl', function($scope, $interval) {
	var tick = function() {
		$scope.clock = Date.now();
	}
	tick();
	$interval(tick, 1000);
});

//Auth Controller 
app.controller('AuthorizationCtrl', function($scope, authorizationService) {
	
	$scope.profile = authorizationService.profile;
	
	$scope.userLogout = function () {
		//authorizationService.logout();			
		swal("Log-out - to do!", "Logout request for " + $scope.profile.firstName, "warning");         
	}		
	
	$scope.userProfile = function () {					
		swal("Profile - to do!", 
			"Logged in as: " + $scope.profile.firstName + " " + $scope.profile.lastName, 
			"warning");      	
	
	}
});


//Main Controller
app.controller('MainCtrl', ['$rootScope', '$scope', '$http', '$timeout', 'authorizationService',
	function($rootScope, $scope, $http, $timeout, authorizationService) {
		
		/*
		$scope.profile = authorizationService.profile;
			
		$scope.isAdmin = authorizationService.hasRealmRole('admin');
		$scope.isManager = authorizationService.hasRealmRole('manager');
		$scope.isArrivalOperator = authorizationService.hasRealmRole('arival_operator');
		$scope.isDepartureOperator = authorizationService.hasRealmRole('departure_operator');
		$scope.isCollector = authorizationService.hasRealmRole('collector');
		*/
	}	
]);


//Arrival Controller
app.controller('ArrivalCtrl', function($rootScope, $scope, $http, $filter, 
		authorizationService, arrivalService, vehicleRegistryService, appconfigService) {	
	
	//visibility of edit form
	hideEditPanel();			
	
	//set-up
	initModel();
	
	initArrivalDataTable();
	findAllUndeparted();	
	
		
	$scope.newArrivalData = function () {				
		swal({
			title: 'Save arrival data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			hideEditPanel();
			clearSearch();		
								
			//prepare save, more validations needed here
			$scope.tpModel.arrivalTime = moment($scope.tpModel.arrivalTime, 'YYYY-MM-DD HH:mm A').toDate();			
			$scope.tpModel.arrivalRecorder = authorizationService.profile.username; //from authorization service
			$scope.tpModel.status = "ARRIVED";
			
			//call service to save data
			var arrivalData = {terminalPass : $scope.tpModel};			
			arrivalService.newArrival(arrivalData)
			.then(function(response){
				swal('Success!', 'Arrival successfully recorded.', 'success');
				initModel();
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.cancelArrivalData = function () {
		initModel();
		hideEditPanel();		
		clearSearch();
	};
	
	$scope.populateArrivalData = function () {
		var  plateNumber = $scope.arrival_search;
		if (!plateNumber) {
			swal('Warning!', 'Type Plate Number in the search box.', 'warning');
			return;
		}		
		arrivalService.findArrivalByPlateNumber(plateNumber)
		.then(function(response){			
			swal ('Error', "Vehicle is already inside terminal.", 'error');
		},function(response){			
			//find in vehicle registry
			vehicleRegistryService.findByPlateNumber(plateNumber)
			.then(function(response) {
				var vehicle = response.data;
				//init model
				$scope.tpModel.plateNumber = vehicle.plateNumber;
				$scope.tpModel.bodyNumber = vehicle.bodyNumber;
				$scope.tpModel.busCompany = vehicle.busCompany;
				$scope.tpModel.arrivalTime = moment({}).format('YYYY-MM-DD HH:mm A');
				
				showEditPanel();
			}, function(response) {
				swal ('Warning!', 'Plate Number not found. Please double check input.', 'warning');
				showEditPanel();
			});
		});
	};	
		
	$scope.refreshTable = function () {
		findAllUndeparted();
	};
	
	
	
	//for angular typeahead	
	$scope.queryVehicleByPlateNumber = function(plateNumber) {
		//should be in cofig
		var url = appconfigService.bcs_backend_url + '/public/api/vehicle_registry/queryVehicleByPlateNumber/' + plateNumber
		return $http({
			method: 'GET',
			url: url
		}).then(function successCallback(response) {
			//console.clear();
			$scope.results = response.data
			return response.data.map(function(item) {
				//console.log(item.plateNumber + item.busCompany)
				return {"plateNumber" : item.plateNumber, "busCompany" : item.busCompany};
		  });
		});	
		
	};	
		
	function clearSearch () {
		$scope.arrival_search = null;
	}	
	
	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};
	
	function initModel(src) {
		$scope.tpModel = $rootScope.tpObjectFactory();
		if (src) {
			$scope.tpModel = angular.copy(src)
			//take care of time values
			$scope.tpModel.arrivalTime = moment($scope.tpModel.arrivalTime).format();
		} 
		//for typeahead
		$scope.arrival_search = null;	
	}
	
			
	
	function initArrivalDataTable() {
		//for the data tables
		$scope.datatable = $('#arrivalTable').DataTable({
			"order": [[0, "desc"]],
			"responsive": true
		});
	};

	function refreshArrivalDataTable() {
		$scope.datatable.destroy();
		initArrivalDataTable();
		$scope.datatable.clear();
		$scope.undepartedVehicles.forEach(function(item){
			
			//8 columns
			$scope.datatable.row.add([
				/*$filter('date')(item.terminalPass.arrivalTime, 'yyyy-MM-dd HH:mm'),*/
				moment(item.terminalPass.arrivalTime).format('YYYY-MM-DD HH:mm A'),
				item.terminalPass.plateNumber,
				item.terminalPass.bodyNumber,
				item.terminalPass.busCompany,
				item.terminalPass.tripOrigin || item.terminalPass.arrivalOrigin,
				item.terminalPass.tripDestination || item.terminalPass.arrivalDestination,
				item.terminalPass.id,
				item.terminalPass.status
			]);
		});
		$scope.datatable.draw();
	};
	
	function findAllUndeparted () {
		
		arrivalService.findAllUndeparted()
		.then(function(response){
			$scope.undepartedVehicles = response.data;
			refreshArrivalDataTable();
		},function(response) {
			$scope.undepartedVehicles = [];
			refreshArrivalDataTable();
		});
		
	};	
	
});


//Assessment Controller
app.controller('AssessmentCtrl', function($rootScope, $scope, $http, $filter, $compile,
		authorizationService, assessmentService, appconfigService) {	

	$scope.todo = function () {
		swal ('Warning', 'to do!', 'warning');
	};	
	
	//set-up
	hideEditPanel();
	initAssessmentDataTable();
	findAllUndeparted();
	

	$scope.populateAssessmentData = function (id) {
		hideEditPanel();
		assessmentService.findById(id)
		.then(function(response){			
			//we found a row, copy the terminal pass object					
			initModel(response.data.terminalPass);				
			showEditPanel();
		},function(response){
			swal ('Error', "Data is no longer available.", 'error');
			//refresh
			findAllUndeparted();
		});		
	};

	
	$scope.refreshTable = function () {
		findAllUndeparted();
	};
	
	$scope.closeEditPanel = function () {
		hideEditPanel();
	};
	
	
	$scope.saveArrival = function () {
		swal({
			title: 'Save arrival data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			//simulate a patch by populating only the columns we changed
			var tpObject = {};
			tpObject.id = $scope.tpCurrent.id;
			tpObject.plateNumber = $scope.tpCurrent.plateNumber;			
			tpObject.arrivalTime = moment($scope.tpCurrent.arrivalTime, 
				'YYYY-MM-DD HH:mm').toDate();
			tpObject.arrivalOrigin = $scope.tpCurrent.arrivalOrigin;
			tpObject.arrivalDestination = $scope.tpCurrent.arrivalDestination;
			
			//from authorization service
			tpObject.arrivalRecorder = authorizationService.profile.username;
			tpObject.tripAssessor = authorizationService.profile.username;
			
			var assessmentData = {terminalPass : tpObject};
			//call service to save data			
			assessmentService.saveArrival($scope.tpCurrent.id, assessmentData)
			.then(function(response){
				swal('Success!', 'Arrival successfully saved.', 'success');
				initModel(response.data.terminalPass);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.saveUnloading = function () {
		swal({
			title: 'Save Unloading data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			//simulate a patch by populating only the columns we need
			var tpObject = {};
			tpObject.id = $scope.tpCurrent.id;
			tpObject.plateNumber = $scope.tpCurrent.plateNumber;
			tpObject.tripUnloadingBay = $scope.tpCurrent.tripUnloadingBay;
			//take care of dates
			tpObject.tripUnloadingStart = moment($scope.tpCurrent.tripUnloadingStart, 
				'YYYY-MM-DD HH:mm').toDate();
			tpObject.tripUnloadingEnd = moment($scope.tpCurrent.tripUnloadingEnd,
				'YYYY-MM-DD HH:mm').toDate();
						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			//call service to save data				
			var assessmentData = {terminalPass : tpObject};			
			assessmentService.saveUnloading($scope.tpCurrent.id, assessmentData)
			.then(function(response){
				swal('Success!', 'Unloading data successfully saved.', 'success');
				initModel(response.data.terminalPass);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.saveLoading = function () {
		swal({
			title: 'Save loading data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			//simulate a patch by populating only the columns we need
			var tpObject = {};
			tpObject.id = $scope.tpCurrent.id;
			tpObject.plateNumber = $scope.tpCurrent.plateNumber;
			tpObject.tripLoadingBay = $scope.tpCurrent.tripLoadingBay;
			tpObject.tripLoadingStart = moment($scope.tpCurrent.tripLoadingStart,
				'YYYY-MM-DD HH:mm').toDate();
			tpObject.tripLoadingEnd = moment($scope.tpCurrent.tripLoadingEnd,
				'YYYY-MM-DD HH:mm').toDate();						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			//call service to save data
			var assessmentData = {terminalPass : tpObject};
			assessmentService.saveLoading($scope.tpCurrent.id, assessmentData)
			.then(function(response){
				swal('Success!', 'Loading data successfully saved.', 'success');
				initModel(response.data.terminalPass);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.saveTripDetails = function () {
		swal({
			title: 'Save trip details data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			//simulate a patch by populating only the columns we need
			var tpObject = {};
			tpObject.id = $scope.tpCurrent.id;
			tpObject.plateNumber = $scope.tpCurrent.plateNumber;
			
			tpObject.tripType = $scope.tpCurrent.tripType;
			tpObject.tripCoverage = $scope.tpCurrent.tripCoverage;
			tpObject.tripOrigin = $scope.tpCurrent.tripOrigin;
			tpObject.tripDestination = $scope.tpCurrent.tripDestination;
						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			var assessmentData = {terminalPass : tpObject};
			assessmentService.saveTripDetails($scope.tpCurrent.id, assessmentData)
			.then(function(response){
				swal('Success!', 'Trip details data successfully saved.', 'success');
				initModel(response.data.terminalPass);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.saveFees = function () {
		swal({
			title: 'Save fees data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			//simulate a patch by populating only the columns we need
			var tpObject = {};
			tpObject.id = $scope.tpCurrent.id;
			tpObject.plateNumber = $scope.tpCurrent.plateNumber;
			
			tpObject.tripTerminalFee = $scope.tpCurrent.tripTerminalFee;
			tpObject.tripParkingFee = $scope.tpCurrent.tripParkingFee;
						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			var assessmentData = {terminalPass : tpObject};
			assessmentService.saveFees($scope.tpCurrent.id, assessmentData)
			.then(function(response){
				swal('Success!', 'Fees data successfully saved.', 'success');
				initModel(response.data.terminalPass);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};

	
	
	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};
	
	
	function initModel(src) {		
		$scope.tpCurrent = $rootScope.tpObjectFactory ();
		if (src) {
			$scope.tpCurrent = angular.copy(src);
			
			//datetime variables are always a pain
			$scope.tpCurrent.arrivalTime = src.arrivalTime ? 
				moment(src.arrivalTime).format('YYYY-MM-DD HH:mm A') :
				null;
				
			$scope.tpCurrent.tripUnloadingStart = src.tripUnloadingStart ? 
				moment(src.tripUnloadingStart).format('YYYY-MM-DD HH:mm A'):
				null; 
				
			$scope.tpCurrent.tripUnloadingEnd   =  src.tripUnloadingEnd ? 
				moment(src.tripUnloadingEnd).format('YYYY-MM-DD HH:mm A'):
				null;
			
			$scope.tpCurrent.tripLoadingStart = src.tripLoadingStart ? 
				moment(src.tripLoadingStart).format('YYYY-MM-DD HH:mm A') :
				null;
			
			$scope.tpCurrent.tripLoadingEnd   = src.tripLoadingEnd ? 
				moment(src.tripLoadingEnd).format('YYYY-MM-DD HH:mm A') :
				null;
				
		}
	};
		
	
	function refreshAssessmentDataTable () {
		$scope.datatable.destroy();
		initAssessmentDataTable();
		$scope.datatable.clear();
		$scope.undepartedVehicles.forEach(function(item){
			var control = '<button ng-click="populateAssessmentData(' + item.terminalPass.id + ')" ' + 
			              '   type="button" class="btn btn-info btn-circle"> '+
						  '   <i class="fa fa fa-flash"></i> ' +
						  ' </button>';
			if ((item.terminalPass.status === "PAID" || item.terminalPass.status === "APPROVED")) {
				control = "...";
			}
			//9 columns
			$scope.datatable.row.add([
				item.terminalPass.id,
				/*$filter('date')(item.terminalPass.arrivalTime, 'yyyy-MM-dd HH:mm'), */
				moment(item.terminalPass.arrivalTime).format('YYYY-MM-DD HH:mm A'),
				item.terminalPass.plateNumber,				
				item.terminalPass.busCompany + " " + (item.terminalPass.bodyNumber||""),
				item.terminalPass.tripOrigin || "UNASSIGNED",
				item.terminalPass.tripDestination || "UNASSIGNED",
				item.terminalPass.tripAssessor || "-",
				item.terminalPass.status,
				control
			]);
		});
		$scope.datatable.draw();		
	};

	function findAllUndeparted () {
		assessmentService.findAllUndeparted()
		.then(function(response){
			$scope.undepartedVehicles = response.data;
			refreshAssessmentDataTable();
		},function(response) {
			$scope.undepartedVehicles = [];
			refreshAssessmentDataTable();
		});
		
	};

	
	function initAssessmentDataTable() {
		//for the data tables
		$scope.datatable = $('#assessmentTable').DataTable({
			"order": [[0, "desc"]],
			"responsive": true,
			"createdRow": function(row, data, dataIndex) {
				//make angular buttons clickable
				$compile(angular.element(row).contents())($scope);
			}
		});
	};

			
});

//Vehicle Controller
app.controller('VehicleCtrl', function($rootScope, $scope, $http, $filter, $compile,
		authorizationService, vehicleRegistryService, appconfigService) {	
	
	//visibility of edit form
	hideEditPanel();			
	
	//set-up
	initModel();
	
	initVehicleDataTable();
	findAllVehicles();	
		
	
	$scope.saveVehicle = function () {				
		swal({
			title: 'Save vehicle data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			hideEditPanel();			
			//call service to save data
			if ($scope.vehicleModel.id) {
				//update
				vehicleRegistryService.updateVehicle($scope.vehicleModel.id, $scope.vehicleModel)
				.then(function(response){
					swal('Success!', 'Vehicle data successfully updated.', 'success');
					initModel();
					$scope.refreshTable();
				}, function(response){
					swal('Opps!', 'Error encountered while saving data.', 'error');
				});
			} else {
				//insert
				vehicleRegistryService.newVehicle($scope.vehicleModel)
				.then(function(response){
					swal('Success!', 'New vehicle data successfully recorded.', 'success');
					initModel();
					$scope.refreshTable();
				}, function(response){
					swal('Opps!', 'Error encountered while saving data.', 'error');
				});
			}
		});			
	};
	
	
	
	$scope.deleteVehicle = function () {				
		swal({
			title: 'Delete vehicle data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){			
			hideEditPanel();			
			//call service to save data							
			vehicleService.deleteVehicle($scope.vehicleModel.id)
			.then(function(response){
				swal('Success!', 'Vehicle successfully updated.', 'success');
				initModel();
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});			
	};
	
	$scope.cancelUpdateVehicleData = function () {
		initModel();
		hideEditPanel();		
	};
	
	$scope.populateNewVehicleData = function () {			
		initModel();
		showEditPanel();				
	};
	
	$scope.populateVehicleData = function (id) {			
		//find in vehicle registry
		vehicleRegistryService.findById(id)
		.then(function(response) {			
			initModel(response.data);
			showEditPanel();
		}, function(response) {
			swal ('Warning!', 'Vehicle data not found.', 'warning');
		});		
	};	
	
		
	$scope.refreshTable = function () {
		//expensive
		findAllVehicles();
	};
	

	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};

	
	function initModel(src) {
		if (src) {			
			$scope.vehicleModel = angular.copy(src);			
		} else {			
			$scope.vehicleModel = {
				id 			: null,
				plateNumber	: null,			
				bodyNumber 	: null,
				busCompany 	: null,	

				makeModel 	: null,
				motorNumber : null,
				chassisNumber : null,
				caseNumber 	: null,			
			};
			
		}
		//for updates
		$scope.vehicleModelOrig = angular.copy($scope.vehicleModel);		
	}
	
	function findAllVehicles () {
		vehicleRegistryService.findAll()
		.then(function(response){
			$scope.allVehicles = response.data;
			refreshVehicleDataTable();			
		});
	}		
	
	function initVehicleDataTable() {
		//for the data tables
		$scope.datatable = $('#vehicleTable').DataTable({
			"order": [[0, "asc"]],
			"responsive": true,
			"createdRow": function(row, data, dataIndex) {
				//make angular buttons clickable
				$compile(angular.element(row).contents())($scope);
			}
		});
	};

	function refreshVehicleDataTable() {
		$scope.datatable.destroy();
		initVehicleDataTable();
		$scope.datatable.clear();
		$scope.allVehicles.forEach(function(item){
			var editControl = '<button ng-click="populateVehicleData(' + item.id + ')" ' + 
			              '   type="button" class="btn btn-info btn-circle"> '+
						  '   <i class="fa fa fa-flash"></i> ' +
						  ' </button>' ;
			var deleteControl =  '<button ng-click="todo(' + item.id + ')" ' + 
			              '   type="button" class="btn btn-danger btn-circle"> '+
						  '   <i class="fa fa fa-trash"></i> ' +
						  ' </button>';		
			
			
			//7 columns
			$scope.datatable.row.add([
				item.plateNumber,
				item.bodyNumber,
				item.busCompany,
				item.makeModel,
				item.motorNumber,
				item.chassisNumber,
				item.caseNumber,
				editControl,
				deleteControl,
			]);
		});
		$scope.datatable.draw();
	};
	
	
});


//Bus Payment Controller
app.controller('BusPaymentCtrl', function($rootScope, $scope, $http, $filter, $compile, 
		authorizationService, busPaymentService, appconfigService) {	
	
	//visibility of edit form
	hideEditPanel();			
	
	//set-up
	initModelTp();
	initModelPayment();
	
	initBusPaymentDataTable();
	findAllUndeparted();	
	
		
	$scope.saveBusPaymentData = function () {				
		swal({
			title: 'Save BusPayment data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			hideEditPanel();
			
			var tpObject = {};
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			
			tpObject.approvedTine = moment($scope.tpModel.approvedTime,
				'YYYY-MM-DD HH:mm A').toDate();
			tpObject.status = "DEPARTED";
			//from authorization service
			tpObject.approvedBy = authorizationService.profile.username;
			
			var busPaymentData = {terminalPass : tpObject};
			busPaymentService.updateBusPayment($scope.tpModel.id, busPaymentData)
			.then(function(response){
				swal('Success!', 'BusPayment data successfully recorded.', 'success');
				initModel(response.data);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.cancelBusPaymentData = function () {
		initModelTp();
		initModelPayment();
		hideEditPanel();		
		clearSearch();
	};
	
	$scope.populateBusPaymentData = function (id) {
		busPaymentService.findBusPaymentById(id)
		.then(function(response){			
			initModelTp(response.data.terminalPass);			
		});
		//show it anyway
		showEditPanel();
	};	
		
	$scope.refreshTable = function () {
		findAllUndeparted();
	};	
	
	
	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};

	
	function initModelTp(src) {		
		$scope.tpModel = $rootScope.tpObjectFactory();
		if (src) {
			$scope.tpModel = angular.copy(src);
			//format dates, a real pain
			//$scope.tpModel.approvedTime = src.approvedTime ?
			//	moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
			//	moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	};
	
	function initModelPayment(src) {		
		$scope.tpModel = $rootScope.paymentObjectFactory();
		if (src) {
			$scope.paymentModel = angular.copy(src);
			//format dates, a real pain
			//$scope.tpModel.approvedTime = src.approvedTime ?
			//	moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
			//	moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	};
	
	function initModelPaymentItem(src) {		
		$scope.tpModel = $rootScope.paymentItemObjectFactory();
		if (src) {
			$scope.paymentItemModel = angular.copy(src);
			//format dates, a real pain
			//$scope.tpModel.approvedTime = src.approvedTime ?
			//	moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
			//	moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	};
	
	
	function initBusPaymentDataTable() {
		//for the data tables
		$scope.datatable = $('#busPaymentTable').DataTable({
			"order": [[0, "desc"]],
			"responsive": true,
			"createdRow": function(row, data, dataIndex) {
				//make angular buttons clickable
				$compile(angular.element(row).contents())($scope);
			}
		});
	};

	function refreshBusPaymentDataTable() {
		$scope.datatable.destroy();
		initBusPaymentDataTable();
		$scope.datatable.clear();
		$scope.undepartedVehicles.forEach(function(item){
			var control = '<button ng-click="populateBusPaymentData(' + item.terminalPass.id + ')" ' + 
			              '   type="button" class="btn btn-info btn-circle"> '+
						  '   <i class="fa fa fa-flash"></i> ' +
						  ' </button>' ;
			 
			var isPaid = item.terminalPass.paymentIdNumber ? 
							'<center><i class="fa fa fa-check text-success"></center>' : 
							'<center><i class="fa fa fa-times text-danger"></center>';
							
			var fees = item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee ?
							item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee :
							"FREE";
			
			//12 columns
			$scope.datatable.row.add([
				item.terminalPass.id,
				item.terminalPass.plateNumber,
				item.terminalPass.busCompany + ' ' + item.terminalPass.bodyNumber,				
				item.terminalPass.tripOrigin,
				item.terminalPass.tripDestination,	
				item.terminalPass.tripType,
				item.terminalPass.tripCoverage,
				fees,
				item.terminalPass.tripAssessor,
				isPaid,				
				item.terminalPass.status,
				control,
			]);
		});
		$scope.datatable.draw();
	};
	
	function findAllUndeparted () {
		busPaymentService.findAllUndeparted()
		.then(function(response){
			$scope.undepartedVehicles = response.data;
			refreshBusPaymentDataTable();
		},function(response) {
			$scope.undepartedVehicles = [];
			refreshBusPaymentDataTable();
		});
		
	};	
	
});



//Approval Controller
app.controller('ApprovalCtrl', function($rootScope, $scope, $http, $filter, $compile, 
		authorizationService, approvalService, appconfigService) {	
	
	//visibility of edit form
	hideEditPanel();			
	
	//set-up
	initModel();
	
	initApprovalDataTable();
	findAllUndeparted();	
	
		
	$scope.saveApprovalData = function () {				
		swal({
			title: 'Save Approval data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			hideEditPanel();
			
			var tpObject = {};
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			
			tpObject.approvedTine = moment($scope.tpModel.approvedTime,
				'YYYY-MM-DD HH:mm A').toDate();
			tpObject.status = "DEPARTED";
			//from authorization service
			tpObject.approvedBy = authorizationService.profile.username;
			
			var approvalData = {terminalPass : tpObject};
			approvalService.updateApproval($scope.tpModel.id, approvalData)
			.then(function(response){
				swal('Success!', 'Approval data successfully recorded.', 'success');
				initModel(response.data);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.cancelApprovalData = function () {
		initModel();
		hideEditPanel();		
		clearSearch();
	};
	
	$scope.populateApprovalData = function (id) {
		approvalService.findApprovalById(id)
		.then(function(response){			
			initModel(response.data.terminalPass);
			showEditPanel();
		},function(response){			
			swal ('Error', "Vehicle not approved for approval.", 'error');
		});
	};	
		
	$scope.refreshTable = function () {
		findAllUndeparted();
	};	
	
	
	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};

	
	function initModel(src) {		
		$scope.tpModel = $rootScope.tpObjectFactory();
		if (src) {
			$scope.tpModel = angular.copy(src);
			//format dates, a real pain
			$scope.tpModel.approvedTime = src.approvedTime ?
				moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
				moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	}
	
			
	
	function initApprovalDataTable() {
		//for the data tables
		$scope.datatable = $('#approvalTable').DataTable({
			"order": [[0, "desc"]],
			"responsive": true,
			"createdRow": function(row, data, dataIndex) {
				//make angular buttons clickable
				$compile(angular.element(row).contents())($scope);
			}
		});
	};

	function refreshApprovalDataTable() {
		$scope.datatable.destroy();
		initApprovalDataTable();
		$scope.datatable.clear();
		$scope.undepartedVehicles.forEach(function(item){
			var control = '<button ng-click="populateApprovalData(' + item.terminalPass.id + ')" ' + 
			              '   type="button" class="btn btn-info btn-circle"> '+
						  '   <i class="fa fa fa-flash"></i> ' +
						  ' </button>' ;
			 
			var isPaid = item.terminalPass.paymentIdNumber ? 
							'<i class="fa fa fa-check text-success">' : 
							'<i class="fa fa fa-times text-danger">';
							
			var fees = item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee ?
							item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee :
							"FREE";
			
			//12 columns
			$scope.datatable.row.add([
				item.terminalPass.id,
				item.terminalPass.plateNumber,
				item.terminalPass.busCompany + ' ' + item.terminalPass.bodyNumber,				
				item.terminalPass.tripOrigin,
				item.terminalPass.tripDestination,	
				item.terminalPass.tripType,
				item.terminalPass.tripCoverage,
				fees,
				item.terminalPass.tripAssessor,
				isPaid,				
				item.terminalPass.status,
				control,
			]);
		});
		$scope.datatable.draw();
	};
	
	function findAllUndeparted () {
		approvalService.findAllUndeparted()
		.then(function(response){
			$scope.undepartedVehicles = response.data;
			refreshApprovalDataTable();
		},function(response) {
			$scope.undepartedVehicles = [];
			refreshApprovalDataTable();
		});
		
	};	
	
});


//Departure Controller
app.controller('DepartureCtrl', function($rootScope, $scope, $http, $filter, $compile, 
		authorizationService,departureService, appconfigService) {	
	
	//visibility of edit form
	hideEditPanel();			
	
	//set-up
	initModel();
	
	initDepartureDataTable();
	findAllUndeparted();	
	
		
	$scope.saveDepartureData = function () {				
		swal({
			title: 'Save Departure data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			hideEditPanel();
			
			var tpObject = {};
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			// format date
			tpObject.departureTime = moment($scope.tpModel.departureTime,
				'YYYY-MM-DD HH:mm A').toDate();
				
			tpObject.status = "DEPARTED";
			//from authorization service
			tpObject.departureRecorder = authorizationService.profile.username;
			
			var departureData = {terminalPass : tpObject};
			departureService.updateDeparture($scope.tpModel.id, departureData)
			.then(function(response){
				swal('Success!', 'Departure data successfully recorded.', 'success');
				initModel(response.data.terminalPass);
				$scope.refreshTable();
			}, function(response){
				swal('Opps!', 'Error encountered while saving data.', 'error');
			});
		});
			
	};
	
	$scope.cancelDepartureData = function () {
		initModel();
		hideEditPanel();		
		clearSearch();
	};
	
	$scope.populateDepartureData = function (id) {
		departureService.findDepartureById(id)
		.then(function(response){			
			initModel(response.data.terminalPass);
			showEditPanel();
		},function(response){			
			swal ('Error', "Vehicle not approved for departure.", 'error');
		});
	};	
		
	$scope.refreshTable = function () {
		findAllUndeparted();
	};	
	
	
	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};

	
	function initModel(src) {		
		$scope.tpModel = $rootScope.tpObjectFactory();
		if (src) {
			$scope.tpModel = angular.copy(src);
			//datetime variables are always a pain
			$scope.tpModel.departureTime = src.departureTime ?
				moment(src.departureTime).format('YYYY-MM-DD HH:mm A') :
				moment({}).format('YYYY-MM-DD HH:mm A');
		} 
		
	}
	
			
	
	function initDepartureDataTable() {
		//for the data tables
		$scope.datatable = $('#departureTable').DataTable({
			"order": [[0, "desc"]],
			"responsive": true,
			"createdRow": function(row, data, dataIndex) {
				//make angular buttons clickable
				$compile(angular.element(row).contents())($scope);
			}
		});
	};

	function refreshDepartureDataTable() {
		$scope.datatable.destroy();
		initDepartureDataTable();
		$scope.datatable.clear();
		$scope.undepartedVehicles.forEach(function(item){
			var control = '<button ng-click="populateDepartureData(' + item.terminalPass.id + ')" ' + 
			              '   type="button" class="btn btn-success btn-circle"> '+
						  '   <i class="fa fa fa-flash"></i> ' +
						  ' </button>' ;
			
			
			//8 columns
			$scope.datatable.row.add([
				item.terminalPass.id,
				item.terminalPass.plateNumber,
				item.terminalPass.bodyNumber,
				item.terminalPass.busCompany,
				item.terminalPass.tripOrigin,
				item.terminalPass.tripDestination,				
				item.terminalPass.status,
				control,
			]);
		});
		$scope.datatable.draw();
	};
	
	function findAllUndeparted () {
		departureService.findAllUndeparted()
		.then(function(response){
			$scope.undepartedVehicles = response.data;
			refreshDepartureDataTable();
		},function(response) {
			$scope.undepartedVehicles = [];
			refreshDepartureDataTable();
		});
		
	};	
	
});
