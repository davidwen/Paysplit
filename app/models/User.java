package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.data.validation.Validation;
import play.db.jpa.Model;
import play.mvc.Scope.Session;
import controllers.Application;
import exceptions.EntityNotFoundException;
import exceptions.TooManyEntitiesException;

/**
 * A user represents a user who can make expenses and payments.
 */
@Entity
@Table(name="users")
public class User extends Model{

    @Column(nullable=false, unique=true)
    public String username;

    @Column(nullable=false)
    public String hashedPassword;

    @Column(nullable=false)
    public String emailAddress;

    public Date addDate;

    public Date lastLoginDate;

    public static User fromUsername(String username) throws EntityNotFoundException {
        List<User> users = User.find("byUsername", username).fetch();
        if (users.size() == 1) {
            return users.get(0);
        } if (users.isEmpty()) {
            throw new EntityNotFoundException(
                    "No users found with username " + username);
        } else {
            throw new TooManyEntitiesException(
                    "Unexpected number of users with username " + username +
                    ": " + users.size());
        }
    }

    public static User fromSession(Session session) {
        String userIdString = session.get("userId");
        if (userIdString == null) {
            Validation.addError("username", "Session expired, please log in again");
            Validation.keep();
            Application.index();
        }
        Long userId = Long.parseLong(session.get("userId"));
        return findById(userId);
    }

    public static boolean isTour(User user) {
        return user.username.equals("tour");
    }
}