package com.instrumentalist.elite.utils.math;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.AntiBot;
import com.instrumentalist.elite.hacks.features.combat.KillAura;
import com.instrumentalist.elite.hacks.features.combat.Teams;
import com.instrumentalist.elite.utils.IMinecraft;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.List;

public class TargetUtil {

    public static boolean noKillAura = false;

    public static boolean isBot(LivingEntity entity) {
        if (!ModuleManager.getModuleState(new AntiBot()) || !(entity instanceof PlayerEntity) || entity instanceof ClientPlayerEntity) return false;
        return AntiBot.Companion.inBotList(entity);
    }

    public static boolean isTeammate(LivingEntity entity) {
        if (!ModuleManager.getModuleState(new Teams()) || !(entity instanceof PlayerEntity) || entity instanceof ClientPlayerEntity) return false;
        return Teams.Companion.isInClientPlayersTeam(entity);
    }

    public static List<Entity> getSingletonTargetsAsList() {
        if (ModuleManager.getModuleState(new KillAura()) && KillAura.closestEntity != null) {
            return Collections.singletonList(KillAura.closestEntity);
        } else if (IMinecraft.mc.targetedEntity instanceof LivingEntity && !(IMinecraft.mc.targetedEntity instanceof ArmorStandEntity)) {
            return Collections.singletonList(IMinecraft.mc.targetedEntity);
        }
        return Collections.emptyList();
    }
}
