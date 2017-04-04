/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.controller.internal;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import ph.gov.naga.model.TerminalPass;
import ph.gov.naga.service.TerminalPassService;

/**
 *
 * @author Drei
 */
@RestController
@RequestMapping("/internal/api/terminal_pass")
public class TerminalPassController {

    public static final Logger logger = LoggerFactory.getLogger(TerminalPassController.class);

    @Autowired
    private TerminalPassService terminalPassService;

    @RequestMapping("/")
    private ResponseEntity<Page<TerminalPass>> findAllPageble(Pageable pgbl) {
        Page<TerminalPass> result = terminalPassService.findAllPageable(pgbl);
        if (result.getNumberOfElements() <= 0) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/{id}")
    private ResponseEntity<TerminalPass> find(@PathVariable Long id) {
        TerminalPass result = terminalPassService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/findById/{id}")
    private ResponseEntity<TerminalPass> findById(@PathVariable Long id) {
        TerminalPass result = terminalPassService.findById(id);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/findByPlateNumber/{plateNumber}")
    private ResponseEntity<List<TerminalPass>> findByPlateNumber(@PathVariable String plateNumber) {
        List<TerminalPass> result = terminalPassService.findByPlateNumber(plateNumber);
        if (result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/findByBodyNumber/{bodyNumber}")
    private ResponseEntity<List<TerminalPass>> findByBodyNumber(@PathVariable String bodyNumber) {
        List<TerminalPass> result = terminalPassService.findByBodyNumber(bodyNumber);
        if (result.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody TerminalPass terminalPass, UriComponentsBuilder ucBuilder) {
        logger.info("Creating TerminalPass : {}", terminalPass);

        TerminalPass savedRow = terminalPassService.save(terminalPass);
        if (savedRow == null) {
            logger.error("Unable to create TerminalPass : {}", terminalPass);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/internal/api/terminal_passes/{id}").buildAndExpand(savedRow.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@PathVariable("id") long id, @RequestBody TerminalPass terminalPass) {
        logger.info("Updating TerminalPass with id {}", id);

        TerminalPass savedRow = terminalPassService.save(terminalPass);
        if (savedRow == null) {
            logger.error("Unable to update TerminalPass with id {} ", id);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(savedRow, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        logger.info("Deleting TerminalPass with id {}", id);
        
        TerminalPass savedRow = terminalPassService.findById(id);
        if (savedRow == null) {
            logger.error("Unable to TerminalPass vehicle with id {} ", id);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        //prevent deletion if already paid
        if (savedRow.getPaymentIdNumber() != null) {
            logger.error("Unable to delete TerminalPass with id {} ", id);
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        terminalPassService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
