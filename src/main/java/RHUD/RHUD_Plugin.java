/*
 * Copyright (c) 2018, Jos <Malevolentdev@gmail.com>
 * Copyright (c) 2023, Beardedrasta <Beardedrasta@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package RHUD;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import javax.swing.SwingUtilities;

import com.google.inject.Provides;
import RHUD.helpers.*;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.itemstats.ItemStatPlugin;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.plugins.xptracker.XpTrackerService;

import java.util.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ImageUtil;

import java.awt.Point;
import java.io.IOException;
import java.text.NumberFormat;


@PluginDescriptor(name = "RHUD", description = "Experience and Status bar hud for combat and skilling.", tags = { "exp",
		"xp", "tracker", "status", "bar" })
@PluginDependency(XpTrackerPlugin.class)
@PluginDependency(ItemStatPlugin.class)
public class RHUD_Plugin extends Plugin {
	// ------------------ Injected Dependencies ------------------
	@Inject
	private HUD hud;
	@Inject
	private Client client;
	@Inject
	private RHUD_Config config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private OverlayManager manager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private ClientToolbar clientToolbar;

	private RHUDPanel sidePanel;
	private NavigationButton navButton;
	private boolean editMode;
	@Getter
	public Skill currentSkill;


	// ------------------ XP Tracking Maps ------------------
	private final Map<Skill, Integer> previousXpMap = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> lastXpGainMap = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> xpAtSessionStartMap = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> xpGainedInSessionMap = new EnumMap<>(Skill.class);

	@Getter
	long startTime = System.currentTimeMillis();

	@Override
	protected void startUp() {
		clientThread.invokeLater(this::initSessionXp);

		// Apply the persisted RHUD dimensions BEFORE the overlay is added.
		// Without this, RuneLite can briefly give the overlay its default/preferred
		// size and syncAltResizeToConfig() may mistake that startup size for a user
		// resize, overwriting the saved bar dimensions.
		hud.applyConfigSize();
		manager.add(hud);

		SwingUtilities.invokeLater(() -> {
			sidePanel = new RHUDPanel(this, config, configManager);
			navButton = NavigationButton.builder()
					.tooltip("RHUD")
					.icon(createSidebarIcon())
					.priority(7)
					.panel(sidePanel)
					.build();
			clientToolbar.addNavigation(navButton);
		});
	}

	@Override
	protected void shutDown() {
		manager.remove(hud);
		if (navButton != null) {
			SwingUtilities.invokeLater(() -> clientToolbar.removeNavigation(navButton));
		}
		navButton = null;
		sidePanel = null;
	}

	private BufferedImage createSidebarIcon() {
		BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(255, 152, 0));
			g.fillRoundRect(2, 4, 20, 16, 5, 5);
			g.setColor(new Color(35, 35, 35));
			g.fillRoundRect(4, 6, 16, 12, 3, 3);
			g.setColor(new Color(255, 152, 0));
			g.fillRect(6, 8, 12, 3);
			g.fillRect(6, 13, 8, 3);
		} finally {
			g.dispose();
		}
		return image;
	}

	void resetHudOverlay() {
		manager.resetOverlay(hud);
	}

	void refreshHudFromConfig() {
		hud.applyConfigSize();
	}

	boolean isEditMode() {
		return editMode;
	}

	void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	void onEditorPanelVisibilityChanged(boolean visible) {
		if (visible) {
			hud.beginSidebarEdit();
		} else {
			hud.endSidebarEdit();
		}
	}


	@Provides
	RHUD_Config provideConfig(ConfigManager configManager) {
		return configManager.getConfig(RHUD_Config.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(RHUD_Config.GROUP)) {
			return;
		}

		// Keep the live overlay synchronized with values saved by either the
		// standard RuneLite config panel or RHUD's sidebar control center.
		hud.applyConfigSize();

		// The custom sidebar contains ordinary Swing controls, so refresh those
		// controls when RuneLite's standard plugin settings menu changes a value.
		if (sidePanel != null) {
			SwingUtilities.invokeLater(sidePanel::rebuildFromConfig);
		}
	}


	@Subscribe
	public void onStatChanged(StatChanged statChanged) {

		// Skip HP if user wants that
		if (statChanged.getSkill() == Skill.HITPOINTS && config.ignoreHitpoints()) {
			return;
		}

		// Also skip “Overall” to avoid double-counting
		Skill skill = statChanged.getSkill();
		if ("Overall".equalsIgnoreCase(skill.getName())) {
			return;
		}

		int newXp = statChanged.getXp();
		if (!xpAtSessionStartMap.containsKey(skill)) {
			xpAtSessionStartMap.put(skill, newXp);
			xpGainedInSessionMap.put(skill, 0);
		}

		int oldXp = previousXpMap.getOrDefault(skill, -1);
		if (oldXp == -1) {
			// first time we see this skill, store xp, skip difference calc
			previousXpMap.put(skill, newXp);
			return;
		}

		// Calculate XP difference
		int xpDiff = newXp - oldXp;
		if (xpDiff > 0) {
			// Update total session xp
			int oldSessionXp = xpGainedInSessionMap.getOrDefault(skill, 0);
			int newSessionXp = oldSessionXp + xpDiff;
			xpGainedInSessionMap.put(skill, newSessionXp);

			// Track the “most recent skill”
			lastXpGainMap.put(skill, xpDiff);
		}
		// Update old xp for next event
		previousXpMap.put(skill, newXp);

		Integer lastXP = lastXpGainMap.put(skill, statChanged.getXp());

		if (lastXP != null && lastXP != statChanged.getXp()) {
			currentSkill = skill;
		}
	}

	private void initSessionXp() {
		// Called at plugin start or on reset
		for (Skill skill : Skill.values()) {
			xpAtSessionStartMap.put(skill, client.getSkillExperience((skill)));
			xpGainedInSessionMap.put(skill, 0);
		}
	}

	public int getlastXpGainForSkill(Skill skill) {
		return lastXpGainMap.getOrDefault(skill, 0);
	}

	public int xpGainedInSessionMap(Skill skill) {
		return xpGainedInSessionMap.getOrDefault(skill, 0);
	}
}

/////// HUD SETUP ///////

class HUD extends OverlayPanel {
	private Client client;
	private RHUD_Plugin plugin;
	private RHUD_Config config;
	private SpriteManager spriteManager;
	private ItemStatChangesService itemStatService;
	private XpTrackerService xpTrackerService;
	private ConfigManager configManager;

	private Image prayerIcon, heartDisease, heartPoison, heartVenom, heartIcon, energyIcon, specialIcon;

	private static final int IMAGE_SIZE = 17;
	private static final int MAX_RUN_ENERGY_VALUE = 100;
	private static final int MAX_SPECIAL_ATTACK_VALUE = 100;
	public int ACTIVE = 0;
	private static final Dimension ICON_DIMENSIONS = new Dimension(18, 17);

	// Colors you use for bars
	private static final Color VENOMED_COLOR = new Color(0, 65, 0, 255);
	private static final Color POISONED_COLOR = new Color(0, 145, 0, 255);
	private static final Color DISEASE_COLOR = new Color(176, 134, 53, 255);
	private static final Color PARASITE_COLOR = new Color(196, 62, 109, 255);
	private static final Color HEAL_COLOR = new Color(255, 112, 6, 150);
	private static final Color ACTIVE_PRAYER_COLOR = new Color(43, 234, 159, 255);
	private static final Color PRAYER_HEAL_COLOR = new Color(57, 255, 186, 75);
	private static final Color RUN_STAMINA_COLOR = new Color(168, 124, 62, 255);
	private static final Color RUN_ACTIVE = new Color(185, 187, 0, 255);
	private static final Color ENERGY_HEAL_COLOR = new Color(199, 118, 0, 218);
	private static final Color SPECIAL_ACTIVE = new Color(4, 173, 1, 255);

	private Dimension lastManagedSize;
	private OverlayPosition positionBeforeSidebar;
	private Point locationBeforeSidebar;
	private boolean sidebarEditing;

	private final Map<Layout.BARMODE, Render> barMode = new EnumMap<>(Layout.BARMODE.class);
	private final Map<Layout.XPBARMODE, XpRender> xpBarMode = new EnumMap<Layout.XPBARMODE, XpRender>(Layout.XPBARMODE.class);

	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		// Example message on loginw
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "RHUD plugin is active!", null);
		}
	}

	@Inject
	private ItemManager itemManager;

	private final GradientPaint gradient = new GradientPaint(0, 0, new Color(139, 0, 98), 0, 90 + 5, Color.BLACK, true);

	@Inject
	private HUD(Client client, RHUD_Plugin plugin, RHUD_Config config,
				SkillIconManager iconManager,
				ItemStatChangesService itemStatService,
				SpriteManager spriteManager,
				XpTrackerService xpTrackerService,
				ConfigManager configManager) {
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.itemStatService = itemStatService;
		this.spriteManager = spriteManager;
		this.xpTrackerService = xpTrackerService;
		this.configManager = configManager;

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMinimumSize(32);
		setResizable(true);

		prayerIcon = ImageUtil.resizeCanvas(
				ImageUtil.resizeImage(iconManager.getSkillImage(Skill.PRAYER, true), IMAGE_SIZE, IMAGE_SIZE),
				ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		heartDisease = ImageUtil.resizeCanvas(
				ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART),
				ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		heartPoison = ImageUtil.resizeCanvas(
				ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART),
				ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		heartVenom = ImageUtil.resizeCanvas(
				ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART),
				ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);

		setBarMode();
	}

	private void setBarMode() {
		xpBarMode.put(Layout.XPBARMODE.DISABLED, null);
		xpBarMode.put(Layout.XPBARMODE.EXPERIENCE, new XpRender(
				// max value: difference between next level XP and current level XP
				() -> {
					Skill skill = config.mostRecentSkill()
							? (plugin.currentSkill == null ? config.skill() : plugin.currentSkill)
							: config.skill();
					int xp = client.getSkillExperience(skill);
					int currentLevel = Experience.getLevelForXp(xp);
					int nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
					int currentLevelXP = Experience.getXpForLevel(currentLevel);
					return nextLevelXP - currentLevelXP;
				},
				// current value: XP progress within the level
				() -> {
					Skill skill = config.mostRecentSkill()
							? (plugin.currentSkill == null ? config.skill() : plugin.currentSkill)
							: config.skill();
					int xp = client.getSkillExperience(skill);
					int currentLevel = Experience.getLevelForXp(xp);
					int currentLevelXP = Experience.getXpForLevel(currentLevel);
					return xp - currentLevelXP;
				},
				// heal supplier (if you don’t have healing for XP, just return 0)
				() -> 0,
				// color supplier for the XP bar
				() -> config.colorXP(),
				// notch or secondary color supplier – use a default or a config value
				() -> new Color(0x6C6C6C)// config.colorXPNotches(),
		));
		barMode.put(Layout.BARMODE.DISABLED, null);
		barMode.put(Layout.BARMODE.HITPOINTS, new Render(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.HITPOINTS),
				() -> client.getBoostedSkillLevel(Skill.HITPOINTS),
				() -> getRestoreValue(Skill.HITPOINTS.getName()),
				() -> {
					final int poisonState = client.getVarpValue(VarPlayer.POISON);

					if (poisonState >= 1000000) {
						return VENOMED_COLOR;
					}

					if (poisonState > 0) {
						return POISONED_COLOR;
					}

					if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
						return DISEASE_COLOR;
					}

					if (client.getVarbitValue(Varbits.PARASITE) >= 1) {
						return PARASITE_COLOR;
					}

					if (config.lifeColor()) {
						int maxHealth = client.getRealSkillLevel(Skill.HITPOINTS);
						int currentHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);

						// Calculate health percentage
						float healthPercentage = (float) currentHealth / maxHealth;

						// Interpolate between green (full health) and red (low health)
						int red = (int) (255 * (1 - healthPercentage)); // Increases as health decreases
						int green = (int) (255 * healthPercentage); // Decreases as health decreases

						return new Color(red, green, 0); // Dynamic color based on health percentage
					}

					return config.colorHealthBar();
				},
				() -> HEAL_COLOR,
				() -> {
					final int poisonState = client.getVarpValue(VarPlayer.POISON);

					if (poisonState > 0 && poisonState < 50) {
						return heartPoison;
					}

					if (poisonState >= 1000000) {
						return heartVenom;
					}

					if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
						return heartDisease;
					}

					return heartIcon;
				}));
		barMode.put(Layout.BARMODE.PRAYER, new Render(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.PRAYER),
				() -> client.getBoostedSkillLevel(Skill.PRAYER),
				() -> getRestoreValue(Skill.PRAYER.getName()),
				() -> {
					Color prayerColor = config.colorPrayBar();

					for (Prayer pray : Prayer.values()) {
						if (client.getVarbitValue(4101) != 0) {
							prayerColor = ACTIVE_PRAYER_COLOR;
							break;
						}
					}

					return prayerColor;
				},
				() -> PRAYER_HEAL_COLOR,
				() -> prayerIcon));
		barMode.put(Layout.BARMODE.RUN_ENERGY, new Render(
				() -> MAX_RUN_ENERGY_VALUE,
				() -> client.getEnergy() / 100,
				() -> getRestoreValue("Run Energy"),
				() -> {
					if (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
						return RUN_STAMINA_COLOR;
					} else if (client.getVarpValue(173) == 1) {
						return RUN_ACTIVE;
					} else {
						return config.colorRunBar();
					}
				},
				() -> ENERGY_HEAL_COLOR,
				() -> energyIcon));
		barMode.put(Layout.BARMODE.SPECIAL_ATTACK, new Render(
				() -> MAX_SPECIAL_ATTACK_VALUE,
				() -> client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10,
				() -> 0,
				() -> {
					if (client.getVarpValue(301) == 1) {
						return SPECIAL_ACTIVE;
					} else {
						return config.colorSpecialBar();
					}
				},
				config::colorSpecialBar,
				() -> specialIcon));
	}


	private int styledBarHeight(int slot) {
		switch (slot) {
			case 1: return config.bar1UseGlobalStyle() ? config.xpBarHeight() : config.bar1Height();
			case 2: return config.bar2UseGlobalStyle() ? config.barHeight() : config.bar2Height();
			case 3: return config.bar3UseGlobalStyle() ? config.barHeight() : config.bar3Height();
			case 4: return config.bar4UseGlobalStyle() ? config.barHeight() : config.bar4Height();
			case 5: return config.bar5UseGlobalStyle() ? config.barHeight() : config.bar5Height();
			default: return config.barHeight();
		}
	}

	private boolean styledIcon(int slot) {
		switch (slot) {
			case 2: return config.bar2UseGlobalStyle() ? config.enableSkillIcon() : config.bar2ShowIcon();
			case 3: return config.bar3UseGlobalStyle() ? config.enableSkillIcon() : config.bar3ShowIcon();
			case 4: return config.bar4UseGlobalStyle() ? config.enableSkillIcon() : config.bar4ShowIcon();
			case 5: return config.bar5UseGlobalStyle() ? config.enableSkillIcon() : config.bar5ShowIcon();
			default: return false;
		}
	}

	private RHUD_Config.BarTextFormat styledTextFormat(int slot) {
		switch (slot) {
			case 2: return config.bar2UseGlobalStyle() ? RHUD_Config.BarTextFormat.CURRENT_MAX : config.bar2TextFormat();
			case 3: return config.bar3UseGlobalStyle() ? RHUD_Config.BarTextFormat.CURRENT_MAX : config.bar3TextFormat();
			case 4: return config.bar4UseGlobalStyle() ? RHUD_Config.BarTextFormat.CURRENT_MAX : config.bar4TextFormat();
			case 5: return config.bar5UseGlobalStyle() ? RHUD_Config.BarTextFormat.CURRENT_MAX : config.bar5TextFormat();
			default: return RHUD_Config.BarTextFormat.HIDDEN;
		}
	}

	private Color styledColor(int slot) {
		switch (slot) {
			case 1: return !config.bar1UseGlobalStyle() && config.bar1UseCustomColor() ? config.bar1CustomColor() : null;
			case 2: return !config.bar2UseGlobalStyle() && config.bar2UseCustomColor() ? config.bar2CustomColor() : null;
			case 3: return !config.bar3UseGlobalStyle() && config.bar3UseCustomColor() ? config.bar3CustomColor() : null;
			case 4: return !config.bar4UseGlobalStyle() && config.bar4UseCustomColor() ? config.bar4CustomColor() : null;
			case 5: return !config.bar5UseGlobalStyle() && config.bar5UseCustomColor() ? config.bar5CustomColor() : null;
			default: return null;
		}
	}

	private boolean warningActive(Layout.BARMODE mode) {
		if (mode == Layout.BARMODE.HITPOINTS && config.lowHealthWarning()) {
			int current = client.getBoostedSkillLevel(Skill.HITPOINTS);
			return current > 0 && current <= config.lowHealthThreshold();
		}
		if (mode == Layout.BARMODE.PRAYER && config.lowPrayerWarning()) {
			int current = client.getBoostedSkillLevel(Skill.PRAYER);
			return current >= 0 && current <= config.lowPrayerThreshold();
		}
		return false;
	}

	private Color warningColor(Layout.BARMODE mode) {
		if (mode == Layout.BARMODE.HITPOINTS) return config.lowHealthWarningColor();
		if (mode == Layout.BARMODE.PRAYER) return config.lowPrayerWarningColor();
		return Color.RED;
	}

	private boolean styledBackground(int slot) {
		switch (slot) {
			case 1: return config.bar1UseGlobalStyle() ? config.showBarBackground() : config.bar1ShowBackground();
			case 2: return config.bar2UseGlobalStyle() ? config.showBarBackground() : config.bar2ShowBackground();
			case 3: return config.bar3UseGlobalStyle() ? config.showBarBackground() : config.bar3ShowBackground();
			case 4: return config.bar4UseGlobalStyle() ? config.showBarBackground() : config.bar4ShowBackground();
			case 5: return config.bar5UseGlobalStyle() ? config.showBarBackground() : config.bar5ShowBackground();
			default: return config.showBarBackground();
		}
	}

	private int styledOpacity(int slot) {
		switch (slot) {
			case 1: return config.bar1UseGlobalStyle() ? 100 : config.bar1Opacity();
			case 2: return config.bar2UseGlobalStyle() ? 100 : config.bar2Opacity();
			case 3: return config.bar3UseGlobalStyle() ? 100 : config.bar3Opacity();
			case 4: return config.bar4UseGlobalStyle() ? 100 : config.bar4Opacity();
			case 5: return config.bar5UseGlobalStyle() ? 100 : config.bar5Opacity();
			default: return 100;
		}
	}

	private int activeBarCount() {
		return (xpBarMode.get(config.bar1BarMode()) != null ? 1 : 0) + activeRegularBarCount();
	}

	void applyConfigSize() {
		int height = 0;
		if (xpBarMode.get(config.bar1BarMode()) != null) height += styledBarHeight(1);
		if (barMode.get(config.bar2BarMode()) != null) height += styledBarHeight(2);
		if (barMode.get(config.bar3BarMode()) != null) height += styledBarHeight(3);
		if (barMode.get(config.bar4BarMode()) != null) height += styledBarHeight(4);
		if (barMode.get(config.bar5BarMode()) != null) height += styledBarHeight(5);
		height += Math.max(0, activeBarCount() - 1) * config.barSpacing();
		if (config.layout() == Layout.VIEW.VERTICAL) {
			setPreferredSize(new Dimension(Math.max(32, height), config.barWidth()));
		} else {
			setPreferredSize(new Dimension(config.barWidth(), Math.max(32, height)));
		}
		lastManagedSize = getPreferredSize() == null ? null : new Dimension(getPreferredSize());
	}

	void beginSidebarEdit() {
		if (sidebarEditing) {
			return;
		}

		sidebarEditing = true;
		positionBeforeSidebar = getPosition();
		Point current = getPreferredLocation();
		locationBeforeSidebar = current == null ? null : new Point(current);

		// Opening a RuneLite PluginPanel temporarily changes the game canvas size.
		// OverlayManager can clamp/re-anchor a HUD during that resize. Pin RHUD to
		// a predictable visible editor location and restore the real placement when
		// the sidebar closes. This does not alter the user's saved HUD placement.
		setPreferredLocation(null);
		setPosition(OverlayPosition.TOP_LEFT);
	}

	void endSidebarEdit() {
		if (!sidebarEditing) {
			return;
		}

		setPosition(positionBeforeSidebar == null ? OverlayPosition.ABOVE_CHATBOX_RIGHT : positionBeforeSidebar);
		setPreferredLocation(locationBeforeSidebar == null ? null : new Point(locationBeforeSidebar));
		positionBeforeSidebar = null;
		locationBeforeSidebar = null;
		sidebarEditing = false;
	}

	private int activeRegularBarCount() {
		int count = 0;
		if (barMode.get(config.bar2BarMode()) != null) count++;
		if (barMode.get(config.bar3BarMode()) != null) count++;
		if (barMode.get(config.bar4BarMode()) != null) count++;
		if (barMode.get(config.bar5BarMode()) != null) count++;
		return count;
	}

	private void syncAltResizeToConfig() {
		Dimension preferred = getPreferredSize();
		if (preferred == null || preferred.width <= 0 || preferred.height <= 0) {
			return;
		}
		if (lastManagedSize != null && lastManagedSize.equals(preferred)) {
			return;
		}

		int requestedWidth = config.layout() == Layout.VIEW.VERTICAL ? preferred.height : preferred.width;
		requestedWidth = Math.max(90, Math.min(520, requestedWidth));
		if (requestedWidth != config.barWidth()) {
			configManager.setConfiguration(RHUD_Config.GROUP, "barWidth", requestedWidth);
		}

		int regularBars = activeRegularBarCount();
		if (regularBars > 0) {
			int requestedTotal = config.layout() == Layout.VIEW.VERTICAL ? preferred.width : preferred.height;
			int xpHeight = xpBarMode.get(config.bar1BarMode()) != null ? config.xpBarHeight() : 0;
			int requestedBarHeight = Math.max(2, Math.min(100, (requestedTotal - xpHeight) / regularBars));
			if (requestedBarHeight != config.barHeight()) {
				configManager.setConfiguration(RHUD_Config.GROUP, "barHeight", requestedBarHeight);
			}
		}

		lastManagedSize = new Dimension(preferred);
	}

	@Override
	public Dimension render(Graphics2D g) {
		syncAltResizeToConfig();
		if (config.hideInInterfaces() && isBlockingInterfaceOpen()) {
			return null;
		}
		Dimension dimension = null;
		final boolean hide = config.hideInInterfaces() && isBlockingInterfaceOpen();

		if (!hide) {
			int width = config.barWidth(), adjX = config.barOffsetX();
			int adjY = config.barOffsetY();
			int totalHeight = 0;
			int totalWidth = 0;
			int x = adjX, y = adjY;

			XpRender Bar1 = xpBarMode.get(config.bar1BarMode());
			Render Bar2 = barMode.get(config.bar2BarMode());
			Render Bar3 = barMode.get(config.bar3BarMode());
			Render Bar4 = barMode.get(config.bar4BarMode());
			Render Bar5 = barMode.get(config.bar5BarMode());


			g.setColor(Color.BLACK);
			if (config.showHeader() && config.layout() != Layout.VIEW.VERTICAL) {
				if (Bar1 != null) {
					totalHeight += styledBarHeight(1) + (totalHeight > 0 ? config.barSpacing() : 0);
					drawBackdropRect(g, adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar2 != null) {
					totalHeight += styledBarHeight(2) + (totalHeight > 0 ? config.barSpacing() : 0);
					drawBackdropRect(g, adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar3 != null) {
					totalHeight += styledBarHeight(3) + (totalHeight > 0 ? config.barSpacing() : 0);
					drawBackdropRect(g, adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar4 != null) {
					totalHeight += styledBarHeight(4) + (totalHeight > 0 ? config.barSpacing() : 0);
					drawBackdropRect(g, adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar5 != null) {
					totalHeight += styledBarHeight(5) + (totalHeight > 0 ? config.barSpacing() : 0);
					drawBackdropRect(g, adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}

				drawGradientBar(g, adjX - 2, adjY - 20, totalHeight + 22, false);
			} else {
				if (Bar1 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += styledBarHeight(1) + (totalHeight > 0 ? config.barSpacing() : 0);
					} else {
						totalHeight += styledBarHeight(1) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar2 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += styledBarHeight(2) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 7, adjY - 4, totalHeight - 3, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY - 3, totalHeight - 2, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						totalHeight += styledBarHeight(2) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					} else {
						totalHeight += styledBarHeight(2) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar3 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += styledBarHeight(3) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 7, adjY - 4, totalHeight + 5, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY - 3, totalHeight + 6, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						// drawBackdropRect(g, adjX - 7, adjY - 4, totalHeight + 3, config.barWidth() + 5);
						// drawGradientBar(g, adjX - 6, adjY - 3, totalHeight + 4, true);
					} else {
						totalHeight += styledBarHeight(3) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar4 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += styledBarHeight(4) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 7, adjY - 4, totalHeight + 1, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY - 3, totalHeight + 2, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						totalHeight += styledBarHeight(4) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					} else {
						totalHeight += styledBarHeight(4) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar5 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += styledBarHeight(5) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 7, adjY - 4, totalHeight + 3, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY - 3, totalHeight + 4, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						// drawBackdropRect(g, adjX - 7, adjY - 4, totalHeight + 3, config.barWidth() + 5);
						// drawGradientBar(g, adjX - 6, adjY - 3, totalHeight + 4, true);
					} else {
						totalHeight += styledBarHeight(5) + (totalHeight > 0 ? config.barSpacing() : 0);
						drawBackdropRect(g, adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}

			}

			if (config.enableXpTracking()) {
				renderTrackerOverlay(g, x, y, totalHeight);
			}

			FontAssistant.updateFont(config.fontName(), config.fontSize(), config.fontStyle());
			FontAssistant.initFont(g);
			try {
				buildIcons();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			if (Bar1 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar1.renderBar(config, g, x - 2, y - 2, width, styledBarHeight(1), styledBackground(1), styledOpacity(1), styledColor(1));
					x += styledBarHeight(1) + config.barSpacing();
				} else {
					Bar1.renderBar(config, g, adjX, adjY, width, styledBarHeight(1), styledBackground(1), styledOpacity(1), styledColor(1));
					adjY += styledBarHeight(1) + config.barSpacing();
					y += y;
				}
			}

			if (Bar2 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar2.renderBar(config, g, x - 2, y - 2, styledBarHeight(2), styledIcon(2), styledTextFormat(2), styledBackground(2), styledOpacity(2), styledColor(2), warningActive(config.bar2BarMode()), warningColor(config.bar2BarMode()));
					x += styledBarHeight(2) + config.barSpacing();
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar2.renderBar(config, g, adjX, adjY, styledBarHeight(2), styledIcon(2), styledTextFormat(2), styledBackground(2), styledOpacity(2), styledColor(2), warningActive(config.bar2BarMode()), warningColor(config.bar2BarMode()));
					// adjY += styledBarHeight(2) + config.barSpacing();
				} else {
					Bar2.renderBar(config, g, adjX, adjY, styledBarHeight(2), styledIcon(2), styledTextFormat(2), styledBackground(2), styledOpacity(2), styledColor(2), warningActive(config.bar2BarMode()), warningColor(config.bar2BarMode()));
					adjY += styledBarHeight(2) + config.barSpacing();
				}
			}

			if (Bar3 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar3.renderBar(config, g, x, y - 2, styledBarHeight(3), styledIcon(3), styledTextFormat(3), styledBackground(3), styledOpacity(3), styledColor(3), warningActive(config.bar3BarMode()), warningColor(config.bar3BarMode()));
					x += styledBarHeight(3) + config.barSpacing();
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar3.renderBar(config, g, adjX + width / 2, adjY, styledBarHeight(3), styledIcon(3), styledTextFormat(3), styledBackground(3), styledOpacity(3), styledColor(3), warningActive(config.bar3BarMode()), warningColor(config.bar3BarMode()));
					adjY += styledBarHeight(3) + config.barSpacing();
				} else {
					Bar3.renderBar(config, g, adjX, adjY, styledBarHeight(3), styledIcon(3), styledTextFormat(3), styledBackground(3), styledOpacity(3), styledColor(3), warningActive(config.bar3BarMode()), warningColor(config.bar3BarMode()));
					adjY += styledBarHeight(3) + config.barSpacing();
				}
			}

			if (Bar4 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar4.renderBar(config, g, x + 2, y - 2, styledBarHeight(4), styledIcon(4), styledTextFormat(4), styledBackground(4), styledOpacity(4), styledColor(4), warningActive(config.bar4BarMode()), warningColor(config.bar4BarMode()));
					x += styledBarHeight(4) + config.barSpacing();
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar4.renderBar(config, g, adjX, adjY, styledBarHeight(4), styledIcon(4), styledTextFormat(4), styledBackground(4), styledOpacity(4), styledColor(4), warningActive(config.bar4BarMode()), warningColor(config.bar4BarMode()));
					// adjY += styledBarHeight(4) + config.barSpacing();
				} else {
					Bar4.renderBar(config, g, adjX, adjY, styledBarHeight(4), styledIcon(4), styledTextFormat(4), styledBackground(4), styledOpacity(4), styledColor(4), warningActive(config.bar4BarMode()), warningColor(config.bar4BarMode()));
					adjY += styledBarHeight(4) + config.barSpacing();
				}
			}

			if (Bar5 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar5.renderBar(config, g, x + 4, y - 2, styledBarHeight(5), styledIcon(5), styledTextFormat(5), styledBackground(5), styledOpacity(5), styledColor(5), warningActive(config.bar5BarMode()), warningColor(config.bar5BarMode()));
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar5.renderBar(config, g, adjX + width / 2, adjY, styledBarHeight(5), styledIcon(5), styledTextFormat(5), styledBackground(5), styledOpacity(5), styledColor(5), warningActive(config.bar5BarMode()), warningColor(config.bar5BarMode()));
					// adjY += config.barHeight();
				} else {
					Bar5.renderBar(config, g, adjX, adjY, styledBarHeight(5), styledIcon(5), styledTextFormat(5), styledBackground(5), styledOpacity(5), styledColor(5), warningActive(config.bar5BarMode()), warningColor(config.bar5BarMode()));
				}
			}

			g.setColor(Color.WHITE);
			Player localPlayer = client.getLocalPlayer();
			if (localPlayer != null && config.showHeader()) {
				String playerName = localPlayer.getName();
				int combatLevel = localPlayer.getCombatLevel();
				g.drawString(playerName + "     Lv." + combatLevel, 5, -5);
			}

			if (Bar1 == null && Bar2 == null && Bar3 == null && Bar4 == null && Bar5 == null) {
				dimension = new Dimension(Math.max(config.trackerWidth(), 140), 32);
			} else if (config.layout() == Layout.VIEW.VERTICAL) {
				dimension = new Dimension(totalHeight, config.barWidth());
			} else {
				dimension = new Dimension(config.barWidth(), totalHeight);
			}
		}

		if (plugin.isEditMode() && dimension != null) {
			drawEditGuides(g, dimension);
		}

		return dimension;
	}

	private void drawEditGuides(Graphics2D g, Dimension dimension) {
		final Stroke oldStroke = g.getStroke();
		final Color oldColor = g.getColor();

		int x = config.barOffsetX() - 5;
		int y = config.barOffsetY() - (config.showHeader() && config.layout() != Layout.VIEW.VERTICAL ? 24 : 6);
		int width = Math.max(20, dimension.width + 10);
		int height = Math.max(20, dimension.height + (config.showHeader() && config.layout() != Layout.VIEW.VERTICAL ? 28 : 12));

		g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 5f, 4f }, 0f));
		g.setColor(new Color(255, 152, 0, 230));
		g.drawRect(x, y, width, height);

		g.setStroke(oldStroke);
		int handle = 7;
		int half = handle / 2;
		int cx = x + width / 2;
		int cy = y + height / 2;
		int right = x + width;
		int bottom = y + height;
		int[][] points = {
				{x, y}, {cx, y}, {right, y},
				{x, cy}, {right, cy},
				{x, bottom}, {cx, bottom}, {right, bottom}
		};
		for (int[] point : points) {
			g.fillRect(point[0] - half, point[1] - half, handle, handle);
		}

		String size = dimension.width + " x " + dimension.height;
		FontMetrics fm = g.getFontMetrics();
		int labelW = fm.stringWidth(size) + 10;
		int labelH = fm.getHeight() + 4;
		int labelX = x + Math.max(0, (width - labelW) / 2);
		int labelY = y - labelH - 3;
		if (labelY < 0) {
			labelY = y + 4;
		}
		g.setColor(new Color(20, 20, 20, 220));
		g.fillRoundRect(labelX, labelY, labelW, labelH, 6, 6);
		g.setColor(new Color(255, 152, 0));
		g.drawString(size, labelX + 5, labelY + fm.getAscent() + 2);

		g.setStroke(oldStroke);
		g.setColor(oldColor);
	}

	private void renderTrackerOverlay(Graphics2D g, int x, int y, int total) {
		Skill skill;
		if (config.mostRecentSkill() && plugin.currentSkill != null) {
			skill = plugin.currentSkill;
		} else {
			skill = config.skill();
		}

		String name = skill.getName();
		int currentXP = client.getSkillExperience(skill);
		int currentLevel = Experience.getLevelForXp(currentXP);
		int nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int xpNeeded = Math.max(0, nextLevelXP - currentXP);
		int startXp = xpTrackerService.getStartGoalXp(skill);
		int goalXp = xpTrackerService.getEndGoalXp(skill);
		NumberFormat f = NumberFormat.getNumberInstance(Locale.US);

		Color accent = getSkillAccentColor(name);
		Color labelColor = new Color(190, 190, 190);
		Color valueColor = new Color(245, 245, 245);
		Color panelTop = new Color(30, 30, 30, 235);
		Color panelBottom = new Color(12, 12, 12, 235);
		Color border = new Color(0, 0, 0, 230);

		java.util.List<String[]> rows = new ArrayList<>();
		rows.add(new String[] { "Current XP", f.format(currentXP) });

		if (config.xpNeeded()) {
			rows.add(new String[] { "To Next Level", f.format(xpNeeded) });
		}

		if (goalXp > currentXP) {
			if (config.actionsNeeded()) {
				int actionsLeft = xpTrackerService.getActionsLeft(skill);
				if (actionsLeft != Integer.MAX_VALUE) {
					rows.add(new String[] { "Actions Remaining", f.format(actionsLeft) });
				}
			}

			if (config.xpGained()) {
				rows.add(new String[] { "Session XP", f.format(plugin.xpGainedInSessionMap(skill)) });
			}

			if (config.xpHour()) {
				int xpHr = xpTrackerService.getXpHr(skill);
				if (xpHr != 0) {
					rows.add(new String[] { "XP / Hour", f.format(xpHr) });
				}
			}

			if (config.showTTG()) {
				String timeLeft = formatGoalTime(xpTrackerService.getTimeTilGoal(skill));
				rows.add(new String[] { "Time to Level", timeLeft });
			}
		}

		int panelWidth = Math.max(140, config.trackerWidth());
		int padding = 7;
		int headerHeight = 22;
		int rowHeight = 14;
		int progressHeight = config.showPercent() ? 22 : 0;
		int panelHeight = padding + headerHeight + (rows.size() * rowHeight) + progressHeight + padding;

		Point trackerPoint = getTrackerPoint(x, y, total, panelWidth, panelHeight);
		int px = trackerPoint.x;
		int py = trackerPoint.y;

		Paint oldPaint = g.getPaint();
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		Font oldFont = g.getFont();

		g.setPaint(new GradientPaint(px, py, panelTop, px, py + panelHeight, panelBottom));
		g.fillRoundRect(px, py, panelWidth, panelHeight, 8, 8);
		g.setColor(border);
		g.drawRoundRect(px, py, panelWidth - 1, panelHeight - 1, 8, 8);

		// Skill-colored accent line gives the tracker a visual connection to RHUD.
		g.setColor(accent);
		g.fillRoundRect(px + 1, py + 1, panelWidth - 2, 3, 6, 6);

		FontAssistant.updateFont(config.fontName(), 13, config.fontStyle());
		FontAssistant.initFont(g);
		Font bodyFont = g.getFont();
		Font headerFont = bodyFont.deriveFont(Font.BOLD, Math.max(11f, bodyFont.getSize2D()));
		g.setFont(headerFont);
		FontMetrics headerMetrics = g.getFontMetrics();
		String header = name.toUpperCase(Locale.US) + "  •  LEVEL " + client.getBoostedSkillLevel(skill);
		g.setColor(accent);
		g.drawString(header, px + padding, py + 7 + headerMetrics.getAscent());

		g.setFont(bodyFont);
		FontMetrics fm = g.getFontMetrics();
		int textY = py + padding + headerHeight + fm.getAscent();
		int rightX = px + panelWidth - padding;
		for (String[] row : rows) {
			g.setColor(labelColor);
			g.drawString(row[0], px + padding, textY);
			g.setColor(valueColor);
			g.drawString(row[1], rightX - fm.stringWidth(row[1]), textY);
			textY += rowHeight;
		}

		if (config.showPercent()) {
			double rawProgress = goalXp > startXp ? getSkillProgress(startXp, currentXP, goalXp) : 100.0;
			int percent = (int) Math.max(0, Math.min(100, rawProgress));
			int barX = px + padding;
			int barY = py + panelHeight - padding - 10;
			int barW = panelWidth - (padding * 2);
			int barH = 8;

			g.setColor(new Color(55, 55, 55, 235));
			g.fillRoundRect(barX, barY, barW, barH, 5, 5);
			int fillW = (int) Math.round(barW * (percent / 100.0));
			if (fillW > 0) {
				g.setColor(accent);
				g.fillRoundRect(barX, barY, fillW, barH, 5, 5);
			}
			g.setColor(new Color(0, 0, 0, 180));
			g.drawRoundRect(barX, barY, barW - 1, barH - 1, 5, 5);

			String pct = percent + "%";
			g.setColor(valueColor);
			g.drawString(pct, rightX - fm.stringWidth(pct), barY - 2);
			g.setColor(labelColor);
			g.drawString("Level Progress", barX, barY - 2);
		}

		g.setFont(oldFont);
		g.setStroke(oldStroke);
		g.setColor(oldColor);
		g.setPaint(oldPaint);

		// The tracker is now drawn directly so the old PanelComponent does not add
		// a second RuneLite-style background over the RHUD tracker.
		panelComponent.getChildren().clear();
	}

	private Point getTrackerPoint(int x, int y, int total, int panelWidth, int panelHeight) {
		if (config.layout() == Layout.VIEW.VERTICAL) {
			if (config.pos() == Layout.POINT.LEFT) return new Point(x - panelWidth - 8, y + 4);
			if (config.pos() == Layout.POINT.TOPLEFT) return new Point(x - 8, y + 4 - panelHeight);
			if (config.pos() == Layout.POINT.TOPRIGHT) return new Point(x + total - panelWidth, y + 4 - panelHeight);
			if (config.pos() == Layout.POINT.BOTTOMLEFT) return new Point(x - 4, y + 10 + config.barWidth());
			if (config.pos() == Layout.POINT.BOTTOMRIGHT) return new Point(x + total - panelWidth, y + 10 + config.barWidth());
			return new Point(x + total, y + 4);
		}

		if (config.pos() == Layout.POINT.LEFT) return new Point(x - panelWidth - 6, y - 4);
		if (config.pos() == Layout.POINT.TOPLEFT) return new Point(x, y - 4 - panelHeight);
		if (config.pos() == Layout.POINT.TOPRIGHT) return new Point(x + config.barWidth() - panelWidth, y - 4 - panelHeight);
		if (config.pos() == Layout.POINT.BOTTOMLEFT) return new Point(x, y + 4 + total);
		if (config.pos() == Layout.POINT.BOTTOMRIGHT) return new Point(x + config.barWidth() - panelWidth, y + 4 + total);
		return new Point(x + config.barWidth() + 6, y - 4);
	}

	private String formatGoalTime(String timeLeft) {
		String[] parts = timeLeft.split(":");
		if (parts.length == 3) {
			try {
				int hours = Integer.parseInt(parts[0]);
				int minutes = Integer.parseInt(parts[1]);
				int seconds = Integer.parseInt(parts[2]);
				return String.format("%dh %02dm %02ds", hours, minutes, seconds);
			} catch (NumberFormatException ignored) {
				// Fall through and return RuneLite's original text.
			}
		}
		return timeLeft;
	}

	private Color getSkillAccentColor(String name) {
		switch (name) {
			case "Cooking": return new Color(0x8D4AA0);
			case "Attack": return new Color(0xD64545);
			case "Strength": return new Color(0x28A36A);
			case "Defence": return new Color(0x7E91D1);
			case "Ranged": return new Color(0x7D9B32);
			case "Prayer": return new Color(0xE8E8E8);
			case "Magic": return new Color(0x666BD0);
			case "Runecraft": return new Color(0xD9954A);
			case "Construction": return new Color(0xB7A98E);
			case "Hitpoints": return new Color(0xE1DFD8);
			case "Agility": return new Color(0x5559A4);
			case "Herblore": return new Color(0x39B85F);
			case "Thieving": return new Color(0x9B5C83);
			case "Crafting": return new Color(0xB88A52);
			case "Fletching": return new Color(0x267A7B);
			case "Slayer": return new Color(0x8C3026);
			case "Hunter": return new Color(0x91896A);
			case "Mining": return new Color(0x75BEE1);
			case "Smithing": return new Color(0xA5A59B);
			case "Fishing": return new Color(0x86A9C8);
			case "Woodcutting": return new Color(0x688958);
			case "Firemaking": return new Color(0xE8862B);
			case "Farming": return new Color(0x4B8B4B);
			default: return new Color(0xC38CFF);
		}
	}

	private double getSkillProgress(int startXp, int currentXp, int goalXp) {
		double xpGained = currentXp - startXp;
		double xpGoal = goalXp - startXp;

		return ((xpGained / xpGoal) * 100);
	}

	private void drawBackdropRect(Graphics2D g, int x, int y, int width, int height) {
		if (config.showHudBackdrop()) {
			g.drawRect(x, y, width, height);
		}
	}

	public void drawGradientBar(Graphics2D g, int x, int y, int barSize, boolean vertical) {
		if (!config.showHudBackdrop()) {
			return;
		}

		// The start/end colors for the gradient
		Color startColor = new Color(50, 50, 50);
		Color endColor = Color.BLACK;

		for (int i = 0; i < barSize; i++) {
			// 'i' goes from 0 to barSize
			float ratio = (float) i / (float) barSize;

			// Interpolate color
			int red = (int) (startColor.getRed() * (1 - ratio) + endColor.getRed() * ratio);
			int green = (int) (startColor.getGreen() * (1 - ratio) + endColor.getGreen() * ratio);
			int blue = (int) (startColor.getBlue() * (1 - ratio) + endColor.getBlue() * ratio);

			g.setColor(new Color(red, green, blue));

			if (vertical) {
				g.drawLine(x + i, y, x + i, y + config.barWidth() + 3);
			} else {
				g.drawLine(x, y + i, x + config.barWidth() + 3, y + i);
			}
		}
	}

	private void buildIcons() throws IOException {
		if (heartIcon == null) {
			heartIcon = loadAndResize(SpriteID.MINIMAP_ORB_HITPOINTS_ICON);
		}
		if (energyIcon == null) {
			energyIcon = loadAndResize(SpriteID.MINIMAP_ORB_WALK_ICON);
		}
		if (specialIcon == null) {
			specialIcon = loadAndResize(SpriteID.MINIMAP_ORB_SPECIAL_ICON);
		}
	}

	private BufferedImage loadAndResize(int spriteId) {
		BufferedImage image = spriteManager.getSprite(spriteId, 0);
		if (image == null) {
			return null;
		}

		return ImageUtil.resizeCanvas(image, 15, ICON_DIMENSIONS.height);
	}

	private boolean inLms() {
		return client.getWidget(ComponentID.LMS_INGAME_INFO) != null;
	}
	private static final int[] BLOCKING_GROUPS = {
			InterfaceID.BANK,
			InterfaceID.DEPOSIT_BOX,
			InterfaceID.GRAND_EXCHANGE,
	};

	private boolean isBlockingInterfaceOpen() {
		for (final int group : BLOCKING_GROUPS) {
			final Widget root = client.getWidget(group, 0);
			if (root != null && !root.isHidden()) {
				return true;
			}
		}
		return false;
	}

	private int getRestoreValue(String skill) {
		final MenuEntry[] menu = client.getMenu().getMenuEntries();
		final int menuSize = menu.length;
		if (menuSize == 0) {
			return 0;
		}

		final MenuEntry entry = menu[menuSize - 1];
		final Widget widget = entry.getWidget();
		int restoreValue = 0;

		if (widget != null && widget.getId() == ComponentID.INVENTORY_CONTAINER) {
			final Effect change = itemStatService.getItemStatChanges(widget.getItemId());

			if (change != null) {
				for (final StatChange c : change.calculate(client).getStatChanges()) {
					final int value = c.getTheoretical();

					if (value != 0 && c.getStat().getName().equals(skill)) {
						restoreValue = value;
					}
				}
			}
		}

		return restoreValue;
	}
}
