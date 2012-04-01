package controllers;

import java.util.List;

import models.Balance;
import models.User;
import play.mvc.Controller;

/**
 * Controller for and displaying balances
 * @author davidwen
 */
public class Balances extends Controller{

    public static void showBalances() {
        User user = User.fromSession(session);
        List<Balance> positiveBalances = Balance.getPositiveBalances(user);
        List<Balance> negativeBalances = Balance.getNegativeBalances(user);
        for (Balance negativeBalance : negativeBalances) {
            negativeBalance.balance = -1 * negativeBalance.balance;
        }
        render(user, positiveBalances, negativeBalances);
    }

}
