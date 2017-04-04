/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.controller.external;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ph.gov.naga.domain.ApprovalData;
import ph.gov.naga.domain.TerminalPaymentData;
import ph.gov.naga.model.Payment;
import ph.gov.naga.model.PaymentItem;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.service.PaymentItemService;
import ph.gov.naga.service.PaymentService;
import ph.gov.naga.service.TerminalPassService;
//import ph.gov.naga.service.TerminalPaymentService;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/terminal_payment")
public class TerminalPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(TerminalPaymentController.class);

    @Autowired
    private TerminalPassService terminalPassService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentItemService paymentItemService;

    @RequestMapping(value = "/")
    public ResponseEntity<?> findAllUndeparted() {
        logger.info("Finding terminal Pass for payment.");

        List<TerminalPaymentData> inside = this.findAllForPayment();

        if (inside.isEmpty()) {
            logger.warn("No vehicles found.");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(inside, HttpStatus.OK);

    }

    private List<TerminalPaymentData> findAllForPayment() {
        
        
        List<TerminalPaymentData> result = new ArrayList<>();
        
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        
        for (TerminalPass tp : terminalPassList) {
            if (tp.getStatus().compareToIgnoreCase("ASSESSED") == 0
                    || tp.getStatus().compareToIgnoreCase("PAID") == 0
                    || tp.getStatus().compareToIgnoreCase("APPROVED") == 0) {

                TerminalPaymentData data = null;
                if (tp.getPaymentIdNumber() != null && tp.getPaymentIdNumber() > 0L) {
                    data = this.findById(tp.getPaymentIdNumber());
                    if (data != null) {
                        result.add(data);
                    } else {
                        result.add(new TerminalPaymentData(tp, new Payment()));
                    }
                } else {
                    result.add(new TerminalPaymentData(tp, new Payment()));
                }
            }
        }
        return result;
    }

    @RequestMapping(value = "/{id}")
    public ResponseEntity<?> findPayment(@PathVariable("id") Long id) {
        logger.info("Find Payment with id {}", id);

        TerminalPaymentData TerminalPaymentData = this.findById(id);
        if (TerminalPaymentData == null) {
            logger.warn("Payment data with id {} does not exist.", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(TerminalPaymentData, HttpStatus.OK);
    }

    //not working when delegated to TerminalPaymentService, I wonder why...
    private TerminalPaymentData findById(Long id) {

        logger.info("Finding bus payment with id {}", id);

        TerminalPaymentData terminalPaymentData = null;
        TerminalPass terminalPass = null;
        Payment payment = paymentService.findById(id);

        if (payment != null) {
            logger.debug("Found {}, now finding the parent terminal pass...", payment);
            terminalPass = terminalPassService.findById(payment.getId());
            if (terminalPass != null) {
                terminalPaymentData = new TerminalPaymentData(terminalPass, payment);
                logger.info("Found payment data {} with terminal pass.", terminalPaymentData);
            } else {
                logger.error("Payment id {} does not have a parent terminal pass!", payment.getId());
            }
        } else {
            logger.warn("Payment id {} not found", payment);
        }
        return terminalPaymentData;

    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> newPayment(@RequestBody TerminalPaymentData terminalPaymentData, UriComponentsBuilder ucBuilder) {
        logger.info("Saving new payment {}", terminalPaymentData);

        if (!terminalPaymentData.isValid()) {
            logger.warn("Validation failure {}.", terminalPaymentData);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //begin transaction
        //this should be in the a service
        TerminalPaymentData savedRow = this.newBusPayment(terminalPaymentData);
        //end transaction

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/publi/api/bus_payment/{id}")
                .buildAndExpand(savedRow.getPayment().getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);

    }

    @Transactional
    private TerminalPaymentData newBusPayment(TerminalPaymentData terminalPaymentData) {
        logger.info("Saving payment {}", terminalPaymentData);
        //save new payment
        Payment savedPayment = paymentService.save(terminalPaymentData.getPayment());
        Long refId = savedPayment.getId();

        //save new payment items
        for (PaymentItem item : terminalPaymentData.getPayment().getPaymentItems()) {
            item.setPaymentId(refId);
            PaymentItem savedItem = paymentItemService.save(item);
            //append to payment
            savedPayment.getPaymentItems().add(savedItem);
        }

        //update terminal pass
        terminalPaymentData.getTerminalPass().setPaymentIdNumber(refId);
        TerminalPass savedTerminalPass = terminalPassService.save(terminalPaymentData.getTerminalPass());

        return new TerminalPaymentData(savedTerminalPass, savedPayment);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deletePayment(@PathVariable("id") Long id) {
        logger.info("Delete payment with id{}", id);

        TerminalPaymentData TerminalPaymentData = this.findById(id);
        if (TerminalPaymentData == null) {
            logger.warn("Payment data {} not found!.", TerminalPaymentData);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        //begin transaction
        //this should be in the a service
        boolean result = this.deleteTerminalPayment(id);
        //end transaction

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Transactional
    private boolean deleteTerminalPayment(Long id) {
        logger.info("Payment payment id {}", id);
        boolean result = false;
        TerminalPass terminalPass = null;
        Payment payment = null;

        TerminalPaymentData terminalPaymentData = this.findById(id);
        if (terminalPaymentData != null) {
            //delete payment items
            payment = terminalPaymentData.getPayment();
            for (PaymentItem item : payment.getPaymentItems()) {
                logger.info("Deleting Payment Item {}", item);
                paymentItemService.delete(item.getId());
            }
            //delete payment
            logger.info("Deleting Payment {}", payment);
            paymentService.delete(payment.getId());

            //update terminal pass if exist
            terminalPass = terminalPaymentData.getTerminalPass();
            if (terminalPass != null) {
                terminalPass.setPaymentIdNumber(null);
                logger.info("Updating parent terminal pass {}.", terminalPass);
                terminalPassService.save(terminalPass);
                result = true;
            }
        }
        return result;
    }

}
