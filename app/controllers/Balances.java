package controllers;

import java.util.List;

import models.Balance;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Controller for and displaying balances
 * @author davidwen
 */
@With(UserLogin.class)
public class Balances extends Controller{

    public static void showBalances() {
        fillBalancesArgs();
        render();
    }

    public static void mShowBalances() {
        fillBalancesArgs();
        render();
    }

    private static void fillBalancesArgs() {
        User user = UserLogin.getUser();
        List<Balance> positiveBalances = Balance.getPositiveBalances(user);
        List<Balance> negativeBalances = Balance.getNegativeBalances(user);
        for (Balance negativeBalance : negativeBalances) {
            negativeBalance.balance = -1 * negativeBalance.balance;
        }
        renderArgs.put("user", user);
        renderArgs.put("positiveBalances", positiveBalances);
        renderArgs.put("negativeBalances", negativeBalances);
    }
}
