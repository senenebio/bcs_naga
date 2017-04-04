/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ph.gov.naga.domain.TerminalPaymentData;
import ph.gov.naga.model.Payment;
import ph.gov.naga.model.PaymentItem;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.repository.PaymentItemRepository;
import ph.gov.naga.repository.PaymentRepository;
import ph.gov.naga.repository.TerminalPassRepository;

/**
 *
 * @author Drei
 */
public class TerminalPaymentServiceImpl implements TerminalPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(TerminalPaymentServiceImpl.class);

    @Autowired
    TerminalPassRepository terminalPassRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentItemRepository paymentItemRepository;

    @Override
    public TerminalPaymentData findById(Long id) {

        logger.info("Finding bus payment with id {}", id);

        TerminalPaymentData terminalPaymentData = null;
        TerminalPass terminalPass = null;
        Payment payment = paymentRepository.findOne(id);

        if (payment != null) {
            logger.debug("Found {}, now finding the parent terminal pass...", payment);
            terminalPass = terminalPassRepository.findOne(payment.getId());
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

    @Override
    public TerminalPaymentData newTerminalPayment(TerminalPaymentData terminalPaymentData) {
        logger.info("Saving payment {}", terminalPaymentData);
        //save new payment
        Payment savedPayment = paymentRepository.save(terminalPaymentData.getPayment());
        Long refId = savedPayment.getId();

        //save new payment items
        for (PaymentItem item : terminalPaymentData.getPayment().getPaymentItems()) {
            item.setPaymentId(refId);
            PaymentItem savedItem = paymentItemRepository.saveAndFlush(item);
            //append to payment
            savedPayment.getPaymentItems().add(savedItem);
        }

        //update terminal pass
        terminalPaymentData.getTerminalPass().setPaymentIdNumber(refId);
        TerminalPass savedTerminalPass = terminalPassRepository.save(terminalPaymentData.getTerminalPass());

        return new TerminalPaymentData(savedTerminalPass, savedPayment);
    }

    @Override
    public boolean deleteTerminalPayment(Long id) {
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
                paymentItemRepository.delete(item.getId());
            }
            //delete payment
            logger.info("Deleting Payment {}", payment);
            paymentRepository.delete(payment.getId());

            //update terminal pass if exist
            terminalPass = terminalPaymentData.getTerminalPass();
            if (terminalPass != null) {
                terminalPass.setPaymentIdNumber(null);
                logger.info("Updating parent terminal pass {}.", terminalPass);
                terminalPassRepository.save(terminalPass);
                result = true;
            }
        }
        return result;
    }

}
