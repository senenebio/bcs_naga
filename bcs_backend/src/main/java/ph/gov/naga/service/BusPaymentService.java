/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import ph.gov.naga.domain.BusPaymentData;
import ph.gov.naga.model.PaymentItem;

/**
 *
 * @author Drei
 */
public interface BusPaymentService {
    
    List<BusPaymentData> findAllForPayment();
    
    BusPaymentData findPaymentById(Long id);
    
    BusPaymentData findTerminalPassById(Long id);
    
    //List<PaymentItem> findPaymentItemByPaymentId(Long id);    
    
    void deleteTerminalPayment(Long id);
    
    BusPaymentData newTerminalPayment(BusPaymentData busPaymentData);

    BusPaymentData updateTerminalPayment(BusPaymentData busPaymentData);
}
