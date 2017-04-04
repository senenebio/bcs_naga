/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.model;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author Drei
 */
@Entity
@Table(name = "terminal_pass")
@Data
public class TerminalPass {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "body_number")
    private String bodyNumber;

    @Column(name = "company")
    private String busCompany;

    @Column(name = "arrival_time")
    private Date arrivalTime;

    @Column(name = "arrival_origin")
    private String arrivalOrigin;

    @Column(name = "arrival_destination")
    private String arrivalDestination;

    @Column(name = "arrival_recorder")
    private String arrivalRecorder;

    @Column(name = "trip_type")
    private String tripType;

    @Column(name = "trip_coverage")
    private String tripCoverage;

    @Column(name = "trip_origin")
    private String tripOrigin;

    @Column(name = "trip_destination")
    private String tripDestination;

    @Column(name = "trip_unloading_bay")
    private String tripUnloadingBay;

    @Column(name = "trip_unloading_start")
    private Date tripUnloadingStart;

    @Column(name = "trip_unloading_end")
    private Date tripUnloadingEnd;

    @Column(name = "trip_loading_bay")
    private String tripLoadingBay;

    @Column(name = "trip_loading_start")
    private Date tripLoadingStart;

    @Column(name = "trip_loading_end")
    private Date tripLoadingEnd;

    @Column(name = "trip_terminal_fee")
    private BigDecimal tripTerminalFee;

    @Column(name = "trip_parking_fee")
    private BigDecimal tripParkingFee;

    @Column(name = "trip_assessor")
    private String tripAssessor;

    @Column(name = "departure_time")
    private Date departureTime;

    @Column(name = "departure_recorder")
    private String departureRecorder;

    @Column(name = "payment_id_number")
    private Long paymentIdNumber;

    @Column(name = "status")
    private String status;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_time")
    private Date approvedTime;

    @PrePersist
    private void prePersist() {
        //save default values
        if (status == null) {
            status = "UNKNOWN";
        }
        if (tripCoverage == null) {
            tripCoverage = "UNKNOWN";
        }
        if (tripType == null) {
            tripType = "UNKNOWN";
        }

        if (tripTerminalFee == null) {
            tripTerminalFee = new BigDecimal(0);
        }

        if (tripParkingFee == null) {
            tripParkingFee = new BigDecimal(0);
        }
    }
}
