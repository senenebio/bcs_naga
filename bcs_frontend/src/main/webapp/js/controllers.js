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
		$scope.tpModel = appconfigService.tpObjectFactory();
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
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;			
			tpObject.arrivalTime = moment($scope.tpModel.arrivalTime, 
				'YYYY-MM-DD HH:mm').toDate();
			tpObject.arrivalOrigin = $scope.tpModel.arrivalOrigin;
			tpObject.arrivalDestination = $scope.tpModel.arrivalDestination;
			
			//from authorization service
			tpObject.arrivalRecorder = authorizationService.profile.username;
			tpObject.tripAssessor = authorizationService.profile.username;
			
			var assessmentData = {terminalPass : tpObject};
			//call service to save data			
			assessmentService.saveArrival($scope.tpModel.id, assessmentData)
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
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			tpObject.tripUnloadingBay = $scope.tpModel.tripUnloadingBay;
			//take care of dates
			tpObject.tripUnloadingStart = moment($scope.tpModel.tripUnloadingStart, 
				'YYYY-MM-DD HH:mm').toDate();
			tpObject.tripUnloadingEnd = moment($scope.tpModel.tripUnloadingEnd,
				'YYYY-MM-DD HH:mm').toDate();
						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			//call service to save data				
			var assessmentData = {terminalPass : tpObject};			
			assessmentService.saveUnloading($scope.tpModel.id, assessmentData)
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
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			tpObject.tripLoadingBay = $scope.tpModel.tripLoadingBay;
			tpObject.tripLoadingStart = moment($scope.tpModel.tripLoadingStart,
				'YYYY-MM-DD HH:mm').toDate();
			tpObject.tripLoadingEnd = moment($scope.tpModel.tripLoadingEnd,
				'YYYY-MM-DD HH:mm').toDate();						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			//call service to save data
			var assessmentData = {terminalPass : tpObject};
			assessmentService.saveLoading($scope.tpModel.id, assessmentData)
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
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			
			tpObject.tripType = $scope.tpModel.tripType;
			tpObject.tripCoverage = $scope.tpModel.tripCoverage;
			tpObject.tripOrigin = $scope.tpModel.tripOrigin;
			tpObject.tripDestination = $scope.tpModel.tripDestination;
						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			var assessmentData = {terminalPass : tpObject};
			assessmentService.saveTripDetails($scope.tpModel.id, assessmentData)
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
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			
			tpObject.tripTerminalFee = $scope.tpModel.tripTerminalFee;
			tpObject.tripParkingFee = $scope.tpModel.tripParkingFee;
						
			//from authorization service
			tpObject.tripAssessor = authorizationService.profile.username;
			
			var assessmentData = {terminalPass : tpObject};
			assessmentService.saveFees($scope.tpModel.id, assessmentData)
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
		$scope.tpModel = appconfigService.tpObjectFactory ();
		if (src) {
			$scope.tpModel = angular.copy(src);
			
			//datetime variables are always a pain
			$scope.tpModel.arrivalTime = src.arrivalTime ? 
				moment(src.arrivalTime).format('YYYY-MM-DD HH:mm A') :
				null;
				
			$scope.tpModel.tripUnloadingStart = src.tripUnloadingStart ? 
				moment(src.tripUnloadingStart).format('YYYY-MM-DD HH:mm A'):
				null; 
				
			$scope.tpModel.tripUnloadingEnd   =  src.tripUnloadingEnd ? 
				moment(src.tripUnloadingEnd).format('YYYY-MM-DD HH:mm A'):
				null;
			
			$scope.tpModel.tripLoadingStart = src.tripLoadingStart ? 
				moment(src.tripLoadingStart).format('YYYY-MM-DD HH:mm A') :
				null;
			
			$scope.tpModel.tripLoadingEnd   = src.tripLoadingEnd ? 
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
			"dom": 'lfr<B><t>ip', 
			"buttons": ['excel'],
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
	initModelPaymentItem();
	initVars();
	
	initBusPaymentDataTable();
	findAllUndeparted();	
	
		
	$scope.saveBusPaymentData = function () {				
		swal({
			title: 'Save Bus Payment data',
			type: 'question',
			text: 'Please confirm.',
			showCloseButton: true,
			showCancelButton: true,
			confirmButtonText: '<i class="fa fa-thumbs-up"></i> Yes!',
			cancelButtonText: '<i class="fa fa-thumbs-down"></i> Cancel'
		}).then(function(){
			
			hideEditPanel();
			
			var paymentObject = {};	
			paymentObject.id = $scope.paymentModel.id;
			paymentObject.origReceiptNumber = $scope.paymentModel.origReceiptNumber;
			paymentObject.origReceiptDate = moment($scope.paymentModel.origReceiptDate,
				'YYYY-MM-DD HH:mm A').toDate();
			paymentObject.paidBy = 	$scope.paymentModel.paidBy;
			paymentObject.paidByAddress = 	$scope.paymentModel.paidByAddress;
			//from authorization service			
			paymentObject.collectedBy = authorizationService.profile.username;
			
			//to do: payment items make this configurable
			var paymentItems = [];
			if ($scope.terminalFee) {
				var tf = appconfigService.paymentItemObjectFactory();
				tf.itemCode = "001";
				tf.itemTitle = "TERMINAL FEE";
				tf.amount = $scope.terminalFee;
				paymentItems.push(tf);
			}
			if ($scope.parkingFee) {
				var pf = appconfigService.paymentItemObjectFactory();
				pf.itemCode = "002";
				pf.itemTitle = "PARKING FEE";
				pf.amount = $scope.parkingFee;
				paymentItems.push(pf);
			}
			
			var tpObject = {};
			tpObject.id = $scope.tpModel.id;
			tpObject.plateNumber = $scope.tpModel.plateNumber;
			tpObject.status = "PAID";
			
			var busPaymentData = {terminalPass : tpObject, payment : paymentObject, paymentItems: paymentItems};
			if ($scope.isNewPayment) {
				
				busPaymentService.newPayment(busPaymentData)
				.then(function(response){
					swal('Success!', 'New Bus Payment data successfully recorded.', 'success');
					initModel(response.data);
					$scope.refreshTable();
				}, function(response){
					swal('Opps!', 'Error encountered while saving data.', 'error');
				});
			} else {
				busPaymentService.updatePayment(paymentObject.id, busPaymentData)
				.then(function(response){
					swal('Success!', 'Bus Payment data successfully updated.', 'success');
					initModel(response.data);
					$scope.refreshTable();
				}, function(response){
					swal('Opps!', 'Error encountered while saving data.', 'error');
				});
				
			}
			
		});
			
	};
	
	$scope.cancelBusPaymentData = function () {
		initModelTp();
		initModelPayment();		
		hideEditPanel();		
		
	};
	
	$scope.populateBusPaymentData = function (terminalPassId, paymentId) {
		//find the parent terminal pass
		busPaymentService.findTerminalPassById(terminalPassId)
		.then(function(response){
			initModelTp(response.data.terminalPass);
			initModelPayment(response.data.terminalPass);
			
			var paymentItems = response.data.paymentItems;
			
			var assessedFees = $scope.tpModel.tripTerminalFee + $scope.tpModel.tripParkingFee;
			if(!assessedFees) {
				swal('Warning', 'Assessed Fees can not be determined.', 'warning');
				return;
			}
			
			if (paymentId) {				
				//edit mode
				busPaymentService.findBusPaymentById(paymentId)
				.then(function(response){					
					initModelPayment(response.data.payment);
					
					$scope.paymentModel.origReceiptDate = moment($scope.paymentModel.origReceiptDate)
						.format('YYYY-MM-DD HH:mm A');
					
					//see if we found entries
					if (angular.isArray(paymentItems)) {
						paymentItems.forEach(function(item, index) {
							//make this configurable
							if (item.itemCode === "001") {
								$scope.terminalFee = item.amount;
							} else if (item.itemCode === "002"){
								$scope.parkingFee = item.amount;
							}
							
						});
					}
					showEditPanel();
					
			});
			} else	{
				$scope.isNewPayment = true;
				
				//add mode			
				$scope.paymentModel.origReceiptDate = moment({}).format('YYYY-MM-DD HH:mm A');				
				$scope.paymentModel.paidBy = $scope.tpModel.plateNumber;
				$scope.paymentModel.paidByAddress = $scope.tpModel.busCompany;
				
				$scope.terminalFee = $scope.tpModel.tripTerminalFee;
				$scope.parkingFee = $scope.tpModel.tripParkingFee;
				
				showEditPanel();
			}
						
		},function(response){
			swal('Warning', 'Data for TerminalPass ' + terminalPassId + ' is not available.', 'warning');
		});
		
	};	
		
	$scope.refreshTable = function () {
		findAllUndeparted();
	};
	
	$scope.fetchORFromEtracs = function () {
		swal("Warning", "Unable to fetch OR number from Etracs", "warning");
	};

	
	
	function showEditPanel() {
		$scope.isEditPanelVisible = true;
	};
	
	function hideEditPanel() {
		$scope.isEditPanelVisible = false;		
	};

	
	function initModelTp(src) {		
		$scope.tpModel = appconfigService.tpObjectFactory();
		if (src) {
			$scope.tpModel = angular.copy(src);
			//format dates, a real pain
			//$scope.tpModel.approvedTime = src.approvedTime ?
			//	moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
			//	moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	};
	
	
	function initModelPayment(src) {		
		$scope.paymentModel = appconfigService.paymentObjectFactory();
		if (src) {
			$scope.paymentModel = angular.copy(src);
			//format dates, a real pain
			//$scope.tpModel.approvedTime = src.approvedTime ?
			//	moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
			//	moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	};
	
	function initModelPaymentItem(src) {		
		$scope.paymentItemModel = appconfigService.paymentItemObjectFactory();
		if (src) {
			$scope.paymentItemModel = angular.copy(src);
			//format dates, a real pain
			//$scope.tpModel.approvedTime = src.approvedTime ?
			//	moment(src.approvedTime).format('YYYY-MM-DD HH:mm A') :
			//	moment({}).format('YYYY-MM-DD HH:mm A');
		} 
	};
	
	function initVars() {		
		$scope.terminalFee = 0;
		$scope.parkingFee = 0;
		$scope.isNewPayment = false;
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
			
			var paymentId = item.terminalPass.paymentIdNumber;
			var terminalPassId = item.terminalPass.id;
			
					
			var control = '<button ng-click="populateBusPaymentData(' + terminalPassId + ',' + paymentId + ')" ' + 
			              '   type="button" class="btn btn-info btn-circle"> '+
						  '   <i class="fa fa fa-flash"></i> ' +
						  ' </button>' ;

			 
			var isPaid =  paymentId ? 
							'<center><i class="fa fa fa-check text-success"></center>' : 
							'<center><i class="fa fa fa-times text-danger"></center>';
							
			var fees = item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee ?
							item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee :
							"FREE";
			fees = '<strong>' + fees + '</strong>';
							
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
			
			tpObject.approvedTime = moment($scope.tpModel.approvedTime,
				'YYYY-MM-DD HH:mm A').toDate();
			tpObject.status = "APPROVED";
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
		$scope.tpModel = appconfigService.tpObjectFactory();
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
							'<center><i class="fa fa fa-check text-success"></center>' : 
							'<center><i class="fa fa fa-times text-danger"></center>';
							
			var fees = item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee ?
							item.terminalPass.tripTerminalFee + item.terminalPass.tripParkingFee :
							"FREE";
			fees = '<strong>' + fees + '</strong>';
			
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
		$scope.tpModel = appconfigService.tpObjectFactory();
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
