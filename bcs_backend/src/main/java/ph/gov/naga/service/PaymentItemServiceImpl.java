/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ph.gov.naga.model.PaymentItem;
import ph.gov.naga.repository.PaymentItemRepository;

@Service
public class PaymentItemServiceImpl implements PaymentItemService {
    
    @Autowired
    private PaymentItemRepository paymentItemRepository;
    
    @Override
    public Long countAll() {
        return paymentItemRepository.count();
    }
    
    @Override
    public List<PaymentItem> findAll() {
        return paymentItemRepository.findAll();
    }
    
    @Override
    public Page<PaymentItem> findAllPageable(Pageable pgbl) {
        return paymentItemRepository.findAll(pgbl);
    }
    
    @Override
    public PaymentItem findById(Long id) {
        return paymentItemRepository.findOne(id);
    }
    
    @Override
    public List<PaymentItem> findByPaymentId(Long paymentId) {
        return paymentItemRepository.findByPaymentId(paymentId);
    }
    
    @Override
    public PaymentItem save(PaymentItem paymentItem) {
        return paymentItemRepository.saveAndFlush(paymentItem);
    }
    
    @Override
    public void delete(Long id) {
        paymentItemRepository.delete(id);
    }
    
}
