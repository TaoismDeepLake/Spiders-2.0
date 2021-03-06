package tcb.spiderstpo.common.entity.goal;

import java.util.EnumSet;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import tcb.spiderstpo.common.entity.mob.IClimberEntity;
import tcb.spiderstpo.common.entity.mob.Orientation;

public class BetterLeapAtTargetGoal<T extends MobEntity & IClimberEntity> extends Goal {
	private final T leaper;
	private final float leapMotionY;

	private LivingEntity leapTarget;
	private Vector3d forwardJumpDirection;
	private Vector3d upwardJumpDirection;

	public BetterLeapAtTargetGoal(T leapingEntity, float leapMotionYIn) {
		this.leaper = leapingEntity;
		this.leapMotionY = leapMotionYIn;
		this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
	}

	@Override
	public boolean shouldExecute() {
		if(!this.leaper.isBeingRidden()) {
			this.leapTarget = this.leaper.getAttackTarget();

			if(this.leapTarget != null && this.leaper.func_233570_aj_()) {
				Triple<Vector3d, Vector3d, Vector3d> projectedVector = this.getProjectedVector(this.leapTarget.getPositionVec());

				double dstSq = projectedVector.getLeft().lengthSquared();
				double dstSqDot = projectedVector.getMiddle().lengthSquared();

				if(dstSq >= 4.0D && dstSq <= 16.0D && dstSqDot <= 1.2f && this.leaper.getRNG().nextInt(5) == 0) {
					this.forwardJumpDirection = projectedVector.getLeft().normalize();
					this.upwardJumpDirection = projectedVector.getRight().normalize();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.leaper.func_233570_aj_();
	}

	@Override
	public void startExecuting() {
		Vector3d motion = this.leaper.getMotion();

		Vector3d jumpVector = this.forwardJumpDirection;

		if(jumpVector.lengthSquared() > 1.0E-7D) {
			jumpVector = jumpVector.normalize().scale(0.4D).add(motion.scale(0.2D));
		}

		jumpVector = jumpVector.add(this.upwardJumpDirection.scale(this.leapMotionY));
		jumpVector = new Vector3d(jumpVector.x * (1 - Math.abs(this.upwardJumpDirection.x)), jumpVector.y, jumpVector.z * (1 - Math.abs(this.upwardJumpDirection.z)));

		this.leaper.setMotion(jumpVector);

		Orientation orientation = this.leaper.getOrientation();

		float rx = (float) orientation.localZ.dotProduct(jumpVector);
		float ry = (float) orientation.localX.dotProduct(jumpVector);

		this.leaper.rotationYaw = 270.0f - (float) Math.toDegrees(MathHelper.atan2(rx, ry));
	}

	protected Triple<Vector3d, Vector3d, Vector3d> getProjectedVector(Vector3d target) {
		Orientation orientation = this.leaper.getOrientation();
		Vector3d up = orientation.getGlobal(this.leaper.rotationYaw, -90.0f);
		Vector3d diff = target.subtract(this.leaper.getPositionVec());
		Vector3d dot = up.scale(up.dotProduct(diff));
		return Triple.of(diff.subtract(dot), dot, up);
	}
}
