package controllers;

import models.User;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Cookie;

public class UserLogin extends Controller {

    public static final String USER = "USER";
    public static final String COOKIE_NAME = "paysplitLogin";
    @Before
    static void checkAuthenticated() {
        User user = getUserFromSession();
        if (user == null) {
            Validation.addError("username", "Session expired, please log in again");
            Validation.keep();
            Application.index();
        }
        request.args.put(USER, user);
    }

    static User getUserFromSession(){
        User user = null;
        String userIdString = session.get("userId");
        if (userIdString != null) {
            user = User.findById(Long.parseLong(userIdString));
        } else {
            Cookie cookie = request.cookies.get(COOKIE_NAME);
            if (cookie != null) {
                String[] parts = cookie.value.split("::");
                if (parts.length == 2) {
                    String userId = parts[0];
                    String secret = parts[1];
                    if (secret.equals(Cache.get(userId, String.class))) {
                        user = User.findById(Long.parseLong(userId));
                    }
                }
            }
        }
        return user;
    }

    static User getUser() {
        return (User) request.args.get(USER);
    }
}
