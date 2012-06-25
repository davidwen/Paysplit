package controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import models.Balance;
import models.Due;
import models.Expense;
import models.User;
import play.db.jpa.JPA;
import play.mvc.Controller;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import exceptions.EntityNotFoundException;

/**
 * Controller for creating and displaying expenses
 * @author davidwen
 */
public class Expenses extends Controller {

    private static String dueUsername = "dueUsername";
    private static String dueAmount = "dueAmount";

    public static void createExpense() {
        render();
    }

    public static void submitExpense(double amount, String description)
    {
        User user = User.fromSession(session);
        if (User.isTour(user)) {
            validation.addError("amount", "Cannot create expenses on the tour account. Log out and create your own account!");
        }

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
        description = StringUtil.normalizeWhitespace(description);

        Expense expense = new Expense();
        expense.amount = amount;
        expense.description = description;
        expense.user = user;
        expense.addDate = new Date();
        expense.save();

        int iteration = 1;
        BigDecimal totalDues = new BigDecimal(params.get(dueAmount + "0"));
        if (totalDues.compareTo(BigDecimal.ZERO) < 0) {
            validation.addError("due", "Created dues amount exceeds expense amount");
        }
        checkErrorsWithRollback();
        Set<String> dueUsernames = Sets.newHashSet();

        while(params.get(dueUsername + String.valueOf(iteration)) != null) {
            String fromUsername = params.get(dueUsername + String.valueOf(iteration));
            String amountString = params.get(dueAmount + String.valueOf(iteration));
            Due due = new Due();
            due.toUser = user;
            due.expense = expense;
            due.addDate = new Date();

            /* Username validation */
            try {
                due.fromUser = User.fromUsername(fromUsername);
            } catch (EntityNotFoundException e) {
                if (Strings.isNullOrEmpty(fromUsername)) {
                    validation.addError("due", "Missing due username");
                } else {
                    validation.addError("due", "Invalid due username: " + fromUsername);
                }
            }
            if (!dueUsernames.contains(fromUsername)) {
                dueUsernames.add(fromUsername);
            } else {
                validation.addError("due", "Username " + fromUsername + " entered more than once");
            }
            if (user.username.equals(fromUsername)) {
                validation.addError("due", "Cannot create a due from yourself on your expense");
            }

            /* Amount validation */
            try {
                due.amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e) {
                validation.addError("due", "Invalid due amount: " + amountString);
            }
            if (due.amount <= 0) {
                validation.addError("due", "Due amount must be positive");
            }

            totalDues = totalDues.add(new BigDecimal(amountString));
            checkErrorsWithRollback();
            Due.saveDue(due);
            iteration++;
        }
        if (totalDues.doubleValue() != expense.amount) {
            validation.addError("due", "Sum of due amounts do not equal expense amount");
        }
        checkErrorsWithRollback();
        Transactions.showTransactions();
    }

    public static void showExpenses() {
        User user = User.fromSession(session);
        List<Expense> expenses = Expense.find("byUser", user).fetch();
        render(user, expenses);
    }

    public static void showExpense(long expenseId) {
        User user = User.fromSession(session);
        Expense expense = Expense.findById(expenseId);
        if (expense == null) {
            notFound();
        }
        boolean isExpenseOwner =
            expense.user.id == user.id;
        render(user, expense, isExpenseOwner);
    }

    public static void deleteExpense(long expenseId) {
        User user = User.fromSession(session);
        Expense expense = Expense.findById(expenseId);
        if (expense.user.id != user.id) {
            forbidden();
        }
        for (Due due : expense.dues) {
            Balance.adjustBalance(due.toUser, due.fromUser, due.amount);
            due.delete();
        }
        expense.delete();
        flash.success("Expense deleted");
        Transactions.showTransactions();
    }

    private static void checkErrorsWithRollback() {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            JPA.setRollbackOnly();
            createExpense();
        }
    }

}