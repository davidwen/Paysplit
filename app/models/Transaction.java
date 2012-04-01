package models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A transaction is a due or payment between two users.
 */

public class Transaction {

    public static enum Type {
        DUE,
        PAYMENT,
        EXPENSE;

        public static List<String> toStrings() {
            Type[] types = values();
            List<String> values = Lists.newLinkedList();
            for (Type type : types) {
                values.add(type.toString());
            }
            return values;
        }
    }

    public Type type;
    public User fromUser;
    public User toUser;
    public double amount;
    public String description;
    public Date addDate;
    public Date lastModifiedDate;
    public String url;

    public static class AddDateComparator implements Comparator<Transaction> {

        @Override
        public int compare(Transaction o1, Transaction o2) {
            return o2.addDate.compareTo(o1.addDate);
        }
    }

    public static Transaction fromDue(Due due) {
        Transaction t = new Transaction();
        t.type = Type.DUE;
        t.fromUser = due.fromUser;
        t.toUser = due.toUser;
        t.amount = due.amount;
        t.description = due.expense.description;
        t.addDate = due.addDate;
        t.lastModifiedDate = due.lastModifiedDate;
        t.url = Transaction.createUrl(t.type, due.id);
        return t;
    }

    public static Transaction fromExpense(Expense expense) {
        Transaction t = new Transaction();
        t.type = Type.EXPENSE;
        t.fromUser = expense.user;
        t.toUser = null;
        t.amount = expense.amount;
        t.description = expense.description;
        t.addDate = expense.addDate;
        t.lastModifiedDate = expense.lastModifiedDate;
        t.url = Transaction.createUrl(t.type, expense.id);
        return t;
    }

    public static Transaction fromPayment(Payment payment) {
        Transaction t = new Transaction();
        t.type = Type.PAYMENT;
        t.fromUser = payment.fromUser;
        t.toUser = payment.toUser;
        t.amount = payment.amount;
        t.description = payment.description;
        t.addDate = payment.addDate;
        t.lastModifiedDate = payment.lastModifiedDate;
        t.url = Transaction.createUrl(t.type, payment.id);
        return t;
    }

    public static List<Transaction> fromDuesAndPayments(
        List<Due> dues,
        List<Payment> payments)
    {
        List<Transaction> transactions = Lists.newLinkedList();
        for (Due due : dues) {
            transactions.add(fromDue(due));
        }

        for (Payment payment : payments) {
            transactions.add(fromPayment(payment));
        }
        Collections.sort(transactions, new AddDateComparator());
        return transactions;
    }

    public static List<Transaction> fromUser(User user) {
        List<Transaction> transactions = Lists.newLinkedList();
        List<Due> dues = Due.find("byFromUser", user).fetch();
        List<Due> duesTo = Due.find("byToUser", user).fetch();
        dues.addAll(duesTo);
        for (Due due : dues) {
            transactions.add(fromDue(due));
        }

        List<Payment> payments = Payment.find("byFromUser", user).fetch();
        List<Payment> paymentsTo = Payment.find("byToUser", user).fetch();
        payments.addAll(paymentsTo);
        for (Payment payment : payments) {
            transactions.add(fromPayment(payment));
        }

        List<Expense> expenses = Expense.find("byUser", user).fetch();
        for (Expense expense : expenses) {
            transactions.add(fromExpense(expense));
        }
        Collections.sort(transactions, new AddDateComparator());
        return transactions;
    }

    public static String createUrl(Type type, Long id) {
        switch(type){
            case DUE:
                Due due = Due.findById(id);
                return "/expenses/" + due.expense.id + "/show";
            case PAYMENT:
                return "/payments/" + id + "/show";
            case EXPENSE:
                return "/expenses/" + id + "/show";
            default:
                return null;
        }
    }

    public boolean matchesFilter(
        String searchUser,
        String searchType)
    {
        boolean userMatch = true;
        boolean typeMatch = true;
        if (searchUser != null && searchUser.trim().length() > 0) {
            if (!searchUser.equals(toUser == null ? null : toUser.username) &&
                !searchUser.equals(fromUser.username))
            {
                userMatch = false;
            }
        }
        if (searchType != null && searchType.trim().length() > 0) {
            if (!searchType.equals(type.toString())) {
                typeMatch = false;
            }
        }
        return userMatch && typeMatch;
    }

}
