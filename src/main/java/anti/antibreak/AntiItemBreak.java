package anti.antibreak;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static anti.antibreak.ConfigManager.configFile;

public class AntiItemBreak implements ModInitializer {
	public static final String MOD_ID = "antibreak";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		new ConfigManager().checkConfig();

		UseItemCallback.EVENT.register((player, world, hand) -> itemUsed(player, hand));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> itemUsed(player, hand).getResult());
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> itemUsed(player, hand).getResult());
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> itemUsed(player, hand).getResult());
	}

	private TypedActionResult<ItemStack> itemUsed(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);

		if (configFile.get("enable_mod").getAsBoolean()) {
			if (itemStack.isDamageable() && (itemStack.getMaxDamage() - itemStack.getDamage() <= 1)) {
				return TypedActionResult.fail(itemStack);
			}
		}

		return TypedActionResult.pass(itemStack);
	}
}