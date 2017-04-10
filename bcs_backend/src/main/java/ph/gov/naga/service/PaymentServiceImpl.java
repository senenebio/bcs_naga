/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ph.gov.naga.model.Payment;
import ph.gov.naga.repository.PaymentRepository;

/**
 *
 * @author Drei
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Long countAll() {
        return paymentRepository.count();
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public Page<Payment> findAllPageable(Pageable pgble) {
        return paymentRepository.findAll(pgble);
    }

    @Override
    public Payment findById(Long id) {
        return paymentRepository.findOne(id);
    }

    @Override
    public Payment findByOrigReceiptNumber(Long orNumber) {
        return paymentRepository.origReceiptNumber(orNumber);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.saveAndFlush(payment);
    }

    @Override
    public void delete(Long id) {
        paymentRepository.delete(id);
        paymentRepository.flush();
    }

}
