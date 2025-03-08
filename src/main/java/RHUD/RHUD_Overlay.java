/*
 * Copyright (c) 2019, Jos <Malevolentdev@gmail.com>
 * Copyright (c) 2019, Rheon <https://github.com/Rheon-D>
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
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import RHUD.helpers.OverlayManager;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.MenuEntry;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.ImageUtil;
import RHUD.helpers.ModeSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.overlay.components.TitleComponent;
import static RHUD.helpers.VertTrackerPlacement.Tracker_Bottom;
import static RHUD.helpers.VertTrackerPlacement.Tracker_Top;


public class RHUD_Overlay extends OverlayPanel {

    private static final Color ACTIVE_PRAYER_COLOR = new Color(43, 234, 159, 255);
    private static final Color POISONED_COLOR = new Color(0, 145, 0, 255);
    private static final Color VENOMED_COLOR = new Color(0, 65, 0, 255);
    private static final Color HEAL_COLOR = new Color(255, 112, 6, 150);
    private static final Color PRAYER_HEAL_COLOR = new Color(57, 255, 186, 75);
    private static final Color ENERGY_HEAL_COLOR = new Color(199, 118, 0, 218);
    private static final Color RUN_STAMINA_COLOR = new Color(168, 124, 62, 255);
    private static final Color SPECIAL_ACTIVE = new Color(4, 173, 1, 255);
    private static final Color RUN_ACTIVE = new Color(185, 187, 0, 255);
    private static final Color DISEASE_COLOR = new Color(176, 134, 53, 255);
    private static final Color PARASITE_COLOR = new Color(196, 62, 109, 255);
    private static final Color BACKGROUND = new Color(0, 0, 0, 120);
    private static final int BORDER_SIZE = 1;
    private static final int IMAGE_SIZE = 17;
    private static final Dimension ICON_DIMENSIONS = new Dimension(18, 17);
    private static final int MAX_SPECIAL_ATTACK_VALUE = 100;
    private static final int MAX_RUN_ENERGY_VALUE = 100;

    private final Client client;
    private final RHUD_Plugin plugin;
    private final RHUD_Config config;
    private final ItemStatChangesService itemStatService;
    private final SpriteManager spriteManager;

    private final Image prayerIcon;
    private final Image heartDisease;
    private final Image heartPoison;
    private final Image heartVenom;
    private Image heartIcon;
    private Image specialIcon;
    private Image energyIcon;
    private final XpTrackerService xpTrackerService;

    private int currentXP;
    private int currentLevel;
    private int nextLevelXP;
    private final Map<ModeSet, RHUD_StatusRender> modeSet = new EnumMap<>(ModeSet.class);

    private final RHUD.helpers.FontHandler FontHandler = new RHUD.helpers.FontHandler();

    @Inject
    private RHUD_Overlay(Client client, RHUD_Plugin plugin, RHUD_Config config, SkillIconManager skillIconManager, ItemStatChangesService itemstatservice, SpriteManager spriteManager, OverlayManager overlayManager, XpTrackerService xpTrackerService) {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.itemStatService = itemstatservice;
        this.spriteManager = spriteManager;
        this.xpTrackerService = xpTrackerService;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        prayerIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.PRAYER, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
        heartDisease = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
        heartPoison = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
        heartVenom = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);

        initModes();
    }

    private void initModes() {
        modeSet.put(ModeSet.DISABLED, null);
        modeSet.put(ModeSet.HITPOINTS, new RHUD_StatusRender(
                () -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.HITPOINTS),
                () -> client.getBoostedSkillLevel(Skill.HITPOINTS),
                () -> getRestoreValue(Skill.HITPOINTS.getName()),
                () ->
                {
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

                    return config.colorHealthBar();
                },
                () -> HEAL_COLOR,
                () ->
                {
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
                }
        ));
        modeSet.put(ModeSet.PRAYER, new RHUD_StatusRender(
                () -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.PRAYER),
                () -> client.getBoostedSkillLevel(Skill.PRAYER),
                () -> getRestoreValue(Skill.PRAYER.getName()),
                () ->
                {
                    Color prayerColor = config.colorPrayBar();

                    for (Prayer pray : Prayer.values()) {
                        if (client.isPrayerActive(pray)) {
                            prayerColor = ACTIVE_PRAYER_COLOR;
                            break;
                        }
                    }

                    return prayerColor;
                },
                () -> PRAYER_HEAL_COLOR,
                () -> prayerIcon
        ));
        modeSet.put(ModeSet.RUN_ENERGY, new RHUD_StatusRender(
                () -> MAX_RUN_ENERGY_VALUE,
                () -> client.getEnergy() / 100,
                () -> getRestoreValue("Run Energy"),
                () ->
                {
                    if (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
                        return RUN_STAMINA_COLOR;
                    } else if (client.getVarpValue(173) == 1) {
                        return RUN_ACTIVE;
                    } else {
                        return config.colorRunBar();
                    }
                },
                () -> ENERGY_HEAL_COLOR,
                () -> energyIcon
        ));
        modeSet.put(ModeSet.SPECIAL_ATTACK, new RHUD_StatusRender(
                () -> MAX_SPECIAL_ATTACK_VALUE,
                () -> client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10,
                () -> 0,
                () ->
                {
                    if (client.getVarpValue(301) == 1) {
                        return SPECIAL_ACTIVE;
                    } else {
                        return config.colorSpecialBar();
                    }
                },
                config::colorSpecialBar,
                () -> specialIcon
        ));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Dimension dimension = null;
        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        client.getWidget(WidgetInfo.FIXED_VIEWPORT_INVENTORY_CONTAINER);
        if (bankContainer == null || bankContainer.isHidden()) {
            int width = RHUD_StatusRender.DEFAULT_WIDTH, adjustedX = 0, adjustedY = 0;
            int height = RHUD_StatusRender.DEFAULT_HEIGHT;
            if (config.XPtracker()) {
                if (config.mostRecentSkill()) {
                    renderBar(graphics);
                }
                if (!config.mostRecentSkill()) {
                    renderBarRecent(graphics);
                }
            }
            if (config.enableTip()) {
                renderTrackerOverlay(graphics);
                renderTrackerIcons(config, graphics, width, height, new SkillIconManager());
            }
            if (!config.vertBars() && !config.SidebySide()) {
                dimension = new Dimension(config.barWidth(), width);
            } else if (config.vertBars()) {
                dimension = new Dimension(width, config.barWidth());
            } else {
                dimension = new Dimension(500, width);
            }
            if (!plugin.isBarsDisplayed()) {
                return null;
            }
            FontHandler.updateFont(config.fontName(), config.fontSize(), config.fontStyle());
            FontHandler.handleFont(graphics);

            buildIcons();

            RHUD_StatusRender Bar1 = modeSet.get(config.bar1BarMode());
            RHUD_StatusRender Bar2 = modeSet.get(config.bar2BarMode());
            RHUD_StatusRender Bar3 = modeSet.get(config.bar3BarMode());
            RHUD_StatusRender Bar4 = modeSet.get(config.bar4BarMode());

            if (Bar1 != null) {
                if (config.vertBars()) {
                    Bar1.renderBar(config, graphics, adjustedX + 11, adjustedY, width);
                    dimension = new Dimension(width + 11, config.barWidth());
                } else if (config.SidebySide()) {
                    Bar1.renderBar(config, graphics, adjustedX, adjustedY + 20, width);
                    dimension = new Dimension(500, width + 20);
                } else {
                    Bar1.renderBar(config, graphics, adjustedX, adjustedY + 20, width);
                    dimension = new Dimension(config.barWidth(), width + 20);
                }
            }

            if (Bar2 != null) {
                if (config.vertBars()) {
                    Bar2.renderBar(config, graphics, adjustedX + 31, adjustedY, width);
                    dimension = new Dimension(width + 31, config.barWidth());
                } else if (config.SidebySide()) {
                    Bar2.renderBar(config, graphics, adjustedX + 250, adjustedY + 20, width);
                    dimension = new Dimension(500, width + 20);
                } else {
                    Bar2.renderBar(config, graphics, adjustedX, adjustedY + 40, width);
                    dimension = new Dimension(config.barWidth(), width + 40);
                }
            }

            if (Bar3 != null) {
                if (config.vertBars()) {
                    Bar3.renderBar(config, graphics, adjustedX + 51, adjustedY, width);
                    dimension = new Dimension(width + 51, config.barWidth());
                } else if (config.SidebySide()) {
                    Bar3.renderBar(config, graphics, adjustedX, adjustedY + 40, width);
                    dimension = new Dimension(500, width + 40);
                } else {
                    Bar3.renderBar(config, graphics, adjustedX, adjustedY + 60, width);
                    dimension = new Dimension(config.barWidth(), width + 60);
                }
            }

            if (Bar4 != null) {
                if (config.vertBars()) {
                    Bar4.renderBar(config, graphics, adjustedX + 71, adjustedY, width);
                    dimension = new Dimension(width + 71, config.barWidth());
                } else if (config.SidebySide()) {
                    Bar4.renderBar(config, graphics, adjustedX + 250, adjustedY + 40, width);
                    dimension = new Dimension(500, width + 40);
                } else {
                    Bar4.renderBar(config, graphics, adjustedX, adjustedY + 80, width);
                    dimension = new Dimension(config.barWidth(), width + 80);
                }
            }
        }
        return dimension;
    }


    private int getRestoreValue(String skill) {
        final MenuEntry[] menu = client.getMenuEntries();
        final int menuSize = menu.length;
        if (menuSize == 0) {
            return 0;
        }

        final MenuEntry entry = menu[menuSize - 1];
        final Widget widget = entry.getWidget();
        int restoreValue = 0;

        if (widget != null && widget.getId() == WidgetInfo.INVENTORY.getId()) {
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

    private void buildIcons() {
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
        return client.getWidget(WidgetInfo.LMS_KDA) != null;
    }

    private void renderBar(Graphics2D graphics) {
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
            int adjustedY;
            int adjustedWidth;
            adjustedY = 8;

            if (config.SidebySide() && !config.vertBars()) {
                adjustedWidth = 500;
            } else {
                adjustedWidth = config.barWidth();
            }

            final int filledHeight = getBarHeight(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);

            //Render the overlay
            Color barColor = config.colorXP();

            Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
            if (bankContainer == null || bankContainer.isHidden()) {
                drawBarLarge(graphics, 0, adjustedY, adjustedWidth, filledHeight, barColor, config.colorXPNotches());
            }
        }
    }

    private void drawBarLarge(Graphics graphics, int adjustedX, int adjustedY, int adjustedWidth, int fill, Color barColor, Color notchColor) {

        Skill skill;
        int adjustedHeight;
        if (config.mostRecentSkill()) {
            if (plugin.getCurrentSkill() == null) {
                skill = config.skill();
            } else skill = plugin.getCurrentSkill();
            currentXP = client.getSkillExperience(skill);
        }
        currentLevel = Experience.getLevelForXp(currentXP);
        nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
        int currentLevelXP = Experience.getXpForLevel(currentLevel);
        final int filledWidth = getBarHeight(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);
        if (config.SidebySide() && !config.vertBars()) {
            adjustedHeight = 500;
        } else {
            adjustedHeight = config.barWidth();
        }

        graphics.setColor(BACKGROUND);

        if (config.vertBars()) {
            graphics.drawRect(adjustedX, adjustedY - 8, 12 - BORDER_SIZE, adjustedWidth - BORDER_SIZE);
            graphics.fillRect(adjustedX, adjustedY - 8, 12, adjustedWidth);
        } else {
            graphics.drawRect(adjustedX, adjustedY, adjustedWidth - BORDER_SIZE, 12 - BORDER_SIZE);
            graphics.fillRect(adjustedX, adjustedY, adjustedWidth, 12);
        }

        graphics.setColor(barColor);
        if (config.vertBars()) {
            graphics.fillRect(adjustedX + BORDER_SIZE, adjustedY + BORDER_SIZE + (config.barWidth() - filledWidth) - 8, 12 - BORDER_SIZE * 2, filledWidth - BORDER_SIZE * 2);
        } else {
            graphics.fillRect(adjustedX + BORDER_SIZE, adjustedY + BORDER_SIZE, filledWidth - BORDER_SIZE * 2, 12 - BORDER_SIZE * 2);
        }

        graphics.setColor(notchColor);
        if (!config.vertBars()) {
            graphics.fillRect(adjustedX + (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 2 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 3 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 4 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 5 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 6 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 7 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 8 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
            graphics.fillRect(adjustedX + 9 * (adjustedHeight / 10), adjustedY + 1, 1, 12 - BORDER_SIZE * 2);
        } else {
            graphics.fillRect(adjustedX + 1, adjustedY + (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 2 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 3 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 4 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 5 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 6 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 7 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 8 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
            graphics.fillRect(adjustedX + 1, adjustedY + 9 * (adjustedHeight / 10) - 8, 12 - BORDER_SIZE * 2, 1);
        }
    }


    private void renderBarRecent(Graphics2D graphics) {
        //Get info for experience
        Skill skill;
        skill = config.skill();
        currentXP = client.getSkillExperience(skill);
        currentLevel = Experience.getLevelForXp(currentXP);
        nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
        int currentLevelXP = Experience.getXpForLevel(currentLevel);

        //Calc starting position for bar
        int adjustedX = 0;
        int adjustedY = 8;
        int adjustedWidth;

        if (config.SidebySide() && !config.vertBars()) {
            adjustedWidth = 500;
        } else {
            adjustedWidth = config.barWidth();
        }

        final int filledWidth = getBarWidth(nextLevelXP - currentLevelXP, currentXP - currentLevelXP, adjustedWidth);

        //Render the overlay
        Color barColor = config.colorXP();
        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankContainer == null || bankContainer.isHidden()) {
            drawBarLarge(graphics, adjustedX, adjustedY, adjustedWidth, filledWidth, barColor, config.colorXPNotches());
        }
    }

    private void renderTrackerOverlay(Graphics2D graphics) {
        Skill skill;
        String name;

        if (config.mostRecentSkill() && plugin.getCurrentSkill() == null || !config.mostRecentSkill()) {
                skill = config.skill();
                name = config.skill().getName();
            } else if (config.mostRecentSkill()) {
                skill = plugin.getCurrentSkill();
                name = plugin.getCurrentSkill().getName();
            } else {
            skill = plugin.getCurrentSkill();
            name = plugin.getCurrentSkill().getName();

        }
                currentXP = client.getSkillExperience(skill);
                currentLevel = Experience.getLevelForXp(currentXP);
                nextLevelXP = Experience.getXpForLevel(currentLevel + 1);
                final int counterLevel = client.getBoostedSkillLevel(skill);
                final String counterLevelText = Integer.toString(counterLevel);
                int startXp = xpTrackerService.getStartGoalXp(skill);
                int goalXp = xpTrackerService.getEndGoalXp(skill);
                int xpNeeded = nextLevelXP - currentXP;
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



            FontHandler.updateFont(config.fontName(), 14, config.fontStyle());
            FontHandler.handleFont(graphics);
            panelComponent.setPreferredSize(new Dimension(config.trackerWidth(), 100));
            panelComponent.getChildren().clear();
            if (config.placement() == Tracker_Bottom && config.vertBars()) {
                panelComponent.setPreferredLocation(new Point(0, config.barWidth() + 4));
            } else if (config.placement() == Tracker_Top && config.vertBars()) {
                panelComponent.setPreferredLocation(new Point(0, -140));
            } else if (config.SidebySide()) {
                panelComponent.setPreferredLocation(new Point(504, 8));
            } else {
                panelComponent.setPreferredLocation(new Point(config.barWidth() + 4, 8));
            }
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Xp Tracking")
                    .color(BODY_COLOUR)
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
                    }
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
                    }
                }

                if (config.showTTG()) {
                    String timeLeft = xpTrackerService.getTimeTilGoal(skill);
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Time till lvl:")
                            .leftColor(LEFT_TEXT)
                            .right(timeLeft)
                            .build());
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
            panelComponent.render(graphics);
        }

    private void renderTrackerIcons(RHUD_Config config, Graphics2D graphics, int width, int height, SkillIconManager skillIconManager) {
        // Icons and counters overlap the bar at small widths, so they are not drawn when the bars are too small

        final boolean skillIconEnabled = config.enableTip();
        Skill currentSkill;
        if (skillIconEnabled) {
            if (!config.mostRecentSkill()) {
                currentSkill = config.skill();
            } else if (plugin.getCurrentSkill() == null) {
                    currentSkill = config.skill();
                } else {
                    currentSkill = plugin.getCurrentSkill();
                }
                    final Image skill = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(currentSkill, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
                    final int xDraw = 5 + (width / 2) - (skill.getWidth(null) / 2);
                    final int yDraw = 5 + (width / 2) - (skill.getWidth(null) / 2) + 504;
                        if (config.placement() == Tracker_Bottom && config.vertBars()) {
                            graphics.drawImage(skill, xDraw, config.barWidth() + 8, null);
                        } else if (config.placement() == Tracker_Top && config.vertBars()) {
                            graphics.drawImage(skill, xDraw, -136, null);
                        } else if (config.SidebySide()) {
                            graphics.drawImage(skill, yDraw , 12, null);
                        } else {
                            graphics.drawImage(skill, config.barWidth() + 10, 12, null);
                        }
                    }
                }



    private double getSkillProgress(int startXp, int currentXp, int goalXp)
    {
        double xpGained = currentXp - startXp;
        double xpGoal = goalXp - startXp;

        return ((xpGained / xpGoal) * 100);
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