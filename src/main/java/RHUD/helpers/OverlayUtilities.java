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

import RHUD.RHUD_Overlay;
import lombok.extern.slf4j.Slf4j;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class OverlayUtilities {
    // Find and init RuneScape Bold font
    public static Font initRuneScapeBold() {
        Font boldFont;

        try (InputStream inRunescapeBold = RHUD_Overlay.class.getResourceAsStream("RuneScape-Bold-12.ttf")) {
            if (inRunescapeBold == null) {
                log.warn("Font file could not be loaded.");
                boldFont = new Font(Font.DIALOG, Font.BOLD, 16);
            } else {
                boldFont = Font.createFont(Font.TRUETYPE_FONT, inRunescapeBold)
                        .deriveFont(Font.PLAIN, 16);
            }
        } catch (FontFormatException ex) {
            log.warn("Font loaded, but format incorrect.", ex);
            boldFont = new Font(Font.DIALOG, Font.BOLD, 16);
        } catch (IOException ex) {
            log.warn("Font file not found.", ex);
            boldFont = new Font(Font.DIALOG, Font.BOLD, 16);
        }
        return boldFont;
    }
}