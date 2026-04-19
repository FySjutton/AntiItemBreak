package avox.antiitembreak;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum EntryType implements NameableEnum {
    ALWAYS,
    ENCHANTED_ONLY,
    NEVER;

    @Override
    public Component getDisplayName() {
        if (name().equals("ALWAYS")) {
            return Component.translatable("avox.antiitembreak.type.always").withColor(0xff52ff71);
        } else if (name().equals("ENCHANTED_ONLY")) {
            return Component.translatable("avox.antiitembreak.type.enchanted_only").withColor(0xfff7f759);
        } else {
            return Component.translatable("avox.antiitembreak.type.never").withColor(0xfff75959);
        }
    }
}