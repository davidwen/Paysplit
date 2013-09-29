package controllers;

import java.util.List;

import models.Transaction;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

import com.google.common.collect.Lists;

@With(UserLogin.class)
public class Transactions extends Controller {

    public static void showTransactions() {
        User user = UserLogin.getUser();
        List<Transaction> transactions = Transaction.fromUser(user);
        render(user, transactions);
    }

    public static void listTransactionsDataTable(
        String searchUser,
        String searchType,
        int iDisplayStart,
        int iDisplayLength)
    {
        User user = UserLogin.getUser();
        List<Transaction> allTransactions = Transaction.fromUser(user);
        List<Transaction> transactions = Lists.newArrayList();
        for (Transaction transaction : allTransactions) {
            if (transaction.matchesFilter(searchUser, searchType)) {
                transactions.add(transaction);
            }
        }
        int total = allTransactions.size();
        int numMatched = transactions.size();
        transactions = transactions.subList(
            iDisplayStart,
            Math.min(iDisplayStart + iDisplayLength, transactions.size()));
        render("transactions.json", transactions, total, numMatched);
    }

}
