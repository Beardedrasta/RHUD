/*
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

import RHUD.helpers.*;
import lombok.Getter;
import net.runelite.client.config.*;
import net.runelite.api.Skill;

import java.awt.*;
@ConfigGroup(RHUD.RHUD_Config.GROUP)
public interface RHUD_Config extends Config {

	String GROUP = "RHUD";
	Color HEALTH_COLOR = new Color(169, 28, 1, 255);
	Color SPECIAL_COLOR = new Color(73, 143, 71, 255);
	Color RUN_COLOR = new Color(140, 125, 29, 255);
	Color PRAY_COLOR = new Color(38, 157, 157, 255);
	Color EXP_COLOR = new Color(130, 60, 170, 195);

	enum FontStyle {
		BOLD("Bold", Font.BOLD),
		ITALICS("Italics", Font.ITALIC),
		BOLD_ITALICS("Bold and italics", Font.BOLD | Font.ITALIC),
		DEFAULT("Default", Font.PLAIN);

		String name;
		@Getter
		private final int style;

		FontStyle(String name, int style) {
			this.style = style;
			this.name = name;
		}
	}



	@ConfigSection(
			name = "Status Options",
			description = "Configure the Status Bars.",
			position = 97
	)
	String statusSection = "status";

	@ConfigSection(
			name = "Font and Misc",
			description = "Font options and miscellaneous.",
			position = 98
	)
	String fontSection = "font";

	@ConfigSection(
			name = "Color Options",
			description = "Status bar color config settings.",
			position = 99
	)
	String colorSection = "color";

	@ConfigSection(
			name = "Xp Tracker",
			description = "Experience tracker config settings.",
			position = 100
	)
	String xpTracker = "tracker";

	@ConfigItem(
			position = 0,
			keyName = "infoMessage",
			name = "Information",
			description = "You can reset the Xp Tracker leveling information by resetting the runelite xp tracker.",
			section = xpTracker
	)
	default String infoMessage()
	{
		return "Hover 'Information' to see info!";
	}

	// -- Status Section -- //

	@ConfigItem(
			position = 1,
			keyName = "barOffsetX",
			name = "Bar X Offset",
			description = "Set the X offset of the RHUD.",
			section = statusSection
	)
	@Range(
			min = -100,  // Minimum allowed width
			max = 100  // Maximum allowed width
	)
	default int barOffsetX() {
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "barOffsetY",
			name = "Bar Y Offset",
			description = "Set the Y offset of the RHUD.",
			section = statusSection
	)
	@Range(
			min = -100,  // Minimum allowed width
			max = 100  // Maximum allowed width
	)
	default int barOffsetY() {
		return 0;
	}

	@ConfigItem(
			position = 3,
			keyName = "barWidth",
			name = "Bar Width",
			description = "The width of the status bars in the modern resizeable layout.",
			section = statusSection
	)
	@Range(
			min = 90,  // Minimum allowed width
			max = 520  // Maximum allowed width
	)
	default int barWidth() {
		return 511;
	}

	@ConfigItem(
			position = 4,
			keyName = "barHeight",
			name = "Bar Height",
			description = "The Height of the status bars in the modern resizeable layout.",
			section = statusSection
	)
	@Range(
			min = 2,  // Minimum allowed width
			max = 100  // Maximum allowed width
	)
	default int barHeight() {
		return 20;
	}

	@ConfigItem(
			position = 5,
			keyName = "arcSize",
			name = "Corner Curve",
			description = "How rounded the corners of the status bars are",
			section = statusSection
	)
	@Range(
			min = 0,  // Minimum allowed width
			max = 10  // Maximum allowed width
	)
	default int arcSize() {
		return 6;
	}

	@ConfigItem(
			position = 6,
			keyName = "layout",
			name = "Layout",
			description = "Set the orientation of the HUD",
			section = statusSection
	)
	default Layout.VIEW layout() {
		return Layout.VIEW.NORMAL;
	}

	@ConfigItem(
			position = 7,
			keyName = "enableSkillIcon",
			name = "Show Icons & Text",
			description = "Adds Icon and Text to the status bars.",
			section = statusSection
	)
	default boolean enableSkillIcon() {
		return true;
	}

	@ConfigItem(
			position = 8,
			keyName = "bar1BarMode",
			name = "Bar One",
			description = "Configures the first status bar",
			section = statusSection
	)
	default Layout.XPBARMODE bar1BarMode() {
		return Layout.XPBARMODE.EXPERIENCE;
	}

	@ConfigItem(
			position = 9,
			keyName = "bar2BarMode",
			name = "Bar Two",
			description = "Configures the second status bar",
			section = statusSection
	)
	default Layout.BARMODE bar2BarMode() {
		return Layout.BARMODE.HITPOINTS;
	}

	@ConfigItem(
			position = 10,
			keyName = "bar3BarMode",
			name = "Bar Three",
			description = "Configures the third status bar",
			section = statusSection
	)
	default Layout.BARMODE bar3BarMode() {
		return Layout.BARMODE.PRAYER;
	}

	@ConfigItem(
			position = 11,
			keyName = "bar4BarMode",
			name = "Bar Four",
			description = "Configures the fourth status bar",
			section = statusSection
	)
	default Layout.BARMODE bar4BarMode() {
		return Layout.BARMODE.DISABLED;
	}


	@ConfigItem(
			position = 12,
			keyName = "bar5BarMode",
			name = "Bar Five",
			description = "Configures the fifth status bar",
			section = statusSection
	)
	default Layout.BARMODE bar5BarMode() {
		return Layout.BARMODE.DISABLED;
	}


	// -- Font and Misc Section -- //

	@ConfigItem(
			position = 13,
			keyName = "fontName",
			name = "Font",
			description = "Name of the font to use for XP drops. Leave blank to use RuneLite setting. Example: Candara, Consolas, Impact, Arial",
			section = fontSection
	)
	default String fontName() {
		return "";
	}

	@ConfigItem(
			position = 14,
			keyName = "fontStyle",
			name = "Font style",
			description = "Style of the font to use for XP drops. Only works with custom font.",
			section = fontSection
	)
	default FontStyle fontStyle() {
		return RHUD_Config.FontStyle.DEFAULT;
	}

	@ConfigItem(
			position = 15,
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
			position = 16,
			keyName = "showHeader",
			name = "Show Header",
			description = "Display player name and combat level.",
			section = fontSection
	)
	default boolean showHeader()
	{
		return false;
	}

	// -- Color Section -- //

	@ConfigItem(
			position = 17,
			keyName = "lifeColor",
			name = "Life Color",
			description = "Colors the health bar based on current health percentage.",
			section = colorSection
	)
	default boolean lifeColor()
	{
		return false;
	}


	@Alpha
	@ConfigItem(
			position = 18,
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
			position = 19,
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
			position = 20,
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
			position = 21,
			keyName = "colorSpecialBar",
			name = "Special Color",
			description = "Configures the color of the special attack bar.",
			section = colorSection
	)
	default Color colorSpecialBar()
	{
		return (SPECIAL_COLOR);
	}


	// -- Tracker Section -- //

	@ConfigItem(
			position = 22,
			keyName = "enableXpTracking",
			name = "Enable Tracking",
			description = "Display the experience tracker.",
			section = xpTracker
	)
	default boolean enableXpTracking() { return false; }

	@ConfigItem(
			position = 23,
			keyName = "mostRecentSkill",
			name = "Recent Skill",
			description = "Display the most recent skill trained.",
			section = xpTracker
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 24,
			keyName = "ignoreHitpoints",
			name = "Ignore Hitpoints for Recent",
			description = "Ignores the hitpoints skill when recent skill is being tracked",
			section = xpTracker
	)
	default boolean ignoreHitpoints() { return true; }

	@ConfigItem(
			position = 25,
			keyName = "skill",
			name = "Active Skill",
			description = "Choose which skill to track when Recent Skill setting is disabled.",
			section = xpTracker
	)
	default Skill skill()
	{
		return Skill.ATTACK;
	}

	@Alpha
	@ConfigItem(
			position = 26,
			keyName = "xpbarColor",
			name = "Experience Color",
			description = "Configures the color of the Experience bar",
			section = xpTracker
	)
	default Color colorXP()
	{
		return EXP_COLOR;
	}

	@ConfigItem(
			position = 27,
			keyName = "pos",
			name = "Tracker Anchor",
			description = "Position to anchor the xp tracker",
			section = xpTracker
	)
	default Layout.POINT pos() {
		return Layout.POINT.RIGHT;
	}

	@ConfigItem(
			position = 28,
			keyName = "trackerWidth",
			name = "XP Tracker Width",
			description = "The width of the xp tracker.",
			section = xpTracker
	)
	default int trackerWidth() {
		return 170;
	}

	@ConfigItem(
			position = 29,
			keyName = "xpBarHeight",
			name = "Bar Height",
			description = "The Height of the experience bar in the modern resizeable layout.",
			section = xpTracker
	)
	@Range(
			min = 2,  // Minimum allowed width
			max = 100  // Maximum allowed width
	)
	default int xpBarHeight() {
		return 10;
	}

	@ConfigItem(
			position = 30,
			keyName = "xpNeeded",
			name = "Exp Needed",
			description = "Shows the number of xp needed to level-up.",
			section = xpTracker
	)
	default boolean xpNeeded()
	{
		return true;
	}

	@ConfigItem(
			position = 31,
			keyName = "actionsNeeded",
			name = "Actions Needed",
			description = "Shows the number of actions needed to level-up.",
			section = xpTracker
	)
	default boolean actionsNeeded()
	{
		return true;
	}

	@ConfigItem(
			position = 32,
			keyName = "xpGained",
			name = "Exp Gained",
			description = "Shows Experience gained for skill.",
			section = xpTracker
	)
	default boolean xpGained()
	{
		return true;
	}


	@ConfigItem(
			position = 33,
			keyName = "xpHour",
			name = "Exp/hr",
			description = "Shows Experience per hour.",
			section = xpTracker
	)
	default boolean xpHour()
	{
		return true;
	}

	@ConfigItem(
			position = 34,
			keyName = "showTTG",
			name = "Time to Level",
			description = "Shows the amount of time until goal lvl reached.",
			section = xpTracker
	)
	default boolean showTTG()
	{
		return true;
	}

	@ConfigItem(
			position = 35,
			keyName = "showPercent",
			name = "Percent",
			description = "Shows the percentage leveled.",
			section = xpTracker
	)
	default boolean showPercent()
	{
		return true;
	}
}
