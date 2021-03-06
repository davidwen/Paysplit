package controllers;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import models.User;
import play.cache.Cache;
import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.Http.Cookie;

/**
 * Controller for general site functions
 * @author davidwen
 */
public class Application extends Controller {

    public static void index() {
        User user = UserLogin.getUserFromSession();
        if (user != null) {
            Dashboard.dashboard();
        }
        render();
    }

    public static void mIndex() {
        User user = UserLogin.getUserFromSession();
        if (user != null) {
            Dashboard.mDashboard();
        }
        render();
    }

    public static void register() {
        render();
    }

    public static void help() {
        render();
    }

    public static void login(String username, String password, Boolean remember) {
        if (checkCredentials(username, password)) {
            if (Boolean.TRUE.equals(remember)) {
                User user = UserLogin.getUserFromSession();
                String uuid = UUID.randomUUID().toString();
                Cache.add(
                    user.id.toString(),
                    uuid,
                    "7d");
                response.setCookie(
                    UserLogin.COOKIE_NAME, user.id + "::" + uuid, "7d");
            }
            Dashboard.dashboard();
        } else {
            index();
        }
    }

    public static void mLogin(String username, String password) {
        if (checkCredentials(username, password)) {
            Dashboard.mDashboard();
        } else {
            mIndex();
        }
    }

    public static void logout() {
        User user = UserLogin.getUserFromSession();
        session.clear();
        Cache.delete(user.id.toString());
        index();
    }

    public static void mLogout() {
        User user = UserLogin.getUserFromSession();
        session.clear();
        Cache.delete(user.id.toString());
        mIndex();
    }

    private static boolean checkCredentials(String username, String password) {
        User user = User.fromUsername(username);
        if (user == null) {
            validation.addError("username", "No username found: " + username);
            validation.keep();
            params.flash();
            return false;
        }

        if (Crypto.passwordHash(password).equals(user.hashedPassword)) {
            session.put("userId", user.getId().toString());
            session.put("username", user.username);
            user.lastLoginDate = new Date();
            user.save();
            return true;
        } else {
            validation.addError("password", "Incorrect password");
            validation.keep();
            return false;
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
            login(username, password, false);
        }
    }
}