/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.controller.external;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.naga.domain.ApprovalData;
import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.service.ApprovalService;
import ph.gov.naga.service.TerminalPassService;

/**
 *
 * @author Drei
 */
@RestController
@CrossOrigin
@RequestMapping("/public/api/approval")
public class ApprovalController {

    public static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);

    @Autowired
    ApprovalService approvalService;

    @Autowired
    TerminalPassService terminalPassService;

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody ApprovalData approvalData) {
        logger.info("Updating TerminalPass {} from approval data: {}", id, approvalData);

        if (!approvalData.isValid()) {
            logger.error("Invalid approval data!");
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TerminalPass tp = terminalPassService.findById(id);
        if (tp == null) {
            logger.warn("Terminal Pass with ID () does not exist.");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        tp.setApprovedTime(approvalData.getTerminalPass().getApprovedTime());
        tp.setApprovedBy(approvalData.getTerminalPass().getApprovedBy());
        tp.setStatus("APPROVED");

        TerminalPass savedRow = terminalPassService.save(tp);
        if (savedRow == null) {
            logger.error("Unable to save : {}", tp);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //success, return the latest from db
        return new ResponseEntity<>(new ApprovalData(savedRow), HttpStatus.OK);
    }

    @RequestMapping(value = "/")
    public ResponseEntity<?> findAllUndepartedVehicles() {
        logger.info("Finding all terminal pass for Approval.");

        List<ApprovalData> inside = approvalService.findAllVehiclesForApproval();

        if (inside.isEmpty()) {
            logger.warn("There are no terminal passes for approval.");
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(inside, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        logger.info("Find Terminal Pass with id {}", id);
        ApprovalData approvalData = approvalService.findUndepartedVehicleById(id);
        if (approvalData == null) {
            logger.warn("Vehicle with id {} is not inside terminal.", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(approvalData, HttpStatus.OK);

    }

    @RequestMapping(value = "/findVehicleByPlateNumber/{plateNumber}")
    public ResponseEntity<?> findByPlateNumberVehicleStillInsideTerminal(@PathVariable("plateNumber") String plateNumber) {
        logger.info("Find Vehicle plate number is still inside terminal {}", plateNumber);
        ApprovalData approvalData = approvalService.findUndepartedVehicleByPlateNumber(plateNumber);
        if (approvalData == null) {
            logger.warn("Vehicle with plate number {} is not inside terminal.", plateNumber);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(approvalData, HttpStatus.OK);

    }

}
