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

	/** Text shown in an individually styled status bar. */
	enum BarTextFormat {
		CURRENT_MAX("Current / Max"),
		CURRENT("Current"),
		PERCENT("Percent"),
		HIDDEN("Hidden");

		private final String label;
		BarTextFormat(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

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



	@ConfigSection(name = "HUD & Bars", description = "Layout, bar assignments, sizing and behavior.", position = 0)
	String statusSection = "status";

	@ConfigSection(name = "Appearance", description = "Header and font appearance.", position = 3, closedByDefault = true)
	String fontSection = "font";

	@ConfigSection(name = "Global Colors", description = "Default colors used by bars that use Global Style.", position = 4, closedByDefault = true)
	String colorSection = "color";

	@ConfigSection(name = "XP Tracker", description = "Experience tracker settings.", position = 1)
	String xpTracker = "tracker";


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
			name = "Global Icons & Text",
			description = "Default icon/text visibility for bars using Global Style.",
			section = statusSection
	)
	default boolean enableSkillIcon() {
		return true;
	}

	@ConfigItem(
			position = 8,
			keyName = "showBarBackground",
			name = "Show Bar Background",
			description = "Draws the dark background behind the unfilled portion of each status bar.",
			section = statusSection
	)
	default boolean showBarBackground() {
		return true;
	}

	@ConfigItem(
			position = 9,
			keyName = "showHudBackdrop",
			name = "Show HUD Backdrop / Border",
			description = "Draws the outer RHUD gradient backdrop and border around the bars.",
			section = statusSection
	)
	default boolean showHudBackdrop() {
		return true;
	}

	@ConfigItem(
			position = 10,
			keyName = "bar1BarMode",
			name = "Bar One",
			description = "Configures the first status bar",
			section = statusSection
	)
	default Layout.XPBARMODE bar1BarMode() {
		return Layout.XPBARMODE.EXPERIENCE;
	}

	@ConfigItem(
			position = 11,
			keyName = "bar2BarMode",
			name = "Bar Two",
			description = "Configures the second status bar",
			section = statusSection
	)
	default Layout.BARMODE bar2BarMode() {
		return Layout.BARMODE.HITPOINTS;
	}

	@ConfigItem(
			position = 12,
			keyName = "bar3BarMode",
			name = "Bar Three",
			description = "Configures the third status bar",
			section = statusSection
	)
	default Layout.BARMODE bar3BarMode() {
		return Layout.BARMODE.PRAYER;
	}

	@ConfigItem(
			position = 13,
			keyName = "bar4BarMode",
			name = "Bar Four",
			description = "Configures the fourth status bar",
			section = statusSection
	)
	default Layout.BARMODE bar4BarMode() {
		return Layout.BARMODE.DISABLED;
	}


	@ConfigItem(
			position = 14,
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
			description = "Optional custom font name for RHUD text. Leave blank to use the default.",
			section = fontSection
	)
	default String fontName() {
		return "";
	}

	@ConfigItem(
			position = 14,
			keyName = "fontStyle",
			name = "Font style",
			description = "Font style used by RHUD text.",
			section = fontSection
	)
	default FontStyle fontStyle() {
		return RHUD_Config.FontStyle.DEFAULT;
	}

	@ConfigItem(
			position = 15,
			keyName = "fontSize",
			name = "Font size",
			description = "Font size used by RHUD text.",
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

	@ConfigItem(
			position = 36,
			keyName = "hideInterfaces",
			name = "Hide Behind Interfaces",
			description = "Hides the bars while the bank interface is open.",
			section = statusSection
	)
	default boolean hideInInterfaces() { return false; }

	// -- RHUD 6: Individual bar styling -- //
	@ConfigItem(position = 37, keyName = "barSpacing", name = "Bar Spacing", description = "Space in pixels between RHUD bars.", section = statusSection)
	@Range(min = 0, max = 12)
	default int barSpacing() { return 0; }

	@ConfigItem(position = 47, keyName = "bar1UseGlobalStyle", name = "Bar 1 Use Global Style", description = "Use the global RHUD style for Bar 1.", section = statusSection)
	default boolean bar1UseGlobalStyle() { return true; }

	@ConfigItem(position = 48, keyName = "bar1Height", name = "Bar 1 Height", description = "Custom height for Bar 1 when global style is disabled.", section = statusSection)
	@Range(min = 2, max = 100)
	default int bar1Height() { return xpBarHeight(); }


	@ConfigItem(position = 50, keyName = "bar1ShowBackground", name = "Bar 1 Background", description = "Show the unfilled background for Bar 1 when global style is disabled.", section = statusSection)
	default boolean bar1ShowBackground() { return true; }

	@ConfigItem(position = 51, keyName = "bar1Opacity", name = "Bar 1 Opacity", description = "Opacity percentage for Bar 1 when global style is disabled.", section = statusSection)
	@Range(min = 10, max = 100)
	default int bar1Opacity() { return 100; }

	@ConfigItem(position = 57, keyName = "bar2UseGlobalStyle", name = "Bar 2 Use Global Style", description = "Use the global RHUD style for Bar 2.", section = statusSection)
	default boolean bar2UseGlobalStyle() { return true; }

	@ConfigItem(position = 58, keyName = "bar2Height", name = "Bar 2 Height", description = "Custom height for Bar 2 when global style is disabled.", section = statusSection)
	@Range(min = 2, max = 100)
	default int bar2Height() { return barHeight(); }


	@ConfigItem(position = 60, keyName = "bar2ShowBackground", name = "Bar 2 Background", description = "Show the unfilled background for Bar 2 when global style is disabled.", section = statusSection)
	default boolean bar2ShowBackground() { return true; }

	@ConfigItem(position = 61, keyName = "bar2Opacity", name = "Bar 2 Opacity", description = "Opacity percentage for Bar 2 when global style is disabled.", section = statusSection)
	@Range(min = 10, max = 100)
	default int bar2Opacity() { return 100; }

	@ConfigItem(position = 67, keyName = "bar3UseGlobalStyle", name = "Bar 3 Use Global Style", description = "Use the global RHUD style for Bar 3.", section = statusSection)
	default boolean bar3UseGlobalStyle() { return true; }

	@ConfigItem(position = 68, keyName = "bar3Height", name = "Bar 3 Height", description = "Custom height for Bar 3 when global style is disabled.", section = statusSection)
	@Range(min = 2, max = 100)
	default int bar3Height() { return barHeight(); }


	@ConfigItem(position = 70, keyName = "bar3ShowBackground", name = "Bar 3 Background", description = "Show the unfilled background for Bar 3 when global style is disabled.", section = statusSection)
	default boolean bar3ShowBackground() { return true; }

	@ConfigItem(position = 71, keyName = "bar3Opacity", name = "Bar 3 Opacity", description = "Opacity percentage for Bar 3 when global style is disabled.", section = statusSection)
	@Range(min = 10, max = 100)
	default int bar3Opacity() { return 100; }

	@ConfigItem(position = 77, keyName = "bar4UseGlobalStyle", name = "Bar 4 Use Global Style", description = "Use the global RHUD style for Bar 4.", section = statusSection)
	default boolean bar4UseGlobalStyle() { return true; }

	@ConfigItem(position = 78, keyName = "bar4Height", name = "Bar 4 Height", description = "Custom height for Bar 4 when global style is disabled.", section = statusSection)
	@Range(min = 2, max = 100)
	default int bar4Height() { return barHeight(); }


	@ConfigItem(position = 80, keyName = "bar4ShowBackground", name = "Bar 4 Background", description = "Show the unfilled background for Bar 4 when global style is disabled.", section = statusSection)
	default boolean bar4ShowBackground() { return true; }

	@ConfigItem(position = 81, keyName = "bar4Opacity", name = "Bar 4 Opacity", description = "Opacity percentage for Bar 4 when global style is disabled.", section = statusSection)
	@Range(min = 10, max = 100)
	default int bar4Opacity() { return 100; }

	@ConfigItem(position = 87, keyName = "bar5UseGlobalStyle", name = "Bar 5 Use Global Style", description = "Use the global RHUD style for Bar 5.", section = statusSection)
	default boolean bar5UseGlobalStyle() { return true; }

	@ConfigItem(position = 88, keyName = "bar5Height", name = "Bar 5 Height", description = "Custom height for Bar 5 when global style is disabled.", section = statusSection)
	@Range(min = 2, max = 100)
	default int bar5Height() { return barHeight(); }


	@ConfigItem(position = 90, keyName = "bar5ShowBackground", name = "Bar 5 Background", description = "Show the unfilled background for Bar 5 when global style is disabled.", section = statusSection)
	default boolean bar5ShowBackground() { return true; }

	@ConfigItem(position = 91, keyName = "bar5Opacity", name = "Bar 5 Opacity", description = "Opacity percentage for Bar 5 when global style is disabled.", section = statusSection)
	@Range(min = 10, max = 100)
	default int bar5Opacity() { return 100; }



	// ---------- RHUD 6.4: advanced per-bar style ----------
	@ConfigItem(position = 52, keyName = "bar1UseCustomColor", name = "Bar 1 Custom Color", description = "Use a custom fill color for Bar 1.", section = statusSection)
	default boolean bar1UseCustomColor() { return false; }
	@ConfigItem(position = 53, keyName = "bar1CustomColor", name = "Bar 1 Color", description = "Custom fill color for Bar 1.", section = statusSection)
	default Color bar1CustomColor() { return new Color(128, 74, 170); }

	@ConfigItem(position = 62, keyName = "bar2ShowIcon", name = "Bar 2 Icon", description = "Show the icon for Bar 2.", section = statusSection)
	default boolean bar2ShowIcon() { return true; }
	@ConfigItem(position = 63, keyName = "bar2TextFormat", name = "Bar 2 Text", description = "Text format for Bar 2.", section = statusSection)
	default BarTextFormat bar2TextFormat() { return BarTextFormat.CURRENT_MAX; }
	@ConfigItem(position = 64, keyName = "bar2UseCustomColor", name = "Bar 2 Custom Color", description = "Use a custom fill color for Bar 2.", section = statusSection)
	default boolean bar2UseCustomColor() { return false; }
	@ConfigItem(position = 65, keyName = "bar2CustomColor", name = "Bar 2 Color", description = "Custom fill color for Bar 2.", section = statusSection)
	default Color bar2CustomColor() { return HEALTH_COLOR; }

	@ConfigItem(position = 72, keyName = "bar3ShowIcon", name = "Bar 3 Icon", description = "Show the icon for Bar 3.", section = statusSection)
	default boolean bar3ShowIcon() { return true; }
	@ConfigItem(position = 73, keyName = "bar3TextFormat", name = "Bar 3 Text", description = "Text format for Bar 3.", section = statusSection)
	default BarTextFormat bar3TextFormat() { return BarTextFormat.CURRENT_MAX; }
	@ConfigItem(position = 74, keyName = "bar3UseCustomColor", name = "Bar 3 Custom Color", description = "Use a custom fill color for Bar 3.", section = statusSection)
	default boolean bar3UseCustomColor() { return false; }
	@ConfigItem(position = 75, keyName = "bar3CustomColor", name = "Bar 3 Color", description = "Custom fill color for Bar 3.", section = statusSection)
	default Color bar3CustomColor() { return PRAY_COLOR; }

	@ConfigItem(position = 82, keyName = "bar4ShowIcon", name = "Bar 4 Icon", description = "Show the icon for Bar 4.", section = statusSection)
	default boolean bar4ShowIcon() { return true; }
	@ConfigItem(position = 83, keyName = "bar4TextFormat", name = "Bar 4 Text", description = "Text format for Bar 4.", section = statusSection)
	default BarTextFormat bar4TextFormat() { return BarTextFormat.CURRENT_MAX; }
	@ConfigItem(position = 84, keyName = "bar4UseCustomColor", name = "Bar 4 Custom Color", description = "Use a custom fill color for Bar 4.", section = statusSection)
	default boolean bar4UseCustomColor() { return false; }
	@ConfigItem(position = 85, keyName = "bar4CustomColor", name = "Bar 4 Color", description = "Custom fill color for Bar 4.", section = statusSection)
	default Color bar4CustomColor() { return RUN_COLOR; }

	@ConfigItem(position = 92, keyName = "bar5ShowIcon", name = "Bar 5 Icon", description = "Show the icon for Bar 5.", section = statusSection)
	default boolean bar5ShowIcon() { return true; }
	@ConfigItem(position = 93, keyName = "bar5TextFormat", name = "Bar 5 Text", description = "Text format for Bar 5.", section = statusSection)
	default BarTextFormat bar5TextFormat() { return BarTextFormat.CURRENT_MAX; }
	@ConfigItem(position = 94, keyName = "bar5UseCustomColor", name = "Bar 5 Custom Color", description = "Use a custom fill color for Bar 5.", section = statusSection)
	default boolean bar5UseCustomColor() { return false; }
	@ConfigItem(position = 95, keyName = "bar5CustomColor", name = "Bar 5 Color", description = "Custom fill color for Bar 5.", section = statusSection)
	default Color bar5CustomColor() { return SPECIAL_COLOR; }


	// ---------- RHUD 7.0: reactive warnings ----------
	@ConfigSection(name = "Warnings", description = "Low Hitpoints and Prayer warning effects.", position = 2, closedByDefault = true)
	String warningSection = "warningSection";

	@ConfigItem(position = 0, keyName = "lowHealthWarning", name = "Low Health Warning", description = "Pulse/glow the health bar when health is low.", section = warningSection)
	default boolean lowHealthWarning() { return false; }

	@ConfigItem(position = 1, keyName = "lowHealthThreshold", name = "Health Threshold", description = "Current Hitpoints value that activates the warning at or below this number.", section = warningSection)
	@Range(min = 1, max = 99)
	default int lowHealthThreshold() { return 30; }

	@ConfigItem(position = 2, keyName = "lowHealthWarningColor", name = "Health Warning Color", description = "Glow color for low health.", section = warningSection)
	default Color lowHealthWarningColor() { return new Color(255, 45, 45); }

	@ConfigItem(position = 3, keyName = "lowPrayerWarning", name = "Low Prayer Warning", description = "Pulse/glow the prayer bar when prayer is low.", section = warningSection)
	default boolean lowPrayerWarning() { return false; }

	@ConfigItem(position = 4, keyName = "lowPrayerThreshold", name = "Prayer Threshold", description = "Current Prayer points that activate the warning at or below this number.", section = warningSection)
	@Range(min = 1, max = 99)
	default int lowPrayerThreshold() { return 20; }

	@ConfigItem(position = 5, keyName = "lowPrayerWarningColor", name = "Prayer Warning Color", description = "Glow color for low prayer.", section = warningSection)
	default Color lowPrayerWarningColor() { return new Color(0, 220, 255); }

	@ConfigItem(position = 6, keyName = "warningPulse", name = "Pulse Effect", description = "Animate warning intensity instead of using a steady glow.", section = warningSection)
	default boolean warningPulse() { return true; }

	@ConfigItem(position = 7, keyName = "warningGlowSize", name = "Glow Size", description = "Thickness of the warning glow.", section = warningSection)
	@Range(min = 1, max = 8)
	default int warningGlowSize() { return 3; }

}
