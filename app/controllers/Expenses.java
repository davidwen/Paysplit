package controllers;

import helpers.Emailer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import models.Balance;
import models.Due;
import models.Expense;
import models.User;

import org.apache.commons.mail.EmailException;

import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Controller for creating and displaying expenses
 * @author davidwen
 */
@With(UserLogin.class)
public class Expenses extends Controller {

    private static String dueUsername = "dueUsername";
    private static String dueAmount = "dueAmount";

    public static void createExpense() {
        render();
    }

    public static void submitExpense(Double amount, String description, String expenseUsername)
    {
        User user = UserLogin.getUser();
        if (User.isTour(user)) {
            validation.addError("expense", "Cannot create expenses on the tour account. Log out and create your own account!");
        }

        validation.required(amount);
        if (amount <= 0) {
            validation.addError("expense", "Expense amount must be positive");
        }

        User expenseUser = User.fromUsername(expenseUsername);
        if (expenseUser == null) {
            validation.addError("expense", "Invalid expense username: " + expenseUsername);
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
        expense.createdBy = user;
        expense.user = expenseUser;
        expense.addDate = new Date();
        expense.save();

        int iteration = 1;
        BigDecimal totalDues = new BigDecimal(params.get(dueAmount + "0"));
        if (totalDues.compareTo(BigDecimal.ZERO) < 0) {
            validation.addError("due", "Created dues amount exceeds expense amount");
        }
        checkErrorsWithRollback();
        Set<String> dueUsernames = Sets.newHashSet();

        Set<String> emails = Sets.newHashSet(expenseUser.emailAddress, user.emailAddress);
        List<Due> dues = Lists.newArrayList();
        while(params.get(dueUsername + String.valueOf(iteration)) != null) {
            String fromUsername = params.get(dueUsername + String.valueOf(iteration));
            String amountString = params.get(dueAmount + String.valueOf(iteration));
            Due due = new Due();
            due.toUser = expenseUser;
            due.expense = expense;
            due.addDate = new Date();

            /* Username validation */
            due.fromUser = User.fromUsername(fromUsername);
            if (due.fromUser == null) {
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
            if (expenseUser.username.equals(fromUsername)) {
                validation.addError("due", "Username " + fromUsername + " entered more than once");
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
            dues.add(due);
            emails.add(due.fromUser.emailAddress);
            iteration++;
        }
        if (totalDues.doubleValue() != expense.amount) {
            validation.addError("due", "Sum of due amounts do not equal expense amount");
        }
        checkErrorsWithRollback();
        try {
            Emailer.sendExpenseEmail(emails, expense, dues);
        } catch (EmailException ex) {
            System.out.println(ex);
        }
        Transactions.showTransactions();
    }

    public static void showExpenses() {
        User user = UserLogin.getUser();
        List<Expense> expenses = Expense.find("byUser", user).fetch();
        render(user, expenses);
    }

    public static void showExpense(long expenseId) {
        User user = UserLogin.getUser();
        Expense expense = Expense.findById(expenseId);
        if (expense == null) {
            notFound();
        }
        boolean isExpenseOwner =
            expense.user.id == user.id ||
            expense.createdBy.id == user.id;
        render(user, expense, isExpenseOwner);
    }

    public static void deleteExpense(long expenseId) {
        User user = UserLogin.getUser();
        Expense expense = Expense.findById(expenseId);
        if (expense.user.id != user.id && expense.createdBy.id != user.id) {
            forbidden();
        }
        Set<String> emails = Sets.newHashSet(
                expense.user.emailAddress, expense.createdBy.emailAddress);
        for (Due due : expense.dues) {
            Balance.adjustBalance(due.toUser, due.fromUser, due.amount);
            emails.add(due.fromUser.emailAddress);
            due.delete();
        }
        expense.delete();
        try {
            Emailer.sendDeleteExpenseEmail(emails, expense, expense.dues);
        } catch (EmailException ex) {
            System.out.println(ex);
        }
        flash.success("Expense deleted");
        Transactions.showTransactions();
    }

    private static void checkErrorsWithRollback() {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            JPA.em().getTransaction().rollback();
            createExpense();
        }
    }

}