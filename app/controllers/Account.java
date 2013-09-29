package controllers;

import models.User;
import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.With;

@With(UserLogin.class)
public class Account extends Controller {

    public static void showSettings() {
        User user = UserLogin.getUser();
        if (User.isTour(user)) {
            Dashboard.dashboard();
        }
        render(user);
    }

    public static void changePassword(
        String oldPassword,
        String newPassword)
    {
        User user = UserLogin.getUser();
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
        User user = UserLogin.getUser();
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

    public static void setReceiveEmail(Boolean receiveEmail) {
        User user = UserLogin.getUser();
        if (Boolean.TRUE.equals(receiveEmail)) {
            user.receiveEmail = true;
        } else {
            user.receiveEmail = false;
        }
        user.save();
        flash.put("receiveEmailConfirmation", "true");
        showSettings();
    }
}
