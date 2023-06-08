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
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.AlternateSprites;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class XPBarOverlay extends Overlay {

    private static final int IMAGE_SIZE = 14;
    private final RastaXPConfig config;
    private final Client client;
    private static final Logger logger = LoggerFactory.getLogger(XPBarOverlay.class);
    private static final Color BACKGROUND = new Color(0, 0, 0, 120);
    private static final int WIDTH = 512;
    static final int HEIGHT = 18;
    private static final int BORDER_SIZE = 2;
    private static final Color PRAYER_COLOR = new Color(50, 200, 200, 175);
    private static final Color QUICK_PRAYER_COLOR = new Color(57, 255, 186, 225);
    private static final Color TITLE_COLOUR = new Color(255, 152, 31, 165);

    private static final Color BODY_COLOUR = new Color(255, 190, 30, 225);

    private static final Color POISONED_COLOR = new Color(0, 145, 0, 195);
    private static final Color VENOMED_COLOR = new Color(0, 65, 0, 195);
    private static final Color HEALTH_COLOR = new Color(225, 35, 0, 195);
    private static final Color DISEASE_COLOR = new Color(255, 193, 75, 181);
    private static final Color RUN_COLOR = new Color(199, 174, 0, 195);

    private static final Color RUN_ACTIVE = new Color(255, 216, 0, 195);
    private static final Color RUN_STAMINA_ACTIVE = new Color(160, 124, 72, 255);
    private static final Color SPECIAL_COLOR = new Color(106, 203, 103, 195);

    private static final Color SPECIAL_ACTIVE = new Color(5, 252, 1, 195);

    private static final int HEALTH_LOCATION_X = 0;
    private static final int PRAYER_LOCATION_X = 1;
    private static final int RUN_LOCATION_X = 2;
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
    private final SpriteManager spriteManager;



    @Inject
    private XPBarOverlay(Client client, RastaXPPlugin plugin, RastaXPConfig config, SkillIconManager skillIconManager, TooltipManager tooltipManager, SpriteManager spriteManager) {
        super(plugin);
        this.skillIconManager = skillIconManager;
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.tooltipManager = tooltipManager;
        this.spriteManager = spriteManager;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "XPBar overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();
        if (config.XPtracker()) {
            String name;
            Skill skill;
            if (plugin.getCurrentSkill() == null) {
                skill = config.skill();
                name = config.skill().getName();
            } else {
                name = plugin.getCurrentSkill().getName();
                skill = plugin.getCurrentSkill();
            }
            int xpNeeded = nextLevelXP - currentXP;
            final int counterLevel = client.getBoostedSkillLevel(skill);
            final String counterLevelText = Integer.toString(counterLevel);
            NumberFormat f = NumberFormat.getNumberInstance(Locale.US);
            StringBuilder sb = new StringBuilder();
            sb.append("Current: ").append(f.format(currentXP));
            sb.append("</br>");
            sb.append("Next Lv: ").append(f.format(nextLevelXP));
            sb.append("</br>");
            sb.append("Needed: ").append(f.format(xpNeeded));
            sb.append("</br>");
            sb.append("Level: ").append(counterLevelText);
            if (config.enableTip()) {
                if (this.getBounds().contains(
                        client.getMouseCanvasPosition().getX(),
                        client.getMouseCanvasPosition().getY())) {
                    tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(name + " " + "XP", TITLE_COLOUR)));
                    tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag(sb.toString(), BODY_COLOUR)));

                }
            }
            if (config.mostRecentSkill()) {
                renderBar(graphics, -1, +5);
            }
            if (!config.mostRecentSkill()) {
                renderBarRecent(graphics, -1, +5);
            }
        }
        if (config.displayHealthAndPrayer()) {
            final BufferedImage healthPoison = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART), IMAGE_SIZE, IMAGE_SIZE);
            final BufferedImage healthVenom = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART), IMAGE_SIZE, IMAGE_SIZE);
            final BufferedImage healthDisease = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART), IMAGE_SIZE, IMAGE_SIZE);
            final BufferedImage heartImage = loadAndResize(SpriteID.MINIMAP_ORB_HITPOINTS_ICON);
            final BufferedImage prayerImage = ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.PRAYER, true), IMAGE_SIZE, IMAGE_SIZE);

            final int counterHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
            final int counterPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
            final int poisonState = client.getVarpValue(VarPlayer.POISON);
            final String counterHealthText = Integer.toString(counterHealth);
            final String counterPrayerText = Integer.toString(counterPrayer);

            if (config.enableSmall()) {
                renderThreeBars(graphics, -1, 1);
                renderIcons(graphics, +105, +15, prayerImage, PRAYER_LOCATION_X);
                renderCounters(graphics, -118, +17, counterPrayerText, PRAYER_LOCATION_X);
                renderCounters(graphics, -118, +35, counterHealthText, HEALTH_LOCATION_X);
                if (poisonState >= 1000000) {
                    renderIcons(graphics, +105, 16 * 2, healthPoison, HEALTH_LOCATION_X);
                }
                if (poisonState >= 1000000) {
                    renderIcons(graphics, +105, 16 * 2, healthVenom, HEALTH_LOCATION_X);
                }
                if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
                    renderIcons(graphics, +105, 16 * 2, healthDisease, HEALTH_LOCATION_X);
                }
                if (poisonState <= 0) {
                    renderIcons(graphics, +105, 16 * 2, heartImage, HEALTH_LOCATION_X);
                }
            } else {
                renderThreeBars(graphics, -1, 1);
                renderIcons(graphics, +230, +15, prayerImage, PRAYER_LOCATION_X);
                renderCounters(graphics, 15, +17, counterPrayerText, PRAYER_LOCATION_X);
                renderCounters(graphics, 15, +35, counterHealthText, HEALTH_LOCATION_X);
                if (poisonState >= 1000000) {
                    renderIcons(graphics, +230, 16 * 2, healthPoison, HEALTH_LOCATION_X);
                }
                if (poisonState >= 1000000) {
                    renderIcons(graphics, +230, 16 * 2, healthVenom, HEALTH_LOCATION_X);
                }
                if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
                    renderIcons(graphics, +230, 16 * 2, healthDisease, HEALTH_LOCATION_X);
                }
                if (poisonState <= 0) {
                    renderIcons(graphics, +230, 16 * 2, heartImage, HEALTH_LOCATION_X);
                }
            }
        }
        if (config.displayRun()) {
            final BufferedImage runImage = loadAndResize(SpriteID.MINIMAP_ORB_WALK_ICON);
            final int counterEnergy = client.getEnergy() / 100;
            final String counterEnergyText = Integer.toString(counterEnergy);
            int adjustedY;
            if (config.displayHealthAndPrayer()) {
                adjustedY = 51;
            } else {
                adjustedY = 15;
            }
            if (config.enableSmall()) {
                renderRunBar(graphics, -1, 1, 10);
                renderCounters(graphics, -118, adjustedY + 2, counterEnergyText, RUN_LOCATION_X);
                renderIcons(graphics, +105, adjustedY, runImage, RUN_LOCATION_X);
            } else {
                renderRunBar(graphics, -1, 1, 10);
                renderCounters(graphics, 15, adjustedY + 2, counterEnergyText, PRAYER_LOCATION_X);
                renderIcons(graphics, +230, adjustedY, runImage, RUN_LOCATION_X);
            }
        }

        if (config.displaySpecial()) {
            final BufferedImage specialImage = loadAndResize(SpriteID.MINIMAP_ORB_SPECIAL_ICON);
            final int counterSpecial = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
            final String counterSpecialText = Integer.toString(counterSpecial);

            int adjustedY = 0;
            adjustedY = adjustedY +15;

            if (config.displayRun()) {
                adjustedY = adjustedY + 18;
            }
            if (config.displayHealthAndPrayer()) {
                if (config.displayRun() && config.displayHealthAndPrayer()) {
                    adjustedY = adjustedY + 36;
                } else if (config.displayHealthAndPrayer()) {
                    adjustedY = adjustedY + 36;
                }
            }

            if (config.enableSmall()){
                renderSpecialBar(graphics,-1, 1, 10);
                renderCounters(graphics, -118, adjustedY+2, counterSpecialText, RUN_LOCATION_X);
                renderIcons(graphics, +105, adjustedY, specialImage, RUN_LOCATION_X);
            } else {
                renderSpecialBar(graphics, -1, 1, 10);
                renderCounters(graphics, 15, adjustedY+2, counterSpecialText, PRAYER_LOCATION_X);
                renderIcons(graphics, +230, adjustedY, specialImage, RUN_LOCATION_X);
            }
        }
        if (config.enableSmall()) {
            return new Dimension(256, 92);
        } else {
            return new Dimension(520, 92);
        }
    }


    private void renderBarRecent(Graphics2D graphics, int x, int y) {
        //Get info for experience
        Skill skill;
        skill = config.skill();
        currentXP = client.getSkillExperience(skill);
        currentLevel = Experience.getLevelForXp(currentXP);
        nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
        int currentLevelXP = Experience.getXpForLevel(currentLevel);

        //Calc starting position for bar
        int adjustedX = x;
        int adjustedY;
        int adjustedWidth = WIDTH;
        int adjustedHeight = 255;

        if (client.isResized()) {
            adjustedX = 0;
            adjustedWidth = WIDTH + 7;
        }
        adjustedY = y;

        final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
        final int filledHeight = getBarHeight(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedHeight);

        //Format tooltip display

        //Render the overlay
        Color barColor = config.colorXP();
        if (config.enableSmall()) {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBarSmall(graphics, adjustedX, adjustedY, adjustedHeight, filledHeight, barColor, config.colorXPNotches());
            }
        } else {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBarLarge(graphics, adjustedX, adjustedY, adjustedWidth, filledWidth, barColor, config.colorXPNotches());
            }
        }
    }

    private void renderBar(Graphics2D graphics, int x, int y) {
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

            //Calc starting position for bar
            int adjustedX = x;
            int adjustedY;
            int adjustedWidth = WIDTH;
            int adjustedHeight = 255;

            if (client.isResized()) {
                adjustedX = x;
                adjustedWidth = WIDTH + 7;
            }
            adjustedY = y;

            final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
            final int filledHeight = getBarHeight(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedHeight);

            //Format tooltip display

            //Render the overlay
            Color barColor = config.colorXP();

            if (config.enableSmall()) {
                Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null || bankContainer.isHidden()) {
                    drawBarLarge(graphics, adjustedX, adjustedY, adjustedHeight, filledHeight, barColor, config.colorXPNotches());
                }
            } else {
                Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null || bankContainer.isHidden()) {
                    drawBarLarge(graphics, adjustedX, adjustedY, adjustedWidth, filledWidth, barColor, config.colorXPNotches());
                }
            }
        }
    }

    private void renderThreeBars(Graphics2D graphics, int x, int y) {

        int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
        int currentPray = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPray = client.getRealSkillLevel(Skill.PRAYER);


        //Calc starting positions for bars
        int adjustedX = x;
        int adjustedY;
        int adjustedWidth = WIDTH;
        int adjustedHeight = 255;

        if (client.isResized()) {
            adjustedX = x;
            adjustedWidth = WIDTH + 7;
        }
        adjustedY = y;

        final int filledWidthHP = getBarWidth(maxHP, currentHP, adjustedWidth);
        final int filledHeightHP = getBarHeight(maxHP, currentHP, adjustedHeight);
        final int filledWidthPray = getBarWidth(maxPray, currentPray, adjustedWidth);
        final int filledHeightPray = getBarHeight(maxPray, currentPray, adjustedHeight);
        Color prayerBar = PRAYER_COLOR;
        for (Prayer pray : Prayer.values()) {
            if (client.isPrayerActive(pray)) {
                prayerBar = QUICK_PRAYER_COLOR;
            }
        }

        final int poisonState = client.getVarpValue(VarPlayer.POISON);
        Color healthBar = HEALTH_COLOR;
        if (poisonState > 0 && poisonState < 50) {
            healthBar = POISONED_COLOR;
        }

        if (poisonState >= 1000000) {
            healthBar = VENOMED_COLOR;
        }

        if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
            healthBar = DISEASE_COLOR;
        }
        //Render the overlays
        if (config.enableSmall()) {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBars(graphics, adjustedX, adjustedY + 18, adjustedHeight, filledHeightPray, prayerBar);  //config.colorPray());
                drawBars(graphics, adjustedX, adjustedY + (18 * 2), adjustedHeight, filledHeightHP, healthBar);
            }
        } else {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBars(graphics, adjustedX, adjustedY + 18, adjustedWidth, filledWidthPray, prayerBar);  //config.colorPray());
                drawBars(graphics, adjustedX, adjustedY + (18 * 2), adjustedWidth, filledWidthHP, healthBar);
            }
        }
    }

    private void renderRunBar(Graphics2D graphics, int x, int y, int height) {

        //Get info for experience
        int currentRun = client.getEnergy() / 100;

        //Calc starting positions for bars
        int adjustedX = x;
        int adjustedY;
        int adjustedWidth = WIDTH;
        int adjustedHeight = 255;

        if (client.isResized()) {
            adjustedX = x;
            adjustedWidth = WIDTH + 7;
        }

        if (config.displayHealthAndPrayer()) {
            adjustedY = 1;
        } else {
            adjustedY = -35;
        }


        final int filledWidthHP = getBarWidth(100, currentRun, adjustedWidth);
        final int filledHeightHP = getBarHeight(100, currentRun, adjustedHeight);

        Color runBar = RUN_COLOR;
        if (client.getVarpValue(173) == 1) {
            runBar = RUN_ACTIVE;
        }
        if (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
            runBar = RUN_STAMINA_ACTIVE;
        }

        //Render the overlays
        if (config.enableSmall()){
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBars(graphics, adjustedX, adjustedY + (18 * 3), adjustedHeight, filledHeightHP, runBar);
            }
        }
        else
        {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBars(graphics, adjustedX, adjustedY + (18 * 3), adjustedWidth, filledWidthHP, runBar);
            }
        }
    }

    private void renderSpecialBar(Graphics2D graphics, int x, int y, int height) {

        //Get info for experience
        int currentSpecial = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;

        //Calc starting positions for bars
        int adjustedX = x;
        int adjustedY = 0;
        int adjustedWidth = WIDTH;
        int adjustedHeight = 255;

        if (client.isResized()) {
            adjustedX = x;
            adjustedWidth = WIDTH + 7;
        }
        adjustedY = adjustedY - 35;
        if (config.displayHealthAndPrayer()) {
            if (config.displayRun() && config.displayHealthAndPrayer()) {
                adjustedY = adjustedY + 0;
            }
        }
        if (config.displayRun()) {
                adjustedY = adjustedY + 18;
            }

        if (config.displayHealthAndPrayer()) {
                adjustedY = adjustedY + 36;
            }


            final int filledWidthHP = getBarWidth(100, currentSpecial, adjustedWidth);
            final int filledHeightHP = getBarHeight(100, currentSpecial, adjustedHeight);

            Color specialBar = SPECIAL_COLOR;
            if (client.getVarpValue(301) == 1) {
                specialBar = SPECIAL_ACTIVE;
            }

            //Render the overlays
            if (config.enableSmall()) {
                Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null || bankContainer.isHidden()) {
                    drawBars(graphics, adjustedX, adjustedY + (18 * 3), adjustedHeight, filledHeightHP, specialBar);
                }
            } else {
                Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                if (bankContainer == null || bankContainer.isHidden()) {
                    drawBars(graphics, adjustedX, adjustedY + (18 * 3), adjustedWidth, filledWidthHP, specialBar);
                }
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
        graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, 12 - BORDER_SIZE);
        graphics.fillRect(adjustedX, adjustedY, adjustedWidth, 12);

        graphics.setColor(barColor);
        graphics.fillRect(adjustedX + BORDER_SIZE,
                adjustedY + BORDER_SIZE,
                fill - BORDER_SIZE * 2,
                12 - BORDER_SIZE * 2);

        graphics.setColor(notchColor);
        graphics.fillRect(adjustedX + (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 2 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 3 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 4 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 5 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 6 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 7 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 8 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 9 * (adjustedWidth / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
    }

    private void drawBarSmall(Graphics graphics, int adjustedX, int adjustedY, int adjustedHeight, int fill, Color barColor, Color notchColor) {

        graphics.setColor(BACKGROUND);
        graphics.drawRect(adjustedX, adjustedY, adjustedHeight - BORDER_SIZE, 12 - BORDER_SIZE);
        graphics.fillRect(adjustedX, adjustedY, adjustedHeight, 12);

        graphics.setColor(barColor);
        graphics.fillRect(adjustedX + BORDER_SIZE,
                adjustedY + BORDER_SIZE,
                fill - BORDER_SIZE * 2,
                12 - BORDER_SIZE * 2);

        graphics.setColor(notchColor);
        graphics.fillRect(adjustedX + (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 2 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 3 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 4 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 5 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 6 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 7 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 8 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
        graphics.fillRect(adjustedX + 9 * (adjustedHeight / 10), adjustedY + 1, 2, 12 - BORDER_SIZE * 2);
    }


    private void renderIcons(Graphics2D graphics, int adjustedX, int adjustedY, BufferedImage image, int counterPadding) {

        if (config.enableSkillIcon()) {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            client.getWidget(WidgetInfo.FIXED_VIEWPORT_INVENTORY_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                graphics.drawImage(image, adjustedX + ICON_AND_COUNTER_OFFSET_X + PADDING, adjustedY + ICON_AND_COUNTER_OFFSET_Y - image.getWidth(null), null);
            }
        }
    }

    private void renderCounters(Graphics2D graphics,int adjustedX, int adjustedY, String counterText, int counterPadding) {
        final int widthOfCounter = graphics.getFontMetrics().stringWidth(counterText);
        final int centerText = (WIDTH - PADDING) / 2 - (widthOfCounter / 2);

        if (config.enableSkillText()) {
            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
                graphics.setFont(FontManager.getRunescapeBoldFont());
                textComponent.setColor(Color.ORANGE);
                textComponent.setOutline(true);
                textComponent.setText(counterText);
            if (bankContainer != null && !bankContainer.isHidden()) {
            } else {
                textComponent.setPosition(new Point(adjustedX + centerText + counterPadding, adjustedY + COUNTER_ICON_HEIGHT));
            }
        }
        if (!config.enableSkillText()) {
            return;
        }

        textComponent.render(graphics);
    }

    private BufferedImage loadAndResize(int spriteId)
    {
        BufferedImage image = spriteManager.getSprite(spriteId, 0);
        if (image == null)
        {
            return null;
        }

        return ImageUtil.resizeCanvas(image, 15, IMAGE_SIZE);
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
