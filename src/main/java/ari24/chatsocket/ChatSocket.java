package ari24.chatsocket;

import ari24.chatsocket.config.BaseConfigModel;
import ari24.chatsocket.config.ChatSocketConfig;
import ari24.chatsocket.ws.ChatsocketServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ChatSocket implements ModInitializer {
	public static final String MOD_ID = "chatsocket";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ChatSocketConfig CONFIG = ChatSocketConfig.createAndLoad();

	private ChatsocketServer server;

	@Override
	public void onInitialize() {
		server = new ChatsocketServer(new InetSocketAddress(CONFIG.host(), CONFIG.port()));
		server.start();

		LOGGER.info("Registering ChatSocket mod...");

		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			server.handleChatMessage(message, sender, receptionTimestamp);
        });

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			server.handleGameMessage(message, overlay);
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            try {
                server.stop();
            } catch (InterruptedException e) {
                LOGGER.error("Error stopping server", e);
            }
        });

		CONFIG.subscribeToCommunicationType((communicationType -> {
			if (communicationType == BaseConfigModel.CommunicationType.PLAIN_TEXT) {
				LOGGER.info("Communication type changed to PLAIN_TEXT");
			} else {
				LOGGER.info("Communication type changed to JSON");
			}

			if (server != null) {
				server.broadcastCommunicationTypeChange();
			}
		}));
	}

	public static void executeIncomingMessage(String message) {
		if (Minecraft.getInstance().player == null) {
			LOGGER.warn("Player is null, cannot handle message");
			return;
		}

		if (message.startsWith("/")) {
			Minecraft.getInstance().player.connection.sendCommand(message.substring(1));
		} else {
			Minecraft.getInstance().player.connection.sendChat(message);
		}
	}
}