package pizzaaxx.bteconosur.chats;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pizzaaxx.bteconosur.serverPlayer.ChatManager;
import pizzaaxx.bteconosur.serverPlayer.ServerPlayer;
import pizzaaxx.bteconosur.methods.CodeGenerator;
import pizzaaxx.bteconosur.projects.Project;

import java.util.HashMap;
import java.util.Map;

public class Command implements CommandExecutor {
    public static String chatsPrefix = "§f[§aCHAT§f] §7>>§r ";
    public static Map<String, String> chatInvites = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player p = (Player) sender;
        ServerPlayer s = new ServerPlayer(p);
        ChatManager manager = s.getChatManager();
        Chat pChat = manager.getChat();

        if (args.length == 0) {
            p.sendMessage(chatsPrefix + "Estás en el chat §a" + pChat.getFormattedName() + "§f.");
        } else {
            if (args[0].equalsIgnoreCase("argentina") || args[0].equalsIgnoreCase("bolivia") || args[0].equalsIgnoreCase("chile") || args[0].equalsIgnoreCase("paraguay") || args[0].equalsIgnoreCase("peru") || args[0].equalsIgnoreCase("uruguay") || args[0].equalsIgnoreCase("global")) {
                if (!(pChat.getName().equals(args[0]))) {
                    manager.setChat(args[0]);

                    p.sendMessage(chatsPrefix + "Te has unido al chat de §a" + args[0].toUpperCase() + "§f. §7(Jugadores: " + manager.getChat().getMembersAmount() + ")");

                } else {
                    p.sendMessage(chatsPrefix + "Ya estás en este chat.");
                }
            } else if (Bukkit.getOfflinePlayer(args[0]).isOnline()) {
                ServerPlayer target = new ServerPlayer(Bukkit.getPlayer(args[0]));
                p.sendMessage(chatsPrefix + "§a" + target.getName() + "§f está en el chat §a" + target.getChatManager().getChat().getFormattedName() + "§f.");
            } else if (args[0].equalsIgnoreCase("project") || args[0].equalsIgnoreCase("proyecto")) {
                try {
                    Project project = new Project(p.getLocation());

                    if (project.getAllMembers() != null && project.getAllMembers().contains(p)) {
                        if (!(pChat.getName().equals(args[0]))) {
                            manager.setChat("project_" + project.getId());

                            p.sendMessage(chatsPrefix + "Te has unido al chat del proyecto §a" + project.getName(true) + "§f. §7(Jugadores: " + manager.getChat().getMembersAmount() + ")");
                        } else {
                            p.sendMessage(chatsPrefix + "Ya estás en este chat.");
                        }
                    } else {
                        p.sendMessage(chatsPrefix + "No puedes unirte al chat de un proyecto del que no eres miembro a menos que te inviten.");
                    }
                } catch (Exception exception) {
                    p.sendMessage(chatsPrefix + "No estás dentro de ningún proyecto.");
                }
            } else if (args[0].equals("toggle") || args[0].equals("alternar")) {
                if (p.hasPermission("bteconosur.chat.toggle")) {
                    if (manager.toggleChat()) {
                        p.sendMessage(chatsPrefix + "Has ocultado el chat.");
                    } else {
                        p.sendMessage(chatsPrefix + "Ahora puedes ver el chat.");
                    }
                } else {
                    p.sendMessage(chatsPrefix + "§cNo tienes permiso para hacer eso.");
                }
            } else if (args[0].equals("default") || args[0].equals("predeterminado")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("argentina") || args[1].equalsIgnoreCase("bolivia") || args[1].equalsIgnoreCase("chile") || args[1].equalsIgnoreCase("paraguay") || args[1].equalsIgnoreCase("peru") || args[1].equalsIgnoreCase("uruguay") || args[1].equalsIgnoreCase("global")) {
                        if (!(manager.getDefaultChat().getName().equals(args[1]))) {
                            manager.setDefaultChat(args[1]);
                            p.sendMessage(chatsPrefix + "Chat predeterminado establecido en el chat §a" + args[1].toUpperCase() + "§f.");
                        } else {
                            p.sendMessage(chatsPrefix + "Este ya es tu chat predeterminado.");
                        }
                    } else if (args[1].equalsIgnoreCase("project") || args[1].equals("proyecto")) {
                        try {
                            Project project = new Project(p.getLocation());

                            if (project.getAllMembers() != null && project.getAllMembers().contains(p)) {
                                if (!(manager.getDefaultChat().getName().equals("project_" + project.getId()))) {
                                    manager .setDefaultChat("project_" + project.getId());

                                    p.sendMessage(chatsPrefix + "Chat predeterminado establecido en el chat del proyecto §a" + project.getName(true) + "§f.");
                                } else {
                                    p.sendMessage(chatsPrefix + "Este ya es tu chat predeterminado.");
                                }
                            } else {
                                p.sendMessage(chatsPrefix + "Solo los miembros del proyecto pueden establecer el chat del proyecto como predeterminado.");
                            }
                        } catch (Exception exception) {
                            p.sendMessage(chatsPrefix + "No estás dentro de ningún proyecto.");
                        }
                    } else {
                        p.sendMessage(chatsPrefix + "Posibles chats: §aGLOBAL§f, §aARGENTINA§f, §aBOLIVIA§f, §aCHILE§f, §aPARAGUAY§f, §aPERU§f, §aURUGUAY§f, §aPROJECT§f.");
                    }
                } else {
                    if (!(pChat.equals(manager.getDefaultChat()))) {
                        manager.setChat(manager.getDefaultChat().getName());

                        p.sendMessage(chatsPrefix + "Te has unido a tu chat predeterminado: §a" + manager.getChat().getFormattedName());
                    } else {
                        p.sendMessage(chatsPrefix + "Ya estás en tu chat predeterminado.");
                    }
                }
            } else if (args[0].equals("invite") || args[0].equals("invitar")) {
                if (args.length > 1) {
                    if (Bukkit.getOfflinePlayer(args[1]).isOnline()) {
                        Player target = Bukkit.getPlayer(args[1]);

                        if (new ServerPlayer(target).getChatManager().getChat().equals(pChat)) {
                            p.sendMessage(chatsPrefix + "El jugador ya se encuentra en tu chat.");
                            return true;
                        }

                        String code = CodeGenerator.generateCode(6);

                        while (chatInvites.containsKey(code)) {
                            code = CodeGenerator.generateCode(6);
                        }

                        chatInvites.put(code, pChat.getName());

                        p.sendMessage(chatsPrefix + "Has invitado a §a" + new ServerPlayer(target).getName() + "§f a tu chat.");

                        target.sendMessage(chatsPrefix + "§a" + s.getName() + "§f te ha invitado a su chat (" + pChat.getFormattedName() + "). Usa §a/chat" + code + "§f para unirte.");
                    } else {
                        p.sendMessage(chatsPrefix + "El jugador no está online.");
                    }
                } else {
                    p.sendMessage(chatsPrefix + "Introduce un jugador a invitar.");
                }
            } else {
                if (args[0].matches("[a-z]{6}") && chatInvites.containsKey(args[0])) {
                    if (!(pChat.getName().equals(chatInvites.get(args[0])))) {
                        manager.setChat(chatInvites.get(args[0]));

                        Chat newChat = manager.getChat();

                        p.sendMessage(chatsPrefix + "Te has unido al chat §a" + newChat.getFormattedName() + "§f. §7(Jugadores: " + newChat.getMembersAmount() + ")");
                    } else {
                        p.sendMessage(chatsPrefix + "Ya estás en este chat.");
                    }
                } else {
                    p.sendMessage(chatsPrefix + "Código de invitación inválido.");
                }
            }
        }
        return true;
    }
}
