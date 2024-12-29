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

import java.util.ArrayList;
import java.util.List;

import static anti.antibreak.ConfigManager.configFile;
import static anti.antibreak.ConfigManager.defaultConfig;

public class SettingWidget extends ElementListWidget<SettingWidget.Entry> {
    private final JsonObject editedConfigFile;

    public SettingWidget(int width, int height, ArrayList<String> settings, JsonObject eCF) {
        super(MinecraftClient.getInstance(), width, height - 24 - 35, 24, 25);

        editedConfigFile = eCF;
        for (String key : configFile.keySet()) {
            editedConfigFile.add(key, configFile.get(key));
        }

        for (String setting : settings) {
            addEntry(new Entry(setting));
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

        public Entry(String setting) {
            this.setting = setting;
            this.displayText = Text.translatable("anti.antibreak.config.option." + setting).getString();

            this.resetButton = ButtonWidget.builder(Text.translatable("anti.antibreak.config.button_text.reset"), btn -> resetButton(this.textField, this.button, setting, btn))
                    .dimensions(width / 2 + width / 4 - 50 + 100 + 3, 0, textRenderer.getWidth(Text.translatable("anti.antibreak.config.button_text.reset")) + 7, 20)
                    .build();
            // text
            if (false) {
                this.textField = new TextFieldWidget(textRenderer, width / 2 + width / 4 - 50, 0, 100, 20, Text.of(setting));
                this.textField.setText(editedConfigFile.get(setting).getAsString());
                this.textField.setChangedListener(newValue -> textChanged(setting, newValue, this.resetButton));
                textChanged(setting, this.textField.getText(), this.resetButton);
            } else {
                this.button = ButtonWidget.builder(Text.empty(), btn -> buttonHandler(btn, setting, this.resetButton))
                        .dimensions(width / 2 + width / 4 - 50, 0, 100, 20)
                        .build();
                displayButtonValue(this.button, setting, resetButton);
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
            children.add(resetButton);
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
            children.add(resetButton);
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
            resetButton.setY(y);
            resetButton.render(context, mouseX, mouseY, tickDelta);
            context.drawCenteredTextWithShadow(textRenderer, displayText, width / 4, y + entryHeight / 2, 0xFFFFFF);
        }
    }

    private void buttonHandler(ButtonWidget button, String setting, ButtonWidget resetButton) {
        boolean newValue = !editedConfigFile.get(setting).getAsBoolean();
        editedConfigFile.addProperty(setting, newValue);

        displayButtonValue(button, setting, resetButton);
    }

    private void displayButtonValue(ButtonWidget button, String setting, ButtonWidget resetButton) {
        Text result;

        boolean newValue = editedConfigFile.get(setting).getAsBoolean();
        result = Text.translatable("anti.antibreak.config.button_text." + (newValue ? "on" : "off"));
        resetButton.active = newValue != defaultConfig.get(setting).getAsBoolean();

        button.setMessage(result);
    }

    private void resetButton(TextFieldWidget textField, ButtonWidget button, String setting, ButtonWidget resetButton) {
        if (textField != null) {
            textField.setText(defaultConfig.get(setting).getAsString());
            resetButton.active = false;
        } else if (button != null) {
            editedConfigFile.addProperty(setting, defaultConfig.get(setting).getAsBoolean());
            displayButtonValue(button, setting, resetButton);
        }
    }

    private void textChanged(String setting, String newSvalue, ButtonWidget resetButton) {
        resetButton.active = !newSvalue.equals(defaultConfig.get(setting).getAsString());
    }
}