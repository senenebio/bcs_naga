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
import ph.gov.naga.domain.BusPaymentData;
import ph.gov.naga.service.BusPaymentService;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/bus_payment")
public class BusPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(BusPaymentController.class);

    @Autowired
    BusPaymentService busPaymentService;

    @RequestMapping(value = "/")
    public ResponseEntity<?> findAllUndeparted() {
        logger.info("Finding Terminal Passes for Payment.");

        List<BusPaymentData> inside = this.busPaymentService.findAllForPayment();

        if (inside.isEmpty()) {
            logger.warn("No vehicles found.");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(inside, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}")
    public ResponseEntity<?> findPayment(@PathVariable("id") Long id) {
        logger.info("Find Payment with id {}", id);

        BusPaymentData busPaymentData = this.busPaymentService.findPaymentById(id);
        if (busPaymentData == null) {
            logger.warn("Payment data with id {} does not exist.", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(busPaymentData, HttpStatus.OK);
    }

    @RequestMapping(value = "/findTerminalPassById/{id}")
    ResponseEntity<?> findTerminalPassById(@PathVariable("id") Long id) {
        logger.info("Find Terminal Pass id {}", id);
        List<BusPaymentData> inside = this.busPaymentService.findAllForPayment();
        for (BusPaymentData bp : inside) {
            if (bp.getTerminalPass().getId().compareTo(id) == 0) {
                logger.info("Found {}", bp);
                return new ResponseEntity<>(bp, HttpStatus.OK);
            }
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> newPayment(@RequestBody BusPaymentData busPaymentData, UriComponentsBuilder ucBuilder) {
        logger.info("Saving new payment {}", busPaymentData);

        if (!busPaymentData.isValid()) {
            logger.warn("Validation failure {}.", busPaymentData);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //begin transaction (try)
        BusPaymentData savedRow = this.busPaymentService.newTerminalPayment(busPaymentData);
        //end transaction (catch)

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/public/api/bus_payment/{id}")
                .buildAndExpand(savedRow.getPayment().getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);

    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updatePayment(@PathVariable("id") Long id, @RequestBody BusPaymentData busPaymentData) {
        logger.info("Updating payment {}", busPaymentData);

        if (!busPaymentData.isValid()) {
            logger.warn("Validation failure {}.", busPaymentData);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        //begin transaction (try)
        BusPaymentData savedRow = this.busPaymentService.updateTerminalPayment(busPaymentData);
        //end transaction (catch)
        
        return new ResponseEntity<>(savedRow, HttpStatus.OK);

    }

}
