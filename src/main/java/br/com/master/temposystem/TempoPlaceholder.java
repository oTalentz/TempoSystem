package br.com.master.temposystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class TempoPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public TempoPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "tempo";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        long tempoTotal = plugin.getDataConfig().getLong("tempos." + player.getUniqueId(), 0);
        long segundos = tempoTotal / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;

        return horas + "h " + (minutos % 60) + "m " + (segundos % 60) + "s";
    }
}
