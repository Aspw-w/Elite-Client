package com.instrumentalist.elite.utils.math;

import com.instrumentalist.elite.hacks.ModuleManager;
import com.instrumentalist.elite.hacks.features.combat.AntiBot;
import com.instrumentalist.elite.hacks.features.combat.Teams;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

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
}
