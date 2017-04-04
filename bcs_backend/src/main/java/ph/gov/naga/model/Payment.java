/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author Drei
 */
@Entity
@Table(name = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "or_number")
    private String origReceiptNumber;

    @Column(name = "or_date")
    private Date origReceiptDate;

    @Column(name = "paidby")
    private String paidBy;

    @Column(name = "paidby_address")
    private String paidByAddress;

    @Column(name = "collectedby")
    private String collectedBy;

    @Column(name = "etracs_objid")
    private String etracsObjectId;

    @Column(name = "etracs_postdate")
    private String etracsPostDate;

    //see: http://stackoverflow.com/questions/3325387/infinite-recursion-with-jackson-json-and-hibernate-jpa-issue
    @OneToMany(mappedBy = "payment")
    @JsonManagedReference
    List<PaymentItem> paymentItems;
}
