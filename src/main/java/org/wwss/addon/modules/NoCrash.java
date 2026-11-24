
package org.wwss.addon.modules;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.util.math.BlockPos;
import org.wwss.addon.Addon;
import org.wwss.addon.AddonTemplate;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class NoCrash extends Module {

    private double originalSpeed;

    private boolean hasBlockAhead(double distance) {
        Vec3d pos = new Vec3d(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ()
        );
        Vec3d direction = mc.player.getRotationVec(1.0f);

        BlockPos target = BlockPos.ofFloored(
            pos.add(direction.multiply(distance))
        );

        return !mc.world.getBlockState(target).isAir();
    }
    private boolean slowingDown = false;

    private SettingGroup settingsgroup = settings.getDefaultGroup();

    private final Setting<Double> distance = settingsgroup.add(new DoubleSetting.Builder()
        .name("distance")
        .min(0.0)
        .max(10.0)
        .sliderMin(0.0)
        .sliderMax(10.0)
        .build());

    public NoCrash() {
        super(AddonTemplate.CATEGORY, "NoCrash", "a module to stop moving whenelytra flying.");
    }

    @Override
    public void onActivate() {
        double dis = distance.get();
        if(hasBlockAhead(dis)){

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


