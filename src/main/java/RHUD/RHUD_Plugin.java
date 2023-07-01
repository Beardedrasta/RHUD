package RHUD;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.itemstats.ItemStatPlugin;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.Map;


@PluginDescriptor(
		name = "RHUD",
		description = "Experience and Status bar hud for combat and skilling.",
		tags = {"exp", "xp", "tracker", "status", "bar"}
)
@PluginDependency(XpTrackerPlugin.class)
@PluginDependency(ItemStatPlugin.class)
public class RHUD_Plugin extends Plugin
{
	@Inject
	private RHUD_Overlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private RHUD_Config config;

	@Inject
	private ClientThread clientThread;

	@Getter(AccessLevel.PACKAGE)
	private boolean barsDisplayed;

	@Getter(AccessLevel.PACKAGE)
	public Skill currentSkill;

	private final Map<Skill, Integer> skillList = new EnumMap<>(Skill.class);

	private int lastCombatActionTickCount;

	@Override
	protected void startUp()
	{
		clientThread.invokeLater(this::checkStatusBars);
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		barsDisplayed = false;
	}

	@Provides
	RHUD_Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RHUD_Config.class);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		checkStatusBars();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (RHUD_Config.GROUP.equals(event.getGroup()) && event.getKey().equals("hideAfterCombatDelay"))
		{
			clientThread.invokeLater(this::checkStatusBars);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {

		Integer lastXP = skillList.put(statChanged.getSkill(), statChanged.getXp());

		if (lastXP != null && lastXP != statChanged.getXp())
		{
			currentSkill = statChanged.getSkill();
		}
	}

	public void checkStatusBars()
	{
		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}

		final Actor interacting = localPlayer.getInteracting();

		if (config.hideAfterCombatDelay() == 0)
		{
			barsDisplayed = true;
		}
		else if ((interacting instanceof NPC && ArrayUtils.contains(((NPC) interacting).getComposition().getActions(), "Attack"))
				|| (interacting instanceof Player && client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1))
		{
			lastCombatActionTickCount = client.getTickCount();
			barsDisplayed = true;
		}
		else if (client.getTickCount() - lastCombatActionTickCount >= config.hideAfterCombatDelay())
		{
			barsDisplayed = false;
		}
	}
}
