/*
 * Copyright (c) 2019, Jos <Malevolentdev@gmail.com>
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

import RHUD.helpers.VertTrackerPlacement;
import net.runelite.api.Skill;
import net.runelite.client.config.*;
import RHUD.helpers.ModeSet;
import RHUD.helpers.BarTextMode;
import lombok.Getter;
import java.awt.*;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
@ConfigGroup(RHUD.RHUD_Config.GROUP)
public interface RHUD_Config extends Config {
	String GROUP = "SB";
	Color COUNTER_COLOR = new Color(255, 255, 255, 255);
	Color EXP_COLOR = new Color(130, 60, 170, 195);
	Color HEALTH_COLOR = new Color(169, 28, 1, 255);
	Color PRAY_COLOR = new Color(38, 157, 157, 255);
	Color RUN_COLOR = new Color(140, 125, 29, 255);
	Color SPECIAL_COLOR = new Color(73, 143, 71, 255);


	enum FontStyle {
		BOLD("Bold", Font.BOLD),
		ITALICS("Italics", Font.ITALIC),
		BOLD_ITALICS("Bold and italics", Font.BOLD | Font.ITALIC),
		DEFAULT("Default", Font.PLAIN);

		String name;
		@Getter
		private int style;

		FontStyle(String name, int style) {
			this.style = style;
			this.name = name;
		}
	}

	@ConfigSection(
			name = "Experience Options",
			description = "Configure the Experience Bar.",
			position = 97
	)
	String expSection = "experience";

	@ConfigSection(
			name = "Status Options",
			description = "Configure the Status Bars.",
			position = 98
	)
	String statusSection = "status";

	@ConfigSection(
			name = "Font and Misc",
			description = "Font options and miscellaneous.",
			position = 99
	)
	String fontSection = "font";

	@ConfigSection(
			name = "Color Options",
			description = "Status bar color config settings.",
			position = 100
	)
	String colorSection = "color";

	@ConfigItem(
			position = 1,
			keyName = "expBar",
			name = "Experience Bar",
			description = "Enables the experience bar.",
			section = expSection
	)
	default boolean XPtracker() { return true; }

	@ConfigItem(
			position = 2,
			keyName = "mostRecentSkill",
			name = "Recent Skill",
			description = "Display the most recent skill trained.",
			section = expSection
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 3,
			keyName = "skill",
			name = "Active Skill",
			description = "Choose which skill to track when Recent Skill setting is disabled.",
			section = expSection
	)
	default Skill skill()
	{
		return Skill.ATTACK;
	}

	@Alpha
	@ConfigItem(
			position = 4,
			keyName = "xpbarColor",
			name = "Experience Color",
			description = "Configures the color of the Experience bar",
			section = expSection
	)
	default Color colorXP()
	{
		return EXP_COLOR;
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "xpbarNotchColor",
			name = "Segment Color",
			description = "Configures the color of the experience segments.",
			section = expSection
	)
	default Color colorXPNotches()
	{
		return new Color(0x6C6C6C);
	}

	@ConfigItem(
			position = 6,
			keyName = "trackerWidth",
			name = "XP Tracker Width",
			description = "The width of the xp tracker.",
			section = expSection
	)
	default int trackerWidth() {
		return 125;
	}

	@ConfigItem(
			position = 7,
			keyName = "enableTip",
			name = "XP Tracker",
			description = "Displays the experience tracker info panel.",
			section = expSection
	)
	default boolean enableTip()
	{
		return false;
	}

	@ConfigItem(
			position = 8,
			keyName = "actionsNeeded",
			name = "Actions Needed",
			description = "Shows the number of actions needed to level-up.",
			section = expSection
	)
	default boolean actionsNeeded()
	{
		return true;
	}

	@ConfigItem(
			position = 9,
			keyName = "xpNeeded",
			name = "Exp Needed",
			description = "Shows the number of xp needed to level-up.",
			section = expSection
	)
	default boolean xpNeeded()
	{
		return true;
	}

	@ConfigItem(
			position = 10,
			keyName = "xpHour",
			name = "Exp/hr",
			description = "Shows Experience per hour.",
			section = expSection
	)
	default boolean xpHour()
	{
		return true;
	}

	@ConfigItem(
			position = 11,
			keyName = "showTTG",
			name = "Time to Level",
			description = "Shows the amount of time until goal lvl reached.",
			section = expSection
	)
	default boolean showTTG()
	{
		return true;
	}

	@ConfigItem(
			position = 12,
			keyName = "showPercent",
			name = "Percent",
			description = "Shows the percentage leveled.",
			section = expSection
	)
	default boolean showPercent()
	{
		return true;
	}

	@ConfigItem(
			position = 13,
			keyName = "placement",
			name = "Position",
			description = "Sets the position of the tracker when in vertical enabled.",
			section = expSection
	)
	default VertTrackerPlacement placement() {
		return VertTrackerPlacement.Tracker_Top;
	}

	@ConfigItem(
			position = 14,
			keyName = "bar1BarMode",
			name = "Bar One",
			description = "Configures the first status bar",
			section = statusSection
	)
	default ModeSet bar1BarMode() {
		return ModeSet.HITPOINTS;
	}

	@ConfigItem(
			position = 15,
			keyName = "bar2BarMode",
			name = "Bar Two",
			description = "Configures the second status bar",
			section = statusSection
	)
	default ModeSet bar2BarMode() {
		return ModeSet.PRAYER;
	}

	@ConfigItem(
			position = 16,
			keyName = "bar3BarMode",
			name = "Bar Three",
			description = "Configures the third status bar",
			section = statusSection
	)
	default ModeSet bar3BarMode() {
		return ModeSet.RUN_ENERGY;
	}


	@ConfigItem(
			position = 17,
			keyName = "bar4BarMode",
			name = "Bar Four",
			description = "Configures the fourth status bar",
			section = statusSection
	)
	default ModeSet bar4BarMode() {
		return ModeSet.SPECIAL_ATTACK;
	}

	@Range(
			min = RHUD_StatusRender.MIN_WIDTH,
			max = RHUD_StatusRender.MAX_WIDTH
	)
	@ConfigItem(
			position = 18,
			keyName = "barWidth",
			name = "Bar Width",
			description = "The width of the status bars in the modern resizeable layout.",
			section = statusSection
	)
	default int barWidth() {
		return 252;
	}

	@ConfigItem(
			position = 19,
			keyName = "vertBars",
			name = "Vertical Bars",
			description = "Displays vertical bars.",
			section = statusSection
	)
	default boolean vertBars() { return false ; }

	@ConfigItem(
			position = 20,
			keyName = "2x2Bars",
			name = "2x2 Bars",
			description = "Displays bars stacked 2 x 2. By request from T...",
			section = statusSection
	)
	default boolean SidebySide() { return false ; }

	@ConfigItem(
			position = 21,
			keyName = "textMode",
			name = "Icon & Text",
			description = "Sets the position of the text and the icon.",
			section = statusSection
	)
	default BarTextMode textMode() {
		return BarTextMode.Both_Bottom;
	}

	@ConfigItem(
			position = 22,
			keyName = "enableText",
			name = "Show Text",
			description = "Show # value on the status bars.",
			section = statusSection
	)
	default boolean enableCounter() {
		return true;
	}

	@ConfigItem(
			position = 23,
			keyName = "enableSkillIcon",
			name = "Show Icons",
			description = "Adds Icon to the status bars.",
			section = statusSection
	)
	default boolean enableSkillIcon() {
		return true;
	}

	@ConfigItem(
			position = 24,
			keyName = "enableRestorationBars",
			name = "Show Restores",
			description = "Highlights status bar with consumable regen amount.",
			section = statusSection
	)
	default boolean enableRestorationBars() {
		return true;
	}

	@ConfigItem(
			position = 25,
			keyName = "fontName",
			name = "Font",
			description = "Name of the font to use for XP drops. Leave blank to use RuneLite setting.",
			section = fontSection
	)
	default String fontName() {
		return "Impact";
	}

	@ConfigItem(
			position = 26,
			keyName = "fontStyle",
			name = "Font style",
			description = "Style of the font to use for XP drops. Only works with custom font.",
			section = fontSection
	)
	default FontStyle fontStyle() {
		return RHUD.RHUD_Config.FontStyle.DEFAULT;
	}

	@ConfigItem(
			position = 27,
			keyName = "fontSize",
			name = "Font size",
			description = "Size of the font to use for XP drops. Only works with custom font.",
			section = fontSection
	)
	default int fontSize()
	{
		return 12;
	}


	@ConfigItem(
			position = 28,
			keyName = "hideAfterCombatDelay",
			name = "Hide after combat delay",
			description = "Amount of ticks before hiding status bars after no longer in combat. 0 = always show status bars.",
			section = fontSection
	)
	@Units(Units.TICKS)
	default int hideAfterCombatDelay() {
		return 0;
	}


	@Alpha
	@ConfigItem(
			position = 29,
			keyName = "counterColor",
			name = "Text Color",
			description = "Configures the color of the counter Text.",
			section = colorSection
	)
	default Color counterColor() {
		return COUNTER_COLOR;
	}

	@Alpha
	@ConfigItem(
			position = 30,
			keyName = "colorHealthBar",
			name = "Health Color",
			description = "Configures the color of the health bar.",
			section = colorSection
	)
	default Color colorHealthBar()
	{
		return (HEALTH_COLOR);
	}

	@Alpha
	@ConfigItem(
			position = 31,
			keyName = "colorPrayBar",
			name = "Pray Color",
			description = "Configures the color of the prayer bar.",
			section = colorSection
	)
	default Color colorPrayBar()
	{
		return (PRAY_COLOR);
	}

	@Alpha
	@ConfigItem(
			position = 32,
			keyName = "colorRunBar",
			name = "Run Color",
			description = "Configures the color of the energy bar.",
			section = colorSection
	)
	default Color colorRunBar()
	{
		return (RUN_COLOR);
	}

	@Alpha
	@ConfigItem(
			position = 33,
			keyName = "colorSpecialBar",
			name = "Special Color",
			description = "Configures the color of the special attack bar.",
			section = colorSection
	)
	default Color colorSpecialBar()
	{
		return (SPECIAL_COLOR);
	}
}
