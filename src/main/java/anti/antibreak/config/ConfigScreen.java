package anti.antibreak.config;

import anti.antibreak.ConfigManager;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TabButtonWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static anti.antibreak.ConfigManager.configFile;
import static anti.antibreak.AntiItemBreak.LOGGER;

public class ConfigScreen extends Screen {
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    public static HashMap<String, String> errors = new HashMap<>();
    private final JsonObject editedConfigFile = new JsonObject();
    private final Screen PARENT;
    private ButtonWidget saveButton;

    private final Identifier discordTexture = Identifier.of("antibreak", "textures/gui/discord_logo.png");

    public ConfigScreen(Screen screen) {
        super(Text.translatable("anti.antibreak.title"));
        this.PARENT = screen;
    }

    @Override
    public void init() {
        ButtonWidget saveButton = ButtonWidget.builder(Text.translatable("anti.antibreak.config.button_text.done"), btn -> close()).dimensions(width / 4, height - 25, width / 2, 20).build();
        this.addDrawableChild(saveButton);
        this.saveButton = saveButton;

        Tab[] tabs = new Tab[2];
        tabs[0] = new newTab(this, Text.translatable("anti.antibreak.config.tabs.general").getString(), new ArrayList<>(List.of("enable_mod", "min_durability")), false);
        tabs[1] = new newTab(this, Text.translatable("anti.antibreak.config.tabs.filters").getString(), new ArrayList<>(List.of("wooden_category", "stone_category", "iron_category", "gold_category", "diamond_category", "netherite_category", "other_category")), true);

        TabNavigationWidget tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabs).build();
        this.addDrawableChild(tabNavigation);

        tabNavigation.selectTab(0, false);
        tabNavigation.init();

        ButtonWidget discordButton = ButtonWidget.builder(Text.empty(), btn -> openDiscord()).dimensions(width - 25, height - 25, 20, 20).build();
        this.addDrawableChild(discordButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTexture(RenderLayer::getGuiTextured, discordTexture, width - 21, height - 21, 0, 0, 12, 12, 12, 12);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return errors.isEmpty();
    }

    @Override
    public void close() {
        saveFile();
        client.setScreen(PARENT);
    }

    public class newTab extends GridScreenTab {
        public SettingWidget settingWidget;
        public newTab(ConfigScreen parent, String tabName, ArrayList<String> settings, boolean searchBar) {
            super(Text.of(tabName));
            GridWidget.Adder adder = grid.createAdder(1);

            SearchField searchField = searchBar ? new SearchField(this, textRenderer, width) : null;

            if (searchBar) {
                adder.add(searchField);
            }

            settingWidget = new SettingWidget(width, height, settings, editedConfigFile, parent, searchBar);
            adder.add(settingWidget);
        }
    }

    private void saveFile() {
        for (Element tab : ((TabNavigationWidget) this.children().get(1)).children()) {
            newTab tabElm = (newTab) ((TabButtonWidget) tab).getTab();
            for (SettingWidget.Entry a : tabElm.settingWidget.children()) {
                if (a.textField != null) {
                    editedConfigFile.addProperty(a.setting, a.textField.getText());
                }
            }
        }
        boolean saved = new ConfigManager().saveConfigFile(editedConfigFile);
        if (saved) {
            configFile = editedConfigFile;
        }
    }

    public void updateDoneButton() {
        saveButton.active = errors.isEmpty();
        if (!errors.isEmpty()) {
            saveButton.setMessage(Text.translatable("anti.antibreak.config.button_text.save_error").append(Text.literal(errors.values().toArray()[0].toString())).withColor(0xFF4F4F));
        } else {
            saveButton.setMessage(Text.translatable("anti.antibreak.config.button_text.done"));
        }
    }

    protected void openDiscord() {
        try {
            Util.getOperatingSystem().open("https://discord.gg/tqn38v6w7k");
            LOGGER.info("Anti Item Break: Opening discord support server invite link in browser... (https://discord.gg/tqn38v6w7k)");
        } catch (Exception e) {
            LOGGER.error("Anti Item Break: Failed to open discord link! Link: https://discord.gg/tqn38v6w7k");
            LOGGER.error(String.valueOf(e));
        }
    }
}