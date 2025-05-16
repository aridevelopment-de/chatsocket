package ari24.chatsocket.ws;

import ari24.chatsocket.ChatSocket;
import ari24.chatsocket.config.BaseConfigModel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Instant;

public class ChatsocketServer extends WebSocketServer {

    public ChatsocketServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public ChatsocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        ChatSocket.LOGGER.info("New connection from " + conn.getRemoteSocketAddress().getHostName() + ":" + conn.getRemoteSocketAddress().getPort());
        sendCommunicationTypeChange(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        ChatSocket.LOGGER.info("Closed connection from " + conn.getRemoteSocketAddress().getHostName() + ":" + conn.getRemoteSocketAddress().getPort() + " with code " + code + " and reason: " + reason);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ChatSocket.LOGGER.error("Error in chatsocket server: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        ChatSocket.LOGGER.info("ChatSocket server started on " + getAddress().getHostName() + ":" + getPort());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        handleIncomingMessage(conn, message);
    }

    private void handleIncomingMessage(WebSocket conn, String message) {
        if (ChatSocket.CONFIG.communicationType() == BaseConfigModel.CommunicationType.PLAIN_TEXT) {
            ChatSocket.executeIncomingMessage(message);
            return;
        }

        JsonObject object;

        try {
            object = JsonParser.parseString(message).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            ChatSocket.LOGGER.error("Error parsing JSON message: " + e.getMessage());
            sendErrorMessage(conn, "Error parsing JSON message: " + e.getMessage());
            return;
        }

        if (!object.has("type") || !object.has("message")) {
            sendErrorMessage(conn, "Invalid message format: " + message);
            return;
        }

        String type = object.get("type").getAsString();
        String messageString = object.get("message").getAsString();

        if (type.equals("chat")) {
            ChatSocket.executeIncomingMessage(messageString);
        } else if (type.equals("command")) {
            ChatSocket.executeIncomingMessage("/" + messageString);
        } else {
            sendErrorMessage(conn, "Unknown message type: " + type);
        }
    }

    public void broadcastCommunicationTypeChange() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "communicationType");
        object.addProperty("communicationType", ChatSocket.CONFIG.communicationType().name());
        this.broadcast(object.toString());
    }

    public void sendCommunicationTypeChange(WebSocket socket) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "communicationType");
        object.addProperty("communicationType", ChatSocket.CONFIG.communicationType().name());
        socket.send(object.toString());
    }

    public void sendErrorMessage(WebSocket conn, String message) {
        if (ChatSocket.CONFIG.communicationType() == BaseConfigModel.CommunicationType.PLAIN_TEXT) {
            conn.send(message);
            return;
        }

        JsonObject object = new JsonObject();
        object.addProperty("error", message);
        object.addProperty("type", "error");
        conn.send(object.toString());
    }

    public void handleChatMessage(Component message, GameProfile sender, Instant receptionTimestamp) {
        if (ChatSocket.CONFIG.communicationType() == BaseConfigModel.CommunicationType.PLAIN_TEXT) {
            this.broadcast(message.getString());
            return;
        }

        JsonObject object = new JsonObject();
        object.addProperty("messageString", message.getString());
        object.addProperty("type", "chat");

        JsonObject gameProfileElement = new JsonObject();
        gameProfileElement.addProperty("uuid", sender.getId().toString());
        gameProfileElement.addProperty("name", sender.getName());
        // gameProfileElement.add("properties", sender.getProperties());
        object.add("gameProfile", gameProfileElement);
        object.addProperty("timestamp", receptionTimestamp.toString());

        this.broadcast(object.toString());
    }

    public void handleGameMessage(Component message) {
        if (ChatSocket.CONFIG.communicationType() == BaseConfigModel.CommunicationType.PLAIN_TEXT) {
            this.broadcast(message.getString());
            return;
        }

        JsonObject object = new JsonObject();
        object.addProperty("messageString", message.getString());
        object.addProperty("type", "game");
        this.broadcast(object.toString());
    }
}
