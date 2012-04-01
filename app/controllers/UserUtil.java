package controllers;

import play.data.validation.Validation;

public class UserUtil {

    public static boolean validatePassword(
            Validation validation,
            String password)
    {
        validation.required(password);
        if (password != null && password.length() < 4) {
            validation.addError(
                "password", "Password must be at least 4 characters long");
            return false;
        }
        return true;
    }
}