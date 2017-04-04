/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.naga.model.PaymentItem;

/**
 *
 * @author Drei
 */
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {

    PaymentItem findById(Long id);

    List<PaymentItem> findByPaymentId(Long id);
}
