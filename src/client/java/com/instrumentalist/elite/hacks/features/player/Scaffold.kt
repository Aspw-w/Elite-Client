package com.instrumentalist.elite.hacks.features.player

import com.instrumentalist.elite.events.features.TickEvent
import com.instrumentalist.elite.events.features.UpdateEvent
import com.instrumentalist.elite.hacks.Module
import com.instrumentalist.elite.hacks.ModuleCategory
import com.instrumentalist.elite.hacks.ModuleManager
import com.instrumentalist.elite.hacks.features.movement.Speed
import com.instrumentalist.elite.utils.ChatUtil
import com.instrumentalist.elite.utils.IMinecraft
import com.instrumentalist.elite.utils.math.TargetUtil
import com.instrumentalist.elite.utils.math.TimerUtil
import com.instrumentalist.elite.utils.move.MovementUtil
import com.instrumentalist.elite.utils.packet.PacketUtil
import com.instrumentalist.elite.utils.rotation.RotationUtil
import com.instrumentalist.elite.utils.value.BooleanValue
import com.instrumentalist.elite.utils.value.FloatValue
import com.instrumentalist.elite.utils.value.IntValue
import com.instrumentalist.elite.utils.value.ListValue
import net.minecraft.block.*
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.item.PlayerHeadItem
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import java.util.*

class Scaffold : Module("Scaffold", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true) {
    companion object {
        @Setting
        private val tower = BooleanValue("Tower", true)

        @Setting
        private val towerCenter = BooleanValue("Tower Center", false) { tower.get() }

        @Setting
        private val towerWhen =
            ListValue("Tower When", arrayOf("Always", "Moving", "Standing"), "Always") { tower.get() }

        @Setting
        private val towerMode =
            ListValue(
                "Tower Mode",
                arrayOf("Vanilla", "NCP", "Hypixel"),
                "Vanilla"
            ) { tower.get() }

        @Setting
        private val towerSpeed = FloatValue("Tower Speed", 1f, 0.1f, 1f) {
            tower.get() && towerMode.get().equals("vanilla", true)
        }

        @Setting
        private val rotationMode = ListValue("Rotation Mode", arrayOf("Math", "Simple", "Hypixel", "None"), "Math")

        @Setting
        private val hypixelMode =
            BooleanValue("Hypixel Mode", false) { rotationMode.get().equals("math", true) }

        @Setting
        private val rotationSpeed = FloatValue("Rotation Speed", 40f, 0f, 180f) {
            rotationMode.get().equals("math", true) || rotationMode.get().equals("simple", true) || rotationMode.get()
                .equals("hypixel", true)
        }

        @Setting
        private val randomizedRotation =
            BooleanValue("Randomized Rotation", true) { rotationMode.get().equals("math", true) }

        @Setting
        private val randomTurnSpeed = FloatValue("Random Turn Speed", 15f, 0f, 20f) {
            rotationMode.get().equals("math", true) && randomizedRotation.get()
        }

        @Setting
        private val searchRange = IntValue("Search Range", 2, 0, 5, "m")

        @Setting
        private val customTimer = BooleanValue("Custom Timer", false)

        @Setting
        private val towerTimerSpeed =
            FloatValue("Tower Timer Speed", 1.5f, 0.1f, 10f) { customTimer.get() && tower.get() }

        @Setting
        private val normalTimerSpeed = FloatValue("Normal Timer Speed", 1.5f, 0.1f, 10f) { customTimer.get() }

        @Setting
        private val keepY = BooleanValue("KeepY", true)

        @Setting
        private val keepYOnlySpeed = BooleanValue("KeepY Only Speed", true) { keepY.get() }

        @Setting
        private val intelligentPicker = BooleanValue("Intelligent Picker", true)

        @Setting
        private val noSprint = BooleanValue("No Sprint", false)

        @Setting
        private val down = BooleanValue("Down", true)

        var hotbarStackSize: Int = 0
        var jumped = false
        var wasTowering = false
        var startedScaffold = false
        var lastSlot: Int? = null
        var spoofTick = 0
    }

    private var firstJumped = false
    private var once = false
    private var hypBasePlaced = false
    private var hypTowerTicks = 0
    private var jumpGround = 0.0
    private var checkGround = false
    private var launchY: Int? = null
    private var hypStartIsAllowed = false

    override fun onDisable() {
        if (IMinecraft.mc.player != null && IMinecraft.mc.world != null) {
            if (wasTowering) {
                when (towerMode.get().lowercase(Locale.getDefault())) {
                    "vanilla", "ncp" -> {
                        if (hasAroundBlock())
                            MovementUtil.setVelocityY(-0.4)
                    }

                    "hypixel" -> launchY = IMinecraft.mc.player!!.blockPos.y - 1
                }
            }
            if (once) {
                RotationUtil.reset()
                TimerUtil.reset()
                TargetUtil.noKillAura = false
                if (lastSlot != null) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = lastSlot!!
                    spoofTick = 160
                }
            }
        }

        firstJumped = false
        jumped = false
        once = false
        hypStartIsAllowed = false
        hypBasePlaced = false
        hypTowerTicks = 0
        jumpGround = 0.0
        wasTowering = false
        checkGround = false
        wasTowering = false
        launchY = null
        startedScaffold = false
    }

    override fun onEnable() {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        launchY = IMinecraft.mc.player!!.blockPos.y
        lastSlot = IMinecraft.mc.player!!.inventory.selectedSlot
    }

    override fun onTick(event: TickEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        if (down.get())
            IMinecraft.mc.options.sneakKey.isPressed = false

        if (!jumped && IMinecraft.mc.player!!.isOnGround && (rotationMode.get().equals("math", true) && hypixelMode.get() || rotationMode.get().equals("hypixel", true)))
            IMinecraft.mc.options.jumpKey.isPressed = false

        if (wasTowering)
            IMinecraft.mc.options.jumpKey.isPressed = false

        if (!wasTowering && InputUtil.isKeyPressed(
                IMinecraft.mc.window.handle,
                InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.boundKeyTranslationKey).code
            ) && !ModuleManager.getModuleState(Speed())
        )
            IMinecraft.mc.options.jumpKey.isPressed = true
    }

    private fun countBlockItems(inventory: PlayerInventory) {
        hotbarStackSize = 0
        for (i in 0..8) {
            val stack = inventory.getStack(i)
            if (stack.isEmpty || stack.item !is BlockItem) continue
            val block = (stack.item as BlockItem).block
            if (!block.defaultState.isAir &&
                !(block is ChestBlock || block is CobwebBlock || block is CakeBlock || block is CandleCakeBlock || block is BrewingStandBlock || block is EnderChestBlock || block is ShulkerBoxBlock || block is FurnaceBlock ||
                        block is CraftingTableBlock || block is CrafterBlock || block is SmokerBlock || block is BlastFurnaceBlock ||
                        block is CartographyTableBlock || block is AnvilBlock || block is BellBlock || block is BeaconBlock ||
                        block is DragonEggBlock || block is LeverBlock || block is EnchantingTableBlock || block is ButtonBlock ||
                        block is GrindstoneBlock || block is LoomBlock || block is NoteBlock || block is FenceGateBlock ||
                        block is DoorBlock || block is TrapdoorBlock || block is StonecutterBlock || block is SignBlock ||
                        block is WallSignBlock || block is HangingSignBlock || block is WallHangingSignBlock || block is RepeaterBlock ||
                        block is ComparatorBlock || block is DispenserBlock || block is JigsawBlock || block is CommandBlock ||
                        block is StructureBlock || block is HopperBlock || block is BedBlock || block is FenceBlock || block is SlabBlock || block is PressurePlateBlock || block is WallBlock || block is StairsBlock || block is LadderBlock || block is ChainBlock || block is CarpetBlock || block is BarrelBlock || block is RailBlock || block is PoweredRailBlock || block is DetectorRailBlock || block.asItem() is PlayerHeadItem || block is MushroomPlantBlock || block.asItem() == Items.TORCH || block.asItem() == Items.REDSTONE || block.asItem() == Items.REDSTONE_TORCH || block.asItem() == Items.STRING)
            )
                hotbarStackSize += stack.count
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return

        countBlockItems(IMinecraft.mc.player!!.inventory)

        if (hotbarStackSize <= 0) {
            if (wasTowering) {
                when (towerMode.get().lowercase(Locale.getDefault())) {
                    "vanilla", "ncp" -> {
                        if (hasAroundBlock())
                            MovementUtil.setVelocityY(-0.4)
                    }

                    "hypixel" -> launchY = IMinecraft.mc.player!!.blockPos.y - 1
                }
            }
            firstJumped = false
            jumped = false
            hypBasePlaced = false
            hypTowerTicks = 0
            jumpGround = 0.0
            wasTowering = false
            checkGround = false
            wasTowering = false
            if (once) {
                RotationUtil.reset()
                TimerUtil.reset()
                TargetUtil.noKillAura = false
                if (lastSlot != null) {
                    IMinecraft.mc.player!!.inventory.selectedSlot = lastSlot!!
                    spoofTick = 160
                }
            }
            once = false
            return
        }

        once = true

        if (noSprint.get())
            IMinecraft.mc.player!!.isSprinting = false

        if (MovementUtil.isDiagonal(39f) && (rotationMode.get().equals("hypixel", true) || rotationMode.get().equals("math", true) && hypixelMode.get()))
            MovementUtil.strafe(0.03f)

        if (!jumped && (rotationMode.get().equals("math", true) && hypixelMode.get() || rotationMode.get().equals("hypixel", true))) {
            if (!IMinecraft.mc.player!!.isOnGround && MovementUtil.fallTicks >= 7 && firstJumped) {
                launchY = IMinecraft.mc.player!!.blockPos.y
                jumped = true
            }
            if (!firstJumped && IMinecraft.mc.player!!.isOnGround) {
                IMinecraft.mc.player!!.jump()
                firstJumped = true
            }
        }

        var hypixelPlaced = false

        if (IMinecraft.mc.player!!.isOnGround)
            hypTowerTicks = 0
        else {
            val currentFallTicks = hypTowerTicks
            hypTowerTicks = currentFallTicks + 1
        }

        if (customTimer.get())
            TimerUtil.timerSpeed = if (wasTowering) towerTimerSpeed.get() else normalTimerSpeed.get()

        if (tower.get() && (!towerMode.get()
                .equals("hypixel", true) || !IMinecraft.mc.player!!.hasStatusEffect(StatusEffects.JUMP_BOOST) && (!ModuleManager.getModuleState(Speed())) || IMinecraft.mc.player!!.hasStatusEffect(StatusEffects.SPEED)) && (towerWhen.get()
                .equals("always", true) || towerWhen.get()
                .equals("standing", true) && !MovementUtil.isMoving() || towerWhen.get()
                .equals("moving", true) && MovementUtil.isMoving())
        ) {
            if (InputUtil.isKeyPressed(
                    IMinecraft.mc.window.handle,
                    InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.boundKeyTranslationKey).code
                )
            ) {
                if (!towerMode.get().equals("hypixel", true) || IMinecraft.mc.player!!.isOnGround)
                    wasTowering = true
                if (hasAroundBlock() && wasTowering) {
                    if (towerWhen.get().equals("always", true) && !MovementUtil.isMoving() || towerWhen.get()
                            .equals("standing", true)
                    )
                        MovementUtil.stopMoving()
                    if (towerCenter.get() && !MovementUtil.isMoving())
                        IMinecraft.mc.player!!.setPosition(
                            IMinecraft.mc.player!!.blockPos.x.toDouble() + 0.5,
                            IMinecraft.mc.player!!.pos.y,
                            IMinecraft.mc.player!!.blockPos.z.toDouble() + 0.5
                        )
                    when (towerMode.get().lowercase(Locale.getDefault())) {
                        "vanilla" -> {
                            if (MovementUtil.isMoving() && towerSpeed.get() >= 0.6)
                                MovementUtil.setVelocityY(0.5)
                            else MovementUtil.setVelocityY(towerSpeed.get().toDouble())
                        }

                        "ncp" -> {
                            if (IMinecraft.mc.player!!.isOnGround) {
                                jumpGround = IMinecraft.mc.player!!.y
                                MovementUtil.setVelocityY(0.42)
                            }
                            if (IMinecraft.mc.player!!.y > jumpGround + 0.79f) {
                                IMinecraft.mc.player!!.setPosition(IMinecraft.mc.player!!.x, IMinecraft.mc.player!!.y.toInt().toDouble(), IMinecraft.mc.player!!.z)
                                MovementUtil.setVelocityY(0.42)
                                jumpGround = IMinecraft.mc.player!!.y
                            }
                        }

                        "hypixel" -> {
                            if (MovementUtil.isMoving()) {
                                if (IMinecraft.mc.player!!.isOnGround)
                                    checkGround = true
                                if (checkGround) {
                                    if (MovementUtil.fallTicks >= 18) {
                                        wasTowering = false
                                        hypixelPlaced = true
                                    } else {
                                        when (MovementUtil.fallTicks % 3) {
                                            0 -> {
                                                if (IMinecraft.mc.player!!.hasStatusEffect(StatusEffects.SPEED))
                                                    MovementUtil.strafe(
                                                        0.22f + ((IMinecraft.mc.player!!.getStatusEffect(StatusEffects.SPEED)?.amplifier!! + 1) * if ((IMinecraft.mc.player!!.getStatusEffect(
                                                                StatusEffects.SPEED
                                                            )?.amplifier!!) == 0
                                                        ) 0.036f else 0.042f)
                                                    )
                                                else MovementUtil.strafe(0.22f)
                                                MovementUtil.setVelocityY(0.42)
                                            }

                                            1 -> MovementUtil.setVelocityY(0.33)
                                            2 -> MovementUtil.setVelocityY((IMinecraft.mc.player!!.blockPos.y + 1.0) - IMinecraft.mc.player!!.pos.y)
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (wasTowering) {
                    when (towerMode.get().lowercase(Locale.getDefault())) {
                        "vanilla", "ncp" -> {
                            if (hasAroundBlock())
                                MovementUtil.setVelocityY(-0.4)
                        }

                        "hypixel" -> launchY = IMinecraft.mc.player!!.blockPos.y - 1
                    }
                }
            } else if (wasTowering) {
                when (towerMode.get().lowercase(Locale.getDefault())) {
                    "vanilla", "ncp" -> {
                        if (hasAroundBlock())
                            MovementUtil.setVelocityY(-0.4)
                    }

                    "hypixel" -> launchY = IMinecraft.mc.player!!.blockPos.y - 1
                }
                hypBasePlaced = false
                hypTowerTicks = 0
                jumpGround = 0.0
                wasTowering = false
            }
        } else if (wasTowering) {
            when (towerMode.get().lowercase(Locale.getDefault())) {
                "vanilla", "ncp" -> {
                    if (hasAroundBlock())
                        MovementUtil.setVelocityY(-0.4)
                }

                "hypixel" -> launchY = IMinecraft.mc.player!!.blockPos.y - 1
            }
            hypBasePlaced = false
            hypTowerTicks = 0
            jumpGround = 0.0
            wasTowering = false
        }

        if (wasTowering)
            startedScaffold = true

        if (keepY.get()) {
            if (keepYOnlySpeed.get()) {
                if ((!ModuleManager.getModuleState(Speed()) || InputUtil.isKeyPressed(
                        IMinecraft.mc.window.handle,
                        InputUtil.fromTranslationKey(IMinecraft.mc.options.jumpKey.boundKeyTranslationKey).code
                    )) && (wasTowering || MovementUtil.fallTicks >= 4)
                )
                    launchY = IMinecraft.mc.player!!.blockPos.y
            }
        } else if (wasTowering || MovementUtil.fallTicks >= 4) launchY = IMinecraft.mc.player!!.blockPos.y

        if (IMinecraft.mc.player!!.squaredDistanceTo(
                Vec3d(
                    IMinecraft.mc.player!!.x,
                    launchY!!.toDouble(),
                    IMinecraft.mc.player!!.z
                )
            ) > 15
        )
            launchY = IMinecraft.mc.player!!.blockPos.y

        if (down.get() && InputUtil.isKeyPressed(
                IMinecraft.mc.window.handle,
                InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.boundKeyTranslationKey).code
            ) && !wasTowering
        )
            launchY = IMinecraft.mc.player!!.blockPos.y - 1

        val targetBlock =
            BlockPos.Mutable(IMinecraft.mc.player!!.blockPos.x.toDouble(), launchY!! - 0.75, IMinecraft.mc.player!!.blockPos.z.toDouble())

        TargetUtil.noKillAura = true

        if (rotationMode.get().equals("simple", true) || rotationMode.get()
                .equals("hypixel", true) || !rotationMode.get()
                .equals("none", true) && !startedScaffold
        ) {
            val direction = MovementUtil.getPlayerDirection() - 180f

            val yaw = (direction % 360 + 360) % 360

            val isNorth = yaw < 80f || yaw > 280f
            val isSouth = yaw > 100f && yaw < 260f
            val isEast = yaw > 10f && yaw < 170f
            val isWest = yaw > 190f && yaw < 350f

            val groundYaw = if (rotationMode.get().equals("hypixel", true) || rotationMode.get().equals("math", true) && hypixelMode.get()) {
                if (!IMinecraft.mc.world!!.getBlockState(BlockPos(IMinecraft.mc.player!!.blockPos.down(1))).isAir)
                    direction + 70f
                else if (MovementUtil.isDiagonal(6f)) {
                    when {
                        isNorth && isWest -> 45f
                        isNorth && isEast -> 135f
                        isSouth && isEast -> 225f
                        isSouth && isWest -> 315f
                        else -> (direction + 60f) % 360
                    }
                } else (direction + 60f) % 360
            } else direction

            val offGroundYaw = if (rotationMode.get().equals("hypixel", true) || rotationMode.get().equals("math", true) && hypixelMode.get()) {
                if (!IMinecraft.mc.world!!.getBlockState(BlockPos(IMinecraft.mc.player!!.blockPos.down(1))).isAir)
                    direction + 70f
                else if (MovementUtil.isDiagonal(6f)) {
                    when {
                        isNorth && isWest -> 45f
                        isNorth && isEast -> 135f
                        isSouth && isEast -> 225f
                        isSouth && isWest -> 315f
                        else -> (direction + 60f) % 360
                    }
                } else (direction + 60f) % 360
            } else direction

            val groundPitch = if (rotationMode.get().equals("hypixel", true) || rotationMode.get().equals("math", true) && hypixelMode.get()) 78f else 70f
            val offGroundPitch = if (rotationMode.get().equals("hypixel", true) || rotationMode.get().equals("math", true) && hypixelMode.get()) 88f else 80f

            if (IMinecraft.mc.player!!.isOnGround)
                RotationUtil.setRotation(
                    groundYaw,
                    groundPitch,
                    rotationSpeed.get()
                )
            else
                RotationUtil.setRotation(
                    offGroundYaw,
                    offGroundPitch,
                    rotationSpeed.get()
                )
        }

        if (!IMinecraft.mc.world!!.getBlockState(targetBlock).isAir) return

        startedScaffold = true

        if (!hypixelPlaced) {
            placeBlock(targetBlock)
            hypBasePlaced = true
        }
    }

    private fun placeBlock(pos: BlockPos) {
        if (!IMinecraft.mc.world!!.getBlockState(pos).isAir) return

        val slot = findBlockSlot(IMinecraft.mc.player?.inventory!!) ?: return

        if (IMinecraft.mc.player?.inventory!!.selectedSlot != slot)
            IMinecraft.mc.player?.inventory!!.selectedSlot = slot

        if (startedScaffold) {
            val upperRange = listOf(
                BlockPos(0, 1, 0),
                BlockPos(1, 1, 0),
                BlockPos(0, 1, 1),
                BlockPos(-1, 1, 0),
                BlockPos(0, 1, -1),
                BlockPos(1, 1, -1),
                BlockPos(-1, 1, 1),
                BlockPos(1, 1, 1),
                BlockPos(-1, 1, -1)
            ).sortedBy {
                IMinecraft.mc.player!!.squaredDistanceTo(
                    IMinecraft.mc.player!!.x + it.x.toDouble(),
                    IMinecraft.mc.player!!.y + it.y.toDouble(),
                    IMinecraft.mc.player!!.z + it.z.toDouble()
                )
            }

            val extendedRange = mutableListOf<BlockPos>()
            val range = if (down.get() && InputUtil.isKeyPressed(
                    IMinecraft.mc.window.handle,
                    InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.boundKeyTranslationKey).code
                )
            ) 1 else searchRange.get()

            for (y in listOf(0, -1)) {
                for (x in -range..range) {
                    for (z in -range..range) {
                        extendedRange.add(BlockPos(x, y, z))
                    }
                }
            }

            extendedRange.sortBy {
                IMinecraft.mc.player!!.squaredDistanceTo(
                    IMinecraft.mc.player!!.x + it.x.toDouble(),
                    IMinecraft.mc.player!!.y + it.y.toDouble(),
                    IMinecraft.mc.player!!.z + it.z.toDouble()
                )
            }

            for (offset in extendedRange) {
                val neighbourPos = pos.add(offset)
                if (!IMinecraft.mc.world!!.getBlockState(neighbourPos).isAir) {
                    val hitPos = Vec3d.ofCenter(neighbourPos)
                    val oppositeDirection = Direction.getFacing(
                        offset.x.toDouble(), offset.y.toDouble(),
                        offset.z.toDouble()
                    ).opposite

                    val bhr = BlockHitResult(hitPos, oppositeDirection, neighbourPos, false)
                    IMinecraft.mc.execute {
                        try {
                            if (IMinecraft.mc.player?.inventory!!.selectedSlot != slot)
                                IMinecraft.mc.player?.inventory!!.selectedSlot = slot

                            if (rotationMode.get().equals("math", true))
                                RotationUtil.scaffoldRotation(
                                    hypixelMode.get(), Vec3d.ofCenter(neighbourPos),
                                    rotationSpeed.get(),
                                    randomizedRotation.get(),
                                    randomTurnSpeed.get()
                                )

                            if (IMinecraft.mc.interactionManager!!.interactBlock(
                                    IMinecraft.mc.player,
                                    Hand.MAIN_HAND,
                                    bhr
                                ).isAccepted
                            ) {
                                IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                                IMinecraft.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    return
                }
            }

            if (down.get() && InputUtil.isKeyPressed(
                    IMinecraft.mc.window.handle,
                    InputUtil.fromTranslationKey(IMinecraft.mc.options.sneakKey.boundKeyTranslationKey).code
                )
            ) {
                for (offset in upperRange) {
                    val neighbourPos = pos.add(offset)
                    if (!IMinecraft.mc.world!!.getBlockState(neighbourPos).isAir) {
                        val hitPos = Vec3d.ofCenter(neighbourPos)
                        val oppositeDirection = Direction.getFacing(
                            offset.x.toDouble(), offset.y.toDouble(),
                            offset.z.toDouble()
                        ).opposite

                        val bhr = BlockHitResult(hitPos, oppositeDirection, neighbourPos, false)
                        IMinecraft.mc.execute {
                            try {
                                if (IMinecraft.mc.player?.inventory!!.selectedSlot != slot)
                                    IMinecraft.mc.player?.inventory!!.selectedSlot = slot

                                if (rotationMode.get().equals("math", true))
                                    RotationUtil.scaffoldRotation(
                                        hypixelMode.get(), Vec3d.ofCenter(neighbourPos),
                                        rotationSpeed.get(),
                                        randomizedRotation.get(),
                                        randomTurnSpeed.get()
                                    )

                                if (IMinecraft.mc.interactionManager!!.interactBlock(
                                        IMinecraft.mc.player,
                                        Hand.MAIN_HAND,
                                        bhr
                                    ).isAccepted
                                ) {
                                    IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                                    IMinecraft.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return
                    }
                }
            }

            var hitPos = Vec3d.ofCenter(pos)

            val side = getPlaceSide(pos) ?: return
            val neighbour = if (side == Direction.UP) {
                pos
            } else {
                pos.offset(side).also {
                    hitPos = hitPos.add(side.offsetX * 0.5, side.offsetY * 0.5, side.offsetZ * 0.5)
                }
            }

            val playerPos = IMinecraft.mc.player?.pos!!
            if (playerPos.x % 1.0 == 0.5) {
                hitPos = hitPos.add(if (playerPos.x > pos.x) 0.5 else -0.5, 0.0, 0.0)
            }
            if (playerPos.z % 1.0 == 0.5) {
                hitPos = hitPos.add(0.0, 0.0, if (playerPos.z > pos.z) 0.5 else -0.5)
            }

            val bhr = BlockHitResult(hitPos, side.opposite, neighbour, false)

            IMinecraft.mc.execute {
                try {
                    if (IMinecraft.mc.player?.inventory!!.selectedSlot != slot)
                        IMinecraft.mc.player?.inventory!!.selectedSlot = slot

                    if (rotationMode.get().equals("math", true))
                        RotationUtil.scaffoldRotation(
                            hypixelMode.get(), Vec3d.ofCenter(neighbour),
                            rotationSpeed.get(),
                            randomizedRotation.get(),
                            randomTurnSpeed.get()
                        )

                    if (IMinecraft.mc.interactionManager!!.interactBlock(
                            IMinecraft.mc.player,
                            Hand.MAIN_HAND,
                            bhr
                        ).isAccepted
                    ) {
                        IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                        IMinecraft.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return
        }

        var hitPos = Vec3d.ofCenter(pos)

        val side = getPlaceSide(pos) ?: return
        val neighbour = if (side == Direction.UP) {
            pos
        } else {
            pos.offset(side).also {
                hitPos = hitPos.add(side.offsetX * 0.5, side.offsetY * 0.5, side.offsetZ * 0.5)
            }
        }

        val bhr = BlockHitResult(hitPos, side.opposite, neighbour, false)

        IMinecraft.mc.execute {
            try {
                if (IMinecraft.mc.player?.inventory!!.selectedSlot != slot)
                    IMinecraft.mc.player?.inventory!!.selectedSlot = slot

                if (rotationMode.get().equals("math", true))
                    RotationUtil.scaffoldRotation(
                        hypixelMode.get(), Vec3d.ofCenter(neighbour),
                        rotationSpeed.get(),
                        randomizedRotation.get(),
                        randomTurnSpeed.get()
                    )

                if (IMinecraft.mc.interactionManager!!.interactBlock(
                        IMinecraft.mc.player!!,
                        Hand.MAIN_HAND,
                        bhr
                    ).isAccepted
                ) {
                    IMinecraft.mc.player!!.swingHand(Hand.MAIN_HAND)
                    IMinecraft.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun findBlockSlot(inv: PlayerInventory): Int? {
        var maxStackSlot: Int? = null
        var maxStackSize = 0

        for (i in inv.main.indices) {
            if (i !in 0..8) continue
            val stack = inv.getStack(i)
            if (stack.isEmpty || stack.item !is BlockItem) continue
            val block = (stack.item as BlockItem).block
            if (!block.defaultState.isAir &&
                !(block is ChestBlock || block is CobwebBlock || block is CakeBlock || block is CandleCakeBlock || block is BrewingStandBlock || block is EnderChestBlock || block is ShulkerBoxBlock || block is FurnaceBlock ||
                        block is CraftingTableBlock || block is CrafterBlock || block is SmokerBlock || block is BlastFurnaceBlock ||
                        block is CartographyTableBlock || block is AnvilBlock || block is BellBlock || block is BeaconBlock ||
                        block is DragonEggBlock || block is LeverBlock || block is EnchantingTableBlock || block is ButtonBlock ||
                        block is GrindstoneBlock || block is LoomBlock || block is NoteBlock || block is FenceGateBlock ||
                        block is DoorBlock || block is TrapdoorBlock || block is StonecutterBlock || block is SignBlock ||
                        block is WallSignBlock || block is HangingSignBlock || block is WallHangingSignBlock || block is RepeaterBlock ||
                        block is ComparatorBlock || block is DispenserBlock || block is JigsawBlock || block is CommandBlock ||
                        block is StructureBlock || block is HopperBlock || block is BedBlock || block is FenceBlock || block is SlabBlock || block is PressurePlateBlock || block is WallBlock || block is StairsBlock || block is LadderBlock || block is ChainBlock || block is CarpetBlock || block is BarrelBlock || block is RailBlock || block is PoweredRailBlock || block is DetectorRailBlock || block.asItem() is PlayerHeadItem || block is MushroomPlantBlock || block.asItem() == Items.TORCH || block.asItem() == Items.REDSTONE || block.asItem() == Items.REDSTONE_TORCH || block.asItem() == Items.STRING)
            ) {
                if (intelligentPicker.get()) {
                    if (stack.count > maxStackSize) {
                        maxStackSize = stack.count
                        maxStackSlot = i
                    }
                } else return i
            }
        }

        return maxStackSlot
    }

    private fun getPlaceSide(blockPos: BlockPos): Direction? {
        val lookVec = blockPos.toCenterPos().subtract(IMinecraft.mc.player?.eyePos)
        var bestRelevancy = -Double.MAX_VALUE
        var bestSide: Direction? = null

        for (side in Direction.entries) {
            val neighbor = blockPos.offset(side)
            val state = IMinecraft.mc.world?.getBlockState(neighbor) ?: continue

            if (state.isAir) continue
            if (!state.fluidState.isEmpty) continue

            val relevancy = side.axis.choose(lookVec.x, lookVec.y, lookVec.z) * side.direction.offset()
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy
                bestSide = side
            }
        }

        return bestSide
    }

    private fun hasAroundBlock(): Boolean {
        if (IMinecraft.mc.player == null || IMinecraft.mc.world == null) return false
        return !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y - 1,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!!,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! + 1,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z + 1
            )
        )?.isAir!! || !IMinecraft.mc.world?.getBlockState(
            BlockPos(
                IMinecraft.mc.player?.blockPos?.x!! - 1,
                IMinecraft.mc.player!!.blockPos.y - 2,
                IMinecraft.mc.player!!.blockPos.z - 1
            )
        )?.isAir!!
    }
}
