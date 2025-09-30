package avox.antiitembreak;

import avox.antiitembreak.config.ConfigSystem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AntiItemBreak implements ModInitializer {
	public static final KeyBinding bypassKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			Text.translatable("avox.antiitembreak.keybind.bypass_key").getString(),
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_LEFT_ALT,
			KeyBinding.Category.create(Identifier.of(Text.translatable("avox.antiitembreak.title").getString()))
	));

	public static final LinkedHashMap<String, ArrayList<Item>> itemCategories = new LinkedHashMap<>();

	@Override
	public void onInitialize() {
        ConfigSystem.CONFIG.load();

		UseItemCallback.EVENT.register((player, world, hand) -> itemUsed(player, hand));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> itemUsed(player, hand));
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> itemUsed(player, hand));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> itemUsed(player, hand));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> updateCategoryList());
	}

	private ActionResult itemUsed(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);

		if (ConfigSystem.CONFIG.instance().enableMod && !bypassKeybind.isPressed()) {
			if (itemStack.isDamageable() && (itemStack.getMaxDamage() - itemStack.getDamage() <= ConfigSystem.CONFIG.instance().minimumDurability)) {
				Map<String, EntryType> overrides = ConfigSystem.CONFIG.instance().itemOverrides;
				String translationKey = itemStack.getItem().getTranslationKey();
				boolean blockClick = true;

				if (overrides.containsKey(translationKey)) {
					EntryType type = overrides.get(translationKey);
					if (type.equals(EntryType.ENCHANTED_ONLY) && !itemStack.hasEnchantments()) {
						blockClick = false;
					} else if (type.equals(EntryType.NEVER)) {
						blockClick = false;
					}
				}
				if (blockClick) {
					Text holdText;
					if (bypassKeybind.isUnbound()) {
						holdText = Text.translatable("avox.antiitembreak.message.no_bypass_btn");
					} else {
						holdText = Text.translatable("avox.antiitembreak.message.hold_to_bypass", bypassKeybind.getBoundKeyLocalizedText().getString());
					}
					player.sendMessage(Text.translatable("avox.antiitembreak.message.blocked_usage", itemStack.getMaxDamage() - itemStack.getDamage(), holdText).withColor(0xfff75959), true);
					return ActionResult.FAIL;
				}
			}
		}

		return ActionResult.PASS;
	}

	private void updateCategoryList() {
		itemCategories.clear();
        for (Item item : Registries.ITEM) {
            ItemStack stack = item.getDefaultStack();
            if (stack.isDamageable()) {
                // Block all armor pieces
                if (stack.get(DataComponentTypes.EQUIPPABLE) == null) {
                    String category = Arrays.stream(item.getTranslationKey().split("_")).toList().getFirst();
                    itemCategories.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
                }
            }
        }

        ArrayList<String> categoriesToRemove = new ArrayList<>();
        ArrayList<Item> otherCategory = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Item>> category : itemCategories.entrySet()) {
            if (category.getValue().size() <= 1) {
                categoriesToRemove.add(category.getKey());
                otherCategory.addAll(category.getValue());
            }
        }

        categoriesToRemove.forEach(itemCategories::remove);
        itemCategories.put("other", otherCategory);
	}
}