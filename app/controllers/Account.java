package controllers;

import models.User;
import play.libs.Crypto;
import play.mvc.Controller;

public class Account extends Controller {

    public static void showSettings() {
        User user = User.fromSession(session);
        if (User.isTour(user)) {
            Dashboard.dashboard();
        }
        render(user);
    }

    public static void changePassword(
        String oldPassword,
        String newPassword)
    {
        User user = User.fromSession(session);
        if (user.hashedPassword.equals(Crypto.passwordHash(oldPassword))) {
            user.hashedPassword = Crypto.passwordHash(newPassword);
        } else {
            validation.addError("password", "Incorrect Password");
        }
        if (validation.hasErrors()) {
            validation.keep();
        } else {
            user.save();
            flash.put("passwordConfirmation", "true");
        }
        showSettings();
    }

    public static void changeEmail(String newEmail)
    {
        User user = User.fromSession(session);
        validation.email("email", newEmail);
        if (!user.emailAddress.equals(newEmail)) {
            user.emailAddress = newEmail;
        } else {
            validation.addError("email", "E-mail address same as original");
        }
        if (validation.hasErrors()) {
            validation.keep();
        } else {
            user.save();
            flash.put("emailConfirmation", "true");
        }
        showSettings();
    }
}
