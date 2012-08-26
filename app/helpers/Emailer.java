package helpers;

import java.text.NumberFormat;
import java.util.Collection;

import models.Due;
import models.Expense;
import models.Payment;
import play.libs.Mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * Util to send email notifications
 * @author davidwen
 */
public class Emailer {
    private static final NumberFormat CURRENCY_FORMAT =
        NumberFormat.getCurrencyInstance();
    private static final String FROM_ADDRESS = "noreply@paysplit.net";

    public static void sendExpenseEmail(
        Collection<String> emails,
        Expense expense,
        Collection<Due> dues)
    throws EmailException {
        String msg = "A new expense has been submitted on Paysplit\n\n" +
            getExpenseMessage(expense, dues);
        sendEmails(
            emails,
            "New Paysplit Expense - " + expense.description,
            msg);
    }

    public static void sendPaymentEmail(
        Collection<String> emails,
        Payment payment)
    throws EmailException {
        String msg =
            "A new payment has been submitted on Paysplit\n" +
            "\n" +
            "Description: " + payment.description + "\n" +
            "Amount: " + CURRENCY_FORMAT.format(payment.amount) + "\n" +
            "Paid By: " + payment.fromUser.username + "\n" +
            "Paid To: " + payment.toUser.username + "\n" +
            "Date: " + payment.addDate + "\n";
        sendEmails(
            emails,
            "New Paysplit Payment - " + payment.description,
            msg);
    }

    public static void sendDeleteExpenseEmail(
        Collection<String> emails,
        Expense expense,
        Collection<Due> dues)
    throws EmailException {
        String msg = "The following expense and dues have been deleted on Paysplit\n\n" +
            getExpenseMessage(expense, dues);
        sendEmails(
            emails,
            "Deleted Paysplit Expense - " + expense.description,
            msg);
    }

    private static void sendEmails(
        Collection<String> emails,
        String subject,
        String msg)
    throws EmailException {
        msg = msg + "\n\n" +
            "To disable notification messages like this, " +
            "you may change your notification settings in " +
            "your Paysplit account settings page.";
        for (String emailAddress : emails) {
            SimpleEmail email = new SimpleEmail();
            email.setFrom(FROM_ADDRESS, "Paysplit");
            email.addTo(emailAddress);
            email.setSubject(subject);
            email.setMsg(msg);
            Mail.send(email);
        }   
    }

    private static String getExpenseMessage(Expense expense, Collection<Due> dues) {
        String msg =
            "Description: " + expense.description + "\n" +
            "Amount: " + CURRENCY_FORMAT.format(expense.amount) + "\n" +
            "Paid By: " + expense.user.username + "\n" +
            "Submitted By: " + expense.createdBy.username + "\n" +
            "Date: " + expense.addDate + "\n" +
            "\n" +
            "Dues From Expense:";
        for (Due due : dues) {
            msg += "\n" + due.fromUser.username + " paid " +
                CURRENCY_FORMAT.format(due.amount); 
        }
        return msg;
    }

}