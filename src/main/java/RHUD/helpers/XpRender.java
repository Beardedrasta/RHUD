
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

        refresh();

        final int fillHeight = getBarHeight(maxVal, currVal, width);
        final int filledWidth = getBarWidth(maxVal, currVal, config.xpBarHeight());
        final Color fillColor = colorSupplier.get();

        int adjX;
        int adjY;
        adjY = y;
        adjX = x;

        g.setColor(BACKGROUND);
        g.drawRoundRect(adjX, adjY - 2, width - BORDER, config.xpBarHeight() - BORDER, config.arcSize(), config.arcSize());
        g.fillRoundRect(adjX, adjY - 2, width, config.xpBarHeight(), config.arcSize(), config.arcSize());

        renderRestore(config, g, adjX, adjY, width);

        g.setColor(fillColor);
        g.fillRoundRect(adjX + BORDER, adjY + BORDER - 2, fillHeight - BORDER * 2, filledWidth - BORDER, config.arcSize(), config.arcSize());

        float spacing = width / 10f;
        int notchCount = 9;
        for (int i = 1; i <= notchCount; i++) {
            int notchX = adjX + Math.round(i * spacing);
            int notchHeight = config.xpBarHeight();
            g.setColor(Color.BLACK);
            g.fillRect(notchX, adjY + 1 - 2, 1, notchHeight - 1);
        }
    }

    private void renderRestore(RHUD_Config config, Graphics2D g, int x, int y, int width) {
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
        g.fillRoundRect(fillX - 2, y + BORDER, fillWidth + 2, config.xpBarHeight() - BORDER, config.arcSize(), config.arcSize());
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
