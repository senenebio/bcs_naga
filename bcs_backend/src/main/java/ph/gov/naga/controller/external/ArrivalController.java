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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ph.gov.naga.domain.ArrivalData;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.service.ArrivalService;
import ph.gov.naga.service.TerminalPassService;
import utils.CustomError;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/arrival")
public class ArrivalController {

    private static final Logger logger = LoggerFactory.getLogger(ArrivalController.class);

    @Autowired
    ArrivalService arrivalService;

    @Autowired
    TerminalPassService terminalPassService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody ArrivalData arrivalData, UriComponentsBuilder ucBuilder) {
        logger.info("Creating new TerminalPass from arrival data: {}", arrivalData);
        
        //check for validity
        if (!arrivalData.isValid()) {
            logger.error("Invalid arrival data");
            return new ResponseEntity<>(new CustomError("Validation failure."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        //check for existing entry information
        if (!arrivalService.isVehicleAllowedEntry(arrivalData.getTerminalPass().getPlateNumber())) {
            logger.warn("Vehicle with plate {} not allowed entry!",
                    arrivalData.getTerminalPass().getPlateNumber());
            return new ResponseEntity<>(new CustomError("Vehicle not allowed to enter."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        //populate terminal pass data
        TerminalPass tp = arrivalData.getTerminalPass();        
        //set status to "ARRIVED"
        tp.setStatus("ARRIVED");
        
        
        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to save data {}", tp);
            return new ResponseEntity<>(new CustomError("Error while saving data."),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //success
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/public/api/arrival/{plateNumber}")
                .buildAndExpand(savedRow.getPlateNumber()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/")
    public ResponseEntity<?> findAllUndepartedVehicles() {
        logger.info("Finding All Vehicles Still Inside Terminal.");

        List<ArrivalData> inside = arrivalService.findAllUndepartedVehicles();
        if (inside.isEmpty()) {
            logger.warn("There are no vehicles inside terminal.");
            return new ResponseEntity<>(new CustomError("No vehicles found."),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(inside, HttpStatus.OK);
    }
    
    

    @RequestMapping(value = "/findVehicleByPlateNumber/{plateNumber}")
    public ResponseEntity<?> findByPlateNumberVehicleStillInsideTerminal(@PathVariable("plateNumber") String plateNumber) {
        logger.info("Checking if plate number {} is still inside terminal ", plateNumber);
        ArrivalData arrivalData = arrivalService.findUndepartedVehicleByPlateNumber(plateNumber);
        if (arrivalData == null) {
            logger.warn("Vehicle with plate number {} is not inside terminal.", plateNumber);
            return new ResponseEntity<>(new CustomError("Vehicle not inside terminal."),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(arrivalData, HttpStatus.OK);
    }
    
    

}
