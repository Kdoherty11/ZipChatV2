package models.entities;

import com.google.common.base.Objects;
import models.ForeignEntity;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "private_rooms")
public class PrivateRoom extends AbstractRoom {

    @ManyToOne
    @JoinColumn(name = "sender")
    @ForeignEntity
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name = "receiver")
    @ForeignEntity
    @Constraints.Required
    public User receiver;

    public boolean senderInRoom = true;
    public boolean receiverInRoom = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "requestId")
    public Request request;

    public PrivateRoom() {
    }

    public PrivateRoom(Request request) {
        this.request = request;
        this.sender = request.sender;
        this.receiver = request.receiver;
    }

    public static List<PrivateRoom> getRoomsByUserId(long userId) {
        String queryString = "select p from PrivateRoom p where (p.sender.userId = :userId and p.senderInRoom = true) or (p.receiver.userId = :userId and p.receiverInRoom = true)";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("userId", userId);

        return query.getResultList();
    }

    public boolean removeUser(long userId) {
        if (userId == sender.userId) {
            if (!receiverInRoom) {
                JPA.em().remove(this);
            } else {
                senderInRoom = false;
            }
        } else if (userId == receiver.userId) {
            if (!senderInRoom) {
                JPA.em().remove(this);
            } else {
                receiverInRoom = false;
            }
        } else {
            return false;
        }

        // Allow both users to request each other again
        JPA.em().remove(request);
        return true;
    }

    public boolean isUserInRoom(long userId) {
        return (sender.userId == userId && senderInRoom) ||
                (receiver.userId == userId && receiverInRoom);
    }

    public static Optional<PrivateRoom> getRoom(long senderId, long receiverId) {

        String queryString = "select p from PrivateRoom p where " +
                "((p.sender.userId = :senderId and p.receiver.userId = :receiverId)" +
                " or (p.receiver.userId = :senderId and p.sender.userId = :receiverId))" +
                " and p.senderInRoom = true and p.receiverInRoom = true";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId);

        List<PrivateRoom> rooms = query.getResultList();

        if (rooms.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(rooms.get(0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrivateRoom that = (PrivateRoom) o;
        return Objects.equal(senderInRoom, that.senderInRoom) &&
                Objects.equal(receiverInRoom, that.receiverInRoom) &&
                Objects.equal(sender, that.sender) &&
                Objects.equal(receiver, that.receiver) &&
                Objects.equal(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), sender, receiver, senderInRoom, receiverInRoom, request);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("senderId", sender.userId)
                .add("receiverId", receiver.userId)
                .add("senderInRoom", senderInRoom)
                .add("receiverInRoom", receiverInRoom)
                .add("request", request)
                .toString();
    }
}
