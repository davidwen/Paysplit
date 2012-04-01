package controllers;

import models.User;
import play.mvc.Controller;

public class Users extends Controller{

    public static void showUser(Long userId) {
        User user = User.findById(userId);
        User currentUser = User.fromSession(session);
        if (user.id == currentUser.id) {
            Account.showSettings();
        } else {
            render(user);
        }
    }

}
