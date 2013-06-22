package controllers;

import java.util.List;

import models.Balance;
import models.Transaction;
import models.User;
import play.mvc.Controller;

public class Dashboard extends Controller{

    private static int DISPLAY_TRANSACTIONS_NUM = 5;

    public static void dashboard() {
        fillDashboardArgs();
        render();
    }

    public static void mDashboard() {
        fillDashboardArgs();
        render();
    }

    private static void fillDashboardArgs() {
        User user = User.fromSession(session);
        double negativeBalance = 0;
        double positiveBalance = 0;
        for (Balance balance : Balance.getNegativeBalances(user)) {
            negativeBalance += balance.balance;
        }
        negativeBalance = negativeBalance * -1;
        for (Balance balance : Balance.getPositiveBalances(user)) {
            positiveBalance += balance.balance;
        }
        List<Transaction> transactions = Transaction.fromUser(user);
        transactions = transactions.subList(
                0, Math.min(DISPLAY_TRANSACTIONS_NUM, transactions.size()));
        renderArgs.put("user", user);
        renderArgs.put("negativeBalance", negativeBalance);
        renderArgs.put("positiveBalance", positiveBalance);
        renderArgs.put("transactions", transactions);
    }
}
