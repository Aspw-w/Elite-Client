package com.instrumentalist.elite.utils.world;

import java.util.Objects;
import java.util.stream.Stream;

import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public enum ChunkUtil {;

    public static Stream<BlockEntity> getLoadedBlockEntities() {
        return getLoadedChunks().flatMap(chunk -> chunk.getBlockEntities().values().stream());
    }

    public static int getManhattanDistance(ChunkPos a, ChunkPos b)
    {
        return Math.abs(a.x - b.x) + Math.abs(a.z - b.z);
    }

    public static ChunkPos getAffectedChunk(Packet<?> packet) {
        if (packet instanceof BlockUpdateS2CPacket p)
            return new ChunkPos(p.getPos());
        if (packet instanceof ChunkDeltaUpdateS2CPacket p)
            return p.sectionPos.toChunkPos();
        if (packet instanceof ChunkDataS2CPacket p)
            return new ChunkPos(p.getChunkX(), p.getChunkZ());

        return null;
    }

    public static Stream<WorldChunk> getLoadedChunks() {
        int radius = Math.max(2, IMinecraft.mc.options.getClampedViewDistance()) + 3;
        int diameter = radius * 2 + 1;

        ChunkPos center = IMinecraft.mc.player.getChunkPos();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);

        Stream<WorldChunk> stream = Stream.iterate(min, pos -> {
            int x = pos.x;
            int z = pos.z;

            x++;

            if (x > max.x) {
                x = min.x;
                z++;
            }

            if (z > max.z)
                throw new IllegalStateException("Stream limit didn't work.");

            return new ChunkPos(x, z);
        }).limit((long) diameter * diameter).filter(c -> IMinecraft.mc.world.isChunkLoaded(c.x, c.z)).map(c -> IMinecraft.mc.world.getChunk(c.x, c.z)).filter(Objects::nonNull);

        return stream;
    }

    public static int getHighestNonEmptySectionYOffset(Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        if (i == -1)
            return chunk.getBottomY();

        return ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(i));
    }
}