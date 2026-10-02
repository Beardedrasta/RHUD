
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
import RHUD.RHUD_Config;

public class XpRender {
    private static final int BORDER = 1;

    private final Supplier<Integer> maxValSupplier;
    private final Supplier<Integer> currValSupplier;
    private final Supplier<Color> colorSupplier;
    private final Supplier<Color> healColorSupplier;
    private final Supplier<Integer> healSupplier;

    private int maxVal;
    private int currVal;

    private static final Color BACKGROUND = new Color(20, 20, 20, 150);
    private static final Color OVERHEAL_COLOR = new Color(216, 255, 139, 150);

    public XpRender(Supplier<Integer> maxValSupplier, Supplier<Integer> currValSupplier, Supplier<Integer> healSupplier, Supplier<Color> colorSupplier, Supplier<Color> healColorSupplier) {
        this.maxValSupplier = maxValSupplier;
        this.currValSupplier = currValSupplier;
        this.healSupplier = healSupplier;
        this.colorSupplier = colorSupplier;
        this.healColorSupplier = healColorSupplier;
    }


    private void refresh() {
        maxVal = maxValSupplier.get();
        currVal = currValSupplier.get();
    }


    public void renderBar(RHUD_Config config, Graphics2D g, int x, int y, int width) {
        renderBar(config, g, x, y, width, config.xpBarHeight(), config.showBarBackground(), 100, null);
    }

    public void renderBar(RHUD_Config config, Graphics2D g, int x, int y, int width, int customHeight, boolean showBackground, int opacityPercent, Color customColor) {

        refresh();
        Composite oldComposite = g.getComposite();
        float alpha = Math.max(0.10f, Math.min(1.0f, opacityPercent / 100f));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        final Color fillColor = customColor != null ? customColor : colorSupplier.get();
        final double ratio = maxVal <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, (double) currVal / maxVal));

        if (config.layout() == Layout.VIEW.VERTICAL) {
            final int barWidth = customHeight;
            final int barHeight = width;
            final int innerHeight = Math.max(0, barHeight - BORDER * 2);
            final int fillHeight = (int) Math.round(innerHeight * ratio);
            final int fillY = y + BORDER + (innerHeight - fillHeight);

            if (showBackground) {
                g.setColor(BACKGROUND);
                g.drawRoundRect(x - 2, y, barWidth + 2, barHeight, config.arcSize(), config.arcSize());
                g.fillRoundRect(x - 2, y, barWidth + 2, barHeight, config.arcSize(), config.arcSize());
            }

            g.setColor(fillColor);
            if (fillHeight > 0) {
                g.fillRoundRect(x + BORDER, fillY, Math.max(1, barWidth - BORDER * 2), fillHeight,
                        config.arcSize(), config.arcSize());
            }

            // Ten XP segments, rotated for the vertical layout.
            float spacing = barHeight / 10f;
            for (int i = 1; i <= 9; i++) {
                int notchY = y + Math.round(i * spacing);
                g.setColor(Color.BLACK);
                g.fillRect(x + 1, notchY, Math.max(1, barWidth - 1), 1);
            }
        } else {
            final int fillWidth = (int) Math.round(width * ratio);

            if (showBackground) {
                g.setColor(BACKGROUND);
                g.drawRoundRect(x, y - 2, width - BORDER, customHeight - BORDER, config.arcSize(), config.arcSize());
                g.fillRoundRect(x, y - 2, width, customHeight, config.arcSize(), config.arcSize());
            }

            renderRestore(config, g, x, y, width, customHeight);

            g.setColor(fillColor);
            if (fillWidth > 0) {
                g.fillRoundRect(x + BORDER, y + BORDER - 2,
                        Math.max(1, fillWidth - BORDER * 2),
                        Math.max(1, customHeight - BORDER),
                        config.arcSize(), config.arcSize());
            }

            float spacing = width / 10f;
            for (int i = 1; i <= 9; i++) {
                int notchX = x + Math.round(i * spacing);
                g.setColor(Color.BLACK);
                g.fillRect(notchX, y - 1, 1, Math.max(1, customHeight - 1));
            }
        }

        g.setComposite(oldComposite);
    }

    private void renderRestore(RHUD_Config config, Graphics2D g, int x, int y, int width, int customHeight) {
        final Color color = healColorSupplier.get();
        final int heal = healSupplier.get();

        if (heal <= 0) {
            return;
        }

        final int filledCurrentWidth = getBarHeight(maxVal, currVal, config.barWidth());
        final int filledHealWidth = getBarHeight(maxVal, heal, config.barWidth());
        final int fillX, fillWidth;
        g.setColor(color);

        if (filledHealWidth + filledCurrentWidth > config.barWidth()) {
            g.setColor(OVERHEAL_COLOR);
            fillX = x - BORDER + filledCurrentWidth;
            fillWidth = config.barWidth() - filledCurrentWidth - BORDER;
        } else {
            fillX = x - BORDER + (filledCurrentWidth - filledHealWidth) + filledHealWidth;
            fillWidth = filledHealWidth;
        }
        g.fillRoundRect(fillX - 2, y + BORDER, fillWidth + 2, customHeight - BORDER, config.arcSize(), config.arcSize());
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
