package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="expenses")
public class Expense extends Model {

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    public User user;

    @ManyToOne
    @JoinColumn(name="createdBy", nullable=false)
    public User createdBy;

    @Column(columnDefinition="Decimal(8,2)", nullable=false)
    public double amount;

    public String description;

    @Column(name="addDate", nullable=false)
    public Date addDate;

    public Date lastModifiedDate;

    @OneToMany
    @JoinTable(name = "expense_dues",
            joinColumns = @JoinColumn(name="expense_id"),
            inverseJoinColumns = @JoinColumn(name="due_id"))
    public List<Due> dues;

}