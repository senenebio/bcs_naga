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
import ph.gov.naga.domain.DepartureData;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.service.DepartureService;
import ph.gov.naga.service.TerminalPassService;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/departure")
public class DepartureController {

    public static final Logger logger = LoggerFactory.getLogger(DepartureController.class);

    @Autowired
    DepartureService departureService;

    @Autowired
    TerminalPassService terminalPassService;

        
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody DepartureData departureData) {
        logger.info("Updating TerminalPass {} from departure data: {}", id, departureData);

        if (!departureData.isValid()) {
            logger.error("Invalid departure data!");
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass with ID () does not exist.");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        //needs additional validations perhaps 
        tp.setDepartureTime(departureData.getTerminalPass().getDepartureTime());
        tp.setDepartureRecorder(departureData.getTerminalPass().getDepartureRecorder());
        tp.setStatus("DEPARTED");

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to save : {}", tp);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //success, send back what is in the database
        return new ResponseEntity<>(new DepartureData(savedRow), HttpStatus.OK);
    }

    @RequestMapping(value = "/")
    public ResponseEntity<?> findAllUndepartedVehicles() {
        logger.info("Finding All Vehicles Approved for Departure.");

        List<DepartureData> inside = departureService.findAllVehiclesAllowedToDepart();

        if (inside.isEmpty()) {
            logger.warn("There are no vehicles allowed to depart.");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(inside, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        logger.info("Find Terminal Pass with id {}", id);
        DepartureData departureData = departureService.findUndepartedVehicleById(id);
        if (departureData == null) {
            logger.warn("Vehicle with id {} is not inside terminal.", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(departureData, HttpStatus.OK);

    }

    @RequestMapping(value = "/findVehicleByPlateNumber/{plateNumber}")
    public ResponseEntity<?> findByPlateNumberVehicleStillInsideTerminal(@PathVariable("plateNumber") String plateNumber) {
        logger.info("Find Vehicle plate number is still inside terminal {}", plateNumber);
        DepartureData departureData = departureService.findUndepartedVehicleByPlateNumber(plateNumber);
        if (departureData == null) {
            logger.warn("Vehicle with plate number {} is not inside terminal.", plateNumber);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(departureData, HttpStatus.OK);

    }

}
