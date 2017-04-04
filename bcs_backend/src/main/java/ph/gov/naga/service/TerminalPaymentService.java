/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;
import ph.gov.naga.domain.TerminalPaymentData;

/**
 *
 * @author Drei
 */
public interface TerminalPaymentService {

    TerminalPaymentData findById(Long id);

    TerminalPaymentData newTerminalPayment(TerminalPaymentData terminalPaymentData);

    boolean deleteTerminalPayment(Long id);

}
