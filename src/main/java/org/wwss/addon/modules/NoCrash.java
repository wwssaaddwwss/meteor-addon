
package org.wwss.addon.modules;

import org.wwss.addon.AddonTemplate;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class NoCrash extends Module {
    private static final double CHECK_DISTANCE = 5.0;
    private static final double CHECK_STEP = 0.5;
    private static final double MIN_HORIZONTAL_SPEED = 0.001;

    public NoCrash() {
        super(
            AddonTemplate.CATEGORY,
            "no-crash",
            "Holds WASD when an obstacle is close while gliding."
        );
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) {
            setMovementKeys(false);
            return;
        }

        // 只在鞘翅滑翔時檢測
        if (!mc.player.isGliding()) {
            setMovementKeys(false);
            return;
        }

        Vec3d velocity = mc.player.getVelocity();

        // 計算水平速度
        double horizontalSpeed = Math.sqrt(
            velocity.x * velocity.x +
                velocity.z * velocity.z
        );

        if (horizontalSpeed < MIN_HORIZONTAL_SPEED) {
            setMovementKeys(false);
            return;
        }

        // 水平飛行方向
        double dirX = velocity.x / horizontalSpeed;
        double dirZ = velocity.z / horizontalSpeed;

        // 玩家碰撞箱
        Box playerBox = mc.player.getBoundingBox();

        // 檢查前方是否有障礙物
        boolean blocked = hasObstacleAhead(
            playerBox,
            dirX,
            dirZ
        );

        // 只有接近障礙物才按住 WASD
        setMovementKeys(blocked);
    }

    private boolean hasObstacleAhead(Box playerBox, double dirX, double dirZ) {
        for (double distance = CHECK_STEP;
             distance <= CHECK_DISTANCE;
             distance += CHECK_STEP) {

            double dx = dirX * distance;
            double dz = dirZ * distance;

            // 將玩家碰撞箱向前移動
            Box checkBox = new Box(
                playerBox.minX + dx,
                playerBox.minY,
                playerBox.minZ + dz,
                playerBox.maxX + dx,
                playerBox.maxY,
                playerBox.maxZ + dz
            );

            // 檢查該位置是否有方塊碰撞箱
            boolean blocked = mc.world
                .getBlockCollisions(mc.player, checkBox)
                .iterator()
                .hasNext();

            if (blocked) {
                return true;
            }
        }

        return false;
    }

    // 持續模擬按住或放開 WASD
    private void setMovementKeys(boolean pressed) {
        mc.options.forwardKey.setPressed(pressed);
        mc.options.backKey.setPressed(pressed);
        mc.options.leftKey.setPressed(pressed);
        mc.options.rightKey.setPressed(pressed);
    }

    @Override
    public void onDeactivate() {
        setMovementKeys(false);
    }
}
