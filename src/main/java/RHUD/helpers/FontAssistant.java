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
import RHUD.RHUD_Config;
import net.runelite.client.ui.FontManager;

public class FontAssistant {
    private static Font font = null;
    private static boolean useRunescapeFont = true;
    private static String lastFont = "";
    private static int lastFontSize = 0;
    private static RHUD_Config.FontStyle lastFontStyle = RHUD_Config.FontStyle.DEFAULT;

    /**
     * Initialize the current font into the Graphics context.
     */
    public static void initFont(Graphics2D g)
    {
        if (font != null)
        {
            g.setFont(font);
            if (useRunescapeFont)
            {
                // The native RuneScape fonts look best with antialiasing off
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
        }
    }

    /**
     * Create or update the font. If 'fontName' is blank, use RuneScape fonts. Otherwise, use a Java Font.
     */
    public static void updateFont(String fontName, int fontSize, RHUD_Config.FontStyle fontStyle) {
        if (!lastFont.equals(fontName) || lastFontSize != fontSize || lastFontStyle != fontStyle) {
            lastFont = fontName;
            lastFontSize = fontSize;
            lastFontStyle = fontStyle;

            // Convert our config style enum to AWT style bits
            int awtStyle = mapFontStyle(fontStyle);

            if (fontName == null || fontName.isEmpty())
            {
                // Use the built-in RuneScape fonts
                // 1) Decide size: small or normal
                if (fontSize < 16)
                {
                    // small
                    if (fontStyle == RHUD_Config.FontStyle.BOLD || fontStyle == RHUD_Config.FontStyle.BOLD_ITALICS)
                    {
                        // If available, use bold small. If not, fallback to bold or small
                        // Many RuneLite versions only have getRunescapeSmallFont() or getRunescapeBoldFont().
                        font = FontManager.getRunescapeSmallFont();
                        // or if there's a getRunescapeBoldSmallFont(), call that
                    }
                    else
                    {
                        font = FontManager.getRunescapeSmallFont();
                    }
                }
                else
                {
                    // normal
                    if (fontStyle == RHUD_Config.FontStyle.BOLD || fontStyle == RHUD_Config.FontStyle.BOLD_ITALICS)
                    {
                        font = FontManager.getRunescapeBoldFont();
                    }
                    else
                    {
                        font = FontManager.getRunescapeFont();
                    }
                }
                useRunescapeFont = true;
            }
            else
            {
                // The user provided a custom TTF (e.g. "Arial"), so we can do any size or style
                font = new Font(fontName, awtStyle, fontSize);
                useRunescapeFont = false;
            }

//            if ("".equals(fontName)) {
//                if (fontSize < 16) {
//                    font = FontManager.getRunescapeSmallFont();
//                }
//
//                if (fontStyle == Config.FontStyle.BOLD || fontStyle == Config.FontStyle.BOLD_ITALICS) {
//                    font = Manager.RUNESCAPE_BOLD_FONT;
//                    style ^= Font.BOLD;
//                } else {
//                    font = FontManager.getRunescapeFont();
//                }
//                font = font.deriveFont(style);
//                useRunescapeFont = true;
//                return;
//            }
//            font = new Font(fontName, style, fontSize);
//            useRunescapeFont = false;
        }
    }

    /**
     * Convert our config's FontStyle enum to AWT Font style bits.
     */
    private static int mapFontStyle(RHUD_Config.FontStyle fontStyle)
    {
        switch (fontStyle)
        {
            case BOLD:
                return Font.BOLD;
            case ITALICS:
                return Font.ITALIC;
            case BOLD_ITALICS:
                return Font.BOLD | Font.ITALIC;
            case DEFAULT:
            default:
                return Font.PLAIN;
        }
    }
}
