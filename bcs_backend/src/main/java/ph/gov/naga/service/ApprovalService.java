/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import ph.gov.naga.domain.ApprovalData;

/**
 *
 * @author Drei
 */
public interface ApprovalService {

    ApprovalData findUndepartedVehicleByPlateNumber(String plateNumber);

    ApprovalData findUndepartedVehicleById(Long id);

    List<ApprovalData> findAllVehiclesForApproval();

}
