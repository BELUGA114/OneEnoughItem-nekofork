package com.mafuyu404.oneenoughitem.mixin;

import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import com.mafuyu404.oneenoughitem.init.ReplacementControl;
import com.mafuyu404.oneenoughitem.init.Utils;
import com.mafuyu404.oneenoughitem.util.OEILog;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class)
public class ItemStackMixin {
    @Mutable
    @Shadow
    @Final
    @Deprecated
    @Nullable
    private Item item;

    @Mutable
    @Shadow
    @Final
    PatchedDataComponentMap components;

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL"))
    private void replace(ItemLike itemLike, int count, CallbackInfo ci) {
        performReplacement();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void replaceWithComponents(ItemLike itemLike, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        performReplacement();
    }

    private void performReplacement() {
        if (this.item == null) {
            return;
        }

        if (isInCreativeModeTabBuilding()) {
            return;
        }

        // 检查是否需要跳过替换（用于 GUI 显示等场景）
        if (ReplacementControl.shouldSkipReplacement()) {
            return;
        }

        Item newItem = null;
        String originItemId = null;
        
        // 先尝试使用 ItemRedirector（注册阶段）
        if (ItemRedirector.hasRedirects()) {
            newItem = ItemRedirector.lookup(this.item);
            if (newItem != null && newItem != this.item) {
                originItemId = Utils.getItemRegistryName(this.item);
                OEILog.debug("ItemRedirector replaced: {} -> {}",
                        originItemId, Utils.getItemRegistryName(newItem));
            }
        }
        
        // 如果 ItemRedirector 没有替换，尝试 ReplacementCache  ID 映射
        if (newItem == null || newItem == this.item) {
            String targetItemId = ReplacementCache.matchItem(Utils.getItemRegistryName(this.item));
            if (targetItemId != null) {
                newItem = Utils.getItemById(targetItemId);
                if (newItem != null) {
                    originItemId = Utils.getItemRegistryName(this.item);
                    OEILog.debug("ReplacementCache replaced: {} -> {}", originItemId, targetItemId);
                }
            }
        }
        
        // 3. 如果找到了新的物品，执行替换
        if (newItem != null && newItem != this.item) {
            DataComponentPatch currentPatch = this.components.asPatch();

            this.item = newItem;
            this.components = PatchedDataComponentMap.fromPatch(newItem.components(), currentPatch);
            newItem.verifyComponentsAfterLoad((ItemStack) (Object) this);

            OEILog.debug("Successfully replaced item {} with {}",
                    originItemId, Utils.getItemRegistryName(newItem));
        }
    }

    private boolean isInCreativeModeTabBuilding() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String methodName = element.getMethodName();

            if (className.contains("CreativeModeTab") ||
                    className.contains("CreativeModeTabs") ||
                    methodName.contains("buildContents") ||
                    methodName.contains("accept")) {
                return true;
            }
        }
        return false;
    }
}