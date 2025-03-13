package br.com.master.temposystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private File dataFile;
    private FileConfiguration dataConfig;
    private final HashMap<UUID, Long> temposOnline = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("tempo").setExecutor(this);
        carregarArquivoDeDados();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TempoPlaceholder(this).register();
        }

        // Agendar um Runnable para atualizar o tempo online a cada segundo
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (UUID uuid : temposOnline.keySet()) {
                    // Incrementa o tempo online a cada segundo
                    long tempoOnline = System.currentTimeMillis() - temposOnline.get(uuid);
                    dataConfig.set("tempos." + uuid, tempoOnline);
                }
                salvarArquivoDeDados();
            }
        }, 0L, 20L); // 20L = 1 segundo (20 ticks por segundo)
    }

    @Override
    public void onDisable() {
        salvarArquivoDeDados();
    }

    private void carregarArquivoDeDados() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void salvarArquivoDeDados() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método público para acessar o dataConfig
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        temposOnline.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (temposOnline.containsKey(uuid)) {
            long tempoOnline = System.currentTimeMillis() - temposOnline.get(uuid);
            long totalTempo = dataConfig.getLong("tempos." + uuid, 0) + tempoOnline;
            dataConfig.set("tempos." + uuid, totalTempo);
            salvarArquivoDeDados();
            temposOnline.remove(uuid);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            long tempoTotal = dataConfig.getLong("tempos." + player.getUniqueId(), 0);
            long segundos = tempoTotal / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            sender.sendMessage("§aTempo online: " + horas + "h " + (minutos % 60) + "m " + (segundos % 60) + "s");
        } else {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
        }
        return true;
    }
}
