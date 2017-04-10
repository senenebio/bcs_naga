/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.controller.internal;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ph.gov.naga.model.Vehicle;
import ph.gov.naga.service.VehicleService;

/**
 *
 * @author Drei
 */
@RestController
@RequestMapping("/internal/api/vehicle")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleService vehicleService;
    
      
    @RequestMapping("/")
    private ResponseEntity<Page<Vehicle>> findAllPageable(Pageable pgbl) {
        Page<Vehicle> result = vehicleService.findAllPageable(pgbl);
        if (result.getTotalElements() <= 0) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/{id}")
    private ResponseEntity<Vehicle> find(@PathVariable Long id) {
        Vehicle result = vehicleService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/findById/{id}")
    private ResponseEntity<Vehicle> findById(@PathVariable Long id) {
        Vehicle result = vehicleService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/findByPlateNumber/{plateNumber}")
    private ResponseEntity<Vehicle> findByPlateNumber(@PathVariable String plateNumber) {
        Vehicle result = vehicleService.findByPlateNumber(plateNumber);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/findByBodyNumber/{bodyNumber}")
    private ResponseEntity<List<Vehicle>> findByBodyNumber(@PathVariable String bodyNumber) {
        List<Vehicle> result = vehicleService.findByBodyNumber(bodyNumber);
        if (result.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    

}
