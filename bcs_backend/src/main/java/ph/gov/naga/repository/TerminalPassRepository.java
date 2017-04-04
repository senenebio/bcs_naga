/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
public interface TerminalPassRepository extends JpaRepository<TerminalPass, Long> {

    List<TerminalPass> findByPlateNumberOrderByIdDesc(String plateNumber);

    List<TerminalPass> findByBodyNumberOrderByIdDesc(String bodyNumber);

    List<TerminalPass> findByStatus(String status);
    
    TerminalPass findByPaymentIdNumber(long paymentIdNumber);
    
}
