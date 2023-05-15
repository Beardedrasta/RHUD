package com.RastaXP;

import net.runelite.api.Skill;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("RastaXP")
public interface RastaXPConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "skill",
			name = "Active Skill",
			description = "Choose which skill to show at XP bar."
	)
	default Skill skill()
	{
		return Skill.ATTACK;
	}

	@ConfigItem(
			position = 1,
			keyName = "mostRecentSkill",
			name = "Show Recent Skill",
			description = "Display the most recent skill trained."
	)
	default boolean mostRecentSkill() { return false; }

	@ConfigItem(
			position = 2,
			keyName = "displayHealthAndPrayer",
			name = "Display Status Bars",
			description = "Displays Healh and Prayer."
	)
	default boolean displayHealthAndPrayer() { return false; }

	@ConfigItem(
			position = 3,
			keyName = "enableSkillIcon",
			name = "Show Icons",
			description = "Displays prayer and health icons."
	)
	default boolean enableSkillIcon()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			position = 4,
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
			position = 5,
			keyName = "xpbarColor",
			name = "Experience Color",
			description = "Configures the color of the Experience bar"
	)
	default Color colorXP()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
			position = 6,
			keyName = "xpbarNotchColor",
			name = "Segment Color",
			description = "Configures the color of the experience segments."
	)
	default Color colorXPNotches()
	{
		return Color.LIGHT_GRAY;
	}

}
