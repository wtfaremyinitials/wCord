package net.md_5.bungee.tab;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.Collection;

public class CompatList extends TabList {

	private boolean sentPing;

	public CompatList(ProxiedPlayer player) {
		super(player);
	}

	@Override
	public void onUpdate(PlayerListItem playerListItem) {

	}

	@Override
	public void onPingChange(int ping) {
		if (!sentPing) {
			sentPing = true;
			for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
				PlayerListItem packet = new PlayerListItem();
				packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
				PlayerListItem.Item item = new PlayerListItem.Item();
				item.setUuid(player.getUniqueId());
				item.setUsername(player.getName());
				item.setDisplayName(p.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_SNAPSHOT ? ComponentSerializer.toString(TextComponent.fromLegacyText(player.getDisplayName())) :
						player.getDisplayName());
				item.setPing(player.getPing());
				packet.setItems(new PlayerListItem.Item[]
						{
								item
						});
				if (p.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_SNAPSHOT) {
					p.unsafe().sendPacket(packet);
				}else
				{
					PlayerListItem packet2 = new PlayerListItem();
					packet2.setAction(PlayerListItem.Action.REMOVE_PLAYER);
					PlayerListItem.Item item2 = new PlayerListItem.Item();
					item2.setUuid(player.getUniqueId());
					item2.setUsername(player.getName());
					item2.setDisplayName(player.getName());
					item2.setPing(player.getPing());
					packet2.setItems(new PlayerListItem.Item[]
							{
									item2
							});
					p.unsafe().sendPacket(packet2);
				}
				packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
				p.unsafe().sendPacket(packet);
			}
		}
	}

	@Override
	public void onServerChange() {

	}

	@Override
	public void onConnect() {
		PlayerListItem playerListItem = new PlayerListItem();
		playerListItem.setAction(PlayerListItem.Action.ADD_PLAYER);
		Collection<ProxiedPlayer> players = BungeeCord.getInstance().getPlayers();
		PlayerListItem.Item[] items = new PlayerListItem.Item[players.size()];
		playerListItem.setItems(items);
		int i = 0;
		for (ProxiedPlayer p : players) {
			PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
			item.setUuid(p.getUniqueId());
			item.setUsername(p.getName());
			item.setDisplayName(player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_SNAPSHOT ? ComponentSerializer.toString(TextComponent.fromLegacyText(p.getDisplayName())) : p.getDisplayName());
			LoginResult loginResult = ((UserConnection) p).getPendingConnection().getLoginProfile();
			if (loginResult != null) {
				String[][] props = new String[loginResult.getProperties().length][];
				for (int j = 0; j < props.length; j++) {
					props[j] = new String[]
							{
									loginResult.getProperties()[j].getName(),
									loginResult.getProperties()[j].getValue(),
									loginResult.getProperties()[j].getSignature()
							};
				}
				item.setProperties(props);
			} else {
				item.setProperties(new String[0][0]);
			}
			item.setGamemode(((UserConnection) p).getGamemode());
			item.setPing(p.getPing());
		}
		if (player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_SNAPSHOT) {
			player.unsafe().sendPacket(playerListItem);
			for (ProxiedPlayer p : players) {
				PlayerListItem.Item item = new PlayerListItem.Item();
				item.setUuid(p.getUniqueId());
				item.setUsername(p.getName());
				item.setDisplayName(ComponentSerializer.toString(TextComponent.fromLegacyText(p.getDisplayName())));
				PlayerListItem packet = new PlayerListItem();
				packet.setAction(PlayerListItem.Action.ADD_PLAYER.UPDATE_DISPLAY_NAME);
				packet.setItems(new PlayerListItem.Item[]
						{
								item
						});
				player.unsafe().sendPacket(packet);
			}
		} else {
			// Split up the packet
			for (PlayerListItem.Item item : playerListItem.getItems()) {
				PlayerListItem packet = new PlayerListItem();
				packet.setAction(playerListItem.getAction());

				packet.setItems(new PlayerListItem.Item[]
						{
								item
						});
				player.unsafe().sendPacket(packet);
			}
		}

		PlayerListItem packet = new PlayerListItem();
		packet.setAction(PlayerListItem.Action.ADD_PLAYER);
		for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
			PlayerListItem.Item item = new PlayerListItem.Item();
			item.setUuid(player.getUniqueId());
			item.setUsername(player.getName());
			item.setDisplayName(p.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_SNAPSHOT ? ComponentSerializer.toString(TextComponent.fromLegacyText(player.getDisplayName())) :
					player.getDisplayName());
			LoginResult loginResult = ((UserConnection) player).getPendingConnection().getLoginProfile();
			if (loginResult != null) {
				String[][] props = new String[loginResult.getProperties().length][];
				for (int j = 0; j < props.length; j++) {
					props[j] = new String[]
							{
									loginResult.getProperties()[j].getName(),
									loginResult.getProperties()[j].getValue(),
									loginResult.getProperties()[j].getSignature()
							};
				}
				item.setProperties(props);
			} else {
				item.setProperties(new String[0][0]);
			}
			item.setGamemode(((UserConnection) player).getGamemode());
			item.setPing(player.getPing());
			packet.setItems(new PlayerListItem.Item[]
					{
							item
					});
			p.unsafe().sendPacket(packet);
		}

	}

	@Override
	public void onDisconnect() {
		PlayerListItem packet = new PlayerListItem();
		packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
		PlayerListItem.Item item = new PlayerListItem.Item();
		item.setUuid(player.getUniqueId());
		item.setUsername(player.getName());
		item.setDisplayName(player.getName());
		packet.setItems(new PlayerListItem.Item[]
				{
						item
				});
		BungeeCord.getInstance().broadcast(packet);
	}
}
