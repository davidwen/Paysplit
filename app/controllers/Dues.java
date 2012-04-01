package controllers;

import java.util.List;

import models.Due;
import models.User;
import play.mvc.Controller;

/**
 * Controller for displaying dues
 * @author davidwen
 */
public class Dues extends Controller {

    public static void showDues() {
        User user = User.fromSession(session);
        List<Dues> duesTo = Due.find("byFromUser", user).fetch();
        List<Dues> duesFrom = Due.find("byToUser", user).fetch();
        render(user, duesTo, duesFrom);
    }

}
