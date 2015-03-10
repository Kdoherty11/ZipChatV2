package controllers;

import models.entities.Message;
import models.entities.User;
import play.mvc.Result;
import utils.DbUtils;

import java.util.Optional;

public class MessagesController extends BaseController {

    private static final boolean ADD_FAVORITE = true;
    private static final boolean REMOVE_FAVORITE = false;

    public static Result favorite(long messageId, long userId) {
        return favorite(messageId, userId, ADD_FAVORITE);
    }

    public static Result removeFavorite(long messageId, long userId) {
        return favorite(messageId, userId, REMOVE_FAVORITE);
    }

    public static Result favorite(long messageId, long userId, boolean isAddFavorite) {
        Optional<Message> messageOptional = DbUtils.findEntityById(Message.class, messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                if (isAddFavorite) {
                    message.favorite(userOptional.get());
                } else {
                    message.removeFavorite(userOptional.get());
                }
                return OK_RESULT;
            } else {
                return badRequestJson(DbUtils.buildEntityNotFoundError("User", userId));
            }
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError("Message", messageId));
        }
    }
}
