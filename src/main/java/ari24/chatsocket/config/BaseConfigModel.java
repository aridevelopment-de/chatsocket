package ari24.chatsocket.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RestartRequired;

@Modmenu(modId="chatsocket")
@Config(name="chatsocket-config", wrapperName="ChatSocketConfig")
public class BaseConfigModel {
    @RestartRequired
    public int port = 8080;
    @RestartRequired
    public String host = "127.0.0.1";
    @RestartRequired
    public boolean enableChatSocket = true;
    @Hook
    public CommunicationType communicationType = CommunicationType.PLAIN_TEXT;

    public enum CommunicationType {
        PLAIN_TEXT,
        JSON
    }
}
