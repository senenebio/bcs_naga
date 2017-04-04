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
import ph.gov.naga.model.Vehicle;
import ph.gov.naga.service.VehicleService;
import utils.CustomError;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/vehicle_registry")
public class VehicleRegistryController {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleRegistryController.class);
    
    @Autowired
    VehicleService vehicleService;
    
    @RequestMapping("/")
    private ResponseEntity<List<Vehicle>> findAll() {        
        logger.info("Finding all registered vehicles.");
        List<Vehicle> result = vehicleService.findAll();
        if (result.size() <= 0) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    
    @RequestMapping("/{id}")
    private ResponseEntity<Vehicle> findById(@PathVariable("id") Long id) {
        logger.info("Finding vehicle with id {}", id);
        Vehicle result = vehicleService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping("/findVehicleByPlateNumber/{plateNumber}")
    private ResponseEntity<Vehicle> findByPlateNumber(@PathVariable("plateNumber") String plateNumber) {
        logger.info("Finding vehicle with plate number {}", plateNumber);
        Vehicle result = vehicleService.findByPlateNumber(plateNumber);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping("/queryVehicleByPlateNumber/{plateNumber}")
    private ResponseEntity<List<Vehicle>> findByPlateNumberContaining(@PathVariable("plateNumber") String plateNumber) {
        logger.info("Finding vehicle with plate number like {}%", plateNumber);
        List<Vehicle> result = vehicleService.findByPlateNumberContaining(plateNumber.toUpperCase());
        if (result.size() <= 0) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody Vehicle vehicle, UriComponentsBuilder ucBuilder) {
        logger.info("Creating Vehicle : {}", vehicle);

        Vehicle savedRow = vehicleService.save(vehicle);
        if (savedRow == null) {
            logger.error("Unable to create Vehicle : {}", vehicle);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/public/api/vehicle_registry/{id}").buildAndExpand(savedRow.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody Vehicle vehicle) {
        logger.info("Updating Vehicle with id {}", id);

        //check for null
        //if (vehicle == null) {
        //    logger.error("Invalid data");
        //    return new ResponseEntity<>(new CustomError("Got null request"),
        //            HttpStatus.INTERNAL_SERVER_ERROR);
        //}

        //check for validity
        //if (vehicle.isValid() == false) {
        //    logger.error("Validation failure {} ", vehicle);
        //    return new ResponseEntity<>(new CustomError("Validation failure."),
        //            HttpStatus.INTERNAL_SERVER_ERROR);
        //}
        
        Vehicle v = vehicleService.findById(id);
        if (v == null) {
            logger.warn("Vehicle with ID {}, does not exist. ", id);
            return new ResponseEntity<>(new CustomError("Id not found!"),
                    HttpStatus.NOT_FOUND);
        }
        
        Vehicle savedRow = vehicleService.save(vehicle);

        if (savedRow == null) {
            logger.error("Unable to update vehicle with id {} ", id);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(savedRow, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        logger.info("Deleting Vehicle with id {}", id);
        
        Vehicle savedRow = vehicleService.findById(id);
        if (savedRow == null) {
            logger.error("Unable to delete vehicle with id {} ", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        vehicleService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
