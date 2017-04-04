/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.controller.external;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.naga.domain.AssessmentData;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.service.AssessmentService;
import ph.gov.naga.service.TerminalPassService;
import utils.CustomError;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/assessment")
public class AssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    @Autowired
    AssessmentService assessmentService;

    @Autowired
    TerminalPassService terminalPassService;

    @RequestMapping(value = "/")
    private ResponseEntity<?> findAllUndepartedVehicles() {
        logger.info("Finding All Vehicles Still Inside Terminal.");

        List<AssessmentData> inside = assessmentService.findAllUndepartedVehicles();
        if (inside.isEmpty()) {
            logger.warn("There are no vehicles found inside terminal.");
            return new ResponseEntity<>(new CustomError("No vehicles found."),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(inside, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}")
    private ResponseEntity<?> findById(@PathVariable Long id) {
        logger.info("Finding terminal pass with id {}", id);
        TerminalPass result = terminalPassService.findById(id);
        if (result == null) {
            logger.warn("Terminal Pass with id {} does not exist.", id);
            return new ResponseEntity<>(new CustomError("Record not found."),
                    HttpStatus.NOT_FOUND);
        }
        //success
        return new ResponseEntity<>(new AssessmentData(result), HttpStatus.OK);
    }

    @RequestMapping(value = "/arrival/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateArrival(@PathVariable("id") long id, @RequestBody AssessmentData assessmentData) {
        logger.info("Updating Assessment Arrival data with id {} {}", id, assessmentData);

        //check for validity
        if (assessmentData.isValid() == false) {
            logger.error("Validation failure {} ", assessmentData);
            return new ResponseEntity<>(new CustomError("Validation failure."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass ID {}, does not exist. ", id);
            return new ResponseEntity<>(new CustomError("Id not found!"),
                    HttpStatus.NOT_FOUND);
        }

        //add validations here
        tp.setArrivalTime(assessmentData.getTerminalPass().getArrivalTime());
        tp.setArrivalOrigin(assessmentData.getTerminalPass().getArrivalOrigin());
        tp.setArrivalDestination(assessmentData.getTerminalPass().getArrivalDestination());
        tp.setArrivalRecorder(assessmentData.getTerminalPass().getTripAssessor());

        //common
        tp.setTripAssessor(assessmentData.getTerminalPass().getTripAssessor());
        if (tp.getStatus() == null
                || tp.getStatus().compareToIgnoreCase("ARRIVED") == 0
                || tp.getStatus().compareToIgnoreCase("UNKNOWN") == 0) {
            tp.setStatus("ASSESSED");
        }

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to update TerminalPass with id {} {}", id, tp);
            return new ResponseEntity<>(new CustomError("Failed to save assessment data."),
                    HttpStatus.CONFLICT);
        }
        //success
        return new ResponseEntity<>(assessmentData, HttpStatus.OK);
    }

    @RequestMapping(value = "/unloading/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateUnloading(@PathVariable("id") long id, @RequestBody AssessmentData assessmentData) {
        logger.info("Updating Assessment Unloading data with id {} {}", id, assessmentData);

        //check for validity
        if (assessmentData.isValid() == false) {
            logger.error("Validation failure {} ", assessmentData);
            return new ResponseEntity<>(new CustomError("Validation failure."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass ID {}, does not exist. ", id);
            return new ResponseEntity<>(new CustomError("Id not found!"),
                    HttpStatus.NOT_FOUND);
        }

        tp.setTripUnloadingBay(assessmentData.getTerminalPass().getTripUnloadingBay());
        tp.setTripUnloadingStart(assessmentData.getTerminalPass().getTripUnloadingStart());
        tp.setTripUnloadingEnd(assessmentData.getTerminalPass().getTripUnloadingEnd());

        //common
        tp.setTripAssessor(assessmentData.getTerminalPass().getTripAssessor());
        if (tp.getStatus() == null
                || tp.getStatus().compareToIgnoreCase("ARRIVED") == 0
                || tp.getStatus().compareToIgnoreCase("UNKNOWN") == 0) {
            tp.setStatus("ASSESSED");
        }

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to update TerminalPass with id {} {}", id, tp);
            return new ResponseEntity<>(new CustomError("Failed to save assessment data."),
                    HttpStatus.CONFLICT);
        }
        //success
        return new ResponseEntity<>(assessmentData, HttpStatus.OK);
    }

    @RequestMapping(value = "/loading/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateLoading(@PathVariable("id") long id, @RequestBody AssessmentData assessmentData) {
        logger.info("Updating Assessment Loading data with id {} {}", id, assessmentData);

        //check for validity
        if (assessmentData.isValid() == false) {
            logger.error("Validation failure {} ", assessmentData);
            return new ResponseEntity<>(new CustomError("Validation failure."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass ID {}, does not exist. ", id);
            return new ResponseEntity<>(new CustomError("Id not found!"),
                    HttpStatus.NOT_FOUND);
        }

        tp.setTripLoadingBay(assessmentData.getTerminalPass().getTripLoadingBay());
        tp.setTripLoadingStart(assessmentData.getTerminalPass().getTripLoadingStart());
        tp.setTripLoadingEnd(assessmentData.getTerminalPass().getTripLoadingEnd());
        //common
        tp.setTripAssessor(assessmentData.getTerminalPass().getTripAssessor());

        if (tp.getStatus() == null
                || tp.getStatus().compareToIgnoreCase("ARRIVED") == 0
                || tp.getStatus().compareToIgnoreCase("UNKNOWN") == 0) {
            tp.setStatus("ASSESSED");
        }

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to update TerminalPass with id {} {}", id, tp);
            return new ResponseEntity<>(new CustomError("Failed to save assessment data."),
                    HttpStatus.CONFLICT);
        }        
        //success, send back what is in the database        
        return new ResponseEntity<>(new AssessmentData(savedRow), HttpStatus.OK);
    }

    @RequestMapping(value = "/trip_details/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateTripDetails(@PathVariable("id") long id, @RequestBody AssessmentData assessmentData) {
        logger.info("Updating Assessment Trip Details data with id {} {}", id, assessmentData);

        //check for validity
        if (assessmentData.isValid() == false) {
            logger.error("Validation failure {} ", assessmentData);
            return new ResponseEntity<>(new CustomError("Validation failure."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass ID {}, does not exist. ", id);
            return new ResponseEntity<>(new CustomError("Id not found!"),
                    HttpStatus.NOT_FOUND);
        }

        tp.setTripType(assessmentData.getTerminalPass().getTripType());
        tp.setTripCoverage(assessmentData.getTerminalPass().getTripCoverage());
        tp.setTripOrigin(assessmentData.getTerminalPass().getTripOrigin());
        tp.setTripDestination(assessmentData.getTerminalPass().getTripDestination());
        //common
        tp.setTripAssessor(assessmentData.getTerminalPass().getTripAssessor());
        if (tp.getStatus() == null
                || tp.getStatus().compareToIgnoreCase("ARRIVED") == 0
                || tp.getStatus().compareToIgnoreCase("UNKNOWN") == 0) {
            tp.setStatus("ASSESSED");
        }

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to update TerminalPass with id {} {}", id, tp);
            return new ResponseEntity<>(new CustomError("Failed to save assessment data."),
                    HttpStatus.CONFLICT);
        }
        //success, send back the latest from the db
        return new ResponseEntity<>(new AssessmentData(savedRow), HttpStatus.OK);
    }

    @RequestMapping(value = "/fees/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateFees(@PathVariable("id") long id, @RequestBody AssessmentData assessmentData) {
        logger.info("Updating Assessment Fees with id {} {}", id, assessmentData);

        //check for validity
        if (assessmentData.isValid() == false) {
            logger.error("Validation failure {} ", assessmentData);
            return new ResponseEntity<>(new CustomError("Validation failure."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass ID {}, does not exist. ", id);
            return new ResponseEntity<>(new CustomError("Id not found!"),
                    HttpStatus.NOT_FOUND);
        }

        tp.setTripTerminalFee(assessmentData.getTerminalPass().getTripTerminalFee());
        tp.setTripParkingFee(assessmentData.getTerminalPass().getTripParkingFee());

        //common
        tp.setTripAssessor(assessmentData.getTerminalPass().getTripAssessor());
        if (tp.getStatus() == null
                || tp.getStatus().compareToIgnoreCase("ARRIVED") == 0
                || tp.getStatus().compareToIgnoreCase("UNKNOWN") == 0) {
            tp.setStatus("ASSESSED");
        }

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to update TerminalPass with id {} {}", id, tp);
            return new ResponseEntity<>(new CustomError("Failed to save assessment data."),
                    HttpStatus.CONFLICT);
        }
        //success, send back the latest from db
        return new ResponseEntity<>(new AssessmentData(savedRow), HttpStatus.OK);
    }

}
