/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import ph.gov.naga.domain.ArrivalData;
import ph.gov.naga.model.TerminalPass;

/**
 *
 * @author Drei
 */
public interface ArrivalService {

    boolean isVehicleAllowedEntry(String plateNumber);

    ArrivalData findUndepartedVehicleByPlateNumber(String plateNumber);

    ArrivalData findUndepartedVehicleById(Long id);

    List<ArrivalData> findAllUndepartedVehicles();
}
