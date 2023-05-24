package com.RastaXP;

import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("RastaXP")
public interface RastaXPConfig extends Config
{
	Color EXP_COLOR = new Color(130, 60, 170, 175);
	@ConfigItem(
			position = 0,
			keyName = "XPtracker",
			name = "Experience Bar",
			description = "Displays the experience bar."
	)
	default boolean XPtracker() { return true; }

	@ConfigItem(
			position = 1,
			keyName = "displayHealthAndPrayer",
			name = "Status Bars",
			description = "Displays Healh and Prayer."
	)
	default boolean displayHealthAndPrayer() { return false; }

	@ConfigItem(
			position = 2,
			keyName = "skill",
			name = "Active Skill",
			description = "Choose which skill to track when Recent Skill setting is disabled."
	)
	default Skill skill()
	{
		return Skill.ATTACK;
	}

	@ConfigItem(
			position = 3,
			keyName = "mostRecentSkill",
			name = "Toggle Recent Skill",
			description = "Display the most recent skill trained."
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 4,
			keyName = "enableSkillIcon",
			name = "Icons",
			description = "Displays prayer and health icons."
	)
	default boolean enableSkillIcon()
	{
		return true;
	}

	@ConfigItem(
			position = 5,
			keyName = "enableSkillText",
			name = "Text",
			description = "Displays prayer and health status text."
	)
	default boolean enableSkillText()
	{
		return true;
	}

	@ConfigItem(
			position = 6,
			keyName = "enableSmall",
			name = "Small Bars",
			description = "Display bars at a reduced size."
	)
	default boolean enableSmall()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			position = 7,
			keyName = "hpbarColor",
			name = "Health Color",
			description = "Configures the color of the Health bar"
	)
	default Color colorHP()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			position = 8,
			keyName = "xpbarColor",
			name = "Experience Color",
			description = "Configures the color of the Experience bar"
	)
	default Color colorXP()
	{
		return EXP_COLOR;
	}

	@Alpha
	@ConfigItem(
			position = 9,
			keyName = "xpbarNotchColor",
			name = "Segment Color",
			description = "Configures the color of the experience segments."
	)
	default Color colorXPNotches()
	{
		return Color.LIGHT_GRAY;
	}


}
