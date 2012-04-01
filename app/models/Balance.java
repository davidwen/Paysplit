package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.jpa.Model;


/**
 * A balance represents a difference between two users that needs to be paid
 * off. A positive balance means that {@link #fromUser} owes {@link #balance}
 * to {@link #toUser}.
 */
@Entity
@Table(name="balances")
public class Balance extends Model{

    @OneToOne
    @JoinColumn(name="fromUser_id", nullable=false)
    public User fromUser;

    @OneToOne
    @JoinColumn(name="toUser_id", nullable=false)
    public User toUser;

    @Column(columnDefinition="Decimal(8,2)", nullable=false)
    public double balance;

    /**
     * Adds given amount to the balance from fromUser to toUser.
     */
    public static void adjustBalance(User fromUser, User toUser, double amount) {
        Balance balanceFromTo = Balance.fromUsers(fromUser, toUser);
        Balance balanceToFrom = Balance.fromUsers(toUser, fromUser);
        balanceFromTo.balance += amount;
        balanceToFrom.balance -= amount;
        balanceFromTo.save();
        balanceToFrom.save();
    }

    public static Balance fromUsers(User fromUser, User toUser) {
        List<Balance> balances = Balance.find(
                "byFromUserAndToUser", fromUser, toUser).fetch();
        if (balances.size() == 1) {
            return balances.get(0);
        } else if (balances.isEmpty()) {
            Balance balance = new Balance();
            balance.fromUser = fromUser;
            balance.toUser = toUser;
            balance.balance = 0.0;
            balance.save();
            return balance;
        } else {
            throw new RuntimeException(
                    "Unexpected number of balances with usernames " +
                    fromUser.username + ", " + toUser.username +
                    ": " + balances.size());
        }
    }

    public static List<Balance> getPositiveBalances(User fromUser) {
        return Balance.find("byFromUserAndBalanceGreaterThan", fromUser, 0.0).fetch();
    }

    public static List<Balance> getNegativeBalances(User fromUser) {
        return Balance.find("byFromUserAndBalanceLessThan", fromUser, 0.0).fetch();
    }

}