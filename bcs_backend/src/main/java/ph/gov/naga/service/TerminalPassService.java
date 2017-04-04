/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
public interface TerminalPassService {

    Long countAll();
    
    List<TerminalPass> findAll();

    Page<TerminalPass> findAllPageable(Pageable pgbl);

    TerminalPass findById(Long id);

    List<TerminalPass> findByPlateNumber(String plateNumber);

    List<TerminalPass> findByBodyNumber(String bodyNumber);
    
    TerminalPass findByPaymentIdNumber(Long paymentIdNumber);

    TerminalPass save(TerminalPass vehicle);

    void delete(Long id);

    List<TerminalPass> findVehiclesInsideTerminal();

    boolean isInsideTerminal(String plateNumber);

}
