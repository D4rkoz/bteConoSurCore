package pizzaaxx.bteconosur;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.omg.CORBA.ARG_IN;
import pizzaaxx.bteconosur.chats.ChatRegistry;
import pizzaaxx.bteconosur.chats.Events;
import pizzaaxx.bteconosur.commands.*;
import pizzaaxx.bteconosur.country.Country;
import pizzaaxx.bteconosur.discord.DiscordHandler;
import pizzaaxx.bteconosur.discord.commands.*;
import pizzaaxx.bteconosur.events.EventsCommand;
import pizzaaxx.bteconosur.join.Join;
import pizzaaxx.bteconosur.link.LinkDiscord;
import pizzaaxx.bteconosur.link.LinkMinecraft;
import pizzaaxx.bteconosur.points.Scoreboard;
import pizzaaxx.bteconosur.presets.PresetsEvent;
import pizzaaxx.bteconosur.presets.PresetsCommand;
import pizzaaxx.bteconosur.projects.*;
import pizzaaxx.bteconosur.ranks.Donator;
import pizzaaxx.bteconosur.ranks.PrefixCommand;
import pizzaaxx.bteconosur.ranks.PromoteDemote;
import pizzaaxx.bteconosur.ranks.Streamer;
import pizzaaxx.bteconosur.serverPlayer.PlayerRegistry;
import pizzaaxx.bteconosur.serverPlayer.ServerPlayer;
import pizzaaxx.bteconosur.teleport.OnTeleport;
import pizzaaxx.bteconosur.teleport.PWarp;
import pizzaaxx.bteconosur.testing.Testing;
import pizzaaxx.bteconosur.worldedit.IncrementCommand;
import pizzaaxx.bteconosur.worldedit.Polywall;
import pizzaaxx.bteconosur.worldedit.ShortCuts;
import pizzaaxx.bteconosur.yaml.Configuration;
import pizzaaxx.bteconosur.yaml.YamlManager;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static pizzaaxx.bteconosur.Config.gateway;
import static pizzaaxx.bteconosur.country.Country.countryNames;
import static pizzaaxx.bteconosur.discord.Bot.chileBot;
import static pizzaaxx.bteconosur.discord.Bot.conoSurBot;
import static pizzaaxx.bteconosur.projects.ProjectsCommand.background;
import static pizzaaxx.bteconosur.ranks.PromoteDemote.lp;

public final class BteConoSur extends JavaPlugin {

    public static DiscordHandler discord;
    public static World mainWorld = null;
    public static File pluginFolder = null;
    public static String key;
    public static PlayerRegistry playerRegistry = new PlayerRegistry();
    public static ChatRegistry chatRegistry = new ChatRegistry();
    public static Map<Country, Guild> guilds = new HashMap<>();

    @Override
    public void onEnable() {

        getLogger().info("Enabling  BTE Cono Sur!");

        registerListeners(
                new Join(playerRegistry),
                new ProjectActionBar(),
                new OnTeleport(),
                new PresetsEvent(),
                new PRandom(),
                new PresetsEvent(),
                new ShortCuts(playerRegistry),
                new Events(),
                new Scoreboard(),
                new GetCommand(),
                new PrefixCommand(),
                new LobbyCommand(),
                new EventsCommand()
        );

        getCommand("project").setExecutor(new ProjectsCommand());
        getCommand("link").setExecutor(new LinkMinecraft());
        getCommand("unlink").setExecutor(new LinkMinecraft());
        getCommand("nightvision").setExecutor(new NightVisionCommand());
        getCommand("promote").setExecutor(new PromoteDemote());
        getCommand("prefix").setExecutor(new PrefixCommand());
        getCommand("chat").setExecutor(new pizzaaxx.bteconosur.chats.Command());
        getCommand("nickname").setExecutor(new NickNameCommand());
        getCommand("test").setExecutor(new Testing());
        getCommand("demote").setExecutor(new PromoteDemote());
        getCommand("project").setTabCompleter(new TabCompletions());
        getCommand("presets").setExecutor(new PresetsCommand());
        getCommand("googlemaps").setExecutor(new GoogleMapsCommand());
        getCommand("increment").setExecutor(new IncrementCommand(playerRegistry));
        getCommand("pwarp").setExecutor(new PWarp());
        getCommand("/polywalls").setExecutor(new Polywall());
        getCommand("treegroup").setExecutor(new pizzaaxx.bteconosur.worldedit.trees.Events());
        getCommand("/treecover").setExecutor(new pizzaaxx.bteconosur.worldedit.trees.Events());
        getCommand("donator").setExecutor(new Donator());
        getCommand("streamer").setExecutor(new Streamer());
        getCommand("streaming").setExecutor(new StreamingCommand());
        getCommand("get").setExecutor(new GetCommand());
        getCommand("scoreboard").setExecutor(new Scoreboard());
        getCommand("tpdir").setExecutor(new TpDirCommand());
        getCommand("event").setExecutor(new EventsCommand());
        getCommand("lobby").setExecutor(new LobbyCommand());
        getCommand("assets").setExecutor(new LobbyCommand());
        getCommand("event").setExecutor(new EventsCommand());
        getCommand("manageevent").setExecutor(new EventsCommand());
        getCommand("help").setExecutor(new HelpCommand());

        pluginFolder = Bukkit.getPluginManager().getPlugin("bteConoSur").getDataFolder();
        mainWorld = Bukkit.getWorld("BTECS");

        createDirectories(
                "",
                "projects",
                "playerData",
                "link",
                "pending_projects",
                "projectTags",
                "discord",
                "chat",
                "points",
                "trees/schematics"
        );

        // GUI
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);
        background = glass;

        // DISCORD BOT
        JDABuilder builder = JDABuilder.createDefault((String) new YamlManager(pluginFolder, "discord/token.yml").getValue("token"));
        builder.setActivity(Activity.playing("IP: bteconosur.com"));
        builder.setStatus(OnlineStatus.ONLINE);

        registerDiscordListener(builder,
                new LinkDiscord(),
                new ProjectCommand(),
                new RequestResponse(),
                new Events(),
                new ModsCommand(),
                new SchematicCommand(),
                new PlayerCommand(),
                new OnlineWhereCommand(),
                new ScoreboardCommand(),
                new HelpCommand(),
                new HelpButtonsCommand()
        );

        builder.enableIntents(GatewayIntent.DIRECT_MESSAGES);
        try {
            conoSurBot = builder.build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }

        Configuration guildsSection = new Configuration(this, "discord/guilds");
        countryNames.forEach(name -> guilds.put(new Country(name), conoSurBot.getGuildById(guildsSection.getString(name))));


        Configuration configuration = new Configuration(this, "config");
        Config config = new Config(configuration);
        getCommand("btecs_reload").setExecutor(config);

        JDABuilder chile = JDABuilder.createDefault((String) new YamlManager(pluginFolder, "discord/token.yml").getValue("chile"));
        chile.setStatus(OnlineStatus.ONLINE);
        chile.setActivity(Activity.playing("IP: bteconosur.com"));
        try {
            chileBot = chile.build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }

        key = (String) new YamlManager(pluginFolder, "key.yml").getValue("key");

        // LUCKPERMS

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            lp = provider.getProvider();
        }

        EmbedBuilder online = new EmbedBuilder();
        online.setColor(new Color(0, 255, 42));
        online.setTitle("¡El servidor ya está online!");
        online.setDescription("\uD83D\uDD17 **IP:** bteconosur.com");

        discord = new DiscordHandler();

        gateway.sendMessageEmbeds(online.build()).queue();

        chatRegistry.register("global");
        chatRegistry.register("argentina");
        chatRegistry.register("bolivia");
        chatRegistry.register("chile");
        chatRegistry.register("paraguay");
        chatRegistry.register("peru");
        chatRegistry.register("uruguay");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, Scoreboard::checkAutoScoreboards, 300, 300);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling  BTE Cono Sur!");

        EmbedBuilder online = new EmbedBuilder();
        online.setColor(new Color(255, 0, 0));
        online.setTitle("El servidor ha sido apagado.");
        online.setDescription("Te esperamos cuando vuelva a estar disponible.");

        gateway.sendMessageEmbeds(online.build()).queue();

        conoSurBot.shutdown();
        chileBot.shutdown();
    }

    public static void broadcast(String message) {
        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            ServerPlayer s = new ServerPlayer(p);
            if (!(s.isChatHidden())) {
                p.sendMessage(message);
            }
        }
    }

    public static void broadcast(BaseComponent message) {
        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            ServerPlayer s = new ServerPlayer(p);
            if (!(s.isChatHidden())) {
                p.sendMessage(message);
            }
        }
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager()
                    .registerEvents(listener, this);
        }
    }

    private void registerDiscordListener(JDABuilder builder, EventListener... listeners) {
        for (EventListener listener : listeners) {
            builder.addEventListeners(listener);
        }
    }

    private void createDirectories(String... names) {
        for (String name : names) {
            File file = new File(getDataFolder(), name);
            file.mkdirs();
        }
    }

}
