package controllers;

import java.util.Date;
import java.util.List;

import models.Payment;
import models.User;
import play.mvc.Controller;

import com.google.common.base.Strings;

import exceptions.EntityNotFoundException;

public class Payments extends Controller{

    public static void createPayment() {
        render();
    }

    public static void submitPayment(
            String toUsername,
            double amount,
            String description)
    {
        User user = User.fromSession(session);
        if (User.isTour(user)) {
            validation.addError("amount", "Cannot create payments on the tour account. Log out and create your own account!");
        }

        if (Strings.isNullOrEmpty(description)) {
            description = "No description";
        }
        description = StringUtil.normalizeWhitespace(description);

        Payment payment = new Payment();
        payment.fromUser = user;
        payment.addDate = new Date();
        payment.description = description;

        /* Username validation */
        try {
            payment.toUser = User.fromUsername(toUsername);
        } catch (EntityNotFoundException e) {
            validation.addError("username", "Invalid username: " + toUsername);
        }
        if (user.username.equals(toUsername)) {
            validation.addError("username", "Cannot make payment to yourself");
        }

        /* Amount validation */
        if (amount <= 0) {
            validation.addError("amount", "Payment amount must be positive");
        }
        payment.amount = amount;

        checkErrors();
        Payment.savePayment(payment);
        Transactions.showTransactions();
    }

    public static void showPayments() {
        User user = User.fromSession(session);
        List<Payment> paymentsTo = Payment.find("byFromUser", user).fetch();
        List<Payment> paymentsFrom = Payment.find("byToUser", user).fetch();
        render(user, paymentsTo, paymentsFrom);
    }

    public static void showPayment(long paymentId) {
        User user = User.fromSession(session);
        Payment payment = Payment.findById(paymentId);
        render(user, payment);
    }

    private static void checkErrors() {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            createPayment();
        }
    }
}
