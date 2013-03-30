package controllers;

import java.util.Date;
import java.util.List;
import java.util.Set;

import helpers.Emailer;
import models.Balance;
import models.Payment;
import models.User;
import play.mvc.Controller;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import org.apache.commons.mail.EmailException;

public class Payments extends Controller {

    public static void createPayment() {
        render();
    }

    public static void submitPayment(
        String username,
        double amount,
        String description,
        Boolean paymentFromUser)
    {
        User user = User.fromSession(session);
        User otherUser = null;

        if (User.isTour(user)) {
            validation.addError(
                "amount",
                "Cannot create payments on the tour account. Log out and " +
                    "create your own account.");
        }

        if (Strings.isNullOrEmpty(description)) {
            description = "No description";
        }
        description = StringUtil.normalizeWhitespace(description);

        /* Username validation */
        otherUser = User.fromUsername(username);
        if (otherUser == null) {
            validation.addError("username", "Invalid username: " + username);
        }

        if (user.username.equals(username)) {
            validation.addError("username", "Cannot make payment to yourself");
        }

        /* Amount validation */
        if (amount <= 0) {
            validation.addError("amount", "Payment amount must be positive");
        }

        checkErrors();

        Payment payment = new Payment();
        if (paymentFromUser) {
            payment.fromUser = user;
            payment.toUser = otherUser;
        } else {
            payment.fromUser = otherUser;
            payment.toUser = user;
        }
        payment.addDate = new Date();
        payment.description = description;
        payment.amount = amount;

        Payment.savePayment(payment);
        try {
            Emailer.sendPaymentEmail(
                Sets.newHashSet(payment.fromUser.emailAddress, payment.toUser.emailAddress),
                payment);
        } catch (EmailException ex) {
            System.out.println(ex);
        }
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
        if (payment == null) {
            notFound();
        }
        if (payment.fromUser.id != user.id && payment.toUser.id != user.id) {
            forbidden();
        }
        render(user, payment);
    }

    public static void deletePayment(long paymentId) {
        User user = User.fromSession(session);
        Payment payment = Payment.findById(paymentId);
        if (payment.fromUser.id != user.id && payment.toUser.id != user.id) {
            forbidden();
        }
        Set<String> emails = Sets.newHashSet(
            payment.fromUser.emailAddress, payment.toUser.emailAddress);
        Balance.adjustBalance(payment.fromUser, payment.toUser, payment.amount);
        payment.delete();
        try {
            Emailer.sendDeletePaymentEmail(emails, payment);
        } catch (EmailException ex) {
            System.out.println(ex);
        }
        flash.success("Payment deleted");
        Transactions.showTransactions();
    }

    private static void checkErrors() {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            createPayment();
        }
    }
}
