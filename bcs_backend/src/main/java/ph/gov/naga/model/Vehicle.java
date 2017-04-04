/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author Drei
 */
@Entity
@Table(name = "vehicle")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "body_number")
    private String bodyNumber;

    @Column(name = "company")
    private String busCompany;
    
    @Column(name = "make_model")    
    private String makeModel;
    
    @Column(name = "motor_number")    
    private String motorNumber;
    
    @Column(name = "chassis_number")    
    private String chassisNumber;
    
    @Column(name = "case_number")    
    private String caseNumber;
    
    
    
    
    

}
