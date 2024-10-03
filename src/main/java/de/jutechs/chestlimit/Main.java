package de.jutechs.chestlimit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.GameEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Main implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();
        int MAX_CHESTS_PER_CHUNK = ConfigManager.config.maxChests;
        boolean enforceLimit = ConfigManager.config.enforceLimit;
        boolean theFunny = ConfigManager.config.theFunny;

        // Register item use callback
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

            // Get the item in the player's hand
            ItemStack itemStack = player.getStackInHand(hand);
            if (itemStack.getItem() == Items.CHEST || itemStack.getItem() == Items.TRAPPED_CHEST) {
                BlockPos blockPos = hitResult.getBlockPos();
                WorldChunk chunk = serverWorld.getWorldChunk(blockPos);

                int chestCount = 0;
                // Count the number of chests in the chunk
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof ChestBlockEntity) {
                        chestCount++;
                    }
                }

                // If the chest count exceeds the limit, send a message to the player
                if (chestCount >= MAX_CHESTS_PER_CHUNK) {
                        if (theFunny){
                            player.sendMessage(Text.literal("JUST PUT A FRIENDLY MESSAGE LIKE YOU ARE REACHING THE UPPER LIMIOT SERVER IS ABLE TO HANDLE TO NOT RISK LOSING ITEM PLEASE MOVE TO A NEW CHUNK")
                                    .formatted(Formatting.RED), false);
                        } else {
                            player.sendMessage(Text.literal("Too many chests in this chunk! Limit: " + MAX_CHESTS_PER_CHUNK)
                                    .formatted(Formatting.RED), false);
                        };
                    if (enforceLimit) {
                        return ActionResult.FAIL; // Block the chest from being placed
                    }else{
                        return ActionResult.PASS;
                    }
                }
            }

            return ActionResult.PASS; // Allow normal block placement
        });
    }
}