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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ph.gov.naga.model.PaymentItem;
import ph.gov.naga.service.PaymentItemService;

/**
 *
 * @author Drei
 */
@RestController
@RequestMapping("/internal/api/payment_item")
public class PaymentItemController {

    public static final Logger logger = LoggerFactory.getLogger(PaymentItemController.class);

    @Autowired
    private PaymentItemService paymentItemService;

    @RequestMapping("/")
    private ResponseEntity<Page<PaymentItem>> findAllPageable(Pageable pgbl) {
        Page<PaymentItem> result = paymentItemService.findAllPageable(pgbl);
        if (result.getNumberOfElements() <= 0) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping("/{id}")
    private ResponseEntity<PaymentItem> find(@PathVariable Long id) {
        PaymentItem result = paymentItemService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping("/findById/{id}")
    private ResponseEntity<PaymentItem> findByid(@PathVariable Long id) {
        PaymentItem result = paymentItemService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody PaymentItem paymentItem, UriComponentsBuilder ucBuilder) {
        logger.info("Creating PaymentItem : {}", paymentItem);

        PaymentItem savedRow = paymentItemService.save(paymentItem);
        if (savedRow == null) {
            logger.error("Unable to create PaymentItem : {}", paymentItem);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/internal/api/payment_item/{id}").buildAndExpand(savedRow.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody PaymentItem paymentItem) {
        logger.info("Updating PaymentItem with id {}", id);

        PaymentItem savedRow = paymentItemService.save(paymentItem);

        if (savedRow == null) {
            logger.error("Unable to update paymentItem with id {} ", id);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(savedRow, HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        logger.info("Deleting PaymentItem with id {}", id);
        
        PaymentItem savedRow = paymentItemService.findById(id);
        if (savedRow == null) {
            logger.error("Unable to delete paymentItem with id {} ", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        paymentItemService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
