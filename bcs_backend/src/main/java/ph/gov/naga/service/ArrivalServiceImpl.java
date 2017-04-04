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
import ph.gov.naga.domain.ArrivalData;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Service
public class ArrivalServiceImpl implements ArrivalService {

    private static final Logger logger = LoggerFactory.getLogger(ArrivalServiceImpl.class);

    @Autowired
    TerminalPassService terminalPassService;

    @Override
    public boolean isVehicleAllowedEntry(String plateNumber) {
        logger.info("Checking if plateNumber is allowed entry {}", plateNumber);
        //vehicle has no pending terminal pass
        boolean result = plateNumber !=null &&
                !plateNumber.isEmpty() &&
                this.terminalPassService.isInsideTerminal(plateNumber) == false;
        return result;
    }

    @Override
    public ArrivalData findUndepartedVehicleByPlateNumber(String plateNumber) {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                //second check really necessary?
                String status = tp.getStatus();
                if (status != null && status.compareToIgnoreCase("DEPARTED") != 0) {
                    return new ArrivalData(tp);
                }
            }
        }
        return null;
    }

    @Override
    public ArrivalData findUndepartedVehicleById(Long id) {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getId().compareTo(id) == 0) {
                return new ArrivalData(tp);
            }
        }
        return null;
    }

    @Override
    public List<ArrivalData> findAllUndepartedVehicles() {
        List<ArrivalData> result = new ArrayList<>();
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            result.add(new ArrivalData(tp));
        }
        return result;
    }

}
