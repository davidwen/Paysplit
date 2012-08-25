package controllers;

import java.util.Date;

import models.User;
import play.libs.Crypto;
import play.mvc.Controller;

/**
 * Controller for general site functions
 * @author davidwen
 */
public class Application extends Controller {

    public static void index() {
        session.remove("userId");
        session.remove("username");
        render();
    }

    public static void register() {
        render();
    }

    public static void help() {
        render();
    }

    public static void login(String username, String password) {
        User user = User.fromUsername(username);
        if (user == null) {
            validation.addError("username", "No username found: " + username);
            validation.keep();
            params.flash();
            index();
        }

        if (Crypto.passwordHash(password).equals(user.hashedPassword)) {
            session.put("userId", user.getId().toString());
            session.put("username", user.username);
            user.lastLoginDate = new Date();
            user.save();
            Dashboard.dashboard();
        } else {
            validation.addError("password", "Incorrect password");
            validation.keep();
            index();
        }
    }

    public static void addUser(String username, String password, String emailAddress) {
        validation.required(username);
        UserUtil.validatePassword(validation, password);
        validation.required(emailAddress);
        validation.email(emailAddress);
        if (User.find("byUsername", username).fetch().size() > 0) {
            validation.addError(
                    "username", "Username " + username + " already taken");
        }
        if (!username.matches("[a-zA-Z0-9]*")){
            validation.addError(
                    "username", "Only alphanumeric characters allowed in username");
        }
        if (validation.hasErrors()) {
            validation.keep();
            params.flash();
            register();
        } else {
            User user = new User();
            user.username = username;
            user.hashedPassword = Crypto.passwordHash(password);
            user.emailAddress = emailAddress;
            user.addDate = new Date();
            user.save();
            login(username, password);
        }
    }
}