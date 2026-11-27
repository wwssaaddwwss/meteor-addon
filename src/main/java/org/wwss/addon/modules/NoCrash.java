package org.wwss.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.wwss.addon.AddonTemplate;

public class NoCrash extends Module {

    private double originalSpeed;
    private boolean slowingDown = false;

    private final SettingGroup settingsgroup = settings.getDefaultGroup();

    private final Setting<Double> distance = settingsgroup.add(new DoubleSetting.Builder()
        .name("distance")
        .defaultValue(5.0)
        .min(0.0)
        .max(10.0)
        .sliderMin(0.0)
        .sliderMax(10.0)
        .build());

    public NoCrash() {
        super(AddonTemplate.CATEGORY, "NoCrash", "a module to stop moving when elytra flying.");
    }

    private boolean hasBlockAhead(double maxDistance) {
        if (mc.player == null || mc.world == null || maxDistance <= 0) return false;

        Vec3d pos = new Vec3d(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ()
        );
        Vec3d rotation = mc.player.getRotationVec(1.0f);

        Vec3d horizontalDir = new Vec3d(rotation.x, 0, rotation.z);
        if (horizontalDir.lengthSquared() < 1.0E-8) return false;
        horizontalDir = horizontalDir.normalize();

        double step = 0.5;
        for (double d = step; d <= maxDistance; d += step) {
            BlockPos target = BlockPos.ofFloored(pos.add(horizontalDir.multiply(d)));
            BlockState state = mc.world.getBlockState(target);

            if (!state.getCollisionShape(mc.world, target).isEmpty()) {
                return true;
            }
        }

        BlockPos endTarget = BlockPos.ofFloored(pos.add(horizontalDir.multiply(maxDistance)));
        return !mc.world.getBlockState(endTarget).getCollisionShape(mc.world, endTarget).isEmpty();
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isGliding() && hasBlockAhead(distance.get())) {
            mc.player.stopGliding();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        boolean shouldSlow =
            mc.player.isGliding() &&
                hasBlockAhead(distance.get());

        Vec3d velocity = mc.player.getVelocity();

        if (shouldSlow) {
            if (!slowingDown) {
                originalSpeed = velocity.length();
                slowingDown = true;
            }

            double targetSpeed = originalSpeed / 50.0;

            if (velocity.lengthSquared() > 1.0E-8) {
                mc.player.setVelocity(
                    velocity.normalize().multiply(targetSpeed)
                );
            }
        } else {
            slowingDown = false;
        }
    }
}
