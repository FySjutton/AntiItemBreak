package anti.antibreak;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static anti.antibreak.ConfigManager.configFile;

public class AntiItemBreak implements ModInitializer {
	public static final String MOD_ID = "antibreak";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final KeyBinding bypassKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			Text.translatable("anti.antibreak.keybind.bypass_key").getString(),
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_LEFT_ALT,
			Text.translatable("anti.antibreak.title").getString()
	));

	public static final HashMap<String, ArrayList<String>> itemCategories = new HashMap<>(); // category: list<items: minecraft:stone>

	@Override
	public void onInitialize() {
		new ConfigManager().checkConfig();

		UseItemCallback.EVENT.register((player, world, hand) -> itemUsed(player, hand));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> itemUsed(player, hand));
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> itemUsed(player, hand));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> itemUsed(player, hand));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> updateCategoryList());
	}

	private ActionResult itemUsed(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);

		if (configFile.get("enable_mod").getAsBoolean() && !bypassKeybind.isPressed()) {
			if (itemStack.isDamageable() && (itemStack.getMaxDamage() - itemStack.getDamage() <= configFile.get("min_durability").getAsInt())) {
				JsonObject itemsObj = configFile.get("items").getAsJsonObject();
				String translationKey = itemStack.getItem().getTranslationKey();
				boolean bypass = false;

				if (itemsObj.has(translationKey)) {
					int value = itemsObj.get(translationKey).getAsInt();
					if (value == 0 && !itemStack.hasEnchantments()) {
						bypass = true;
					} else if (value != 0) {
						bypass = true;
					}
				}
				if (!bypass) {
					String holdText;
					if (bypassKeybind.isUnbound()) {
						holdText = Text.translatable("anti.antibreak.message.no_bypass_btn").getString();
					} else {
						holdText = String.format(Text.translatable("anti.antibreak.message.hold_to_bypass").getString(), bypassKeybind.getBoundKeyLocalizedText().getString());
					}
					player.sendMessage(Text.of(String.format(Text.translatable("anti.antibreak.message.blocked_usage").getString(), itemStack.getMaxDamage() - itemStack.getDamage(), holdText)), true);
					return ActionResult.FAIL;
				}
			}
		}

		return ActionResult.PASS;
	}

	private void updateCategoryList() {
		itemCategories.clear();
		itemCategories.put("wooden", new ArrayList<>(List.of(
				Items.WOODEN_AXE.getTranslationKey(),
				Items.WOODEN_HOE.getTranslationKey(),
				Items.WOODEN_PICKAXE.getTranslationKey(),
				Items.WOODEN_SHOVEL.getTranslationKey(),
				Items.WOODEN_SWORD.getTranslationKey()
		)));

		itemCategories.put("stone", new ArrayList<>(List.of(
				Items.STONE_AXE.getTranslationKey(),
				Items.STONE_HOE.getTranslationKey(),
				Items.STONE_PICKAXE.getTranslationKey(),
				Items.STONE_SHOVEL.getTranslationKey(),
				Items.STONE_SWORD.getTranslationKey()
		)));

		itemCategories.put("iron", new ArrayList<>(List.of(
				Items.IRON_AXE.getTranslationKey(),
				Items.IRON_HOE.getTranslationKey(),
				Items.IRON_PICKAXE.getTranslationKey(),
				Items.IRON_SHOVEL.getTranslationKey(),
				Items.IRON_SWORD.getTranslationKey()
		)));

		itemCategories.put("gold", new ArrayList<>(List.of(
				Items.GOLDEN_AXE.getTranslationKey(),
				Items.GOLDEN_HOE.getTranslationKey(),
				Items.GOLDEN_PICKAXE.getTranslationKey(),
				Items.GOLDEN_SHOVEL.getTranslationKey(),
				Items.GOLDEN_SWORD.getTranslationKey()
		)));

		itemCategories.put("diamond", new ArrayList<>(List.of(
				Items.DIAMOND_AXE.getTranslationKey(),
				Items.DIAMOND_HOE.getTranslationKey(),
				Items.DIAMOND_PICKAXE.getTranslationKey(),
				Items.DIAMOND_SHOVEL.getTranslationKey(),
				Items.DIAMOND_SWORD.getTranslationKey()
		)));

		itemCategories.put("netherite", new ArrayList<>(List.of(
				Items.NETHERITE_AXE.getTranslationKey(),
				Items.NETHERITE_HOE.getTranslationKey(),
				Items.NETHERITE_PICKAXE.getTranslationKey(),
				Items.NETHERITE_SHOVEL.getTranslationKey(),
				Items.NETHERITE_SWORD.getTranslationKey()
		)));

		List<String> alreadyAdded = itemCategories.values().stream()
				.flatMap(List::stream)
				.toList();

		ArrayList<String> otherCategory = new ArrayList<>();

		for (Item item : Registries.ITEM) {
			ItemStack itemStack = new ItemStack(item, 1);
			if (itemStack.isDamageable()) {
				String translationKey = item.getTranslationKey();
				if (!alreadyAdded.contains(translationKey)) {
					otherCategory.add(translationKey);
				}
			}
		}

		itemCategories.put("other", otherCategory);
	}
}