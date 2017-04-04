/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.naga.model.Payment;

/**
 *
 * @author Drei
 */
public interface PaymentService {

    Long countAll();

    List<Payment> findAll();

    Page<Payment> findAllPageable(Pageable pgbl);

    Payment findById(Long id);

    Payment findByOrigReceiptNumber(Long orNumber);

    Payment save(Payment payment);

    void delete(Long id);

}
