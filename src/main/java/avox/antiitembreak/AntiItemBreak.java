package avox.antiitembreak;

import avox.antiitembreak.config.ConfigSystem;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class AntiItemBreak implements ModInitializer {
	private static final KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("antiitembreak", "category.antiitembreak.antiitembreak"));
	public static final KeyMapping bypassKeybind = new KeyMapping(Component.translatable("avox.antiitembreak.keybind.bypass_key").getString(), GLFW.GLFW_KEY_LEFT_ALT, category);

	private static final LinkedHashMap<String, ArrayList<Item>> itemCategories = new LinkedHashMap<>();
	private static boolean categoriesInitialized = false;

	public static LinkedHashMap<String, ArrayList<Item>> getItemCategories() {
		if (!categoriesInitialized) {
			categoriesInitialized = true;
			updateCategoryList();
		}
		return itemCategories;
	}

	@Override
	public void onInitialize() {
        ConfigSystem.CONFIG.load();

		KeyMappingHelper.registerKeyMapping(bypassKeybind);

		UseItemCallback.EVENT.register((player, _, hand) -> itemUsed(player, hand));
		AttackEntityCallback.EVENT.register((player, _, hand, _, _) -> itemUsed(player, hand));
		AttackBlockCallback.EVENT.register((player, _, hand, _, _) -> itemUsed(player, hand));
		UseBlockCallback.EVENT.register((player, _, hand, _) -> itemUsed(player, hand));
	}

	private InteractionResult itemUsed(Player player, InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);

		if (ConfigSystem.CONFIG.instance().enableMod && !bypassKeybind.isDown()) {
			if (itemStack.isDamageableItem() && (itemStack.getMaxDamage() - itemStack.getDamageValue() <= ConfigSystem.CONFIG.instance().minimumDurability)) {
				Map<String, EntryType> overrides = ConfigSystem.CONFIG.instance().itemOverrides;
				String translationKey = itemStack.getItem().getDescriptionId();
				boolean blockClick = true;

				if (overrides.containsKey(translationKey)) {
					EntryType type = overrides.get(translationKey);
					if (type.equals(EntryType.ENCHANTED_ONLY) && !itemStack.isEnchanted()) {
						blockClick = false;
					} else if (type.equals(EntryType.NEVER)) {
						blockClick = false;
					}
				}
				if (blockClick) {
					Component holdText;
					if (bypassKeybind.isUnbound()) {
						holdText = Component.translatable("avox.antiitembreak.message.no_bypass_btn");
					} else {
						holdText = Component.translatable("avox.antiitembreak.message.hold_to_bypass", bypassKeybind.getTranslatedKeyMessage().getString());
					}
					player.sendOverlayMessage(Component.translatable("avox.antiitembreak.message.blocked_usage", itemStack.getMaxDamage() - itemStack.getDamageValue(), holdText).withColor(0xfff75959));
					return InteractionResult.FAIL;
				}
			}
		}

		return InteractionResult.PASS;
	}

	private static void updateCategoryList() {
		itemCategories.clear();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (stack.isDamageableItem()) {
                // Block all armor pieces
                if (stack.get(DataComponents.EQUIPPABLE) == null) {
                    String category = Arrays.stream(item.getDescriptionId().split("_")).toList().getFirst();
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