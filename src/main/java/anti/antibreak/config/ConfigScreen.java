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
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static anti.antibreak.ConfigManager.configFile;

public class ConfigScreen extends Screen {
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    private final JsonObject editedConfigFile = new JsonObject();
    private final Screen PARENT;

    public ConfigScreen(Screen screen) {
        super(Text.translatable("anti.antibreak.title"));
        this.PARENT = screen;
    }

    @Override
    public void init() {
        Tab[] tabs = new Tab[1];
        tabs[0] = new newTab(Text.translatable("anti.antibreak.config.tabs.general").getString(), new ArrayList<>(List.of("enable_mod")));

        TabNavigationWidget tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabs).build();
        this.addDrawableChild(tabNavigation);

        ButtonWidget saveButton = ButtonWidget.builder(Text.translatable("DONE!!!"), btn -> saveFile()).dimensions(width / 4, height - 25, width / 2, 20).build();
        this.addDrawableChild(saveButton);

        tabNavigation.selectTab(0, false);
        tabNavigation.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        client.setScreen(PARENT);
    }

    private class newTab extends GridScreenTab {
        public SettingWidget settingWidget;
        public newTab(String tabName, ArrayList<String> settings) {
            super(Text.of(tabName));
            GridWidget.Adder adder = grid.createAdder(1);

            settingWidget = new SettingWidget(width, height, settings, editedConfigFile);
            adder.add(settingWidget);
        }
    }

    private void saveFile() {
        for (Element tab : ((TabNavigationWidget) this.children().get(0)).children()) {
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
        close();
    }
}