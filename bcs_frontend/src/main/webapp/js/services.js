'use strict';


app.factory("arrivalService", ['$http', 'appconfigService', function($http, appconfigService) {
	var serviceBase = appconfigService.bcs_backend_url + '/public/api/arrival/'
    var obj = {};
	
    obj.findAllUndeparted = function(){
        return $http.get(serviceBase);
    }	
    
	obj.findArrivalByPlateNumber = function(plateNumber){
        return $http.get(serviceBase + 'findVehicleByPlateNumber/' +  plateNumber);
    }
	
    obj.newArrival = function (arrival) {
		return $http.post(serviceBase, arrival);		
	};		

    return obj;   
}]);


app.factory("assessmentService", ['$http', 'appconfigService', function($http, appconfigService) {
	var serviceBase = appconfigService.bcs_backend_url + '/public/api/assessment/'
    var obj = {};
    
	obj.findAllUndeparted = function(){
        return $http.get(serviceBase);
    }
	
    obj.findById = function(id){
        return $http.get(serviceBase +  id);
    }

    obj.saveAssessment = function (id, assessmentData) {
		return $http.put(serviceBase + id, assessmentData);
	};
	
	//patch would be better here, currently implemented as put cause by CORS
	obj.saveArrival = function (id, assessmentData) {
		return $http.put(serviceBase + 'arrival/' + id, assessmentData);
	};
	
	obj.saveUnloading = function (id, assessmentData) {
		return $http.put(serviceBase + 'unloading/' + id, assessmentData);
	};
	
	obj.saveLoading = function (id, assessmentData) {
		return $http.put(serviceBase + 'loading/' + id, assessmentData);
	};
	
	obj.saveTripDetails = function (id, assessmentData) {
		return $http.put(serviceBase + 'trip_details/' + id, assessmentData);
	};
	
	obj.saveFees = function (id, assessmentData) {
		return $http.put(serviceBase + 'fees/' + id, assessmentData);
	};

    return obj;   
}]);


app.factory("departureService", ['$http', 'appconfigService', function($http, appconfigService) {
	var serviceBase = appconfigService.bcs_backend_url + '/public/api/departure/'
    var obj = {};
	
    obj.findAllUndeparted = function(){
        return $http.get(serviceBase);
    }
	
    obj.findDepartureById = function(id){
        return $http.get(serviceBase + id);
    }
	
	obj.findDepartureByPlateNumber = function(plateNumber){
        return $http.get(serviceBase + 'findVehicleByPlateNumber/' + plateNumber);
    }

    obj.updateDeparture = function (id, departureData) {
		return $http.put(serviceBase + id, departureData);
	};	

    return obj;   
}]);


app.factory("busPaymentService", ['$http', 'appconfigService', function($http, appconfigService) {
	var serviceBase = appconfigService.bcs_backend_url + '/public/api/bus_payment/'
    var obj = {};
	
    obj.findAllUndeparted = function(){
        return $http.get(serviceBase);
    }
	
    obj.findBusPaymentById = function(id){
        return $http.get(serviceBase + id);
    }
		
    obj.newPayment = function (busPaymentData) {
		return $http.post(serviceBase, busPaymentData);
	};
	
	obj.updatePayment = function (id, busPaymentData) {
		return $http.put(serviceBase + id, busPaymentData);
	};

	obj.deletePayment = function (id) {
		return $http.delete(serviceBase + id);
	};	
	
	obj.findTerminalPassById = function (id) {
		return $http.get(serviceBase + 'findTerminalPassById/' +id);
	};

    return obj;   
}]);



app.factory("approvalService", ['$http', 'appconfigService', function($http, appconfigService) {
	var serviceBase = appconfigService.bcs_backend_url + '/public/api/approval/'
    var obj = {};
	
    obj.findAllUndeparted = function(){
        return $http.get(serviceBase);
    }
	
    obj.findApprovalById = function(id){
        return $http.get(serviceBase + id);
    }
	
	obj.findApprovalByPlateNumber = function(plateNumber){
        return $http.get(serviceBase + 'findVehicleByPlateNumber/' + plateNumber);
    }

    obj.updateApproval = function (id, approvalData) {
		return $http.put(serviceBase + id, approvalData);
	};	

    return obj;   
}]);



app.factory("vehicleRegistryService", ['$http', 'appconfigService', function($http, appconfigService) {
	var serviceBase = appconfigService.bcs_backend_url + '/public/api/vehicle_registry/';
	
	var obj = {};
	
	obj.serviceBase = serviceBase;
     
    obj.findAll = function(){
        return $http.get(serviceBase);		
    }
	
	obj.findById = function(id){
        return $http.get(serviceBase + id);		
    }
	
	obj.findByPlateNumber = function(plateNumber) {		
        return $http.get(serviceBase + 'findVehicleByPlateNumber/' + plateNumber);				
    }
	
	obj.queryVehicleByPlateNumber= function(plateNumber) {		
        return $http.get(serviceBase + 'queryVehicleByPlateNumber/' + plateNumber);				
    }
	
	obj.newVehicle= function(vehicleData) {		
        return $http.post(serviceBase, vehicleData);				
    }
	
	obj.updateVehicle = function(id, vehicleData) {		
        return $http.put(serviceBase +  id, vehicleData);				
    }
	
	obj.deleteVehicle = function(id) {		
        return $http.delete(serviceBase +  id);				
    }
	
	
    return obj;   
}]);


// provide keycloak as factory to make it injectable
app.factory('authorizationService', ['$window', function ($window) {
	//return $window._keycloak;
	
	//stubbed for now	
	var profile = {
			"id" 		: 0, 
			"username" 	: 'sebio-demo', 
			"email" 	: 'sebio-demo@naga.gov.ph',
			"firstName"	: 'Senen',
			"lastName"	: 'Ebio' 
	};
	var obj = {};
	obj.profile = profile;
	obj.hasRealmRole = function hasRealmRole(role) {
		return true;
	};
	return obj;
}]);


//injectable configuration
app.factory('appconfigService', [ function () {
	var obj = {};
	
	obj.bcs_backend_url = 'http://localhost:8091';
	obj.etracks_url = 'http://localhost:8092';
	
	//fields here
	obj.tpObjectFactory = function () {
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
	
	//fields here
	obj.paymentObjectFactory = function () {
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
	
	//fields here
	obj.paymentItemObjectFactory = function () {
		var pItemObject = {			
			id: null,
			itemCode: null,
			itemTitle: null,
			amount: 0,
			paymentId: null,
		};		
		return pItemObject;
	};
	
	return obj;
}]);

