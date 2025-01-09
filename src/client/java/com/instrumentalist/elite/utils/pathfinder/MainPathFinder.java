package com.instrumentalist.elite.utils.pathfinder;

import com.instrumentalist.elite.utils.ChatUtil;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainPathFinder {
    private final ArrayList<Vec3d> path = new ArrayList<>();

    public MainPathFinder(final Vec3d startVec3, final Vec3d endVec3) {
        Vec3d startVec31 = new Vec3d(startVec3.x, startVec3.y, startVec3.z);
        Vec3d endVec31 = new Vec3d(endVec3.x, endVec3.y, endVec3.z);
    }

    public ArrayList<Vec3d> getPath() {
        return this.path;
    }

    public static boolean isValid(final int x, final int y, final int z, final boolean checkGround) {
        final BlockPos block1 = new BlockPos(x, y, z);
        final BlockPos block2 = new BlockPos(x, y + 1, z);
        final BlockPos block3 = new BlockPos(x, y - 1, z);
        return !isNotPassable(block1) && !isNotPassable(block2)
                && (isNotPassable(block3) || !checkGround)
                && canWalkOn(block3);
    }

    private static boolean isNotPassable(final BlockPos block) {
        Block b = Objects.requireNonNull(IMinecraft.mc.world).getBlockState(block).getBlock();

        return b.getDefaultState().isSolidBlock(IMinecraft.mc.world, block)
                || b == Blocks.GLASS
                || b == Blocks.GRAY_STAINED_GLASS
                || b == Blocks.GREEN_STAINED_GLASS
                || b == Blocks.BLACK_STAINED_GLASS
                || b == Blocks.BLUE_STAINED_GLASS
                || b == Blocks.BROWN_STAINED_GLASS
                || b == Blocks.CYAN_STAINED_GLASS
                || b == Blocks.LIGHT_BLUE_STAINED_GLASS
                || b == Blocks.LIGHT_GRAY_STAINED_GLASS
                || b == Blocks.LIME_STAINED_GLASS
                || b == Blocks.MAGENTA_STAINED_GLASS
                || b == Blocks.ORANGE_STAINED_GLASS
                || b == Blocks.PINK_STAINED_GLASS
                || b == Blocks.WHITE_STAINED_GLASS
                || b == Blocks.YELLOW_STAINED_GLASS
                || b == Blocks.PURPLE_STAINED_GLASS
                || b == Blocks.RED_STAINED_GLASS
                || b == Blocks.GLASS_PANE
                || b == Blocks.GRAY_STAINED_GLASS_PANE
                || b == Blocks.GREEN_STAINED_GLASS_PANE
                || b == Blocks.BLACK_STAINED_GLASS_PANE
                || b == Blocks.BLUE_STAINED_GLASS_PANE
                || b == Blocks.BROWN_STAINED_GLASS_PANE
                || b == Blocks.CYAN_STAINED_GLASS_PANE
                || b == Blocks.LIGHT_BLUE_STAINED_GLASS_PANE
                || b == Blocks.LIGHT_GRAY_STAINED_GLASS_PANE
                || b == Blocks.LIME_STAINED_GLASS_PANE
                || b == Blocks.MAGENTA_STAINED_GLASS_PANE
                || b == Blocks.ORANGE_STAINED_GLASS_PANE
                || b == Blocks.PINK_STAINED_GLASS_PANE
                || b == Blocks.WHITE_STAINED_GLASS_PANE
                || b == Blocks.YELLOW_STAINED_GLASS_PANE
                || b == Blocks.PURPLE_STAINED_GLASS_PANE
                || b == Blocks.RED_STAINED_GLASS_PANE
                || b instanceof AbstractSkullBlock
                || b instanceof SnowBlock
                || b instanceof SnowyBlock
                || b instanceof DoorBlock
                || b instanceof LeavesBlock
                || b instanceof SlabBlock
                || b instanceof StairsBlock
                || b instanceof CactusBlock
                || b instanceof ChestBlock
                || b instanceof EnderChestBlock
                || b instanceof SkullBlock
                || b instanceof PaneBlock
                || b instanceof FenceBlock
                || b instanceof WallBlock
                || b instanceof StainedGlassBlock
                || b instanceof TintedGlassBlock
                || b instanceof PistonBlock
                || b instanceof PistonHeadBlock
                || b instanceof TrapdoorBlock
                || b instanceof EndPortalBlock
                || b instanceof EndPortalFrameBlock
                || b instanceof BedBlock
                || b instanceof CobwebBlock
                || b instanceof BarrierBlock
                || b instanceof LadderBlock
                || b instanceof CarpetBlock;
    }

    private static boolean canWalkOn(final BlockPos block) {
        Block b = Objects.requireNonNull(IMinecraft.mc.world).getBlockState(block).getBlock();

        return !(b instanceof FenceBlock) && !(b instanceof WallBlock);
    }

    public static boolean canPassThrough(final BlockPos pos) {
        Block block = Objects.requireNonNull(IMinecraft.mc.world).getBlockState(pos).getBlock();
        Block down = Objects.requireNonNull(IMinecraft.mc.world).getBlockState(pos.down()).getBlock();

        return block.getDefaultState().isAir()
                || down instanceof AbstractSkullBlock
                || block instanceof PlantBlock
                || block instanceof SignBlock
                || block instanceof HangingSignBlock
                || block instanceof WallHangingSignBlock
                || block == Blocks.LADDER
                || block == Blocks.VINE
                || block == Blocks.SCAFFOLDING
                || block == Blocks.WATER;
    }

    public static ArrayList<Vec3d> computePath(Vec3d topFrom, final Vec3d to) {
        if (IMinecraft.mc.isInSingleplayer()) {
            ChatUtil.printChat("Pathfinder features was disabled on single player!");
            return null;
        }

        switch (com.instrumentalist.elite.hacks.features.exploit.PathFinder.mode.get().toLowerCase()) {
            case "reconstruct":
                final PathFinder pathfinder = new PathFinder(topFrom, to);
                pathfinder.compute();

                int i = 0;
                Vec3d lastLoc = null;
                Vec3d lastDashLoc = null;
                final ArrayList<Vec3d> path = new ArrayList<>();
                final ArrayList<Vec3d> pathFinderPath = pathfinder.getPath();

                for (final Vec3d pathElm : pathFinderPath) {
                    if (i == 0 || i == pathFinderPath.size() - 1) {
                        if (lastLoc != null) {
                            path.add(lastLoc.add(0.6, 0.0, 0.6));
                        }
                        path.add(pathElm.add(0.6, 0.0, 0.6));
                        lastDashLoc = pathElm;
                    } else {
                        boolean canContinue = true;
                        if (pathElm.squaredDistanceTo(lastDashLoc) > 5 * 5) {
                            canContinue = false;
                        } else {
                            for (int x = Math.min((int) lastDashLoc.x, (int) pathElm.x);
                                 x <= Math.max((int) lastDashLoc.x, (int) pathElm.x); x++) {
                                for (int y = Math.min((int) lastDashLoc.y, (int) pathElm.y);
                                     y <= Math.max((int) lastDashLoc.y, (int) pathElm.y); y++) {
                                    for (int z = Math.min((int) lastDashLoc.z, (int) pathElm.z);
                                         z <= Math.max((int) lastDashLoc.z, (int) pathElm.z); z++) {
                                        if (!isValid(x, y, z, false)) {
                                            canContinue = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (!canContinue) {
                            path.add(lastLoc.add(0.6, 0.0, 0.6));
                            lastDashLoc = lastLoc;
                        }
                    }
                    lastLoc = pathElm;
                    i++;
                }

                return path;

            case "linear":
                return LinearPathFinder.INSTANCE.getPaths(new BlockPos((int) Math.floor(topFrom.x), (int) Math.floor(topFrom.y), (int) Math.floor(topFrom.z)), new BlockPos((int) Math.floor(to.x), (int) Math.floor(to.y), (int) Math.floor(to.z)), com.instrumentalist.elite.hacks.features.exploit.PathFinder.linearSteps.get(), 4);
        }

        return new ArrayList<>(Collections.emptyList());
    }
}