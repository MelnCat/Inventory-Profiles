package org.anti_ad.mc.ipnext.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.container.SlotActionType;
import org.anti_ad.mc.ipnext.config.GuiSettings;
import org.anti_ad.mc.ipnext.event.ContinuousCraftingHandler;
import org.anti_ad.mc.ipnext.event.LockSlotsHandler;
import org.anti_ad.mc.ipnext.event.LockedSlotKeeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)

public class MixinPlayerInteractionManagerForLockedSlotsMovePrevention {

    @Inject(at = @At(value = "HEAD",
            target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;method_2906(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"),
            method = "method_2906",
            cancellable = true)
    public void clickSlot(int syncId,
                          int slotIndex,
                          int button,
                          SlotActionType actionType,
                          PlayerEntity player,
                          CallbackInfoReturnable<ItemStack> cir) {
        boolean move = actionType == SlotActionType.QUICK_MOVE;
        boolean thr = actionType == SlotActionType.THROW;
        if(!LockedSlotKeeper.INSTANCE.getProcessingLockedPickups() && (move || thr)) {
            if (!LockSlotsHandler.INSTANCE.isQMoveActionAllowed(slotIndex)) {
                cir.cancel();
            }
        }
        if (GuiSettings.INSTANCE.getCONTINUOUS_CRAFTING_SAVED_VALUE().getValue()) {
            ContinuousCraftingHandler.INSTANCE.setProcessingClick(true);
        }
    }

    @Inject(at = @At(value = "TAIL",
            target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;method_2906(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"),
            method = "method_2906")
    public void postClickSlot(int syncId,
                              int slotIndex,
                              int button,
                              SlotActionType actionType,
                              PlayerEntity player,
                              CallbackInfoReturnable<ItemStack> cir) {
        if (GuiSettings.INSTANCE.getCONTINUOUS_CRAFTING_SAVED_VALUE().getValue()) {
            ContinuousCraftingHandler.INSTANCE.setProcessingClick(false);
        }
    }


}