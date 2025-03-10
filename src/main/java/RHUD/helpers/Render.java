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
package RHUD.helpers;

import java.awt.*;
import java.util.function.Supplier;
import net.runelite.client.util.ColorUtil;
import RHUD.RHUD_Config;

public class Render {
    public static final int DEFAULT_WIDTH = 20;
    public static final int DEFAULT_HEIGHT = 20;
    private static final int BORDER = 1;
    private static final int MIN_ICON_AND_COUNTER_WIDTH = 16;

    private final Supplier<Integer> maxValSupplier;
    private final Supplier<Integer> currValSupplier;
    private final Supplier<Color> colorSupplier;
    private final Supplier<Color> healColorSupplier;
    private final Supplier<Integer> healSupplier;
    private final Supplier<Image> iconSupplier;

    private int maxVal;
    private int currVal;
    private static final Color BACKGROUND = new Color(20, 20, 20, 150);
    private static final Color OVERHEAL_COLOR = new Color(216, 255, 139, 150);

    public Render(Supplier<Integer> maxValSupplier, Supplier<Integer> currValSupplier, Supplier<Integer> healSupplier, Supplier<Color> colorSupplier, Supplier<Color> healColorSupplier, Supplier<Image> iconSupplier) {
        this.maxValSupplier = maxValSupplier;
        this.currValSupplier = currValSupplier;
        this.healSupplier = healSupplier;
        this.colorSupplier = colorSupplier;
        this.healColorSupplier = healColorSupplier;
        this.iconSupplier = iconSupplier;
    }


    private void refresh() {
        maxVal = maxValSupplier.get();
        currVal = currValSupplier.get();
    }


    public void renderBar(RHUD_Config config, Graphics2D g, int x, int y) {

        refresh();

        int barWidth, barHeight;
        if (config.layout() == Layout.VIEW.VERTICAL) {
            // Swap so bar is “skinny width, tall height”
            barWidth = config.barHeight();
            barHeight = config.barWidth();
        } else {
            barWidth = config.barWidth();
            barHeight = config.barHeight();
        }

        if (config.layout() == Layout.VIEW.VERTICAL) {
            // vertical fill
            int fillHeight = getBarHeight(maxVal, currVal, barHeight - 2);
            // or a direct formula: (int) ((double)currVal / maxVal * barHeight)
            int fillY = y + barHeight - fillHeight;

            g.setColor(BACKGROUND);
            g.drawRoundRect(x - 2, y, barWidth + 2, barHeight, config.arcSize(), config.arcSize());
            g.fillRoundRect(x - 2, y, barWidth + 2, barHeight, config.arcSize(), config.arcSize());

            g.setColor(colorSupplier.get());

            g.setColor(colorSupplier.get());
            g.fillRoundRect(
                    x + BORDER,
                    fillY + BORDER,
                    barWidth - BORDER * 2,
                    fillHeight - BORDER * 2,
                    config.arcSize(),
                    config.arcSize()
            );
            renderIconsAndText(config, g, x, y, barWidth, barHeight);
        } else if (config.layout() == Layout.VIEW.GRID) {
            // horizontal fill
            int fillWidth = getBarWidth(currVal, maxVal, barWidth);
            // or a direct formula: (int) ((double)currVal / maxVal * barWidth)

            g.setColor(BACKGROUND);
            g.drawRoundRect(x, y, barWidth / 2, barHeight, config.arcSize(), config.arcSize());
            g.fillRoundRect(x, y, barWidth / 2, barHeight, config.arcSize(), config.arcSize());

            g.setColor(colorSupplier.get());

            g.setColor(colorSupplier.get());
            g.fillRoundRect(
                    x + BORDER,
                    y + BORDER,
                    fillWidth / 2 - BORDER * 2,
                    barHeight - BORDER * 2,
                    config.arcSize(),
                    config.arcSize()
            );
            renderIconsAndText(config, g, x, y, barWidth, barHeight);
        } else {
            // horizontal fill
            int fillWidth = getBarWidth(currVal, maxVal, barWidth);
            // or a direct formula: (int) ((double)currVal / maxVal * barWidth)

            g.setColor(BACKGROUND);
            g.drawRoundRect(x, y, barWidth, barHeight, config.arcSize(), config.arcSize());
            g.fillRoundRect(x, y, barWidth, barHeight, config.arcSize(), config.arcSize());

            g.setColor(colorSupplier.get());

            g.setColor(colorSupplier.get());
            g.fillRoundRect(
                    x + BORDER,
                    y + BORDER,
                    fillWidth - BORDER * 2,
                    barHeight - BORDER * 2,
                    config.arcSize(),
                    config.arcSize()
            );
            renderIconsAndText(config, g, x, y, barWidth, barHeight);

        }

        if (config.layout() == Layout.VIEW.VERTICAL) {
            renderRestoreVertical(config, g, x, y, barWidth, barHeight);
        } else if (config.layout() == Layout.VIEW.GRID) {
            renderRestoreGrid(config, g, x, y, barWidth, barHeight);
        }
        else
        {
            renderRestoreHorizontal(config, g, x, y, barWidth, barHeight);
        }

        // render overlays like icons and text

//        final int fillHeight = getBarHeight(maxVal, currVal, config.barWidth());
//        final int filledWidth = getBarWidth(maxVal, currVal, config.barHeight());
//        final Color fillColor = colorSupplier.get();
//
//        int adjX;
//        int adjY;
//        adjY = y;
//        adjX = x;
//
//        g.setColor(BACKGROUND);
//        g.drawRoundRect(adjX, adjY, config.barWidth() - BORDER, config.barHeight() - BORDER, config.arcSize(), config.arcSize());
//        g.fillRoundRect(adjX, adjY, config.barWidth(), config.barHeight(), config.arcSize(), config.arcSize());
//
//        renderRestore(config, g, adjX, adjY, width);
//
//        g.setColor(fillColor);
//        g.fillRoundRect(adjX + BORDER, adjY + BORDER, fillHeight - BORDER * 2, filledWidth - BORDER, config.arcSize(), config.arcSize());
//
//        renderIconsAndText(config, g, adjX, adjY, width);
    }

    private void renderRestoreVertical(RHUD_Config config, Graphics2D g, int x, int y, int barWidth, int barHeight)
    {
        // bar is barWidth wide, barHeight tall
        int healVal = healSupplier.get();
        if (healVal <= 0) return;

        // The portion the bar is currently filled
        int currFill  = (int) Math.round((double) currVal / maxVal * barHeight);
        // The portion we would fill for “heal”
        int healFill  = (int) Math.round((double) healVal / maxVal * barHeight);

        // if (currVal + healVal > maxVal) => overheal logic if you want
        // Example:
        if (currVal + healVal > maxVal)
        {
            g.setColor(OVERHEAL_COLOR);
        }
        else
        {
            g.setColor(healColorSupplier.get());
        }

        // Place the “heal” bar *above* the current fill
        // So, if bar is filled from bottom up:
        // the top of the heal fill is (barHeight - currFill - healFill)
        // so fill a rectangle of height = healFill
        int top = y + (barHeight - currFill - healFill);
        g.fillRoundRect(x, top + 1, barWidth, healFill, config.arcSize(), config.arcSize());
    }

    private void renderRestoreHorizontal(RHUD_Config config, Graphics2D g, int x, int y, int barWidth, int barHeight)
    {
        int healVal = healSupplier.get();
        if (healVal <= 0) return;

        int currFill = (int) Math.round((double) currVal / maxVal * barWidth);
        int healFill = (int) Math.round((double) healVal / maxVal * barWidth);

        if (currVal + healVal > maxVal)
        {
            g.setColor(OVERHEAL_COLOR);
        }
        else
        {
            g.setColor(healColorSupplier.get());
        }

        // place the “heal” fill to the right of the current fill
        int left = x + currFill;
        g.fillRoundRect(left - 1, y, healFill, barHeight, config.arcSize(), config.arcSize());
    }

    private void renderRestoreGrid(RHUD_Config config, Graphics2D g, int x, int y, int barWidth, int barHeight)
    {
        int healVal = healSupplier.get();
        if (healVal <= 0) return;

        int currFill = (int) Math.round((double) currVal / maxVal * barWidth/2);
        int healFill = (int) Math.round((double) healVal / maxVal * barWidth/2);

        if (currVal + healVal > maxVal)
        {
            g.setColor(OVERHEAL_COLOR);
        }
        else
        {
            g.setColor(healColorSupplier.get());
        }

        // place the “heal” fill to the right of the current fill
        int left = x + currFill;
        g.fillRoundRect(left - 1, y, healFill, barHeight, config.arcSize(), config.arcSize());
    }

    private void renderIconsAndText(RHUD_Config config, Graphics2D g, int x, int y, int drawnWidth, int drawnHeight) {
        // Icons and counters overlap the bar at small widths, so they are not drawn when the bars are too small
        if (drawnWidth < MIN_ICON_AND_COUNTER_WIDTH) {
            return;
        }

        final Image icon = iconSupplier.get();

        final int iconHeight = icon.getHeight(null);
        final int iconWidth = icon.getWidth(null);

        int centerIconX = x + (drawnWidth / 2);
        int centerIconY = y + (drawnHeight / 2);
        int singleCenterIconY = y + 15;

        int iconX = centerIconX - (iconWidth / 2);
        int iconY = centerIconY - (iconHeight / 2);
        int singleIconY = singleCenterIconY - (iconHeight / 2);

        final String counterText = currVal + "/" + maxVal;
        final String singleCounterText = String.valueOf(currVal);
        FontMetrics metrics = g.getFontMetrics();
        final int textWidth = metrics.stringWidth(counterText);
        final int textHeight = metrics.getHeight();
        final int singleTextWidth = metrics.stringWidth(singleCounterText);

        int centerX = x + (drawnWidth / 2);
        int centerY = y + (drawnHeight / 2);
        int singleCenterY = y + (iconHeight + textHeight) + 5;

        int textX = centerX - (textWidth / 2);
        int singleTextX = centerX - (singleTextWidth / 2);
        int textY = centerY + (metrics.getAscent() / 2);
        int singleTextY = singleCenterY + (metrics.getAscent() / 2);

        if (config.enableSkillIcon()) {
            if (config.layout() == Layout.VIEW.VERTICAL && config.barWidth() > textWidth) {
                g.drawImage(icon, iconX, singleIconY + 3, null);
            } else if (config.layout() == Layout.VIEW.GRID && config.barHeight() > textHeight) {
                g.drawImage(icon, x + 5, iconY, null);
            } else {
                if (config.barHeight() > textHeight) {
                    g.drawImage(icon, x + 5, iconY, null);
                }
            }

            if (config.layout() == Layout.VIEW.VERTICAL) {
                if (config.barWidth() > textWidth) {
                    g.setColor(Color.BLACK);
                    g.drawString(singleCounterText, singleTextX + 1, singleTextY + 1);
                    g.setColor(ColorUtil.colorWithAlpha(Color.WHITE, 255));
                    g.drawString(singleCounterText, singleTextX, singleTextY);
                }
            } else if (config.layout() == Layout.VIEW.GRID) {
                if (config.barHeight() > textHeight) {
                    g.setColor(Color.BLACK);
                    g.drawString(counterText, textX + 1 - ((config.barWidth() / 2) / 2), textY + 1);
                    g.setColor(ColorUtil.colorWithAlpha(Color.WHITE, 255));
                    g.drawString(counterText, textX - ((config.barWidth() / 2) / 2), textY);
                }
            } else {
                if (config.barHeight() > textHeight) {
                    g.setColor(Color.BLACK);
                    g.drawString(counterText, textX + 1, textY + 1);
                    g.setColor(ColorUtil.colorWithAlpha(Color.WHITE, 255));
                    g.drawString(counterText, textX, textY);
                }
            }
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

    private static int getBarWidth ( int current, int base, int size)
    {
        final double ratio = (double) current / base;

        if (ratio >= 1) {
            return size;
        }

        return (int) Math.round(ratio * size);
    }
}
