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
import ph.gov.naga.domain.AssessmentData;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Service
public class AssessmentServiceImpl implements AssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentServiceImpl.class);

    @Autowired
    TerminalPassService terminalPassService;

    @Override
    public AssessmentData findUndepartedVehiclesByPlateNumber(String plateNumber) {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                return new AssessmentData(tp);
            }
        }
        return null;
    }

    @Override
    public AssessmentData findUndepartedVehiclesById(Long id) {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getId().compareTo(id) == 0) {
                return new AssessmentData(tp);
            }
        }
        return null;
    }

    @Override
    public List<AssessmentData> findAllUndepartedVehicles() {
        List<AssessmentData> result = new ArrayList<>();
        List<TerminalPass> terminalPassList = terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            result.add(new AssessmentData(tp));
        }
        return result;
    }

}
