/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.domain;

import lombok.Data;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import ph.gov.naga.model.Payment;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Data
public class TerminalPaymentData {

    @NonNull
    private TerminalPass terminalPass;    
    
    @NonNull
    private Payment payment;

    public TerminalPaymentData() {
        this.terminalPass = new TerminalPass();
        this.payment = new Payment();
    }

    
    public TerminalPaymentData(@NonNull TerminalPass tp, @NonNull Payment payment) {
        this.terminalPass = tp;
        this.payment = payment;
    }

    public boolean isValid() {
        boolean result = this.terminalPass != null
                && this.payment != null
                && this.payment.getCollectedBy() != null
                && this.payment.getOrigReceiptNumber() != null
                && this.payment.getOrigReceiptDate() != null;
        
        //may also need to check for payment items to ensure that amount is > 0
        
        return result;
    }

}
