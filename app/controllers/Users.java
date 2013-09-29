package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.With;

@With(UserLogin.class)
public class Users extends Controller{

    public static void showUser(Long userId) {
        User user = User.findById(userId);
        User currentUser = UserLogin.getUser();
        if (user.id == currentUser.id) {
            Account.showSettings();
        } else {
            render(user);
        }
    }

}
