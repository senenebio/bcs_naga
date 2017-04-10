/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ph.gov.naga.model.Vehicle;
import ph.gov.naga.repository.VehicleRepository;

/**
 *
 * @author Drei
 */
@Service
public class VehicleServiceImpl implements VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleServiceImpl.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public Long countAll() {
        return vehicleRepository.count();
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Override
    public Page<Vehicle> findAllPageable(Pageable pgbl) {
        return vehicleRepository.findAll(pgbl);
    }

    @Override
    public Vehicle findById(Long id) {
        return vehicleRepository.findOne(id);
    }

    @Override
    public Vehicle findByPlateNumber(String plateNumber) {
        return vehicleRepository.findByPlateNumber(plateNumber);
    }

    @Override
    public List<Vehicle> findByBodyNumber(String bodyNumber) {
        return vehicleRepository.findByBodyNumber(bodyNumber);

    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.saveAndFlush(vehicle);
    }

    @Override
    public void delete(Long id) {
        vehicleRepository.delete(id);
        vehicleRepository.flush();
    }

    @Override
    public List<Vehicle> findByPlateNumberContaining(String plateNumber) {
        return vehicleRepository.findByPlateNumberContaining(plateNumber);
    }

}
