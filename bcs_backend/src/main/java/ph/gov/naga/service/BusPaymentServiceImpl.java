/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.naga.domain.BusPaymentData;
import ph.gov.naga.model.Payment;
import ph.gov.naga.model.PaymentItem;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Service
public class BusPaymentServiceImpl implements BusPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(BusPaymentServiceImpl.class);

    @Autowired
    private TerminalPassService terminalPassService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentItemService paymentItemService;

    @Override
    public List<BusPaymentData> findAllForPayment() {
        logger.info("Find all Terminal Pass for payment.");

        List<BusPaymentData> result = new ArrayList<>();

        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();

        for (TerminalPass tp : terminalPassList) {
            if (tp.getStatus().compareToIgnoreCase("ASSESSED") == 0
                    || tp.getStatus().compareToIgnoreCase("PAID") == 0
                    || tp.getStatus().compareToIgnoreCase("APPROVED") == 0) {

                if (tp.getPaymentIdNumber() != null && tp.getPaymentIdNumber() > 0L) {
                    Payment payment = this.paymentService.findById(tp.getPaymentIdNumber());
                    if (payment != null) {
                        List<PaymentItem> paymentItems = this.paymentItemService.findByPaymentId(payment.getId());
                        if (paymentItems != null) {
                            result.add(new BusPaymentData(tp, payment, paymentItems));
                        } else {
                            logger.error("Unable to find child payment items for payment {}.", payment.getId());
                        }
                    } else {
                        logger.error("Terminal Pass {} is marked with incorrect paymentIdNumber().", tp.getId());
                        result.add(new BusPaymentData(tp, new Payment(), new ArrayList<PaymentItem>()));
                    }
                } else {
                    logger.info("Terminal Pass {} does not have payment data.", tp.getId());
                    result.add(new BusPaymentData(tp, new Payment(), new ArrayList<PaymentItem>()));
                }
            }
        }
        return result;
    }

    @Override
    public BusPaymentData findPaymentById(Long id) {
        logger.info("Finding Bus payment with id {}", id);

        BusPaymentData busPaymentData = null;
        Payment payment = paymentService.findById(id);
        if (payment != null) {
            logger.debug("Found {}, now finding the parent terminal pass...", payment);
            TerminalPass terminalPass = terminalPassService.findByPaymentIdNumber(payment.getId());
            List<PaymentItem> paymentItems = this.paymentItemService.findByPaymentId(payment.getId());
            if (terminalPass == null) {
                logger.warn("Unable to find parent terminal pass.");
            } else if (paymentItems == null) {
                logger.warn("Unable to find child payment items.");
            } else {
                busPaymentData = new BusPaymentData(terminalPass, payment, paymentItems);
                logger.info("Found payment data {} with terminal pass.", busPaymentData);
            }
        } else {
            logger.warn("Payment id {} not found", payment);
        }
        return busPaymentData;
    }

    @Override
    public BusPaymentData findTerminalPassById(Long id) {
        logger.info("Finding Terminal Pass with id {}", id);
        List<BusPaymentData> inside = findAllForPayment();
        for (BusPaymentData bp : inside) {
            if (bp.getTerminalPass().getId().compareTo(id) == 0) {
                logger.info("Found Terminal Pass {}", bp);
                return bp;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteTerminalPayment(Long id) {
        logger.info("Attempting to delete Payment id {}", id);        
        Payment payment = null;
        TerminalPass terminalPass = null;

        BusPaymentData busPaymentData = this.findPaymentById(id);
        if (busPaymentData != null) {
            
            //update terminal pass if exist, unlink ref
            terminalPass = this.terminalPassService.findById(busPaymentData.getTerminalPass().getId());
            if (terminalPass != null) {
                terminalPass.setPaymentIdNumber(null);
                if (terminalPass.getStatus().compareToIgnoreCase("PAID") == 0) {
                    terminalPass.setStatus("ASSESSED");
                }
                logger.info("Updating parent terminal pass {}.", terminalPass);
                terminalPassService.save(terminalPass);            
            } else {
                logger.error("Parent Terminal Pass not found");
            }
            
            
            //delete payment items
            payment = busPaymentData.getPayment();
            List<PaymentItem> paymentItems = this.paymentItemService.findByPaymentId(id);
            if (paymentItems != null) {
                logger.info("Attempting to delete Payment Items");
                for (PaymentItem item : paymentItems) {
                    logger.info("Deleting Payment Item {}", item);
                    paymentItemService.delete(item.getId());
                }
            }
            //delete payment
            logger.info("Deleting Payment {}", payment);
            paymentService.delete(payment.getId());

        }        
    }

    @Override
    @Transactional
    public BusPaymentData newTerminalPayment(BusPaymentData busPaymentData) {
        logger.info("Saving new payment {}", busPaymentData);

        //save new payment
        Payment savedPayment = paymentService.save(busPaymentData.getPayment());
        Long refId = savedPayment.getId();

        //append new payment items
        List<PaymentItem> savedPaymentItems = new ArrayList<>();
        for (PaymentItem item : busPaymentData.getPaymentItems()) {
            item.setPaymentId(refId);
            PaymentItem savedItem = paymentItemService.save(item);

            savedPaymentItems.add(savedItem);
        }

        //link terminal pass to new payment
        TerminalPass terminalPass = terminalPassService.findById(busPaymentData.getTerminalPass().getId());
        terminalPass.setStatus("PAID");
        terminalPass.setPaymentIdNumber(refId);
        TerminalPass savedTerminalPass = terminalPassService.save(terminalPass);

        return new BusPaymentData(savedTerminalPass, savedPayment, savedPaymentItems);
    }

    @Override
    @Transactional
    public BusPaymentData updateTerminalPayment(BusPaymentData busPaymentData) {
        logger.info("Updating payment {}", busPaymentData);

        //attempt to delete and remove any dependencies
        if (busPaymentData.getPayment().getId() != null) {
            deleteTerminalPayment(busPaymentData.getPayment().getId());
        }

        //save new payment
        Payment savedPayment = paymentService.save(busPaymentData.getPayment());
        Long refId = savedPayment.getId();

        //save new payment items
        List<PaymentItem> savedPaymentItems = new ArrayList<>();
        for (PaymentItem item : busPaymentData.getPaymentItems()) {
            item.setPaymentId(refId);
            PaymentItem savedItem = paymentItemService.save(item);

            savedPaymentItems.add(savedItem);
        }

        //update terminal pass        
        TerminalPass terminalPass = terminalPassService.findById(busPaymentData.getTerminalPass().getId());
        terminalPass.setStatus("PAID");
        terminalPass.setPaymentIdNumber(refId);
        TerminalPass savedTerminalPass = terminalPassService.save(terminalPass);

        return new BusPaymentData(savedTerminalPass, savedPayment, savedPaymentItems);
    }
    
    

}
