package utils;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.exceptions.NetworkIOException;
import controllers.BaseController;
import models.entities.PublicRoom;
import models.entities.Request;
import models.entities.User;
import play.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class NotificationUtils {

    private static class Key {
        private static final String EVENT = "event";
        private static final String FACEBOOK_NAME = "name";
        private static final String CHAT_REQUEST_RESPONSE = "response";
        private static final String FACEBOOK_ID = "facebookId";
        private static final String REQUEST_ID = "requestId";
        private static final String MESSAGE = "message";
        private static final String ROOM_NAME = "roomName";
        private static final String ROOM_ID = "roomId";
    }

    private static class Event {
        private static final String CHAT_REQUEST = "Chat Request";
        private static final String CHAT_REQUEST_RESPONSE = "Chat Request Response";
        private static final String CHAT_MESSAGE = "Chat Message";
    }

    public static final String GCM_API_KEY = "AIzaSyDp2t64B8FsJUAOszaFl14-uiDVoZRu4W4";
    private static final int GCM_RETRIES = 3;

    private static final Sender GCM_SENDER = new Sender(GCM_API_KEY);

    public static final ApnsService SERVICE = APNS.newService()
            .withCert("certificates/dev.p12", "password")
            .withSandboxDestination()
            .build();

    private NotificationUtils() {

    }

    private static String buildAppleMessage(Map<String, String> data) {
        PayloadBuilder builder = PayloadBuilder.newPayload();

        builder.alertBody("New message");
        data.entrySet().forEach(entry -> builder.customField(entry.getKey(), entry.getValue()));

        return builder.build();
    }

    public static String sendAppleNotification(String token, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            SERVICE.push(token, payload);
            return BaseController.OK_STRING;
        } catch (NetworkIOException e) {
            String error = "Problem sending APN " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    public static String sendBatchAppleNotifications(List<String> tokens, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            SERVICE.push(tokens, payload);
            return BaseController.OK_STRING;
        } catch (NetworkIOException e) {
            String error = "Problem sending batch APN " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    private static Message buildGcmMessage(Map<String, String> data) {
        Message.Builder builder = new Message.Builder();

        data.entrySet().forEach(entry -> builder.addData(entry.getKey(), entry.getValue()));

        return builder.build();
    }

    public static String sendAndroidNotification(String regId, Map<String, String> data) {
        Message message = buildGcmMessage(data);

        try {
            GCM_SENDER.send(message, regId, GCM_RETRIES);
            return BaseController.OK_STRING;
        } catch (IOException e) {
            String error = "Problem sending GCM Message " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    public static String sendBatchAndroidNotifications(List<String> regIds, Map<String, String> data) {
        Message message = buildGcmMessage(data);

        try {
            GCM_SENDER.send(message, regIds, GCM_RETRIES);
            return BaseController.OK_STRING;
        } catch (IOException e) {
            String error = "Problem sending batch GCM message " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    public static void sendChatRequest(long requestId, User sender, User receiver) {
        Map<String, String> data = new HashMap<>();
        data.put(Key.EVENT, Event.CHAT_REQUEST);
        data.put(Key.REQUEST_ID, String.valueOf(requestId));
        data.put(Key.FACEBOOK_NAME, sender.name);
        data.put(Key.FACEBOOK_ID, sender.facebookId);
        receiver.sendNotification(data);
    }

    public static void sendChatResponse(User sender, User receiver, Request.Status response) {
        Map<String, String> data = new HashMap<>();
        data.put(Key.EVENT, Event.CHAT_REQUEST_RESPONSE);
        data.put(Key.FACEBOOK_NAME, sender.name);
        data.put(Key.CHAT_REQUEST_RESPONSE, response.toString());
        receiver.sendNotification(data);
    }

    public static void messageSubscribers(PublicRoom publicRoom, User user, String message, Set<Long> userIdsBlacklist) {
        Map<String, String> data = new HashMap<>();
        data.put(Key.EVENT, Event.CHAT_MESSAGE);
        data.put(Key.FACEBOOK_NAME, user.name);
        data.put(Key.FACEBOOK_ID, user.facebookId);
        data.put(Key.MESSAGE, message);
        data.put(Key.ROOM_NAME, publicRoom.name);
        data.put(Key.ROOM_ID, String.valueOf(publicRoom.roomId));
        publicRoom.notifySubscribers(data, userIdsBlacklist);
    }
}
