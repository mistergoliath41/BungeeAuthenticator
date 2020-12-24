package fr.mistergoliath.bungeeauthenticator.bukkit;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.ericrabil.yamlconfiguration.configuration.file.YamlConfiguration;

import fr.mistergoliath.bungeeauthenticator.utils.Constants;

public class BungeeAuthenticator extends JavaPlugin implements PluginMessageListener, Listener {

	private static File dataFolder;
	private String token;
	private static YamlConfiguration config;
	private ArrayList<Player> allowedPlayers = new ArrayList<Player>();
	
	@Override
	public void onEnable() {
		dataFolder = getDataFolder();
		setupConfigs();
		token = getConfig().getString("token");
		if (!token.equals(Constants.DEFAULT_TOKEN)) {
			getServer().getMessenger().registerIncomingPluginChannel(this, "Bungeecord", this);
			getServer().getPluginManager().registerEvents(this, this);
			return;
		}
		getServer().getConsoleSender().sendMessage(Constants.MESSAGE_PREFIX + " §cYou must change the token in the config.yml file.");
		getServer().getPluginManager().disablePlugin(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent e) {if (!allowedPlayers.contains(e.getPlayer())) e.getPlayer().teleport(e.getFrom());}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {e.setCancelled(!allowedPlayers.contains(e.getPlayer()));}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageEvent e) {if (e.getEntity() instanceof Player) e.setCancelled(!allowedPlayers.contains((Player)e.getEntity()));}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {e.setCancelled(!allowedPlayers.contains(e.getPlayer()));}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent e) {e.setCancelled(!allowedPlayers.contains(e.getPlayer()));}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    @Override
		    public void run() {
				//System.out.println(allowedPlayers.contains(e.getPlayer()));
				if (!allowedPlayers.contains(e.getPlayer())) e.getPlayer().kickPlayer("Bad Request");
		    }
		}, 1L);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e) {
		if (allowedPlayers.contains(e.getPlayer())) allowedPlayers.remove(e.getPlayer());
	}
	
	public void setupConfigs() {
		if (!dataFolder.exists()) dataFolder.mkdirs();
		Arrays.stream(new String[]{"config.yml"}).forEach((cf) -> {
			File file = new File(getDataFolder().getAbsolutePath() + File.separator + cf);
			if (!file.exists()) {
				try {
					Files.copy(getClass().getResourceAsStream("/" + cf), file.getAbsoluteFile().toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		config = YamlConfiguration.loadConfiguration(new File(dataFolder.getAbsolutePath() + File.separator + "config.yml"));
	}

	public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("Bungeecord")) return;
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
			if (in.readUTF().equals(this.token)) allowedPlayers.add(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public Player getPlayer(Object obj) {
		return obj instanceof UUID ? Bukkit.getPlayer((UUID)obj) : obj instanceof String ? Bukkit.getPlayer((String)obj) : null;
	}
	
	public static void main(String[] args) throws Exception {
		throw new Exception("Useless Method");
	}
	
}
