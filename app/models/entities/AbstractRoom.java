package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import notifications.AbstractNotification;
import notifications.MessageNotification;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "abstract_rooms")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long roomId;

    @JsonIgnore
    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @JsonIgnore
    @OneToMany(targetEntity = Message.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Message> messages = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoom that = (AbstractRoom) o;
        return Objects.equal(createdAt, that.createdAt) &&
                Objects.equal(lastActivity, that.lastActivity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(createdAt, lastActivity);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roomId", roomId)
                .add("createdAt", createdAt)
                .add("lastActivity", lastActivity)
                .add("messages", messages)
                .toString();
    }

    public static long getId(AbstractRoom room) {
        return room == null ? -1 : room.roomId;
    }
}
