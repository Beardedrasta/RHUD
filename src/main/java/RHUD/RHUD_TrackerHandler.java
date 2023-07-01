package RHUD;

import java.awt.image.BufferedImage;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Skill;

@Getter
@Setter
class RHUD_TrackerHandler
{
    private Skill skill;
    private int currentXp;
    private int currentLevel;
    private Instant time;
    private int size;
    private BufferedImage skillIcon;

    RHUD_TrackerHandler(Skill skill, int currentXp, int currentLevel, Instant time)
    {
        this.skill = skill;
        this.currentXp = currentXp;
        this.currentLevel = currentLevel;
        this.time = time;
    }
}