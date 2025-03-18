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
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.ui.overlay.*;

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
		manager.add(hud);
	}

	@Override
	protected void shutDown() {
		manager.remove(hud);
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
			XpTrackerService xpTrackerService) {
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.itemStatService = itemStatService;
		this.spriteManager = spriteManager;
		this.xpTrackerService = xpTrackerService;

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);

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


	@Override
	public Dimension render(Graphics2D g) {
		Dimension dimension = null;
		Widget bankContainer = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);


		if (bankContainer == null || bankContainer.isHidden()) {
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
					totalHeight += config.xpBarHeight();
					g.drawRect(adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar2 != null) {
					totalHeight += config.barHeight();
					g.drawRect(adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar3 != null) {
					totalHeight += config.barHeight();
					g.drawRect(adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar4 != null) {
					totalHeight += config.barHeight();
					g.drawRect(adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}
				if (Bar5 != null) {
					totalHeight += config.barHeight();
					g.drawRect(adjX - 3, adjY - 21, config.barWidth() + 5, totalHeight + 23);
				}

				drawGradientBar(g, adjX - 2, adjY - 20, totalHeight + 22, false);
			} else {
				if (Bar1 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += config.xpBarHeight();
					} else {
						totalHeight += config.xpBarHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar2 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 7, adjY + 5, totalHeight - 3, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY + 6, totalHeight - 2, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					} else {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar3 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 7, adjY + 5, totalHeight, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY + 6, totalHeight + 1, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						// g.drawRect(adjX - 7, adjY + 5, totalHeight + 3, config.barWidth() + 5);
						// drawGradientBar(g, adjX - 6, adjY + 6, totalHeight + 4, true);
					} else {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar4 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 7, adjY + 5, totalHeight + 1, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY + 6, totalHeight + 2, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					} else {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
						drawGradientBar(g, adjX - 2, adjY - 3, totalHeight + 6, false);
					}
				}
				if (Bar5 != null) {
					if (config.layout() == Layout.VIEW.VERTICAL) {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 7, adjY + 5, totalHeight + 3, config.barWidth() + 5);
						drawGradientBar(g, adjX - 6, adjY + 6, totalHeight + 4, true);
					} else if (config.layout() == Layout.VIEW.GRID) {
						// g.drawRect(adjX - 7, adjY + 5, totalHeight + 3, config.barWidth() + 5);
						// drawGradientBar(g, adjX - 6, adjY + 6, totalHeight + 4, true);
					} else {
						totalHeight += config.barHeight();
						g.drawRect(adjX - 3, adjY - 4, config.barWidth() + 5, totalHeight + 7);
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

				} else {
					Bar1.renderBar(config, g, adjX, adjY, width);
					adjY += config.xpBarHeight();
					y += y;
				}
			}

			if (Bar2 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar2.renderBar(config, g, x - 2, y - 2 + config.xpBarHeight());
					x += config.barHeight();
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar2.renderBar(config, g, adjX, adjY);
					// adjY += config.barHeight();
				} else {
					Bar2.renderBar(config, g, adjX, adjY);
					adjY += config.barHeight();
				}
			}

			if (Bar3 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar3.renderBar(config, g, x, y - 2 + config.xpBarHeight());
					x += config.barHeight();
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar3.renderBar(config, g, adjX + width / 2, adjY);
					adjY += config.barHeight();
				} else {
					Bar3.renderBar(config, g, adjX, adjY);
					adjY += config.barHeight();
				}
			}

			if (Bar4 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar4.renderBar(config, g, x + 2, y - 2 + config.xpBarHeight());
					x += config.barHeight();
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar4.renderBar(config, g, adjX, adjY);
					// adjY += config.barHeight();
				} else {
					Bar4.renderBar(config, g, adjX, adjY);
					adjY += config.barHeight();
				}
			}

			if (Bar5 != null) {
				if (config.layout() == Layout.VIEW.VERTICAL) {
					Bar5.renderBar(config, g, x + 4, y - 2 + config.xpBarHeight());
				} else if (config.layout() == Layout.VIEW.GRID) {
					Bar5.renderBar(config, g, adjX + width / 2, adjY);
					// adjY += config.barHeight();
				} else {
					Bar5.renderBar(config, g, adjX, adjY);
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
				dimension = panelComponent.render(g);
			} else if (config.layout() == Layout.VIEW.VERTICAL) {
				if (config.enableXpTracking()) {
					panelComponent.render(g);
				}
				dimension = new Dimension(totalHeight, config.barWidth());
			} else {
				if (config.enableXpTracking()) {
					panelComponent.render(g);
				}
				dimension = new Dimension(config.barWidth(), totalHeight);
			}
		}
		return dimension;
	}

	private void renderTrackerOverlay(Graphics2D g, int x, int y, int total) {
		XpRender Bar1 = xpBarMode.get(config.bar1BarMode());
		Render Bar2 = barMode.get(config.bar2BarMode());
		Render Bar3 = barMode.get(config.bar3BarMode());
		Render Bar4 = barMode.get(config.bar4BarMode());
		Render Bar5 = barMode.get(config.bar5BarMode());
		g.setColor(Color.BLACK);
		panelComponent.getChildren().clear();
		if (config.layout() == Layout.VIEW.VERTICAL) {
			if (Bar1 == null && Bar2 == null && Bar3 == null && Bar4 == null && Bar5 == null) {
				panelComponent.setPreferredLocation(new Point(x, y));
			} else if (config.pos() == Layout.POINT.LEFT) {
				panelComponent.setPreferredLocation(new Point(x - config.trackerWidth() - 8, y + 4));
			} else if (config.pos() == Layout.POINT.TOPLEFT) {
				panelComponent.setPreferredLocation(new Point(x - 8, y + 4 - panelComponent.getBounds().height));
			} else if (config.pos() == Layout.POINT.TOPRIGHT) {
				panelComponent.setPreferredLocation(new Point(x + total - panelComponent.getBounds().width,
						y + 4 - panelComponent.getBounds().height));
			} else if (config.pos() == Layout.POINT.BOTTOMLEFT) {
				panelComponent.setPreferredLocation(new Point(x - 4, y + 10 + config.barWidth()));
			} else if (config.pos() == Layout.POINT.BOTTOMRIGHT) {
				panelComponent.setPreferredLocation(
						new Point(x + total - panelComponent.getBounds().width, y + 10 + config.barWidth()));
			} else {
				panelComponent.setPreferredLocation(new Point(x + total, y + 4));
			}
		} else {
			if (Bar1 == null && Bar2 == null && Bar3 == null && Bar4 == null && Bar5 == null) {
				panelComponent.setPreferredLocation(new Point(x, y));
			} else if (config.pos() == Layout.POINT.LEFT) {
				panelComponent.setPreferredLocation(new Point(x - config.trackerWidth() - 6, y - 4));
			} else if (config.pos() == Layout.POINT.TOPLEFT) {
				panelComponent.setPreferredLocation(new Point(x, y - 4 - panelComponent.getBounds().height));
			} else if (config.pos() == Layout.POINT.TOPRIGHT) {
				panelComponent.setPreferredLocation(new Point(x + config.barWidth() - panelComponent.getBounds().width,
						y - 4 - panelComponent.getBounds().height));
			} else if (config.pos() == Layout.POINT.BOTTOMLEFT) {
				panelComponent.setPreferredLocation(new Point(x, y + 4 + total));
			} else if (config.pos() == Layout.POINT.BOTTOMRIGHT) {
				panelComponent.setPreferredLocation(
						new Point(x + config.barWidth() - panelComponent.getBounds().width, y + 4 + total));
			} else {
				panelComponent.setPreferredLocation(new Point(x + config.barWidth() + 6, y - 4));
			}
		}
		panelComponent.setPreferredSize(new Dimension(config.trackerWidth(), 100));

		Skill skill;
		String name;
		int totalHeight = 0;

		if (config.mostRecentSkill() && plugin.currentSkill == null || !config.mostRecentSkill()) {
			skill = config.skill();
			name = config.skill().getName();
		} else if (config.mostRecentSkill()) {
			skill = plugin.currentSkill;
			name = plugin.currentSkill.getName();
		} else {
			skill = plugin.currentSkill;
			name = plugin.currentSkill.getName();

		}

		int xpGainedLastTime = plugin.getlastXpGainForSkill(skill);
		int currentXP = client.getSkillExperience(skill);
		int currentLevel = Experience.getLevelForXp(currentXP);
		int nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int xpNeeded = nextLevelXP - currentXP;
		final int counterLevel = client.getBoostedSkillLevel(skill);
		final String counterLevelText = Integer.toString(counterLevel);
		// int startXp = Experience.getXpForLevel(currentLevel);
		// int goalXp = Experience.getXpForLevel(currentLevel + 1);
		int startXp = xpTrackerService.getStartGoalXp(skill);
		int goalXp = xpTrackerService.getEndGoalXp(skill);
		NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
		String skillCurrentXp = f.format(currentXP);

		Color BODY_COLOUR = null;
		Color LEFT_TEXT;

		switch (name) {
			case "Cooking":
				BODY_COLOUR = new Color(0x5B266A);
				break;
			case "Attack":
				BODY_COLOUR = new Color(0xC20404);
				break;
			case "Strength":
				BODY_COLOUR = new Color(0x0D704A);
				break;
			case "Defence":
				BODY_COLOUR = new Color(0x687AB8);
				break;
			case "Ranged":
				BODY_COLOUR = new Color(0x5D761D);
				break;
			case "Prayer":
				BODY_COLOUR = new Color(0xFFFFFF);
				break;
			case "Magic":
				BODY_COLOUR = new Color(0x343791);
				break;
			case "Runecraft":
				BODY_COLOUR = new Color(0xD28735);
				break;
			case "Construction":
				BODY_COLOUR = new Color(0xA39782);
				break;
			case "Hitpoints":
				BODY_COLOUR = new Color(0xCFCEC9);
				break;
			case "Agility":
				BODY_COLOUR = new Color(0x2A2C74);
				break;
			case "Herblore":
				BODY_COLOUR = new Color(0x119E3F);
				break;
			case "Thieving":
				BODY_COLOUR = new Color(0x734161);
				break;
			case "Crafting":
				BODY_COLOUR = new Color(0x997140);
				break;
			case "Fletching":
				BODY_COLOUR = new Color(0x094C4D);
				break;
			case "Slayer":
				BODY_COLOUR = new Color(0x5F1109);
				break;
			case "Hunter":
				BODY_COLOUR = new Color(0x6F694B);
				break;
			case "Mining":
				BODY_COLOUR = new Color(0x75BEE1);
				break;
			case "Smithing":
				BODY_COLOUR = new Color(0x86867C);
				break;
			case "Fishing":
				BODY_COLOUR = new Color(0x728FAA);
				break;
			case "Woodcutting":
				BODY_COLOUR = new Color(0x455E37);
				break;
			case "Firemaking":
				BODY_COLOUR = new Color(0xD16F1A);
				break;
			case "Farming":
				BODY_COLOUR = new Color(0x2A5A2A);
				break;
		}

		LEFT_TEXT = new Color(0xFFDC02);

		FontAssistant.updateFont(config.fontName(), 14, config.fontStyle());
		FontAssistant.initFont(g);

		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
				.text("Xp Tracking")
				.color(Color.ORANGE)
				.build());
		panelComponent.getChildren().add(LineComponent.builder()
				.left(name)
				.leftColor(BODY_COLOUR)
				.right(counterLevelText)
				.rightColor(Color.GREEN)
				.build());
		panelComponent.getChildren().add(LineComponent.builder()
				.left("Current XP:")
				.leftColor(LEFT_TEXT)
				.right(skillCurrentXp)
				.build());
		if (config.xpNeeded()) {
			String skillXpToLevel = f.format(xpNeeded);
			panelComponent.getChildren().add(LineComponent.builder()
					.left("Exp Needed:")
					.leftColor(LEFT_TEXT)
					.right(skillXpToLevel)
					.build());
			ACTIVE += 1;
		}

		if (goalXp > currentXP) {
			if (config.actionsNeeded()) {
				int actionsLeft = xpTrackerService.getActionsLeft(skill);
				if (actionsLeft != Integer.MAX_VALUE) {
					String actionsLeftString = f.format(actionsLeft);
					panelComponent.getChildren().add(LineComponent.builder()
							.left("Actions Needed:")
							.leftColor(LEFT_TEXT)
							.right(actionsLeftString)
							.build());
					ACTIVE += 1;
				}
			}

			int totalSessionXpGained = plugin.xpGainedInSessionMap(skill);

			if (config.xpGained()) {
				panelComponent.getChildren().add(LineComponent.builder()
						.left("XP Gained:")
						.leftColor(LEFT_TEXT)
						.right(f.format(totalSessionXpGained))
						.build());
				ACTIVE += 1;
			}

			if (config.xpHour()) {
				int xpHr = xpTrackerService.getXpHr(skill);
				if (xpHr != 0) {
					String xpHrString = f.format(xpHr);
					panelComponent.getChildren().add(LineComponent.builder()
							.left("Exp Per/h:")
							.leftColor(LEFT_TEXT)
							.right(xpHrString)
							.build());
					ACTIVE += 1;
				}
			}

			if (config.showTTG()) {
				String timeLeft = xpTrackerService.getTimeTilGoal(skill);
				String[] parts = timeLeft.split(":");

				if (parts.length == 3) {
					try {
						// parts[0] => hours, parts[1] => minutes, parts[2] => seconds
						int hours = Integer.parseInt(parts[0]);
						int minutes = Integer.parseInt(parts[1]);
						int seconds = Integer.parseInt(parts[2]);

						// If you want to always display up to 4 digits of hours, do something like:
						// timeLeft = String.format("%4dh %02dm %02ds", hours, minutes, seconds);
						// If you want no leading zeros on hours, simply do:
						timeLeft = String.format("%dh %02dm %02ds", hours, minutes, seconds);
					} catch (NumberFormatException e) {
						// Parsing failed, so leave 'timeLeft' as-is or set to some fallback
					}
				}
				panelComponent.getChildren().add(LineComponent.builder()
						.left("TTL:")
						.leftColor(LEFT_TEXT)
						.right(timeLeft)
						.build());
				ACTIVE += 1;
			}

			if (config.showPercent()) {
				String progress = (int) (getSkillProgress(startXp, currentXP, goalXp)) + "%";
				panelComponent.getChildren().add(LineComponent.builder()
						.left("Percent:")
						.leftColor(LEFT_TEXT)
						.right(progress)
						.build());
			}

		}
		// panelComponent.render(g);
	}

	private double getSkillProgress(int startXp, int currentXp, int goalXp) {
		double xpGained = currentXp - startXp;
		double xpGoal = goalXp - startXp;

		return ((xpGained / xpGoal) * 100);
	}

	public void drawGradientBar(Graphics2D g, int x, int y, int barSize, boolean vertical) {
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
