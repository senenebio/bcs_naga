/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.repository.TerminalPassRepository;

/**
 *
 * @author Drei
 */
@Service
public class TerminalPassServiceImpl implements TerminalPassService {

    private static final Logger logger = LoggerFactory.getLogger(TerminalPassServiceImpl.class);

    @Autowired
    private TerminalPassRepository terminalPassRepository;

    @Override
    public Long countAll() {
        return terminalPassRepository.count();
    }

    @Override
    public List<TerminalPass> findAll() {
        return terminalPassRepository.findAll();
    }

    @Override
    public Page<TerminalPass> findAllPageable(Pageable pgbl) {
        return terminalPassRepository.findAll(pgbl);
    }

    @Override
    public TerminalPass findById(Long id) {
        return terminalPassRepository.findOne(id);
    }

    @Override
    public List<TerminalPass> findByPlateNumber(String plateNumber) {
        return terminalPassRepository.findByPlateNumberOrderByIdDesc(plateNumber);
    }

    @Override
    public List<TerminalPass> findByBodyNumber(String bodyNumber) {
        return terminalPassRepository.findByBodyNumberOrderByIdDesc(bodyNumber);
    }

    @Override
    public TerminalPass save(TerminalPass terminalPass) {
        return terminalPassRepository.saveAndFlush(terminalPass);
    }

    @Override
    public void delete(Long id) {
        terminalPassRepository.delete(id);
        terminalPassRepository.flush();
    }

    @Override
    public List<TerminalPass> findVehiclesInsideTerminal() {
        //this is expensive, alternative: delegate to controller and expose only find by status 
        List<TerminalPass> inside = terminalPassRepository.findByStatus("ARRIVED");
        inside.addAll(terminalPassRepository.findByStatus("ASSESSED"));
        inside.addAll(terminalPassRepository.findByStatus("PAID"));
        inside.addAll(terminalPassRepository.findByStatus("APPROVED"));
        return inside;
    }

    @Override
    public boolean isInsideTerminal(String plateNumber) {
        /*
         return findVehiclesInsideTerminal()
         .stream()
         .anyMatch((tp) -> (tp.getPlateNumber().compareToIgnoreCase(plateNumber) == 0));
         */
        //non functional
        for (TerminalPass tp : findVehiclesInsideTerminal()) {
            if (tp.getPlateNumber().compareToIgnoreCase(plateNumber) == 0) {
                return true;
            }
        }
        return false;

    }

    @Override
    public TerminalPass findByPaymentIdNumber(Long paymentIdNumber) {
        return terminalPassRepository.findByPaymentIdNumber(paymentIdNumber);
    }

}
