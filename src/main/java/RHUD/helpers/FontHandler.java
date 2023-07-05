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

package RHUD.helpers;

import RHUD.RHUD_Config;
import net.runelite.client.ui.FontManager;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class FontHandler
{
    private String lastFont = "";
    private int lastFontSize = 0;
    private boolean useRunescapeFont = true;
    private RHUD_Config.FontStyle lastFontStyle = RHUD_Config.FontStyle.DEFAULT;
    private Font font = null;

    public void handleFont(Graphics2D graphics)
    {
        if (font != null)
        {
            graphics.setFont(font);
            if (useRunescapeFont)
            {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
        }
    }

    public void updateFont(String fontName, int fontSize,  RHUD_Config.FontStyle fontStyle)
    {
        if (!lastFont.equals(fontName) || lastFontSize != fontSize || lastFontStyle != fontStyle)
        {
            lastFont = fontName;
            lastFontSize = fontSize;
            lastFontStyle = fontStyle;

            int style = fontStyle.getStyle();
            // default to runescape fonts
            if ("".equals(fontName))
            {

                if (fontSize < 16)
                {
                    font = FontManager.getRunescapeSmallFont();
                }
                if (fontStyle == RHUD_Config.FontStyle.BOLD
                        || fontStyle == RHUD_Config.FontStyle.BOLD_ITALICS)
                {
                    font = RHUD.helpers.OverlayManager.RUNESCAPE_BOLD_FONT;
                    style ^= Font.BOLD; // Bold is implicit for this Font object, we do not want to derive using bold again.
                }
                else
                {
                    font = FontManager.getRunescapeFont();
                }

                font = font.deriveFont(style);

                useRunescapeFont = true;
                return;
            }

            // use a system wide font
            font = new Font(fontName, style,fontSize);
            useRunescapeFont = false;
        }
    }
}