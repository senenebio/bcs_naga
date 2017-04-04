/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.naga.model.Payment;

/**
 *
 * @author Drei
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment origReceiptNumber(Long orNumber);
}

