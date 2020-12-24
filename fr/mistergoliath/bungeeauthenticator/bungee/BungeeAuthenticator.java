package fr.mistergoliath.bungeeauthenticator.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.Player;

import com.ericrabil.yamlconfiguration.configuration.file.YamlConfiguration;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fr.mistergoliath.bungeeauthenticator.utils.Constants;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeAuthenticator extends Plugin implements Listener {

	private static File dataFolder;
	private String token;
	private static YamlConfiguration config;

	@Override
	public void onEnable() {
		dataFolder = getDataFolder();
		setupConfigs();
		token = getConfig().getString("token");
		if (!token.equals(Constants.DEFAULT_TOKEN)) {
			getProxy().getPluginManager().registerListener(this, this);
			return;
		}
		getProxy().getConsole().sendMessage(new TextComponent(Constants.MESSAGE_PREFIX + " §cYou must change the token in the config.yml file."));
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerConnect(ServerConnectEvent e) throws IOException {
		System.out.println(e.getTarget());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        out.writeUTF(this.token);
        e.getTarget().sendData("Bungeecord", stream.toByteArray());
	}
	
	public static YamlConfiguration getConfig() {
		return config;
	}
	
}
