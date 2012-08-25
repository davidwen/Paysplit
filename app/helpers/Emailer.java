package helpers;

import java.text.NumberFormat;
import java.util.Collection;

import models.Due;
import models.Expense;
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

    public static void sendExpenseEmail(
        Collection<String> emails,
        Expense expense,
        Collection<Due> dues)
    throws EmailException {
        String msg =
            "A new expense has been submitted on Paysplit\n" +
            "\n" +
            "Description: " + expense.description + "\n" +
            "Amount: " + CURRENCY_FORMAT.format(expense.amount) + "\n" +
            "Paid By: " + expense.user.username + "\n" +
            "Submitted By: " + expense.createdBy.username + "\n" +
            "Date: " + expense.addDate + "\n" +
            "\n" +
            "Dues From Expense:\n";
        for (Due due : dues) {
            msg += due.fromUser.username + " paid " +
                CURRENCY_FORMAT.format(due.amount) + "\n"; 
        }
        for (String emailAddress : emails) {
            SimpleEmail email = new SimpleEmail();
            email.setFrom("noreply@paysplit.net");
            email.addTo(emailAddress);
            email.setSubject("New Paysplit Expense - " + expense.description);
            email.setMsg(msg);
            Mail.send(email);   
        }
    }
}