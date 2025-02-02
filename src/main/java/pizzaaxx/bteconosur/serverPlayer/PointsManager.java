package pizzaaxx.bteconosur.serverPlayer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.bteconosur.country.Country;
import pizzaaxx.bteconosur.country.CountryPlayer;
import pizzaaxx.bteconosur.yaml.Configuration;
import sun.security.x509.AVA;

import java.util.*;

import static pizzaaxx.bteconosur.BteConoSur.discord;

public class PointsManager {

    private final TreeMap<Country, Integer> countriesPoints = new TreeMap<>();
    private final ServerPlayer serverPlayer;
    private final DataManager data;

    public PointsManager(ServerPlayer s) {
        data = s.getDataManager();

        serverPlayer = s;

        if (data.contains("points")) {
            ConfigurationSection pointsSection = data.getConfigurationSection("points");
            for (String key : pointsSection.getKeys(false)) {
                countriesPoints.put(new Country(key), pointsSection.getInt(key));
            }
        }
    }

    public int getPoints(Country country) {
        return countriesPoints.getOrDefault(country, 0);
    }

    public void setPoints(Country country, int points) {
        int old = countriesPoints.get(country);
        if (old != points) {
            countriesPoints.put(country, points);
            Map<String, Integer> map = new HashMap<>();
            countriesPoints.forEach((key, value) -> map.put(key.getCountry(), value));
            data.set("points", map);
            data.save();
            int diff = Math.abs(points - old);
            discord.log(country, ":chart_with_" + (diff > 0 ? "up" : "down") + "wards_trend: Se han " + (diff > 0 ? "añadido" : "quitado") + " `" + diff + "` puntos a **" + serverPlayer.getName() + "**. Total: `" + points + "`.");
        }
        serverPlayer.getGroupsManager().checkGroups();
        serverPlayer.getDiscordManager().checkDiscordBuilder(country);

    }

    public int addPoints(Country country, int points) {
        int newAmount = getPoints(country) + points;
        setPoints(country, newAmount);
        return newAmount;
    }

    public int removePoints(Country country, int points) {
        int newAmount = getPoints(country) - points;
        setPoints(country, newAmount);
        return newAmount;
    }

    public Map.Entry<Country, Integer> getMaxPoints() {
        Map.Entry<Country, Integer> max = null;
        for (Map.Entry<Country, Integer> entry : countriesPoints.entrySet()) {
            if (max == null || entry.getValue().compareTo(max.getValue()) > 0) {
                max = entry;
            }
        }
        return max;
    }

    public TreeMap<Country, Integer> getSorted() {
        return countriesPoints;
    }

    public void checkTop(Country country) {
        CountryPlayer cPlayer = new CountryPlayer(serverPlayer, country);
        Configuration max = new Configuration(Bukkit.getPluginManager().getPlugin("bteConoSur"), "points/max");
        List<CountryPlayer> players = new ArrayList<>();
        max.getList(country.getAbbreviation() + "_max").forEach(uuid -> players.add(new CountryPlayer(new ServerPlayer(Bukkit.getOfflinePlayer(UUID.fromString((String) uuid))), country)));
        if (!players.contains(cPlayer)) {
            players.add(cPlayer);
        }
        Collections.sort(players);
        max.set(country.getAbbreviation() + "_max", players.subList(0, 10));
    }

    public enum BuilderRank {
        BUILDER, AVANZADO, VETERANO, MAESTRO;

        public static BuilderRank getFrom(int points) {
            if (points >= 1000) {
                return MAESTRO;
            } else if (points >= 500) {
                return AVANZADO;
            } else if (points >= 150) {
                return VETERANO;
            }
            return BUILDER;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String getAsPrefix() {
            switch (this) {
                case AVANZADO:
                    return "[§2AVANZADO§f]";
                case VETERANO:
                    return "[§eAVANZADO§f]";
                case MAESTRO:
                    return "[§6MAESTRO§f]";
                default:
                    return "[§9BUILDER§f]";
            }
        }
    }

}
