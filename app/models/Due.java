package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * A due represents a payment due from one user to another as a result of a
 * created {@link Expense}.
 */
@Entity
@Table(name="dues")
public class Due extends Model{

    @ManyToOne
    @JoinColumn(name="fromUser_id", nullable=false)
    public User fromUser;

    @ManyToOne
    @JoinColumn(name="toUser_id", nullable=false)
    public User toUser;

    @Column(columnDefinition="Decimal(8,2)", nullable=false)
    public double amount;

    @Column(name="addDate", nullable=false)
    public Date addDate;

    public Date lastModifiedDate;

    @ManyToOne
    @JoinTable(name = "expense_dues",
            joinColumns = @JoinColumn(name="due_id"),
            inverseJoinColumns = @JoinColumn(name="expense_id"))
    public Expense expense;

    public static void saveDue(Due due) {
        due.save();
        Balance.adjustBalance(due.fromUser, due.toUser, due.amount);
    }

}
