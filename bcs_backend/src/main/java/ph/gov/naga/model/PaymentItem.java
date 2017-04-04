/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.naga.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author Drei
 */
@Entity
@Table(name = "payment_item")
@Data
public class PaymentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "item_title")
    private String itemTitle;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "payment_id")
    private Long paymentId;

    //see: http://stackoverflow.com/questions/3325387/infinite-recursion-with-jackson-json-and-hibernate-jpa-issue
    @ManyToOne
    @JoinColumn(name = "payment_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonBackReference
    private Payment payment;

}
