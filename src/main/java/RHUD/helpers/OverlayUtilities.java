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