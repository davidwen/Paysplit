package controllers;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Balance;
import models.Expense;
import models.Transaction;
import models.Transaction.Type;
import models.User;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Controller for driving the tutorial
 * @author davidwen
 */
@With(UserLogin.class)
public class Tutorial extends Controller {

    private static String dueUsername = "dueUsername";
    private static String dueAmount = "dueAmount";
    private static Set<String> sampleUsernames =
            Sets.newHashSet("anne", "bob", "carl", "dan", "eve");

    public static void start() {
        String mode = "tutorial";
        String page = "start";
        render(mode, page);
    }

    public static void createExpense() {
        String mode = "tutorial";
        String page = "expenses";
        render(mode, page);
    }

    public static void submitExpense(double amount, String description)
    {
        User user = UserLogin.getUser();
        validation.required(amount);
        if (amount <= 0) {
            validation.addError("amount", "Expense amount must be positive");
        }

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            createExpense();
        }

        if (Strings.isNullOrEmpty(description)) {
            description = "No description";
        }

        Expense expense = new Expense();
        expense.amount = amount;
        expense.description = description;
        expense.user = user;
        expense.createdBy = user;
        expense.addDate = new Date();

        Map<String, Double> usernameToAmountMap = Maps.newHashMap();

        int iteration = 1;
        double totalDues = Double.parseDouble(params.get(dueAmount + "0"));
        if (totalDues < 0) {
            validation.addError("due", "Created dues amount exceeds expense amount");
        }
        checkErrors();
        Set<String> dueUsernames = Sets.newHashSet();

        while(params.get(dueUsername + iteration) != null) {
            String fromUsername = params.get(dueUsername + iteration);
            String amountString = params.get(dueAmount + iteration);

            /* Username validation */
            if (fromUsername.trim().length() == 0) {
                validation.addError("due", "Missing due username");
            } else if (!sampleUsernames.contains(fromUsername)){
                validation.addError("due", "Invalid due username: " + fromUsername);
            }
            if (!dueUsernames.contains(fromUsername)) {
                dueUsernames.add(fromUsername);
            } else {
                validation.addError("due", "Username " + fromUsername + " entered more than once");
            }

            /* Amount validation */
            Double dueAmount = 0.0;
            try {
                dueAmount = Double.parseDouble(amountString);
            } catch (NumberFormatException e) {
                validation.addError("due", "Invalid due amount: " + amountString);
            }
            if (dueAmount <= 0) {
                validation.addError("due", "Due amount must be positive");
            }

            totalDues += dueAmount;
            checkErrors();
            usernameToAmountMap.put(fromUsername, dueAmount);
            iteration++;
        }
        if (totalDues != expense.amount) {
            validation.addError("due", "Sum of due amounts do not equal expense amount");
        }
        checkErrors();
        Cache.set("expense", expense);
        Cache.set("usernameToAmountMap", usernameToAmountMap);
        showTransactions();
    }

    private static void checkErrors() {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            createExpense();
        }
    }

    public static void showTransactions() {
        Expense expense = Cache.get("expense", Expense.class);
        Map<String, Double> usernameToAmountMap =
                Cache.get("usernameToAmountMap", Map.class);
        List<Transaction> transactions = Lists.newArrayList();
        Transaction t = Transaction.fromExpense(expense);
        transactions.add(t);
        for (String username : usernameToAmountMap.keySet()){
            t = new Transaction();
            t.type = Type.DUE;
            User fromUser = new User();
            fromUser.username = username;
            t.fromUser = fromUser;
            t.toUser = expense.user;
            t.amount = usernameToAmountMap.get(username);
            t.description = expense.description;
            t.addDate = expense.addDate;
            transactions.add(t);
        }
        Collections.sort(transactions, new Transaction.AddDateComparator());
        User user = UserLogin.getUser();
        Cache.set("transactions", transactions);

        String mode = "tutorial";
        String page = "transactions";

        render(user, transactions, mode, page);
    }

    public static void showBalances() {
        User user = UserLogin.getUser();
        List<Transaction> transactions =
                Cache.get("transactions", List.class);
        List<Balance> negativeBalances = Lists.newArrayList();
        for (Transaction t : transactions) {
            if (t.type == Type.DUE) {
                Balance balance = new Balance();
                User toUser = new User();
                toUser.username = t.fromUser.username;
                balance.fromUser = user;
                balance.toUser = toUser;
                balance.balance = t.amount;
                negativeBalances.add(balance);
            }
        }
        List<Balance> positiveBalances = Lists.newArrayList();
        Balance balance = new Balance();
        User toUser = new User();
        toUser.username = "frank";
        balance.fromUser = user;
        balance.toUser = toUser;
        balance.balance = 10.50;
        positiveBalances.add(balance);

        String mode = "tutorial";
        String page = "balances";

        render(user, positiveBalances, negativeBalances, mode, page);
    }

    public static void createPayment() {
        String mode = "tutorial";
        String page = "payments";
        render(mode, page);
    }

}
