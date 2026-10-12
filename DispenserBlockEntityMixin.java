package com.example.papergame.mixin;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper Game rig.
 *
 * Name the LEFT dispenser "paper game left" on an anvil. Only that dispenser is
 * affected. Each time it picks a slot, there is RIG_CHANCE of it picking the
 * highest filled slot (9 if the bottom right is filled) instead of a random one.
 *
 * Odds with all 9 slots filled in both dispensers (random slot each pulse):
 *   fair:           left wins 44.4%, tie 11.1%, right wins 44.4%
 *   RIG_CHANCE 0.3: left wins ~57.8%
 *   RIG_CHANCE 0.5: left wins ~66.7%
 */
@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin {

    private static final String LEFT_NAME = "paper game left";
    private static final float RIG_CHANCE = 0.3f;

    @Inject(method = "chooseNonEmptySlot", at = @At("HEAD"), cancellable = true)
    private void papergame$rig(Random random, CallbackInfoReturnable<Integer> cir) {
        DispenserBlockEntity self = (DispenserBlockEntity) (Object) this;

        Text name = self.getCustomName();
        if (name == null || !name.getString().trim().equalsIgnoreCase(LEFT_NAME)) {
            return;
        }
        if (random.nextFloat() >= RIG_CHANCE) {
            return; // behave normally this time so it looks fair
        }

        // Slots run 0-8, left to right, top to bottom: slot 0 = "1" (top left),
        // slot 8 = "9" (bottom right). Take the highest filled slot.
        for (int i = self.size() - 1; i >= 0; i--) {
            ItemStack stack = self.getStack(i);
            if (!stack.isEmpty()) {
                cir.setReturnValue(i);
                return;
            }
        }
    }
}
