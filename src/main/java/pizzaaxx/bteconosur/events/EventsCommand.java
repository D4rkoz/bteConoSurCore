package pizzaaxx.bteconosur.events;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import pizzaaxx.bteconosur.serverPlayer.ServerPlayer;
import pizzaaxx.bteconosur.country.Country;
import pizzaaxx.bteconosur.misc.Misc;
import pizzaaxx.bteconosur.player.data.PlayerData;

import java.util.*;
import java.util.List;

import static pizzaaxx.bteconosur.BteConoSur.mainWorld;
import static pizzaaxx.bteconosur.country.Country.countryAbbreviations;
import static pizzaaxx.bteconosur.projects.ProjectsCommand.background;
import static pizzaaxx.bteconosur.worldedit.Methods.getSelection;
import static pizzaaxx.bteconosur.worldedit.Methods.polyRegion;
import static pizzaaxx.bteconosur.worldguard.WorldGuardProvider.getWorldGuard;

public class EventsCommand implements CommandExecutor, Listener {

    public static String eventsPrefix = "[§5EVENTO§f] §7>> §f";
    public static Set<CommandSender> startConfirm = new HashSet<>();
    public static Set<CommandSender> stopConfirm = new HashSet<>();
    public static Set<CommandSender> readyConfirm = new HashSet<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getSize() == 27 && inv.getName().equals("Elige un evento")) {
            Player p = (Player) e.getWhoClicked();
            e.setCancelled(true);
            if (e.getCurrentItem() != background) {
                if (e.getSlot() == 26) {
                    p.closeInventory();
                } else {
                    String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).replace("Perú", "Peru").toLowerCase();
                    Event event = new Event(new Country(name));
                    p.closeInventory();
                    p.teleport(event.getTp());
                    p.sendMessage(eventsPrefix + "¡Bienvenido al evento §a" + event.getName() + " (" + StringUtils.capitalize(name.replace("peru", "perú") + ")§f!"));
                    p.sendMessage(eventsPrefix + "Usa §a/event join§f para unirte al evento.");
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender p, org.bukkit.command.Command command, String label, final String[] args) {
        if (command.getName().equals("event")) {
            if (p instanceof Player) {
                Player player = (Player) p;
                if (args.length > 0) {
                    if (args[0].equals("join")) {
                        Country country = null;
                        if (args.length > 1) {
                            country = new Country(args[1]);
                            if (country.getCountry() == null) {
                                p.sendMessage(eventsPrefix + "Introduce un país válido.");
                                return true;
                            }
                        } else {
                            RegionManager regionManager = getWorldGuard().getRegionManager(mainWorld);
                            boolean found = false;
                            for (ProtectedRegion region : regionManager.getApplicableRegions(player.getLocation()).getRegions()) {
                                if (region.getId().startsWith("evento_")) {
                                    country = new Country(region.getId().replace("evento_", ""));
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                player.sendMessage(eventsPrefix + "No estás en la zona de ningún evento.");
                                return true;
                            }
                        }
                        Event event = new Event(country);
                        if (event.getStatus() != Event.Status.OFF) {
                            if (!event.getParticipants().contains(player)) {
                                ServerPlayer s = new ServerPlayer(player);
                                if (s.getMaxPoints() >= event.getMinPoints()) {
                                    event.addParticipant(player);
                                    event.save();
                                    player.sendMessage(eventsPrefix + "¡Te has unido al evento \"" + event.getName() + "\"! ¡Esperamos que te diviertas!");
                                    if (event.getStatus() == Event.Status.ON) {
                                        for (OfflinePlayer offlinePlayer : event.getParticipants()) {
                                            if (offlinePlayer.isOnline() && offlinePlayer != player) {
                                                ((Player) offlinePlayer).sendMessage(eventsPrefix + "¡§a" + s.getChatManager().getDisplayName() + "§f se ha unido al evento §a" + event.getName() + "§f.");
                                            }
                                        }
                                        if (s.newGetPrimaryGroup() == ServerPlayer.PrimaryGroup.DEFAULT) {
                                            s.addSecondaryGroup("evento");
                                        }
                                    } else {
                                        if (s.hasDiscordUser()) {
                                            player.sendMessage(eventsPrefix + "Recibirás una notificación por Discord cuando el evento comience.");
                                        } else {
                                            player.sendMessage(eventsPrefix + "Se te dejará una notificación en Minecraft cuando el evento comience.");
                                        }
                                    }
                                    PlayerData playerData = new PlayerData(player);
                                    playerData.addToList("events", country.getCountry(), false);
                                    playerData.save();
                                } else {
                                    player.sendMessage(eventsPrefix + "Necesitas al menos §a" + event.getMinPoints() + "§f puntos para unirte al evento.");
                                }
                            } else {
                                player.sendMessage(eventsPrefix + "Ya eres parte del evento.");
                            }
                        } else {
                            player.sendMessage(eventsPrefix + "No estás en la zona de ningún evento.");
                        }
                    } else if (args[0].equals("leave")) {
                        Country country = null;
                        if (args.length > 1) {
                            country = new Country(args[1]);
                            if (country.getCountry() == null) {
                                p.sendMessage(eventsPrefix + "Introduce un país válido.");
                                return true;
                            }
                        } else {
                            RegionManager regionManager = getWorldGuard().getRegionManager(mainWorld);
                            boolean found = false;
                            for (ProtectedRegion region : regionManager.getApplicableRegions(player.getLocation()).getRegions()) {
                                if (region.getId().startsWith("evento_")) {
                                    country = new Country(region.getId().replace("evento_", ""));
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                player.sendMessage(eventsPrefix + "No estás en la zona de ningún evento.");
                                return true;
                            }
                        }
                        Event event = new Event(country);
                        if (event.getStatus() != Event.Status.OFF) {
                            if (event.getParticipants().contains(player)) {
                                ServerPlayer s = new ServerPlayer(player);
                                PlayerData playerData = new PlayerData(player);
                                player.sendMessage(eventsPrefix + "Has abandonado el evento \"" + event.getName() + "\".");
                                if (event.getStatus() == Event.Status.ON) {
                                    for (OfflinePlayer offlinePlayer : event.getParticipants()) {
                                        if (offlinePlayer.isOnline() && offlinePlayer != player) {
                                            ((Player) offlinePlayer).sendMessage(eventsPrefix + "§a" + s.getChatManager().getDisplayName() + "§f ha abandonado el evento §a" + event.getName() + "§f.");
                                        }
                                    }
                                    // TODO CHECK IF EVENT GROUP WHEN PROMOTING TO POSTULANTE
                                    if (s.newGetPrimaryGroup() == ServerPlayer.PrimaryGroup.DEFAULT && playerData.getList("events").size() == 1) {
                                        s.removeSecondaryGroup("evento");
                                    }
                                }
                                playerData.removeFromList("events", country.getCountry());
                                playerData.save();

                                event.removeParticipant(player);
                                event.save();
                            } else {
                                player.sendMessage(eventsPrefix + "No eres parte del evento.");
                            }
                        } else {
                            player.sendMessage(eventsPrefix + "No estás en la zona de ningún evento.");
                        }
                    } else {
                        player.sendMessage(eventsPrefix + "Introduce un subcomando válido.");
                    }
                } else {
                    Inventory gui = Bukkit.createInventory(null, 27, "Elige un evento");
                    for (int i = 0; i < 27; i++) {
                        gui.setItem(i, background);
                    }
                    gui.setItem(26, Misc.getCustomHead("§fSalir", null, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="));

                    Event event = new Event(new Country("argentina"));
                    gui.setItem(10, Misc.getCustomHead("§a§lArgentina", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZkMDMzZGM1ZjY3NWFkNTFiYzA2YzdhMTk0OWMzNWExZDM3ZTQ4YTJlMWMyNzg5YzJjZjdkMzBlYzU4ZjMyYyJ9fX0="));

                    event = new Event(new Country("bolivia"));
                    gui.setItem(11, Misc.getCustomHead("§a§lBolivia", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQyYzlmOTg2MThjZDVmN2RiZjBjMWE1NGVlMDk0NzQ2NjJiNzEzYjVhYTI2NWM4NWVmYmZjNDY0MThlOTE1In19fQ=="));

                    event = new Event(new Country("chile"));
                    gui.setItem(12, Misc.getCustomHead("§a§lChile", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTAzNTk0YzBjMTE2YjA1ZDc1NjA2MGEyMjM5ODM3NzQ3ODg4NzMyMjY5MzVkOTYyNzExYmMzZTI1ODQ2ZGM2YiJ9fX0="));

                    event = new Event(new Country("paraguay"));
                    gui.setItem(13, Misc.getCustomHead("§a§lParaguay", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE3OGRlNjkxYjUwOGQ2MjQ5MTA5ZmM1NGFmNmZiYTQ5YmFhODM3N2FkMzcwNjEyZWQ2MTdkNzdkZDZhZDU4OCJ9fX0="));

                    event = new Event(new Country("peru"));
                    gui.setItem(14, Misc.getCustomHead("§a§lPerú", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjRkMDNiZDQ0MTBiYWJkYzY4MjQ5M2IzYzJiYmEyNmU3MzBlNmJjNjU4ZDM4ODhlNzliZjcxMmY4NTMifX19"));

                    event = new Event(new Country("uruguay"));
                    gui.setItem(15, Misc.getCustomHead("§a§lUruguay", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg0NDA1OTdjNGJjMmFhZDYwMGE1NDYwNGRjN2IxZmI3NzEzNDNlMDIyZTZhMmUwMjJmOTBlNDBjYzI1ZjlmOCJ9fX0="));

                    event = new Event(new Country("global"));
                    gui.setItem(16, Misc.getCustomHead("§a§lGlobal", (event.getStatus() == Event.Status.OFF ? "§aEstado: §c§lApagado" : "§aEstado: " + (event.getStatus() == Event.Status.ON ? "§2§lEn curso" : "§e§lPreparado") + "\n§aNombre: §f" + event.getName() + "\n§aFecha: §f" + event.getDate() + "\n§aPuntos mínimos: §f" + event.getMinPoints() + "\n§7§oHaz click para ir"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThkYWExZTNlZDk0ZmYzZTMzZTFkNGM2ZTQzZjAyNGM0N2Q3OGE1N2JhNGQzOGU3NWU3YzkyNjQxMDYifX19"));

                    player.openInventory(gui);
                }
            } else {
                p.sendMessage(eventsPrefix + "No puedes usar este comando desde la consola.");
            }
        }

        if (command.getName().equals("manageevent")) {
            if (args.length > 0) {
                if (countryAbbreviations.contains(args[0])) {
                    Country pais = new Country(args[0]);
                    final String country = pais.getCountry();
                    if (args.length > 1) {
                        Event event = new Event(pais);
                        if (args[1].equals("name")) {
                            if (args.length > 2) {
                                String name = String.join(" ", Arrays.asList(args).subList(2, args.length));
                                if (name.matches("[ A-Za-z0-9/]{1,32}")) {
                                    event.setName(name);
                                    event.save();
                                    p.sendMessage(eventsPrefix + "Has establecido el nombre del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f en §a" + name + "§f.");

                                } else {
                                    p.sendMessage(eventsPrefix + "Introduce un nombre válido.");
                                }
                            } else {
                                p.sendMessage(eventsPrefix + "El nombre del evento del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f es §a" + event.getName() + "§f.");
                            }
                        } else if (args[1].equals("date")) {
                            if (args.length > 2) {
                                String date = String.join(" ", Arrays.asList(args).subList(2, args.length));
                                if (date.matches("[ -A-Za-z0-9/]{1,32}")) {
                                    event.setDate(date);
                                    event.save();
                                    p.sendMessage(eventsPrefix + "Has establecido la fecha del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f en §a" + date + "§f.");

                                } else {
                                    p.sendMessage(eventsPrefix + "Introduce un texto válido.");
                                }
                            } else {
                                p.sendMessage(eventsPrefix + "La fecha del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f es §a" + event.getDate() + "§f.");
                            }
                        } else if (args[1].equals("status")) {
                            if (args.length > 2) {
                                if (args[2].equals("stop") || args[2].equals("off") || args[2].equals("terminar")) {
                                    if (event.getStatus() != Event.Status.OFF) {
                                        if (stopConfirm.contains(p)) {
                                            stopConfirm.remove(p);
                                            event.stop();
                                            p.sendMessage("Has terminado el evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f.");
                                        } else if (event.getStatus() == Event.Status.READY) {
                                            stopConfirm.add(p);
                                            p.sendMessage(eventsPrefix + "§cToda la configuración del evento se perderá.§f Usa el comando de nuevo para confirmar.");
                                        } else {
                                            stopConfirm.add(p);
                                            p.sendMessage(eventsPrefix + "§cNo puedes deshacer esta acción.§f Usa el comando de nuevo para confirmar.");
                                        }
                                    }
                                } else if (args[2].equals("ready") || args[2].equals("listo") || args[2].equals("preparado")) {
                                    if (event.getStatus() == Event.Status.OFF) {
                                        if (readyConfirm.contains(p)) {
                                            readyConfirm.remove(p);

                                            event.prepared();

                                            p.sendMessage("Has marcado el evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f como preparado.");

                                        } else {
                                            readyConfirm.add(p);
                                            p.sendMessage(eventsPrefix + "¿Estás seguro de que quieres marcar el evento como terminado? A continuación se hay una previsualización del evento. Usa el comando de nuevo para confirmar.");
                                            p.sendMessage(">+-----------+[-< §5EVENTO§f >-]+-----------+<");
                                            p.sendMessage("§aNombre: §f" + event.getName());
                                            p.sendMessage("§aFecha: §f" + event.getDate());
                                            p.sendMessage("§aPuntos mínimos: §f" + event.getMinPoints());
                                            p.sendMessage("§aImagen: §f" + event.getImage());
                                            p.sendMessage("§aCoordenadas: §f" + event.getTp().getBlockX() + ", " + event.getTp().getBlockY() + ", " + event.getTp().getBlockZ());
                                            p.sendMessage(">+-----------+[-< ====== >-]+-----------+<");
                                        }
                                    } else {
                                        p.sendMessage(eventsPrefix + "Sólo puedes hacer esto cuando el proyecto está apagado.");
                                    }
                                } else if (args[2].equals("start") || args[2].equals("on") || args[2].equals("empezar")) {
                                    if (event.getStatus() == Event.Status.READY) {
                                        if (startConfirm.contains(p)) {
                                            startConfirm.remove(p);

                                            event.start();

                                            p.sendMessage("Has empezado el evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f.");

                                        } else {
                                            startConfirm.add(p);
                                            p.sendMessage(eventsPrefix + "¿Estás seguro de que quieres iniciar el evento? Usa el comando de nuevo para confirmar.");
                                        }
                                    } else {
                                        p.sendMessage(eventsPrefix + "Solo puedes iniciar un evento cuando este está marcado como listo.");
                                    }
                                }
                            } else {
                                p.sendMessage(eventsPrefix + "Introduce una acción.");
                            }
                        } else if (args[1].equals("tp")) {
                            if (args.length > 2) {
                                if (args[2].equals("here")) {
                                    if (p instanceof Player) {
                                        Player player = (Player) p;
                                        event.setTp(player.getLocation());
                                        event.save();
                                        p.sendMessage(eventsPrefix + "Has establecido el teletransporte al evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f en tu posición actual.");

                                    } else {
                                        p.sendMessage(eventsPrefix + "No puedes usar este comando desde la consola.");
                                    }
                                } else if (args.length > 4 && args[2].matches("^-?[0-9]{1,16}(\\.[0-9]{1,16})?$") && args[3].matches("^-?[0-9]{1,16}(\\.[0-9]{1,16})?$") && args[4].matches("^-?[0-9]{1,16}(\\.[0-9]{1,16})?$") ) {
                                    double x = Double.parseDouble(args[2]);
                                    double y = Double.parseDouble(args[3]);
                                    double z = Double.parseDouble(args[4]);

                                    event.setTp(new Location(mainWorld, x, y, z));
                                    event.save();
                                    p.sendMessage(eventsPrefix + "Has establecido el teletransporte al evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f en §a" + x + ", " + y + ", " + z + "§f.");
                                }
                            } else {
                                p.sendMessage(eventsPrefix + "El teletransporte del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f está en §a" + event.getTp().getBlockX() + ", " +  event.getTp().getBlockY() + ", " + event.getTp().getBlockZ() + "§f.");
                            }

                        } else if (args[1].equals("redefine")) {
                            if (event.getStatus() != Event.Status.ON) {
                                if (p instanceof Player) {
                                    Player player = (Player) p;

                                    List<BlockVector2D> points = new ArrayList<>();

                                    try {
                                        points = polyRegion(getSelection(player)).getPoints();
                                    } catch (IllegalArgumentException e) {
                                        player.sendMessage(eventsPrefix + "Selecciona un área cúbica o poligonal.");
                                    } catch (IncompleteRegionException e) {
                                        player.sendMessage(eventsPrefix + "Selecciona un área primero.");
                                    }

                                    event.setNewRegion(points);
                                    event.save();

                                    p.sendMessage(eventsPrefix + "Has redefinido la región del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f.");

                                } else {
                                    p.sendMessage(eventsPrefix + "No se puede ejecutar este comando desde la consola.");
                                }
                            } else {
                                p.sendMessage(eventsPrefix + "No puedes hacer esto mientras el evento está activo.");
                            }

                        } else if (args[1].equals("minpoints")) {
                            if (args.length > 2) {
                                if (event.getStatus() == Event.Status.ON) {
                                    p.sendMessage(eventsPrefix + "No puedes cambiar esto mientras un evento está activo.");
                                    return true;
                                }
                                if (args[2].matches("[0-9]{1,10}") && Integer.parseInt(args[2]) >= 0) {
                                    event.setMinPoints(Integer.parseInt(args[2]));
                                    event.save();
                                    p.sendMessage(eventsPrefix + "Has establecido el mínimo de puntos para el evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f en §a" + args[2] + "§f.");
                                } else {
                                    p.sendMessage(eventsPrefix + "Introduce un valor válido.");
                                }
                            } else {
                                p.sendMessage(eventsPrefix + "El mínimo de puntos para el evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f es de §a" + event.getMinPoints() + "§f.");
                            }
                        } else if (args[1].equals("image")) {
                            if (args.length > 2) {
                                if (args[2].equals("delete")) {
                                    event.setImage("notSet");
                                    event.save();
                                    p.sendMessage(eventsPrefix + "Has eliminado la imagen del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f.");
                                } else {
                                    event.setImage(args[2]);
                                    event.save();
                                    p.sendMessage(eventsPrefix + "Has establecido la imagen del evento de §a" + country.replace("peru", "perú").toUpperCase() + "§f en\n§b" + args[2]);

                                }
                            } else {
                                p.sendMessage(eventsPrefix + "Introduce un enlace a una imagen.");
                            }
                        }
                    } else {
                        p.sendMessage(eventsPrefix + "Introduce una acción.");
                    }
                } else {
                    p.sendMessage(eventsPrefix + "Introduce un país válido.");
                }
            } else {
                p.sendMessage(eventsPrefix + "Introduce un país para manejar.");
            }
        }
        return true;
    }
}
