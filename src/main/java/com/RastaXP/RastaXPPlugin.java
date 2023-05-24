package com.RastaXP;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.EnumMap;
import java.util.Map;



@Slf4j
@PluginDescriptor(
		name = "RastaHUD",
		description = "Show an experience for tracked skill",
		tags = {"exp", "xp", "tracker", "status"}
)
public class RastaXPPlugin extends Plugin
{

	@Inject
	private XPBarOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	public Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private RastaXPConfig config;

	@Getter(AccessLevel.PACKAGE)
	private boolean barsDisplayed;

	@Getter(AccessLevel.PACKAGE)
	private Skill currentSkill;

	private final Map<Skill, Integer> skillList = new EnumMap<>(Skill.class);

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Provides
	private RastaXPConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RastaXPConfig.class);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {

		Integer lastXP = skillList.put(statChanged.getSkill(), statChanged.getXp());

		if (lastXP != null && lastXP != statChanged.getXp())
		{
			currentSkill = statChanged.getSkill();
		}

		log.info("State CHANGED: " + statChanged.getSkill());
	}
}

