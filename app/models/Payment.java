package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * A payment represents a transaction between two users where {@link fromUser}
 * paid {@link amount} to {@link toUser}.
 */
@Entity
@Table(name="payments")
public class Payment extends Model {

    @ManyToOne
    @JoinColumn(name="fromUser_id", nullable=false)
    public User fromUser;

    @ManyToOne
    @JoinColumn(name="toUser_id", nullable=false)
    public User toUser;

    @Column(columnDefinition="Decimal(8,2)", nullable=false)
    public double amount;

    public String description;

    public Date addDate;

    public Date lastModifiedDate;

    public static void savePayment(Payment payment) {
        payment.save();
        Balance.adjustBalance(payment.fromUser, payment.toUser, -1 * payment.amount);
    }
}