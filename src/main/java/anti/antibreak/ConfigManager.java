package anti.antibreak;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static anti.antibreak.AntiItemBreak.LOGGER;

public class ConfigManager {
    public static JsonObject configFile;
    public static JsonObject defaultConfig;

    public void checkConfig() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        if (Files.notExists(configDir.resolve("antiitembreak.json"))) {
            LOGGER.warn("Anti Item Break: Configuration file not found - generating new config file.");
            generateConfigFile();
        }
        generateConfigArray(false);
    }

    private void generateConfigFile() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            InputStream resource = ConfigManager.class.getResourceAsStream("/assets/antibreak/default_config/antiitembreak.json");
            FileUtils.copyInputStreamToFile(resource, new File(configDir + "/antiitembreak.json"));
        } catch (Exception e) {
            LOGGER.error("Anti Item Break: Could not generate a new antiitembreak.json file (config), the program will now close. This error should not normally occur, and if you need help, please join our discord server. This error indicates that there's something wrong with the jar file, or the program doesn't have access to write files.");
            LOGGER.error("Shutting down minecraft..."); // Should just inactivate mod instead?
            e.printStackTrace();
            MinecraftClient.getInstance().stop();
        }
    }

    private void generateConfigArray(boolean reCall) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        boolean error = false;
        String errorMessage = "";

        try {
            // Load default config
            InputStream resource = ConfigManager.class.getResourceAsStream("/assets/antibreak/default_config/antiitembreak.json");
            Reader reader = new InputStreamReader(resource);
            defaultConfig = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            // Load configuration file
            File config = new File(configDir + "/antiitembreak.json");
            FileReader fileReader = new FileReader(config);
            JsonElement elm = JsonParser.parseReader(fileReader);
            fileReader.close();

            // Validate the configuration file
            JsonObject obj = elm.getAsJsonObject();
            obj.get("enable_mod").getAsBoolean();
            obj.get("items").getAsJsonObject();

            configFile = obj;
        } catch (Exception e) {
            error = true;
            errorMessage = e.getMessage();
        }

        if (error) {
            if (reCall) {
                MinecraftClient.getInstance().stop();
                LOGGER.error("Anti Item Break: An unknown error occurred while trying to generate a new config. Failed multiple times.");
                if (!errorMessage.isEmpty()) {
                    LOGGER.error(errorMessage);
                }
                MinecraftClient.getInstance().stop();
            } else {
                generateConfigFile();
                generateConfigArray(true); // re-call this function
                LOGGER.warn("Anti Item Break: Corrupted configuration file detected, now regenerating a new one. Any previous options will be reset.");
            }
        }
    }

    public boolean saveConfigFile(JsonElement newConfigFile) {
        LOGGER.info("SAVING");
        Path configDir = FabricLoader.getInstance().getConfigDir();
        try {
            File elmFile = new File(configDir + "/antiitembreak.json");
            if (!elmFile.exists()) {
                Files.createDirectories(elmFile.getParentFile().toPath());
            }
            FileWriter writer = new FileWriter(elmFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            gson.toJson(newConfigFile, jsonWriter);
            jsonWriter.flush();
            jsonWriter.close();
            return true;
        } catch (Exception e) {
            LOGGER.error("Anti Item Break: Error saving config file! " + e.getMessage());
            return false;
        }
    }
}