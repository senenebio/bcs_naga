/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.controller.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ph.gov.naga.model.Payment;
import ph.gov.naga.service.PaymentItemService;
import ph.gov.naga.service.PaymentService;

/**
 *
 * @author Drei
 */
@RestController
@RequestMapping("/internal/api/payment")
public class PaymentController {
    
    public static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentItemService paymentItemService;
    
    @RequestMapping("/")
    private ResponseEntity<Page<Payment>> findAllPageable(Pageable pgble) {
        
        Page<Payment> result = paymentService.findAllPageable(pgble);
        if (result.getNumberOfElements() <= 0) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping("/{id}")
    private ResponseEntity<Payment> find(@PathVariable Long id) {
        Payment result = paymentService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping("/findById/{id}")
    private ResponseEntity<Payment> findByid(@PathVariable Long id) {
        Payment result = paymentService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody Payment payment, UriComponentsBuilder ucBuilder) {
        logger.info("Creating Payment : {}", payment);

        Payment savedRow = paymentService.save(payment);
        if (savedRow == null) {
            logger.error("Unable to create Payment : {}", payment);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/internal/api/payment/{id}").buildAndExpand(savedRow.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody Payment payment) {
        logger.info("Updating Payment with id {}", id);

        Payment savedRow = paymentService.save(payment);

        if (savedRow == null) {
            logger.error("Unable to update payment with id {} ", id);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(savedRow, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        logger.info("Deleting Payment with id {}", id);
        
        Payment savedRow = paymentService.findById(id);
        if (savedRow == null) {
            logger.error("Unable to delete payment with id {} ", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        paymentService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }    
}
