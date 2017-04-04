/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.naga.model.PaymentItem;

/**
 *
 * @author Drei
 */
public interface PaymentItemService {

    Long countAll();
    
    List<PaymentItem> findAll();

    Page<PaymentItem> findAllPageable(Pageable pgbl);

    PaymentItem findById(Long id);

    List<PaymentItem> findByPaymentId(Long paymentId);

    PaymentItem save(PaymentItem paymentItem);

    void delete(Long id);

}
