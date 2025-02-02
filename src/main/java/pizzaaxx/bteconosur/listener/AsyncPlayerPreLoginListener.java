package pizzaaxx.bteconosur.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import pizzaaxx.bteconosur.serverPlayer.PlayerRegistry;
import pizzaaxx.bteconosur.serverPlayer.ServerPlayer;

public class AsyncPlayerPreLoginListener implements Listener {

    private final PlayerRegistry playerRegistry;

    public AsyncPlayerPreLoginListener(PlayerRegistry playerRegistry) {
        this.playerRegistry = playerRegistry;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        ServerPlayer serverPlayer = new ServerPlayer(event.getUniqueId());

        playerRegistry.add(serverPlayer);
    }

}
