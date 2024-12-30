package anti.antibreak.config;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static anti.antibreak.AntiItemBreak.itemCategories;
import static anti.antibreak.ConfigManager.configFile;
import static anti.antibreak.ConfigManager.defaultConfig;

public class SettingWidget extends ElementListWidget<SettingWidget.Entry> {
    private final JsonObject editedConfigFile;
    private final ArrayList<String> settings;
    private final HashMap<String, Boolean> categories = new HashMap<>();
    private final JsonObject itemObject;

    public SettingWidget(int width, int height, ArrayList<String> settings, JsonObject eCF) {
        super(MinecraftClient.getInstance(), width, height - 24 - 35, 24, 25);

        this.settings = settings;

        editedConfigFile = eCF;
        for (String key : configFile.keySet()) {
            editedConfigFile.add(key, configFile.get(key));
        }
        itemObject = editedConfigFile.get("items").getAsJsonObject();

        for (String setting : settings) {
            if (setting.contains("_category")) {
                categories.put(setting, false);
            }
        }

        updateEntries();
    }

    private void updateEntries() {
        clearEntries();
        for (String setting : settings) {
            addEntry(new Entry(setting, false));
            if (categories.getOrDefault(setting, false)) {
                for (String child : itemCategories.get(setting.split("_")[0])) {
                    addEntry(new Entry(child, true));
                }
            }
        }
    }

    @Override
    protected int getScrollbarX() {
        return width - 15;
    }

    @Override
    public int getRowWidth() {
        return width - 15;
    }

    public class Entry extends ElementListWidget.Entry<Entry> {
        private ButtonWidget button;
        private ButtonWidget resetButton;
        public TextFieldWidget textField;
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
                // text
                if (false) {
                    this.textField = new TextFieldWidget(textRenderer, width / 2 + width / 4 - 50, 0, 100, 20, Text.of(setting));
                    this.textField.setText(editedConfigFile.get(setting).getAsString());
                    this.textField.setChangedListener(newValue -> textChanged(setting, newValue, this.resetButton));
                    textChanged(setting, this.textField.getText(), this.resetButton);
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
                context.drawCenteredTextWithShadow(textRenderer, displayText, width / 4, y + entryHeight / 2, 0xFFFFFF);
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

    private void textChanged(String setting, String newSvalue, ButtonWidget resetButton) {
        resetButton.active = !newSvalue.equals(defaultConfig.get(setting).getAsString());
    }
}