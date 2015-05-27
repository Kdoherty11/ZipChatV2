package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.sockets.RoomSocket;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.WebSocket;

public class RoomsController extends BaseController {

    @Transactional
    public static WebSocket<JsonNode> joinRoom(final long roomId, final long userId) {

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                Logger.debug("joining " + roomId + " " + userId);
                try {
                    RoomSocket.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }
}
