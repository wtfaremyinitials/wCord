package net.md_5.bungee.connection;

import com.google.gson.Gson;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.StatusResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AnimatedMotd {
	private ScheduledFuture task;
	private int frame = 0;
	private int text = 0;
	private static List<BufferedImage> images;
	private static List<String> motd;

	static {
		load();
	}

	public static void load() {
		images = new ArrayList<BufferedImage>();
		motd = new ArrayList<String>();
		File f = new File("server-icon");
		File[] ff = f.listFiles();
		Arrays.sort(ff);
		if (f.isDirectory()) {
			for (File x : ff) {
				if (x.getName().endsWith(".txt")) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(x));
						String line;
						while ((line = br.readLine()) != null) {
							motd.add(line);
							System.out.println("Got line: " + line);
						}
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					continue;
				}
				try {
					BufferedImage bi = ImageIO.read(x);
					images.add(bi);
					System.out.println("Loaded animated frame... " + x.getName());
				} catch (IOException e) {

				}

			}

		}
	}

	public void handleSend(final ProxyPingEvent pingResult, final InitialHandler initialHandler) {
		BungeeCord.getInstance().getConnectionThrottle().unthrottle(initialHandler.getAddress().getAddress());
		final Gson gson = initialHandler.getHandshake().getProtocolVersion() == ProtocolConstants.MINECRAFT_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
		task = initialHandler.getCh().getHandle().eventLoop().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (initialHandler.getCh().getHandle().isOpen() && initialHandler.thisState == InitialHandler.State.PING) {
					if (images.size() == 0) initialHandler.getCh().close();
					if (images.size() <= frame) {
						frame = 0;
					}
					if (motd.size() == 0) initialHandler.getCh().close();
					if (motd.size() <= text) {
						text = 0;
					}
					if (motd.get(text).startsWith("$")) {
						text = Integer.parseInt(motd.get(text).replace("$", "")) - 1;
					}
					ServerPing si = pingResult.getResponse();
					si.setFavicon(Favicon.create(images.get(frame)));

					si.setDescription(ChatColor.translateAlternateColorCodes('&', motd.get(text)));
					si.getPlayers().setOnline(BungeeCord.getInstance().getOnlineCount());
					initialHandler.unsafe().sendPacket(new StatusResponse(gson.toJson(si)));
					frame++;
					text++;
				} else {
					task.cancel(true);
				}
			}
		}, 0, 120, TimeUnit.MILLISECONDS);

	}
}
