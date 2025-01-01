package anti.antibreak.config;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Timer;
import java.util.TimerTask;

public class SearchField extends TextFieldWidget {
    private Timer timer;
    private static final long DELAY = 800;
    private final String lastText = "";

    public SearchField(ConfigScreen.newTab tab, TextRenderer textRenderer, int width) {
        super(textRenderer, width / 2 - width / 6, 29, width / 3, 15, Text.of("Search Bar"));
        super.setChangedListener(text -> {
            if (!text.equals(lastText)) {
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        tab.settingWidget.search(text);
                        timer.cancel();
                    }
                }, DELAY);
            }
        });
    }
}