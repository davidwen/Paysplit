package controllers;

import java.util.Date;
import java.util.List;

import models.Payment;
import models.User;
import play.mvc.Controller;

import com.google.common.base.Strings;

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

    private static void checkErrors() {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            createPayment();
        }
    }
}
