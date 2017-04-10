/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NonNull;
import ph.gov.naga.model.Payment;
import ph.gov.naga.model.PaymentItem;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Data
public class BusPaymentData {

    @NonNull
    private TerminalPass terminalPass;
    
    @NonNull
    private Payment payment;
    
    List<PaymentItem> paymentItems;

    public BusPaymentData(){
        terminalPass = new TerminalPass();
        payment = new Payment();
        paymentItems = new ArrayList<PaymentItem>();
        
    }
    
    public BusPaymentData(@NonNull TerminalPass terminalPass, 
            @NonNull Payment payment, @NonNull List<PaymentItem> paymentItems){
        this.terminalPass = terminalPass;
        this.payment = payment;
        this.paymentItems = paymentItems;
    }
    
    public boolean isValid (){
        boolean result = this.terminalPass != null && 
                this.payment != null &&
                this.paymentItems != null &&
                this.paymentItems.size() > 0;
        
        return result;
    }
    
}
