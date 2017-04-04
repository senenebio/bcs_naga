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
import ph.gov.naga.domain.DepartureData;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
@Service
public class DepartureServiceImpl implements DepartureService {

    private static final Logger logger = LoggerFactory.getLogger(DepartureServiceImpl.class);

    @Autowired
    TerminalPassService terminalPassService;

    @Override
    public boolean isVehicleAllowedExit(DepartureData departureData) {
        logger.info("Checking if vehicle is allowed to depart {}", departureData);
        boolean result = departureData != null && departureData.isValid();
        //is valid and vehicle have pending terminal pass
        result = result && this.terminalPassService.isInsideTerminal(
                departureData.getTerminalPass().getPlateNumber()) == true;

        return result;
    }

    @Override
    public DepartureData findUndepartedVehicleByPlateNumber(String plateNumber) {
        logger.info("Checking if vehicle {} is inside terminal", plateNumber);
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                return new DepartureData(tp);
            }
        }
        return null;
    }

    @Override
    public DepartureData findUndepartedVehicleById(Long id) {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getId().compareTo(id) == 0) {
                return new DepartureData(tp);
            }
        }
        return null;
    }

    //@Override
    //public List<DepartureData> createDepartureListFromTerminalPassList(List<TerminalPass> terminalPassList) {
    //    List<DepartureData> result = new ArrayList<>();
    //    /*
    //    terminalPassList.stream().forEach((tp) -> {
    //        result.add(new DepartureData(tp));
    //    });
    //    */
    //    //non-functional
    //    for (TerminalPass tp : terminalPassList) {
    //        result.add(new DepartureData(tp));
    //    }
    //    return result;
    //}
    @Override
    public List<DepartureData> findAllVehiclesAllowedToDepart() {
        List<TerminalPass> terminalPassList = this.terminalPassService.findVehiclesInsideTerminal();
        List<DepartureData> result = new ArrayList<>();
        for (TerminalPass tp : terminalPassList) {
            if (tp.getStatus().compareToIgnoreCase("APPROVED") == 0) {
                result.add(new DepartureData(tp));
            }
        }
        return result;
    }

}
