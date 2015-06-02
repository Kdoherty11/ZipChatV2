package controllers;

import models.entities.Message;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;
import utils.NotificationUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.Validators;

import java.util.Optional;

public class MessagesController extends BaseController {

    @Transactional
    public static Result getMessages(long roomId, int limit, int offset) {

        DataValidator validator = new DataValidator(
                new FieldValidator<>("roomId", roomId, Validators.min(1)),
                new FieldValidator<>("limit", limit, Validators.min(0)),
                new FieldValidator<>("offset", offset, Validators.min(0)));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        return okJson(Message.getMessages(roomId, limit, offset));
    }

    @Transactional
    public static Result favorite(long messageId, long userId) {
        return favorite(messageId, userId, true);
    }

    @Transactional
    public static Result removeFavorite(long messageId, long userId) {
        return favorite(messageId, userId, false);
    }

    @Transactional
    public static Result favorite(long messageId, long userId, boolean isAddFavorite) {
        Optional<Message> messageOptional = DbUtils.findEntityById(Message.class, messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                if (isAddFavorite) {
                    User favoritor = userOptional.get();
                    boolean success = message.favorite(favoritor);
                    if (!success) {
                        return badRequestJson("User " + userId + " has already favorited this message");
                    }
                    if (favoritor.userId != message.senderId) {
                        NotificationUtils.sendMessageFavorited(favoritor, message);
                    }
                } else {
                    boolean success = message.removeFavorite(userOptional.get());
                    if (!success) {
                       return badRequestJson("User " + userId + " has not favorited this message");
                    }
                }
                return OK_RESULT;
            } else {
                return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
            }
        } else {
            return DbUtils.getNotFoundResult(Message.ENTITY_NAME, messageId);
        }
    }

    @Transactional
    public static Result flag(long messageId, long userId) {
        return flag(messageId, userId, true);
    }

    @Transactional
    public static Result removeFlag(long messageId, long userId) {
        return flag(messageId, userId, false);
    }

    @Transactional
    public static Result flag(long messageId, long userId, boolean isAddFlag) {
        Optional<Message> messageOptional = DbUtils.findEntityById(Message.class, messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                if (isAddFlag) {
                    User flagged = userOptional.get();
                    boolean success = message.flag(flagged);
                    if (!success) {
                        return badRequestJson("User " + userId + " has already flagged this message");
                    }
                } else {
                    boolean success = message.removeFlag(userOptional.get());
                    if (!success) {
                        return badRequestJson("User " + userId + " has not flagged this message");
                    }
                }
                return OK_RESULT;
            } else {
                return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
            }
        } else {
            return DbUtils.getNotFoundResult(Message.ENTITY_NAME, messageId);
        }
    }
}
