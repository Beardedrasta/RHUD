package com.RastaXP;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import java.util.Locale;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class XPBarOverlay extends Overlay {

    private static final int IMAGE_SIZE = 15;
    private final RastaXPConfig config;
    private final Client client;
    private static final Logger logger = LoggerFactory.getLogger(XPBarOverlay.class);
    private static final Color BACKGROUND = new Color(0, 0, 0, 120);
    private static final int WIDTH = 512;
    static final int HEIGHT = 15;
    private static final int BORDER_SIZE = 2;
    private static final Color PRAYER_COLOR = new Color(50, 200, 200, 175);
    private static final Color QUICK_PRAYER_COLOR = new Color(57, 255, 186, 225);
    private static final Color TITLE_COLOUR = new Color(0xff981f);

    private static final Color BODY_COLOUR = new Color(255, 190, 30, 225);

    private static final int HEALTH_LOCATION_X = 0;
    private static final int PRAYER_LOCATION_X = 1;
    private static final int SKILL_LOCATION_X = 2;
    private static final int PADDING = 1;
    private static final int ICON_AND_COUNTER_OFFSET_X = 1;
    private static final int ICON_AND_COUNTER_OFFSET_Y = 21;
    private static final int COUNTER_ICON_HEIGHT = 18;
    private int currentXP;
    private int currentLevel;
    private int nextLevelXP;

    private final RastaXPPlugin plugin;

    private final SkillIconManager skillIconManager;
    private final TextComponent textComponent = new TextComponent();

    private final TooltipManager tooltipManager;

    private final PanelComponent panelComponent = new PanelComponent();


    @Inject
    private XPBarOverlay(Client client, RastaXPPlugin plugin, RastaXPConfig config, SkillIconManager skillIconManager, TooltipManager tooltipManager) {
        super(plugin);
        this.skillIconManager = skillIconManager;
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.tooltipManager = tooltipManager;
        setPosition(OverlayPosition.TOP_CENTER);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "XPBar overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();
        if (config.XPtracker()) {
            String name;
            if (plugin.getCurrentSkill() == null) {
                name = config.skill().getName();
            } else name = plugin.getCurrentSkill().getName();
            int xpNeeded = nextLevelXP - currentXP;
            NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
            if (this.getBounds().contains(
                    client.getMouseCanvasPosition().getX(),
                    client.getMouseCanvasPosition().getY())) {
                tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(name + " " + "XP:", TITLE_COLOUR)));
                tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(f.format(xpNeeded), BODY_COLOUR)));
            }
            if (config.mostRecentSkill()) {
                Skill skill;
                if (plugin.getCurrentSkill() == null) {
                    skill = config.skill();
                } else {
                    skill = plugin.getCurrentSkill();
                }
                final BufferedImage skillImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(skill, true), IMAGE_SIZE, IMAGE_SIZE);
                final int counterLevel = client.getBoostedSkillLevel(skill);
                final String counterLevelText = Integer.toString(counterLevel);
                renderBar(graphics, -1, 1, 10);
                renderIcons(graphics, -25, +0, skillImage, SKILL_LOCATION_X);
                renderCounters(graphics, -25, 0, counterLevelText, SKILL_LOCATION_X);
            }
            if (!config.mostRecentSkill()) {
                Skill skill;
                if (plugin.getCurrentSkill() == null) {
                    skill = config.skill();
                } else {
                    skill = plugin.getCurrentSkill();
                }
                final BufferedImage skillImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(skill, true), IMAGE_SIZE, IMAGE_SIZE);
                final int counterLevel = client.getBoostedSkillLevel(skill);
                final String counterLevelText = Integer.toString(counterLevel);
                renderBarRecent(graphics, -1, 1, 10);
                renderIcons(graphics, -25, +0, skillImage, SKILL_LOCATION_X);
                renderCounters(graphics, -25, 0, counterLevelText, SKILL_LOCATION_X);
            }
        }
        if (config.displayHealthAndPrayer()) {
            final BufferedImage healthImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.HITPOINTS, true), IMAGE_SIZE, IMAGE_SIZE);
            final BufferedImage prayerImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.PRAYER, true), IMAGE_SIZE, IMAGE_SIZE);

                final int counterHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
                final int counterPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
                final String counterHealthText = Integer.toString(counterHealth);
                final String counterPrayerText = Integer.toString(counterPrayer);


                renderIcons(graphics, -25, +18, prayerImage, PRAYER_LOCATION_X);
                renderIcons(graphics, -25, +33, healthImage, HEALTH_LOCATION_X);
                renderCounters(graphics, -25, +18, counterPrayerText, PRAYER_LOCATION_X);
                renderCounters(graphics, -25, +33, counterHealthText, HEALTH_LOCATION_X);
                renderThreeBars(graphics, -1, 1, 10);
            }
            if (config.enableSmall()) {
                return new Dimension(256, 55);
            } else {
                return new Dimension(520, 55);
            }
        }


    private void renderBarRecent(Graphics2D graphics, int x, int y, int height) {
        //Get info for experience
        Skill skill;
        skill = config.skill();
        currentXP = client.getSkillExperience(skill);
        currentLevel = Experience.getLevelForXp(currentXP);
        nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
        int currentLevelXP = Experience.getXpForLevel(currentLevel);
        boolean isTransparentChatbox = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;

        //Calc starting position for bar
        int adjustedX = x;
        int adjustedY;
        int adjustedWidth = WIDTH;
        int adjustedHeight = 255;

        if (client.isResized()) {
            adjustedX = x;
            adjustedWidth = WIDTH + 7;
        }
        adjustedY = client.isResized() && isTransparentChatbox ? y + 7 : y;

        final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
        final int filledHeight = getBarHeight(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedHeight);

        //Format tooltip display

        //Render the overlay
        Color barColor = config.colorXP();
        if (config.enableSmall()){
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden())
                drawBarSmall(graphics, adjustedX, adjustedY, adjustedHeight, filledHeight, barColor, config.colorXPNotches());
        }
        else
        {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden())
                drawBarLarge(graphics, adjustedX, adjustedY, adjustedWidth, filledWidth, barColor, config.colorXPNotches());
        }
    }

    private void renderBar(Graphics2D graphics, int x, int y, int height) {
        //Get info for experience
        Skill skill;
        if (config.mostRecentSkill()) {
            if (plugin.getCurrentSkill() == null) {
                skill = config.skill();
            } else skill = plugin.getCurrentSkill();
            currentXP = client.getSkillExperience(skill);
            currentLevel = Experience.getLevelForXp(currentXP);
            nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
            int currentLevelXP = Experience.getXpForLevel(currentLevel);
            boolean isTransparentChatbox = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;

            //Calc starting position for bar
            int adjustedX = x;
            int adjustedY;
            int adjustedWidth = WIDTH;
            int adjustedHeight = 255;

            if (client.isResized()) {
                adjustedX = x;
                adjustedWidth = WIDTH + 7;
            }
            adjustedY = client.isResized() && isTransparentChatbox ? y + 7 : y;

            final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
            final int filledHeight = getBarHeight(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedHeight);

            //Format tooltip display

            //Render the overlay
            Color barColor = config.colorXP();

            if (config.enableSmall()){
                Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null || bankContainer.isHidden())
                    drawBarLarge(graphics, adjustedX, adjustedY, adjustedHeight, filledHeight, barColor, config.colorXPNotches());
            }
            else
            {
                Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null || bankContainer.isHidden())
                    drawBarLarge(graphics, adjustedX, adjustedY, adjustedWidth, filledWidth, barColor, config.colorXPNotches());
            }
        }
    }

    private void renderThreeBars(Graphics2D graphics, int x, int y, int height) {

        int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
        int currentPray = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPray = client.getRealSkillLevel(Skill.PRAYER);

        boolean isTransparentChatbox = client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;

        //Calc starting positions for bars
        int adjustedX = x;
        int adjustedY;
        int adjustedWidth = WIDTH;
        int adjustedHeight = 255;

        if (client.isResized()) {
            adjustedX = x;
            adjustedWidth = WIDTH + 7;
        }
        adjustedY = client.isResized() && isTransparentChatbox ? y + 7 : y + 284;

        final int filledWidthHP = getBarWidth(maxHP, currentHP, adjustedWidth);
        final int filledHeightHP = getBarHeight(maxHP, currentHP, adjustedHeight);
        final int filledWidthPray = getBarWidth(maxPray, currentPray, adjustedWidth);
        final int filledHeightPray = getBarHeight(maxPray, currentPray, adjustedHeight);
        Color prayerBar = PRAYER_COLOR;
        for (Prayer pray : Prayer.values())
        {
            if (client.isPrayerActive(pray))
            {
                prayerBar = QUICK_PRAYER_COLOR;
            }
        }

        //Render the overlays
        if (config.enableSmall()){
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden())
                drawBars(graphics, adjustedX, adjustedY + 15, adjustedHeight, filledHeightPray, prayerBar);  //config.colorPray());
            drawBars(graphics, adjustedX, adjustedY + (15 * 2), adjustedHeight, filledHeightHP, config.colorHP());
        }
        else
        {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden())
                drawBars(graphics, adjustedX, adjustedY + 15, adjustedWidth, filledWidthPray, prayerBar);  //config.colorPray());
            drawBars(graphics, adjustedX, adjustedY + (15 * 2), adjustedWidth, filledWidthHP, config.colorHP());
        }
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

    private void drawBarLarge(Graphics graphics, int adjustedX, int adjustedY, int adjustedWidth, int fill, Color barColor, Color notchColor) {

        graphics.setColor(BACKGROUND);
        graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, 10 - BORDER_SIZE);
        graphics.fillRect(adjustedX, adjustedY, adjustedWidth, 10);

        graphics.setColor(barColor);
        graphics.fillRect(adjustedX + BORDER_SIZE,
                adjustedY + BORDER_SIZE,
                fill - BORDER_SIZE * 2,
                10 - BORDER_SIZE * 2);

        graphics.setColor(notchColor);
        graphics.fillRect(adjustedX + (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 2 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 3 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 4 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 5 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 6 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 7 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 8 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 9 * (adjustedWidth / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
    }

    private void drawBarSmall(Graphics graphics, int adjustedX, int adjustedY, int adjustedHeight, int fill, Color barColor, Color notchColor) {

        graphics.setColor(BACKGROUND);
        graphics.drawRect(adjustedX, adjustedY, adjustedHeight - BORDER_SIZE, 10 - BORDER_SIZE);
        graphics.fillRect(adjustedX, adjustedY, adjustedHeight, 10);

        graphics.setColor(barColor);
        graphics.fillRect(adjustedX + BORDER_SIZE,
                adjustedY + BORDER_SIZE,
                fill - BORDER_SIZE * 2,
                10 - BORDER_SIZE * 2);

        graphics.setColor(notchColor);
        graphics.fillRect(adjustedX + (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 2 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 3 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 4 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 5 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 6 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 7 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 8 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 9 * (adjustedHeight / 10), adjustedY + 1, 2, 10 - BORDER_SIZE * 2);
    }


    private void renderIcons(Graphics2D graphics, int adjustedX, int adjustedY, BufferedImage image, int counterPadding) {

        if (config.enableSkillIcon())
            if (client.isResized()) {
                graphics.drawImage(image, adjustedX + ICON_AND_COUNTER_OFFSET_X + PADDING, adjustedY + ICON_AND_COUNTER_OFFSET_Y - image.getWidth(null), null);
            }
    }

    private void renderCounters(Graphics2D graphics,int adjustedX, int adjustedY, String counterText, int counterPadding) {
        final int widthOfCounter = graphics.getFontMetrics().stringWidth(counterText);
        final int centerText = (WIDTH - PADDING) / 2 - (widthOfCounter / 2);

        if (config.enableSkillText()) {
            if (client.isResized()) {
                graphics.setFont(FontManager.getRunescapeBoldFont());
                textComponent.setColor(Color.ORANGE);
                textComponent.setOutline(true);
                textComponent.setText(counterText);
                if (config.enableSmall()) {
                    textComponent.setPosition(new Point(10 + centerText + counterPadding, adjustedY + COUNTER_ICON_HEIGHT));
                } else {
                    textComponent.setPosition(new Point(275 + centerText + counterPadding, adjustedY + COUNTER_ICON_HEIGHT));
                }
            }
        }
        if (!config.enableSkillText()) {
            return;
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

    private static int getBarHeight(int base, int current, int size) {
        final double ratio = (double) current / base;

        if (ratio >= 1) {
            return size;
        }

        return (int) Math.round(ratio * size);
    }
}
