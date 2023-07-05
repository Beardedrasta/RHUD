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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package RHUD;

import RHUD.helpers.TextComponentWithAlpha;
import lombok.RequiredArgsConstructor;
import net.runelite.client.util.ImageUtil;
import java.awt.*;
import java.util.function.Supplier;

import static RHUD.helpers.BarTextMode.*;


@RequiredArgsConstructor
class RHUD_StatusRender {
    private static final Color BACKGROUND = new Color(30, 30, 30, 150);
    private static final Color OVERHEAL_COLOR = new Color(216, 255, 139, 150);
    private static final int SKILL_ICON_HEIGHT = 24;
    //private static final int COUNTER_ICON_HEIGHT = 7;
    private static final int BORDER_SIZE = 1;
    private static final int MIN_ICON_AND_COUNTER_WIDTH = 16;
    static final int DEFAULT_WIDTH = 20;

    static final int DEFAULT_HEIGHT = 20;
    static final int MIN_WIDTH = 60;
    static final int MAX_WIDTH = 500;
    private final Supplier<Integer> maxValueSupplier;
    private final Supplier<Integer> currentValueSupplier;
    private final Supplier<Integer> healSupplier;
    private final Supplier<Color> colorSupplier;
    private final Supplier<Color> healColorSupplier;
    private final Supplier<Image> iconSupplier;
    private int maxValue;
    private int currentValue;


    private final RHUD.helpers.FontHandler FontHandler = new RHUD.helpers.FontHandler();

    private void refreshSkills() {
        maxValue = maxValueSupplier.get();
        currentValue = currentValueSupplier.get();
    }

    public void renderBar(RHUD_Config config, Graphics2D graphics, int x, int y, int width) {
        final int filledHeight = getBarHeight(maxValue, currentValue, config.barWidth());
        final int filledWidth = getBarHeight(maxValue, currentValue, 250);
        final Color fill = colorSupplier.get();
        refreshSkills();

        //Calc starting position for bar
        int adjustedX;
        int adjustedY;
        adjustedY = y;
        adjustedX = x;

        graphics.setColor(BACKGROUND);
        if (config.vertBars()) {
            graphics.drawRect(adjustedX, adjustedY, width - BORDER_SIZE, config.barWidth() - BORDER_SIZE);
            graphics.fillRect(adjustedX, adjustedY, width, config.barWidth());

        } else if (config.SidebySide()) {
            graphics.drawRect(adjustedX, adjustedY, 250 - BORDER_SIZE, width - BORDER_SIZE);
            graphics.fillRect(adjustedX, adjustedY, 250, width);

        } else {
            graphics.drawRect(adjustedX, adjustedY, config.barWidth() - BORDER_SIZE, width - BORDER_SIZE);
            graphics.fillRect(adjustedX, adjustedY, config.barWidth(), width);
        }

        graphics.setColor(fill);
        if (config.vertBars()) {
            graphics.fillRect(adjustedX + BORDER_SIZE, adjustedY + BORDER_SIZE + (config.barWidth() - filledHeight), width - BORDER_SIZE * 2, filledHeight - BORDER_SIZE * 2);
        } else  if (config.SidebySide()) {
            graphics.fillRect(adjustedX + BORDER_SIZE, adjustedY + BORDER_SIZE, filledWidth - BORDER_SIZE * 2, width - BORDER_SIZE * 2);
        } else {
            graphics.fillRect(adjustedX + BORDER_SIZE, adjustedY + BORDER_SIZE, filledHeight - BORDER_SIZE * 2, width - BORDER_SIZE * 2);
        }

        if (config.enableRestorationBars()) {
            renderRestore(config, graphics, adjustedX, adjustedY, width);
        }

        if (config.enableSkillIcon() || config.enableCounter()) {
            renderIconsAndCounters(config, graphics, adjustedX, adjustedY, width);
        }
    }



    private void renderIconsAndCounters(RHUD.RHUD_Config config, Graphics2D graphics, int x, int y, int width) {
        // Icons and counters overlap the bar at small widths, so they are not drawn when the bars are too small
        if (width < MIN_ICON_AND_COUNTER_WIDTH) {
            return;
        }

        final boolean skillIconEnabled = config.enableSkillIcon();

        if (skillIconEnabled) {
            final Image icon = iconSupplier.get();
            final int xDraw = x + (width / 2) - (icon.getWidth(null) / 2);
            if (config.vertBars()) {
                if (config.textMode() == Icon_top_Text_bottom) {
                    graphics.drawImage(icon, xDraw, y + 4, null);
                } else if (config.textMode() == Both_Bottom) {
                    graphics.drawImage(icon, xDraw, config.barWidth() - 21, null);
                } else if (config.textMode() == Text_top_Icon_bottom) {
                    graphics.drawImage(icon, xDraw, config.barWidth() - 21, null);
                } else if (config.textMode() == Both_Top) {
                    graphics.drawImage(icon, xDraw, y + 4, null);
                }
            } else {
                graphics.drawImage(icon, xDraw, y + 2 , null);
            }
        }

        if (config.enableCounter()) {
            FontHandler.updateFont(config.fontName(), config.fontSize(), config.fontStyle());
            FontHandler.handleFont(graphics);
            final String counterText = Integer.toString(currentValue);
            final int widthOfCounter = graphics.getFontMetrics().stringWidth(counterText);
            final int centerText = (width / 2) - (widthOfCounter / 2);
            final int xOffset = skillIconEnabled ? 36 : 21;
            final int hOffset = skillIconEnabled ? SKILL_ICON_HEIGHT : 8;
            final int yOffset = skillIconEnabled ? config.barWidth() - 27 : config.barWidth() - 10;
            final int eOffset = config.barWidth() - 10;

            final TextComponentWithAlpha textComponent = new TextComponentWithAlpha();
            textComponent.setColor(config.counterColor());
            textComponent.setOutline(true);
            textComponent.setText(counterText);
            if (config.vertBars()) {
                if (config.textMode() == Icon_top_Text_bottom) {
                    textComponent.setPosition(new Point(x + centerText, y + eOffset));
                } else if (config.textMode() == Both_Bottom) {
                    textComponent.setPosition(new Point(x + centerText, y + yOffset));
                } else if (config.textMode() == Text_top_Icon_bottom) {
                    textComponent.setPosition(new Point(x + centerText, y + 17));
                } else if (config.textMode() == Both_Top) {
                    textComponent.setPosition(new Point(x + centerText, y + xOffset));
                }
            } else {
                textComponent.setPosition(new Point(x + hOffset + centerText, y + 15));
            }
            textComponent.render(graphics);
        }
    }

    private void renderRestore(RHUD.RHUD_Config config, Graphics2D graphics, int x, int y, int width) {
        final Color color = healColorSupplier.get();
        final int heal = healSupplier.get();

        if (heal <= 0) {
            return;
        }

        final int filledCurrentHeight = getBarHeight(maxValue, currentValue, config.barWidth());
        final int filledHealHeight = getBarHeight(maxValue, heal, config.barWidth());
        final int filledCurrentWidth = getBarWidth(maxValue, currentValue, config.barWidth());
        final int filledHealWidth = getBarHeight(maxValue, heal, config.barWidth());
        final int filledCurrentLeft = getBarWidth(maxValue, currentValue, 250);
        final int filledHealLeft = getBarHeight(maxValue, heal, 250);
        final int fillY, fillX, fillH, fillHeight, fillWidth, fillLeft;
        graphics.setColor(color);

        if (!config.vertBars() && !config.SidebySide()) {
            if (filledHealWidth + filledCurrentWidth > config.barWidth()) {
                graphics.setColor(OVERHEAL_COLOR);
                fillX = x - BORDER_SIZE + filledCurrentWidth;
                fillWidth = config.barWidth() - filledCurrentWidth - BORDER_SIZE;
            } else {
                fillX = x - BORDER_SIZE + (filledCurrentWidth - filledHealWidth) + filledHealWidth;
                fillWidth = filledHealWidth;
            }
            graphics.fillRect(fillX, y + BORDER_SIZE, fillWidth, width - BORDER_SIZE * 2);
        }

        if (config.SidebySide()) {
            if (filledHealLeft + filledCurrentLeft > 250) {
                graphics.setColor(OVERHEAL_COLOR);
                fillH = x - BORDER_SIZE + filledCurrentLeft;
                fillLeft = 250 - filledCurrentLeft - BORDER_SIZE;
            } else {
                fillH = x - BORDER_SIZE + (filledCurrentLeft - filledHealLeft) + filledHealLeft;
                fillLeft = filledHealLeft;
            }
            graphics.fillRect(fillH, y + BORDER_SIZE, fillLeft, width - BORDER_SIZE * 2);
        }

        if (config.vertBars()) {
            if (filledHealHeight + filledCurrentHeight > config.barWidth()) {
                graphics.setColor(OVERHEAL_COLOR);
                fillY = y + BORDER_SIZE;
                fillHeight = config.barWidth() - filledCurrentHeight + BORDER_SIZE;
            } else {
                fillY = y + BORDER_SIZE + config.barWidth() - (filledCurrentHeight + filledHealHeight * 2) + filledHealHeight;
                fillHeight = filledHealHeight;
            }
            graphics.fillRect(x + BORDER_SIZE, fillY, width - BORDER_SIZE * 2, fillHeight);
        }
    }

    private static int getBarHeight ( int base, int current, int size)
    {
        final double ratio = (double) current / base;

        if (ratio >= 1) {
            return size;
        }

        return (int) Math.round(ratio * size);
    }

    private static int getBarWidth ( int base, int current, int size)
    {
        final double ratio = (double) current / base;

        if (ratio >= 1) {
            return size;
        }

        return (int) Math.round(ratio * size);
    }
}
