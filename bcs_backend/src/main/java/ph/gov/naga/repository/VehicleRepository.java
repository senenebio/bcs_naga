/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.naga.model.Vehicle;

/**
 *
 * @author Drei
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Vehicle findByPlateNumber(String plateNumber);

    List<Vehicle> findByBodyNumber(String bodyNumber);

    List<Vehicle> findByPlateNumberContaining(String plateNumber);

}
