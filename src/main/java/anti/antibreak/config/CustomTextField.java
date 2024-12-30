package anti.antibreak.config;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import static anti.antibreak.config.ConfigScreen.errors;

public class CustomTextField extends TextFieldWidget {
    private final ConfigScreen screen;
    public boolean error;

    public CustomTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text, ConfigScreen parent) {
        super(textRenderer, x, y, width, height, text);
        screen = parent;
    }

    public void setError(boolean value, String errorMessage) {
        error = value;
        if (value) {
            if (!errors.containsKey(this.getMessage().getString())) {
                errors.put(this.getMessage().getString(), errorMessage);
                this.setEditableColor(0xa83832);
            }
        } else {
            errors.remove(this.getMessage().getString());
            this.setEditableColor(0xFFFFFF);
        }
        screen.updateDoneButton();
    }
}
