package com.RastaXP;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Varbits;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import java.awt.Dimension;
import java.awt.Graphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.ui.FontManager;

@Slf4j
@PluginDescriptor(
		name = "RastaHUD",
		description = "Show an experience for tracked skill"
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
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		barsDisplayed = true;
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		barsDisplayed = false;
	}

	@Provides
	RastaXPConfig provideConfig(ConfigManager configManager)
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

@Slf4j
class XPBarOverlay extends Overlay {

	private static final int IMAGE_SIZE = 15;
	private RastaXPConfig config;
	private Client client;
	private static final Logger logger = LoggerFactory.getLogger(XPBarOverlay.class);
	private static final Color BACKGROUND = new Color(0, 0, 0, 120);
	private static final int WIDTH = 512;
	static final int HEIGHT = 15;
	private static final int BORDER_SIZE = 2;
	private static final Color PRAYER_COLOR = new Color(50, 200, 200, 175);
	private static final Color QUICK_PRAYER_COLOR = new Color(57, 255, 186, 225);

	private static final int HEALTH_LOCATION_X = 0;
	private static final int PRAYER_LOCATION_X = 1;
	private static final int PADDING = 1;
	private static final int ICON_AND_COUNTER_OFFSET_X = 1;
	private static final int ICON_AND_COUNTER_OFFSET_Y = 21;
	private static final int SKILL_ICON_HEIGHT = 35;
	private static final int COUNTER_ICON_HEIGHT = 18;
	private int currentXP;
	private int currentLevel;
	private int nextLevelXP;

	private final RastaXPPlugin plugin;
	private final SpriteManager spriteManager;

	private final SkillIconManager skillIconManager;
	private final TextComponent textComponent = new TextComponent();

	private final BufferedImage prayerImage;

	@Inject
	private XPBarOverlay(Client client, RastaXPPlugin plugin, RastaXPConfig config, SkillIconManager skillIconManager, SpriteManager spriteManager) {
		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
		this.skillIconManager = skillIconManager;
		prayerImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.PRAYER, true), IMAGE_SIZE, IMAGE_SIZE);
	}

	public Dimension render(Graphics2D g) {

		if (config.displayHealthAndPrayer())
		{
			final BufferedImage healthImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.HITPOINTS, true), IMAGE_SIZE, IMAGE_SIZE);
			final int counterHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
			final int counterPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
			final String counterHealthText = Integer.toString(counterHealth);
			final String counterPrayerText = Integer.toString(counterPrayer);

			renderIconsAndCounters(g, -185, +18, prayerImage, counterPrayerText, PRAYER_LOCATION_X);
			renderIconsAndCounters(g, -185, +33, healthImage, counterHealthText, HEALTH_LOCATION_X);
			renderThreeBars(g, -1, 1, 10);
			return null;
		}
		if (!config.displayHealthAndPrayer()){
			renderBar(g, -1, 1, 10);
		return null;
}

		if (!plugin.isBarsDisplayed()) {
			return null;
		}

		// Hide bar when there are no recent skills, in most recent skill mode.


		final int height, offsetBarX, offsetBarY;

		height = HEIGHT;
		offsetBarX = (-1);
		offsetBarY = (1);
		return null;
	}

	public void renderBar(Graphics2D graphics, int x, int y, int height) {
		//Get info for experience
		Skill skill = config.mostRecentSkill() ? plugin.getCurrentSkill() : config.skill();
		currentXP = client.getSkillExperience(skill);
		currentLevel = Experience.getLevelForXp(currentXP);
		nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int currentLevelXP = Experience.getXpForLevel(currentLevel);
		boolean isTransparentChatbox = client.getVar(Varbits.TRANSPARENT_CHATBOX) == 1;

		//Calc starting position for bar
		int adjustedX = x + 4;
		int adjustedY;
		int adjustedWidth = WIDTH;

		if (client.isResized()) {
			adjustedX = x - 165;
			adjustedWidth = WIDTH + 7;
		}
		adjustedY = client.isResized() && isTransparentChatbox ? y + 7 : y;

		final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);

		//Format tooltip display

		//Render the overlay
		Color barColor = config.colorXP();
		drawBar(graphics, adjustedX, adjustedY, adjustedWidth, filledWidth, barColor, config.colorXPNotches());
	}

	public void renderThreeBars(Graphics2D graphics, int x, int y, int height) {
		//Get info for experience, health, and prayer
		Skill skill = config.mostRecentSkill() ? plugin.getCurrentSkill() : config.skill();
		currentXP = client.getSkillExperience(skill);
		currentLevel = Experience.getLevelForXp(currentXP);
		nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
		int currentLevelXP = Experience.getXpForLevel(currentLevel);

		int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
		int currentPray = client.getBoostedSkillLevel(Skill.PRAYER);
		int maxPray = client.getRealSkillLevel(Skill.PRAYER);

		boolean isTransparentChatbox = client.getVar(Varbits.TRANSPARENT_CHATBOX) == 1;

		//Calc starting positions for bars
		int adjustedX = x -255;
		int adjustedY;
		int adjustedWidth = WIDTH;

		if (client.isResized()) {
			adjustedX = x - 165;
			adjustedWidth = WIDTH + 7;
		}
		adjustedY = client.isResized() && isTransparentChatbox ? y + 7 : y + 284;

		final int filledWidthXP = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
		final int filledWidthHP = getBarWidth(maxHP, currentHP, adjustedWidth);
		final int filledWidthPray = getBarWidth(maxPray, currentPray, adjustedWidth);
		final int quickPrayerState = client.getVar(Varbits.QUICK_PRAYER);
		final Color prayerBar = quickPrayerState == 1 ? QUICK_PRAYER_COLOR : PRAYER_COLOR;

		//Format tooltip display

		//Render the overlays
		drawBar(graphics, adjustedX, adjustedY, adjustedWidth, filledWidthXP, config.colorXP(), config.colorXPNotches());
		drawBars(graphics, adjustedX, adjustedY + 15, adjustedWidth, filledWidthPray, prayerBar);  //config.colorPray());
		drawBars(graphics, adjustedX, adjustedY + (15 * 2), adjustedWidth, filledWidthHP, config.colorHP());
	}

	private void drawBars(Graphics graphics, int adjustedX, int adjustedY, int adjustedWidth, int fill, Color barColor) {

		graphics.setColor(BACKGROUND);
		graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, HEIGHT - BORDER_SIZE);
		graphics.fillRect(adjustedX, adjustedY, adjustedWidth, HEIGHT);

		graphics.setColor(barColor);
		graphics.fillRect(adjustedX + BORDER_SIZE,
				adjustedY + BORDER_SIZE,
				fill - BORDER_SIZE * 2,
				HEIGHT - BORDER_SIZE * 2);

	}

	private void drawBar(Graphics graphics, int adjustedX, int adjustedY, int adjustedWidth, int fill, Color barColor, Color notchColor) {

		graphics.setColor(BACKGROUND);
		graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, 10 - BORDER_SIZE);
		graphics.fillRect(adjustedX, adjustedY, adjustedWidth, 10);

		graphics.setColor(barColor);
		graphics.fillRect(adjustedX + BORDER_SIZE,
				adjustedY + BORDER_SIZE,
				fill - BORDER_SIZE * 2,
				10 - BORDER_SIZE * 2);

		graphics.setColor(notchColor);
		graphics.fillRect(adjustedX + 1 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 2 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 3 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 4 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 5 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 6 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 7 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 8 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
		graphics.fillRect(adjustedX + 9 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);

	}

	private void renderIconsAndCounters(Graphics2D graphics, int adjustedX, int adjustedY, BufferedImage image, String counterText, int counterPadding) {
		final int widthOfCounter = graphics.getFontMetrics().stringWidth(counterText);
		final int centerText = (WIDTH - PADDING) / 2 - (widthOfCounter / 2);

			if (client.isResized() || plugin.getCurrentSkill() == null)
		{
			graphics.setFont(FontManager.getRunescapeSmallFont());
			textComponent.setColor(Color.ORANGE);
			textComponent.setText(counterText);
			textComponent.setPosition(new java.awt.Point(+109 + centerText + counterPadding, adjustedY + COUNTER_ICON_HEIGHT));
		}
		else
		{
			return;
		}

		if (config.enableSkillIcon())
			if (client.isResized())
		{
			graphics.drawImage(image, adjustedX + ICON_AND_COUNTER_OFFSET_X + PADDING, adjustedY + ICON_AND_COUNTER_OFFSET_Y - image.getWidth(null), null);
			//textComponent.setPosition(new java.awt.Point(adjustedX + centerText + counterPadding, adjustedY + SKILL_ICON_HEIGHT));
		}

		textComponent.render(graphics);
	}



	private static int getBarWidth(int base, int current, int size) {
		final double ratio = (double) current / base;

		if (ratio >= 1) {
			return size;
		}

		return (int) Math.round(ratio * size);
	}
}
