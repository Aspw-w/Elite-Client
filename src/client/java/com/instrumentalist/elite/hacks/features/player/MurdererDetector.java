package com.instrumentalist.elite.hacks.features.player;

import com.instrumentalist.elite.events.features.UpdateEvent;
import com.instrumentalist.elite.events.features.WorldEvent;
import com.instrumentalist.elite.hacks.Module;
import com.instrumentalist.elite.hacks.ModuleCategory;
import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;

public class MurdererDetector extends Module {

    public MurdererDetector() {
        super("Murderer Detector", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    public static ArrayList<PlayerEntity> murderers = new ArrayList<>();

    private final ArrayList<Item> items = new ArrayList<>(Arrays.asList(
            Items.IRON_SWORD,
            Items.ENDER_CHEST,
            Items.STONE_SWORD,
            Items.IRON_SHOVEL,
            Items.STICK,
            Items.WOODEN_AXE,
            Items.WOODEN_SWORD,
            Items.DEAD_BUSH,
            Items.SUGAR_CANE,
            Items.STONE_SHOVEL,
            Items.BLAZE_ROD,
            Items.DIAMOND_SHOVEL,
            Items.QUARTZ,
            Items.PUMPKIN_PIE,
            Items.GOLDEN_PICKAXE,
            Items.LEAD,
            Items.NAME_TAG,
            Items.CHARCOAL,
            Items.FLINT,
            Items.BONE,
            Items.CARROT,
            Items.GOLDEN_CARROT,
            Items.COOKIE,
            Items.DIAMOND_AXE,
            Items.ROSE_BUSH,
            Items.PRISMARINE_SHARD,
            Items.COOKED_BEEF,
            Items.NETHER_BRICK,
            Items.COOKED_CHICKEN,
            Items.MUSIC_DISC_BLOCKS,
            Items.GOLDEN_HOE,
            Items.LAPIS_LAZULI,
            Items.GOLDEN_SWORD,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_HOE,
            Items.SHEARS,
            Items.SALMON,
            Items.RED_DYE,
            Items.BREAD,
            Items.OAK_BOAT,
            Items.GLISTERING_MELON_SLICE,
            Items.BOOK,
            Items.JUNGLE_SAPLING,
            Items.GOLDEN_AXE,
            Items.DIAMOND_PICKAXE,
            Items.GOLDEN_SHOVEL
    ));

    @Override
    public void onDisable() {
        murderers.clear();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onWorld(WorldEvent event) {
        murderers.clear();
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return;

        for (PlayerEntity player : IMinecraft.mc.world.getPlayers()) {
            if (!player.getName().getString().isBlank() && !murderers.contains(player) && (player.getMainHandStack().getName().getString().equalsIgnoreCase("knife") || items.contains(player.getMainHandStack().getItem()))) {
                murderers.add(player);
                ChatUtil.printChat("Murderer " + player.getName().getString() + " was detected!");
            }
        }
    }
}
