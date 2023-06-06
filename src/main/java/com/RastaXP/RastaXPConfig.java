package com.RastaXP;

import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("RastaXP")
public interface RastaXPConfig extends Config
{
	Color EXP_COLOR = new Color(130, 60, 170, 195);

	@ConfigSection(
			name = "Toggles",
			description = "On / Off toggle options.",
			position = 97
	)
	String toggleSection = "toggles";

	@ConfigSection(
			name = "Skilling",
			description = "Expereince / Skill related options.",
			position = 98
	)
	String xpSection = "skilling";

	@ConfigSection(
			name = "Misc",
			description = "List of config options.",
			position = 99
	)
	String miscSection = "misc";

	@ConfigItem(
			position = 1,
			keyName = "XPtracker",
			name = "Experience Bar",
			description = "Displays the experience bar.",
			section = toggleSection
	)
	default boolean XPtracker() { return true; }

	@ConfigItem(
			position = 2,
			keyName = "displayHealthAndPrayer",
			name = "Status Bars",
			description = "Displays Healh and Prayer.",
			section = toggleSection
	)
	default boolean displayHealthAndPrayer() { return true; }

	@ConfigItem(
			position = 3,
			keyName = "displayRun",
			name = "Run Energy",
			description = "Displays run energy bar.",
			section = toggleSection
	)
	default boolean displayRun() { return false; }

	@ConfigItem(
			position = 4,
			keyName = "displaySpecial",
			name = "Special Attack",
			description = "Displays the special bar.",
			section = toggleSection
	)
	default boolean displaySpecial() { return true; }

	@ConfigItem(
			position = 5,
			keyName = "skill",
			name = "Active Skill",
			description = "Choose which skill to track when Recent Skill setting is disabled.",
			section = xpSection
	)
	default Skill skill()
	{
		return Skill.ATTACK;
	}

	@ConfigItem(
			position = 6,
			keyName = "mostRecentSkill",
			name = "Recent Skill",
			description = "Display the most recent skill trained.",
			section = xpSection
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 7,
			keyName = "enableSkillIcon",
			name = "Icons",
			description = "Displays prayer and health icons.",
			section = xpSection
	)
	default boolean enableSkillIcon()
	{
		return true;
	}

	@ConfigItem(
			position = 8,
			keyName = "enableSkillText",
			name = "Text",
			description = "Displays prayer and health status text.",
			section = xpSection
	)
	default boolean enableSkillText()
	{
		return true;
	}

	@ConfigItem(
			position = 9,
			keyName = "enableTip",
			name = "Tooltip",
			description = "Displays xp tracked info on mouseover.",
			section = miscSection
	)
	default boolean enableTip()
	{
		return true;
	}

	@ConfigItem(
			position = 10,
			keyName = "enableSmall",
			name = "Small Bars",
			description = "Display bars at a reduced size.",
			section = miscSection
	)
	default boolean enableSmall()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			position = 11,
			keyName = "xpbarColor",
			name = "Experience Color",
			description = "Configures the color of the Experience bar",
			section = miscSection
	)
	default Color colorXP()
	{
		return EXP_COLOR;
	}

	@Alpha
	@ConfigItem(
			position = 12,
			keyName = "xpbarNotchColor",
			name = "Segment Color",
			description = "Configures the color of the experience segments.",
			section = miscSection
	)
	default Color colorXPNotches()
	{
		return new Color(909090);
	}


}
