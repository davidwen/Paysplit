package controllers;

import java.util.List;

import models.Balance;
import models.Due;
import models.Payment;
import models.Transaction;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

@With(UserLogin.class)
public class Interuser extends Controller{

    public static void showStatistics(String fromUsername, String toUsername) {
        User fromUser = User.fromUsername(fromUsername);
        User toUser = User.fromUsername(toUsername);
        if (fromUser == null || toUser == null) {
            return;
        }
        Balance balance = Balance.fromUsers(fromUser, toUser);
        render(fromUser, toUser, balance);
    }

    public static void listInteruserTransactionsDataTable(
        String fromUsername,
        String toUsername,
        int iDisplayStart,
        int iDisplayLength)
    {
        User fromUser = User.fromUsername(fromUsername);
        User toUser = User.fromUsername(toUsername);
        if (fromUser == null || toUser == null) {
            return;
        }
        List<Payment> payments = Payment.find("byFromUserAndToUser", fromUser, toUser).fetch();
        List<Payment> paymentsToFrom = Payment.find("byFromUserAndToUser", toUser, fromUser).fetch();
        payments.addAll(paymentsToFrom);
        List<Due> dues = Due.find("byFromUserAndToUser", fromUser, toUser).fetch();
        List<Due> duesToFrom = Due.find("byFromUserAndToUser", toUser, fromUser).fetch();
        dues.addAll(duesToFrom);
        List<Transaction> transactions = Transaction.fromDuesAndPayments(
                dues, payments);
        int total = transactions.size();
        int numMatched = transactions.size();
        transactions = transactions.subList(
            iDisplayStart,
            Math.min(iDisplayStart + iDisplayLength, transactions.size()));
        render("transactions.json", transactions, total, numMatched);
    }
}
