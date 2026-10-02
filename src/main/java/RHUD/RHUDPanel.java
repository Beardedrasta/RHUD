package RHUD;

import RHUD.helpers.Layout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class RHUDPanel extends PluginPanel
{
    private static final Color ORANGE = new Color(255, 152, 0);

    private final RHUD_Plugin plugin;
    private final RHUD_Config config;
    private final ConfigManager configManager;

    public RHUDPanel(RHUD_Plugin plugin, RHUD_Config config, ConfigManager configManager)
    {
        super(false);
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;

        rebuildFromConfig();
    }

    /**
     * Rebuild the sidebar from ConfigManager-backed values.
     * This keeps RHUD's custom control center synchronized when settings are
     * changed from RuneLite's normal plugin configuration menu.
     */
    void rebuildFromConfig()
    {
        int selectedTab = 0;
        for (java.awt.Component component : getComponents())
        {
            if (component instanceof JTabbedPane)
            {
                selectedTab = ((JTabbedPane) component).getSelectedIndex();
                break;
            }
        }

        removeAll();
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(ColorScheme.DARK_GRAY_COLOR);
        tabs.addTab("HUD", scrollTab(buildHudTab()));
        tabs.addTab("Bars", scrollTab(buildBarsTab()));
        tabs.addTab("XP", scrollTab(buildXpTab()));
        tabs.addTab("Warn", scrollTab(buildWarningsTab()));
        tabs.addTab("Look", scrollTab(buildAppearanceTab()));
        tabs.setMinimumSize(new Dimension(0, 0));

        // Config changes rebuild this panel so controls stay synchronized with
        // RuneLite's standard settings menu. Restore the page the user was on
        // instead of jumping back to HUD every time a setting changes.
        if (selectedTab >= 0 && selectedTab < tabs.getTabCount())
        {
            tabs.setSelectedIndex(selectedTab);
        }

        add(tabs, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel buildHeader()
    {
        JPanel header = panel();
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));

        JLabel title = new JLabel("RHUD", SwingConstants.CENTER);
        title.setForeground(ORANGE);
        title.setFont(title.getFont().deriveFont(20f));
        title.setAlignmentX(CENTER_ALIGNMENT);
        header.add(title);

        JLabel subtitle = new JLabel("HUD Control Center", SwingConstants.CENTER);
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        header.add(subtitle);
        return header;
    }

    private JPanel buildHudTab()
    {
        JPanel content = tabPanel();
        content.add(sectionLabel("EDIT HUD"));
        content.add(info("Use Edit Guides for visual placement. Hold Alt over RHUD to move it or drag an edge/corner to resize."));
        content.add(Box.createVerticalStrut(8));

        JButton edit = fullButton(plugin.isEditMode() ? "Hide Edit Guides" : "Show Edit Guides");
        edit.addActionListener(e -> {
            boolean enabled = !plugin.isEditMode();
            plugin.setEditMode(enabled);
            edit.setText(enabled ? "Hide Edit Guides" : "Show Edit Guides");
        });
        content.add(edit);
        content.add(Box.createVerticalStrut(5));

        JButton resetOverlay = fullButton("Reset HUD Position & Size");
        resetOverlay.addActionListener(e -> plugin.resetHudOverlay());
        content.add(resetOverlay);
        content.add(Box.createVerticalStrut(14));

        content.add(sectionLabel("LAYOUT"));
        content.add(comboRow("Orientation", Layout.VIEW.values(), config.layout(), "layout"));
        content.add(numberRow("Width", config.barWidth(), 90, 520, "barWidth"));
        content.add(numberRow("Bar Height", config.barHeight(), 2, 100, "barHeight"));
        content.add(numberRow("XP Bar Height", config.xpBarHeight(), 2, 100, "xpBarHeight"));
        content.add(numberRow("Corner Curve", config.arcSize(), 0, 10, "arcSize"));
        content.add(Box.createVerticalStrut(10));
        content.add(resetButton("Reset Layout Settings", new String[]{"layout", "barWidth", "barHeight", "xpBarHeight", "arcSize", "barOffsetX", "barOffsetY"}));
        return content;
    }

    private JPanel buildBarsTab()
    {
        JPanel content = tabPanel();
        content.add(sectionLabel("BAR ASSIGNMENTS"));
        content.add(info("Choose what each RHUD row displays. Changes update the HUD immediately."));
        content.add(Box.createVerticalStrut(8));
        content.add(comboRow("Bar 1", Layout.XPBARMODE.values(), config.bar1BarMode(), "bar1BarMode"));
        content.add(comboRow("Bar 2", Layout.BARMODE.values(), config.bar2BarMode(), "bar2BarMode"));
        content.add(comboRow("Bar 3", Layout.BARMODE.values(), config.bar3BarMode(), "bar3BarMode"));
        content.add(comboRow("Bar 4", Layout.BARMODE.values(), config.bar4BarMode(), "bar4BarMode"));
        content.add(comboRow("Bar 5", Layout.BARMODE.values(), config.bar5BarMode(), "bar5BarMode"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("BAR BEHAVIOR"));
        content.add(numberRow("Bar Spacing", config.barSpacing(), 0, 12, "barSpacing"));
        content.add(toggleRow("Icons & Text", config.enableSkillIcon(), "enableSkillIcon"));
        content.add(toggleRow("Life-based HP Color", config.lifeColor(), "lifeColor"));
        content.add(toggleRow("Hide Behind Interfaces", config.hideInInterfaces(), "hideInterfaces"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("INDIVIDUAL BAR STYLE"));
        content.add(info("Turn off Use Global for a bar to give it its own height, background, icons/text and opacity."));
        content.add(Box.createVerticalStrut(6));
        content.add(individualBarPanel(1, config.bar1UseGlobalStyle(), config.bar1Height(), false, RHUD_Config.BarTextFormat.HIDDEN, config.bar1ShowBackground(), config.bar1Opacity(), config.bar1UseCustomColor(), config.bar1CustomColor()));
        content.add(individualBarPanel(2, config.bar2UseGlobalStyle(), config.bar2Height(), config.bar2ShowIcon(), config.bar2TextFormat(), config.bar2ShowBackground(), config.bar2Opacity(), config.bar2UseCustomColor(), config.bar2CustomColor()));
        content.add(individualBarPanel(3, config.bar3UseGlobalStyle(), config.bar3Height(), config.bar3ShowIcon(), config.bar3TextFormat(), config.bar3ShowBackground(), config.bar3Opacity(), config.bar3UseCustomColor(), config.bar3CustomColor()));
        content.add(individualBarPanel(4, config.bar4UseGlobalStyle(), config.bar4Height(), config.bar4ShowIcon(), config.bar4TextFormat(), config.bar4ShowBackground(), config.bar4Opacity(), config.bar4UseCustomColor(), config.bar4CustomColor()));
        content.add(individualBarPanel(5, config.bar5UseGlobalStyle(), config.bar5Height(), config.bar5ShowIcon(), config.bar5TextFormat(), config.bar5ShowBackground(), config.bar5Opacity(), config.bar5UseCustomColor(), config.bar5CustomColor()));
        content.add(Box.createVerticalStrut(10));
        content.add(resetButton("Reset Bar Settings", new String[]{"bar1BarMode", "bar2BarMode", "bar3BarMode", "bar4BarMode", "bar5BarMode", "enableSkillIcon", "lifeColor", "hideInterfaces", "barSpacing", "bar1UseGlobalStyle", "bar1Height", "bar1ShowBackground", "bar1Opacity", "bar2UseGlobalStyle", "bar2Height", "bar2ShowBackground", "bar2Opacity", "bar3UseGlobalStyle", "bar3Height", "bar3ShowBackground", "bar3Opacity", "bar4UseGlobalStyle", "bar4Height", "bar4ShowBackground", "bar4Opacity", "bar5UseGlobalStyle", "bar5Height", "bar5ShowBackground", "bar5Opacity", "bar1UseCustomColor", "bar1CustomColor", "bar2UseCustomColor", "bar2CustomColor", "bar2ShowIcon", "bar2TextFormat", "bar3UseCustomColor", "bar3CustomColor", "bar3ShowIcon", "bar3TextFormat", "bar4UseCustomColor", "bar4CustomColor", "bar4ShowIcon", "bar4TextFormat", "bar5UseCustomColor", "bar5CustomColor", "bar5ShowIcon", "bar5TextFormat"}));
        return content;
    }

    private JPanel individualBarPanel(int bar, boolean useGlobal, int height, boolean showIcon,
                                      RHUD_Config.BarTextFormat textFormat, boolean background,
                                      int opacity, boolean useCustomColor, Color customColor)
    {
        JPanel box = panel();
        box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)), "Bar " + bar));
        box.setAlignmentX(LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, bar == 1 ? 230 : 300));

        box.add(toggleRow("Use Global Style", useGlobal, "bar" + bar + "UseGlobalStyle"));
        box.add(numberRow("Height", height, 2, 100, "bar" + bar + "Height"));

        if (bar != 1)
        {
            box.add(toggleRow("Show Icon", showIcon, "bar" + bar + "ShowIcon"));
            box.add(comboRow("Text", RHUD_Config.BarTextFormat.values(), textFormat, "bar" + bar + "TextFormat"));
        }

        box.add(toggleRow("Background", background, "bar" + bar + "ShowBackground"));
        box.add(numberRow("Opacity %", opacity, 10, 100, "bar" + bar + "Opacity"));
        box.add(toggleRow("Custom Color", useCustomColor, "bar" + bar + "UseCustomColor"));
        box.add(colorRow("Bar Color", customColor, "bar" + bar + "CustomColor"));

        JButton reset = new JButton("Reset This Bar");
        reset.setAlignmentX(LEFT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        reset.addActionListener(e -> {
            String prefix = "bar" + bar;
            String[] keys = bar == 1
                    ? new String[]{prefix+"UseGlobalStyle", prefix+"Height", prefix+"ShowBackground", prefix+"Opacity", prefix+"UseCustomColor", prefix+"CustomColor"}
                    : new String[]{prefix+"UseGlobalStyle", prefix+"Height", prefix+"ShowIcon", prefix+"TextFormat", prefix+"ShowBackground", prefix+"Opacity", prefix+"UseCustomColor", prefix+"CustomColor"};
            for (String key : keys) configManager.unsetConfiguration(RHUD_Config.GROUP, key);
        });
        box.add(Box.createVerticalStrut(4));
        box.add(reset);
        return box;
    }

    private JPanel buildXpTab()
    {
        JPanel content = tabPanel();
        content.add(sectionLabel("TRACKING"));
        content.add(toggleRow("Enable Tracking", config.enableXpTracking(), "enableXpTracking"));
        content.add(toggleRow("Track Recent Skill", config.mostRecentSkill(), "mostRecentSkill"));
        content.add(toggleRow("Ignore Hitpoints", config.ignoreHitpoints(), "ignoreHitpoints"));
        content.add(comboRow("Active Skill", Skill.values(), config.skill(), "skill"));
        content.add(comboRow("Tracker Anchor", Layout.POINT.values(), config.pos(), "pos"));
        content.add(numberRow("Tracker Width", config.trackerWidth(), 90, 520, "trackerWidth"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("TRACKER DETAILS"));
        content.add(toggleRow("XP Needed", config.xpNeeded(), "xpNeeded"));
        content.add(toggleRow("Actions Needed", config.actionsNeeded(), "actionsNeeded"));
        content.add(toggleRow("XP Gained", config.xpGained(), "xpGained"));
        content.add(toggleRow("XP / Hour", config.xpHour(), "xpHour"));
        content.add(toggleRow("Time to Level", config.showTTG(), "showTTG"));
        content.add(toggleRow("Level Percent", config.showPercent(), "showPercent"));
        content.add(Box.createVerticalStrut(10));
        content.add(resetButton("Reset XP Settings", new String[]{"enableXpTracking", "mostRecentSkill", "ignoreHitpoints", "skill", "pos", "trackerWidth", "xpNeeded", "actionsNeeded", "xpGained", "xpHour", "showTTG", "showPercent"}));
        return content;
    }

    private JPanel buildWarningsTab()
    {
        JPanel content = tabPanel();

        content.add(sectionLabel("LOW HEALTH"));
        content.add(toggleRow("Enable Warning", config.lowHealthWarning(), "lowHealthWarning"));
        content.add(numberRow("Trigger At", config.lowHealthThreshold(), 1, 99, "lowHealthThreshold"));
        content.add(colorRow("Glow Color", config.lowHealthWarningColor(), "lowHealthWarningColor"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("LOW PRAYER"));
        content.add(toggleRow("Enable Warning", config.lowPrayerWarning(), "lowPrayerWarning"));
        content.add(numberRow("Trigger At", config.lowPrayerThreshold(), 1, 99, "lowPrayerThreshold"));
        content.add(colorRow("Glow Color", config.lowPrayerWarningColor(), "lowPrayerWarningColor"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("WARNING EFFECT"));
        content.add(toggleRow("Pulse Glow", config.warningPulse(), "warningPulse"));
        content.add(numberRow("Glow Size", config.warningGlowSize(), 1, 8, "warningGlowSize"));
        content.add(Box.createVerticalStrut(10));
        content.add(resetButton("Reset Warning Settings", new String[]{
                "lowHealthWarning", "lowHealthThreshold", "lowHealthWarningColor",
                "lowPrayerWarning", "lowPrayerThreshold", "lowPrayerWarningColor",
                "warningPulse", "warningGlowSize"
        }));
        return content;
    }

    private JPanel buildAppearanceTab()
    {
        JPanel content = tabPanel();
        content.add(sectionLabel("HUD APPEARANCE"));
        content.add(toggleRow("Bar Background", config.showBarBackground(), "showBarBackground"));
        content.add(toggleRow("HUD Backdrop / Border", config.showHudBackdrop(), "showHudBackdrop"));
        content.add(toggleRow("Player Header", config.showHeader(), "showHeader"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("GLOBAL BAR COLORS"));
        content.add(info("These colors are used by bars that still have Use Global Style enabled."));
        content.add(colorRow("Health", config.colorHealthBar(), "colorHealthBar"));
        content.add(colorRow("Prayer", config.colorPrayBar(), "colorPrayBar"));
        content.add(colorRow("Run Energy", config.colorRunBar(), "colorRunBar"));
        content.add(colorRow("Special Attack", config.colorSpecialBar(), "colorSpecialBar"));
        content.add(colorRow("XP Bar", config.colorXP(), "xpbarColor"));
        content.add(Box.createVerticalStrut(12));

        content.add(sectionLabel("FONT"));
        content.add(numberRow("Font Size", config.fontSize(), 8, 40, "fontSize"));
        content.add(comboRow("Font Style", RHUD_Config.FontStyle.values(), config.fontStyle(), "fontStyle"));
        content.add(Box.createVerticalStrut(10));
        content.add(resetButton("Reset Appearance", new String[]{"showBarBackground", "showHudBackdrop", "showHeader", "fontName", "fontSize", "fontStyle", "colorHealthBar", "colorPrayBar", "colorRunBar", "colorSpecialBar", "xpbarColor"}));
        return content;
    }

    private JScrollPane scrollTab(JPanel content)
    {
        JScrollPane scroll = new JScrollPane(
                content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setBorder(null);
        scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scroll.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Critical: keep the RHUD panel from setting RuneLite's minimum frame
        // height to the total height of every control on the selected page.
        scroll.setMinimumSize(new Dimension(0, 0));
        scroll.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 420));

        return scroll;
    }

    private JPanel tabPanel()
    {
        JPanel content = new ViewportWidthPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ColorScheme.DARK_GRAY_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(12, 8, 20, 8));
        return content;
    }

    private JPanel panel()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(ColorScheme.DARK_GRAY_COLOR);
        return p;
    }

    private JLabel sectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(ORANGE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JLabel info(String text)
    {
        JLabel label = new JLabel("<html><body style='width:165px'>" + text + "</body></html>");
        label.setForeground(Color.LIGHT_GRAY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JPanel numberRow(String labelText, int value, int min, int max, String key)
    {
        JPanel row = baseRow();
        JLabel label = rowLabel(labelText);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(Math.max(min, Math.min(max, value)), min, max, 1));
        spinner.setPreferredSize(new Dimension(78, 26));
        spinner.addChangeListener(e -> {
            set(key, ((Number) spinner.getValue()).intValue());
            plugin.refreshHudFromConfig();
        });
        row.add(label);
        row.add(spinner);
        return row;
    }

    private JPanel toggleRow(String labelText, boolean value, String key)
    {
        JPanel row = baseRow();
        JLabel label = rowLabel(labelText);
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(value);
        checkBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        checkBox.addActionListener(e -> {
            set(key, checkBox.isSelected());
            plugin.refreshHudFromConfig();
        });
        row.add(label);
        row.add(checkBox);
        return row;
    }

    private <T> JPanel comboRow(String labelText, T[] values, T selected, String key)
    {
        JPanel row = baseRow();
        JLabel label = rowLabel(labelText);
        JComboBox<T> combo = new JComboBox<>(values);
        combo.setSelectedItem(selected);
        combo.setPreferredSize(new Dimension(112, 26));
        combo.addActionListener(e -> {
            Object value = combo.getSelectedItem();
            if (value != null)
            {
                set(key, value);
                plugin.refreshHudFromConfig();
            }
        });
        row.add(label);
        row.add(combo);
        return row;
    }

    private JButton resetButton(String text, String[] keys)
    {
        JButton button = fullButton(text);
        button.addActionListener(e -> {
            for (String key : keys)
            {
                configManager.unsetConfiguration(RHUD_Config.GROUP, key);
            }
            plugin.refreshHudFromConfig();
        });
        return button;
    }

    private JButton fullButton(String text)
    {
        JButton button = new JButton(text);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return button;
    }

    private JPanel colorRow(String labelText, Color current, String key)
    {
        JPanel row = baseRow();
        row.add(rowLabel(labelText));

        JButton button = new JButton();
        button.setBackground(current);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setToolTipText("Click to choose a color");
        button.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose " + labelText, button.getBackground());
            if (chosen != null)
            {
                button.setBackground(chosen);
                set(key, chosen);
                plugin.refreshHudFromConfig();
            }
        });
        row.add(button);
        return row;
    }

    private JLabel rowLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel baseRow()
    {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setBackground(ColorScheme.DARK_GRAY_COLOR);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    /**
     * A BoxLayout panel that always follows the width of its JScrollPane
     * viewport. This prevents RHUD controls from being clipped off the right
     * side when the vertical scrollbar is visible.
     */
    private static class ViewportWidthPanel extends JPanel implements Scrollable
    {
        @Override
        public Dimension getPreferredScrollableViewportSize()
        {
            return new Dimension(190, 420);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            return Math.max(32, visibleRect.height - 32);
        }

        @Override
        public boolean getScrollableTracksViewportWidth()
        {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight()
        {
            return false;
        }
    }

    private void set(String key, Object value)
    {
        configManager.setConfiguration(RHUD_Config.GROUP, key, value);
    }
}
