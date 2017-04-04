/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import ph.gov.naga.domain.DepartureData;

/**
 *
 * @author Drei
 */
public interface DepartureService {
    
    
    boolean isVehicleAllowedExit(DepartureData departureData);
    
    DepartureData findUndepartedVehicleByPlateNumber (String plateNumber);
    
    DepartureData findUndepartedVehicleById (Long id);
    
    //List<DepartureData> createDepartureListFromTerminalPassList (List<TerminalPass> terminalPassList);
    
    List<DepartureData> findAllVehiclesAllowedToDepart ();

}
