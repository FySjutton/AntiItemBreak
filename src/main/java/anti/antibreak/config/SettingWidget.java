package anti.antibreak.config;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import static anti.antibreak.AntiItemBreak.itemCategories;
import static anti.antibreak.ConfigManager.configFile;
import static anti.antibreak.ConfigManager.defaultConfig;

public class SettingWidget extends ElementListWidget<SettingWidget.Entry> {
    private final ConfigScreen PARENT;
    private final JsonObject editedConfigFile;
    private final ArrayList<String> settings;
    private final JsonObject itemObject;
    private final SearchField searchField;

    private final HashMap<String, Boolean> categories = new HashMap<>();
    private final LinkedHashMap<String, List<String>> searchResults = new LinkedHashMap<>();

    public int width;
    public int height;

    private final Identifier searchIcon = new Identifier("icon/search");

    public SettingWidget(int width, int height, ArrayList<String> settings, JsonObject eCF, ConfigScreen parent, SearchField searchField) {
        super(MinecraftClient.getInstance(), width, height - 24 - 32 + (searchField != null ? -25 : 0), 24 + (searchField != null ? 25 : 0), height - 35, 25);

        this.width = width;
        this.height = height;

        this.settings = settings;
        this.PARENT = parent;
        this.searchField = searchField;

        editedConfigFile = eCF;
        for (String key : configFile.keySet()) {
            editedConfigFile.add(key, configFile.get(key));
        }
        itemObject = editedConfigFile.get("items").getAsJsonObject();

        for (String setting : settings) {
            categories.put(setting, false);
            if (searchField != null) {
                searchResults.put(setting, itemCategories.get(setting.split("_")[0]));
            }
        }

        updateEntries();
    }

    private void updateEntries() {
        clearEntries();

        if (searchField != null) {
            for (String key : searchResults.keySet()) {
                addEntry(new Entry(key, false));
                if (categories.get(key)) {
                    for (String child : searchResults.get(key)) {
                        addEntry(new Entry(child, true));
                    }
                }
            }
        } else {
            for (String setting : settings) {
                addEntry(new Entry(setting, false));
            }
        }

    }

    public void search(String text) {
        searchResults.clear();
        for (String setting : settings) {
            List<String> matches = itemCategories.get(setting.split("_")[0]).stream().filter(str -> Text.translatable(str).getString().toLowerCase().contains(text.toLowerCase())).toList();
            if (!matches.isEmpty()) {
                categories.put(setting, true);
                searchResults.put(setting, matches);
            }
        }
        updateEntries();
    }

    @Override
    protected int getScrollbarPositionX() {
        return width - 15;
    }

    @Override
    public int getRowWidth() {
        return width - 15;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (searchField != null) {
            searchField.render(context, mouseX, mouseY, delta);
            context.drawGuiTexture(searchIcon, width / 2 - width / 6 - 15, 31, 12, 12);
        }
    }

    public class Entry extends ElementListWidget.Entry<Entry> {
        private ButtonWidget button;
        private ButtonWidget resetButton;
        public CustomTextField textField;
        public String setting;
        private String displayText;

        private final TextRenderer textRenderer = client.textRenderer;

        public Entry(String setting, boolean child) {
            this.setting = setting;

            if (setting.contains("_category")) {
                this.button = ButtonWidget.builder(Text.translatable("anti.antibreak.config.filter." + setting), btn -> {
                            categories.put(setting, !categories.get(setting));
                            updateEntries();
                        })
                        .dimensions((width - width / 4 * 3) / 2, 0, width / 4 * 3, 20)
                        .build();
            } else {
                if (child) {
                    this.displayText = Text.translatable(setting).getString();
                } else {
                    this.displayText = Text.translatable("anti.antibreak.config.option." + setting).getString();
                }
                this.resetButton = ButtonWidget.builder(Text.translatable("anti.antibreak.config.button_text.reset"), btn -> resetButton(this.textField, this.button, setting, btn, child))
                        .dimensions(width / 2 + width / 4 - 50 + 100 + 3, 0, textRenderer.getWidth(Text.translatable("anti.antibreak.config.button_text.reset")) + 7, 20)
                        .build();
                if (setting.equals("min_durability")) {
                    this.textField = new CustomTextField(textRenderer, width / 2 + width / 4 - 49, 0, 98, 20, Text.of(setting), PARENT);
                    this.textField.setText(editedConfigFile.get(setting).getAsString());
                    this.textField.setMaxLength(4);
                    this.textField.setChangedListener(newValue -> textChanged(this.textField, setting, newValue, this.resetButton));
                    textChanged(textField, setting, this.textField.getText(), this.resetButton);
                } else {
                    this.button = ButtonWidget.builder(Text.empty(), btn -> buttonHandler(btn, setting, this.resetButton, child))
                            .dimensions(width / 2 + width / 4 - 50, 0, 100, 20)
                            .build();
                    displayButtonValue(this.button, setting, resetButton, child);
                }
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> children = new ArrayList<>();
            if (button != null) {
                children.add(button);
            }
            if (textField != null) {
                children.add(textField);
            }
            if (resetButton != null) {
                children.add(resetButton);
            }
            return children;
        }

        @Override
        public List<? extends Element> children() {
            List<Element> children = new ArrayList<>();
            if (button != null) {
                children.add(button);
            }
            if (textField != null) {
                children.add(textField);
            }
            if (resetButton != null) {
                children.add(resetButton);
            }
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (button != null) {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            }
            if (textField != null) {
                textField.setY(y);
                textField.render(context, mouseX, mouseY, tickDelta);
            }
            if (resetButton != null) {
                resetButton.setY(y);
                resetButton.render(context, mouseX, mouseY, tickDelta);
            }

            if (displayText != null) {
                context.drawCenteredTextWithShadow(textRenderer, displayText, width / 4, y + entryHeight / 2 - textRenderer.fontHeight / 2, 0xFFFFFF);
            }
        }
    }

    private void buttonHandler(ButtonWidget button, String setting, ButtonWidget resetButton, boolean child) {
        if (child) {
            if (itemObject.has(setting)) {
                int value = itemObject.get(setting).getAsInt();
                if (value == 0) {
                    itemObject.addProperty(setting, 1);
                } else {
                    itemObject.remove(setting);
                }
            } else {
                itemObject.addProperty(setting, 0);
            }
        } else {
            boolean newValue = !editedConfigFile.get(setting).getAsBoolean();
            editedConfigFile.addProperty(setting, newValue);
        }

        displayButtonValue(button, setting, resetButton, child);
    }

    private void displayButtonValue(ButtonWidget button, String setting, ButtonWidget resetButton, boolean child) {
        Text result;

        if (child) {
            int value = -1;
            if (itemObject.has(setting)) {
                value = itemObject.get(setting).getAsInt();
                if (value == 0) {
                    result = Text.translatable("anti.antibreak.config.button_text.enchanted_only");
                } else { // 1
                    result = Text.translatable("anti.antibreak.config.button_text.off");
                }
            } else {
                result = Text.translatable("anti.antibreak.config.button_text.on");
            }
            resetButton.active = value != -1;
        } else {
            boolean newValue = editedConfigFile.get(setting).getAsBoolean();
            result = Text.translatable("anti.antibreak.config.button_text." + (newValue ? "on" : "off"));
            resetButton.active = newValue != defaultConfig.get(setting).getAsBoolean();
        }

        button.setMessage(result);
    }

    private void resetButton(TextFieldWidget textField, ButtonWidget button, String setting, ButtonWidget resetButton, boolean child) {
        if (textField != null) {
            textField.setText(defaultConfig.get(setting).getAsString());
            resetButton.active = false;
        } else if (button != null) {
            if (child) {
                itemObject.remove(setting); // standard is always on, and on = nothing in the config file, therefore, remove
            } else {
                editedConfigFile.addProperty(setting, defaultConfig.get(setting).getAsBoolean());
            }
            displayButtonValue(button, setting, resetButton, child);
        }
    }

    private void textChanged(CustomTextField textField, String setting, String newValue, ButtonWidget resetButton) {
        resetButton.active = !newValue.equals(defaultConfig.get(setting).getAsString());

        if (setting.equals("min_durability")) {
            if (!newValue.matches("[1-9]\\d*")) {
                textField.setError(true, Text.translatable("anti.antibreak.message.error_min_durability").getString());
                return;
            }
        }

        textField.setError(false, null);
        editedConfigFile.addProperty(setting, newValue);
    }
}