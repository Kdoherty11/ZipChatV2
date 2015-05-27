package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import models.NoUpdate;
import models.Platform;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import utils.DbUtils;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;


@Entity
@Table(name = "users")
@SequenceGenerator(name="users_userId_seq", sequenceName="users_userId_seq", allocationSize=10)
public class User {

    public static final String ENTITY_NAME = "User";

    @Id
    @GenericGenerator(name = "users_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "users_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "users_gen", strategy=GenerationType.SEQUENCE)
    public long userId;

    @Constraints.Required
    public String facebookId;

    @Constraints.Required
    public String name;

    public String registrationId;

    @Constraints.Required
    public Platform platform;

    @NoUpdate
    @JsonIgnore
    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public static long getId(User user) {
        return user == null ? -1 : user.userId;
    }

    public static String sendNotification(long userId, Map<String, String> data) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class,userId);
        if (userOptional.isPresent()) {
            return userOptional.get().sendNotification(data);
        } else {
            return DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, userId);
        }
    }

    public String sendNotification(Map<String, String> data) {
        if (Strings.isNullOrEmpty(registrationId)) {
            String error = "No registrationId found for " + this;
            Logger.error(error);
            return error;
        }
        
        switch (platform) {
            case android:
                return NotificationUtils.sendAndroidNotification(registrationId, data);
            case ios:
                return NotificationUtils.sendAppleNotification(registrationId, data);
            default:
                throw new UnsupportedOperationException("Sending notifications to " + platform +
                        "devices is not supported");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, facebookId, name, registrationId, platform);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equal(this.userId, other.userId)
                && Objects.equal(this.facebookId, other.facebookId)
                && Objects.equal(this.name, other.name)
                && Objects.equal(this.registrationId, other.registrationId)
                && Objects.equal(this.platform, other.platform);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("name", name)
                .add("facebookId", facebookId)
                .add("registrationId", registrationId)
                .add("platform", platform)
                .toString();
    }
}
