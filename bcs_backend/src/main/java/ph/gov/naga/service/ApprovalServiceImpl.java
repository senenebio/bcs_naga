/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ph.gov.naga.domain.ApprovalData;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    @Autowired
    TerminalPassService terminalPassService;

    
    @Override
    public ApprovalData findUndepartedVehicleByPlateNumber(String plateNumber) {
        logger.info("Checking if vehicle {} is inside terminal", plateNumber);
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                return new ApprovalData(tp);
            }
        }
        return null;
    }

    @Override
    public ApprovalData findUndepartedVehicleById(Long id) {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getId().compareTo(id) == 0) {
                return new ApprovalData(tp);
            }
        }
        return null;
    }

    

    @Override
    public List<ApprovalData> findAllVehiclesForApproval() {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        List<ApprovalData> result = new ArrayList<>();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getStatus().compareToIgnoreCase("ASSESSED") == 0 ||
                tp.getStatus().compareToIgnoreCase("PAID") == 0 ||
                tp.getStatus().compareToIgnoreCase("APPROVED") == 0) {
                result.add(new ApprovalData(tp));
            }
        }
        return result;
    }
}
