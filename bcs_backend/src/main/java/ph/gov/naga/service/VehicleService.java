/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.naga.model.Vehicle;

/**
 *
 * @author Drei
 */
public interface VehicleService {

    Long countAll ();
    
    List<Vehicle> findAll ();
    
    Page<Vehicle> findAllPageable(Pageable pgbl);

    Vehicle findById(Long id);

    Vehicle findByPlateNumber(String plateNumber);

    List<Vehicle> findByBodyNumber(String bodyNumber);

    Vehicle save(Vehicle vehicle);
    
    List<Vehicle> findByPlateNumberContaining(String plateNumber);

    void delete(Long id);

}
