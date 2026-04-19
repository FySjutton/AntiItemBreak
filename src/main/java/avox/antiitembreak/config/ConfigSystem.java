package avox.antiitembreak.config;

import avox.antiitembreak.EntryType;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.SliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import avox.antiitembreak.AntiItemBreak;

public class ConfigSystem {
    public static final ConfigClassHandler<ConfigSystem> CONFIG = ConfigClassHandler.createBuilder(ConfigSystem.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("antiitembreak.json"))
                    .build())
            .build();

    @SerialEntry public boolean enableMod = true;
    @SerialEntry public int minimumDurability = 5;

    @SerialEntry public Map<String, EntryType> itemOverrides = new HashMap<>();

    public static Screen configScreen(Screen parent) {
        return YetAnotherConfigLib.create(CONFIG, (defaults, config, builder) -> builder
                .title(Component.translatable("avox.antiitembreak.title"))

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("avox.antiitembreak.config.tabs.general"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("avox.antiitembreak.config.enable_mod"))
                                .description(OptionDescription.of(Component.translatable("avox.antiitembreak.config.enable_mod.desc")))
                                .binding(true, () -> config.enableMod, newVal -> config.enableMod = newVal)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("avox.antiitembreak.config.min_durability"))
                                .description(OptionDescription.of(Component.translatable("avox.antiitembreak.config.min_durability.desc")))
                                .binding(5, () -> config.minimumDurability, newVal -> config.minimumDurability = newVal)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                    .range(1, 50)
                                    .step(1)
                                )
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                    .name(Component.translatable("avox.antiitembreak.config.tabs.filters"))
                    .groups(dynamicCategories(config))
                    .build()
                )
        ).generateScreen(parent);
    }

    private static List<OptionGroup> dynamicCategories(ConfigSystem config) {
        List<OptionGroup> categories = new ArrayList<>();

        for (Map.Entry<String, ArrayList<Item>> entry : AntiItemBreak.getItemCategories().entrySet()) {
            String categoryName = Arrays.stream(entry.getKey().split("\\."))
                    .reduce((first, second) -> second)
                    .orElse(Component.translatable("avox.antiitembreak.message.unknown_category").getString());
            ArrayList<Item> items = entry.getValue();

            OptionGroup category = OptionGroup.createBuilder()
                    .name(Component.literal(categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1)))
                    .options(categoryOptions(items, config))
                    .collapsed(true)
                    .build();

            categories.add(category);
        }

        return categories;
    }

    private static List<Option<?>> categoryOptions(List<Item> items, ConfigSystem config) {
        List<Option<?>> options = new ArrayList<>();

        for (Item item : items) {
            String key = item.getDescriptionId();

            options.add(
                    Option.<EntryType>createBuilder()
                            .name(Component.translatable(key))
                            .description(OptionDescription.of(Component.translatable("avox.antiitembreak.message.requirement_text", item.getName(new ItemStack(item)))))
                            .binding(
                                    EntryType.ALWAYS,
                                    () -> config.itemOverrides.getOrDefault(key, EntryType.ALWAYS),
                                    newVal -> {
                                        if (newVal == EntryType.ALWAYS) {
                                            config.itemOverrides.remove(key);
                                        } else {
                                            config.itemOverrides.put(key, newVal);
                                        }
                                    }
                            )
                            .controller(opt -> EnumControllerBuilder.create(opt).enumClass(EntryType.class))
                            .build()
            );
        }

        return options;
    }
}
