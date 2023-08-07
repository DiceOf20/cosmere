/*
 * File updated ~ 26 - 5 - 2023 ~ Leaf
 */

package leaf.cosmere.sandmastery.common.manifestation;

import leaf.cosmere.api.Manifestations;
import leaf.cosmere.api.Taldain;
import leaf.cosmere.api.spiritweb.ISpiritweb;
import leaf.cosmere.common.cap.entity.SpiritwebCapability;
import leaf.cosmere.sandmastery.common.capabilities.SandmasterySpiritwebSubmodule;
import leaf.cosmere.sandmastery.common.config.SandmasteryConfigs;
import leaf.cosmere.sandmastery.common.config.SandmasteryServerConfig;
import leaf.cosmere.sandmastery.common.utils.MiscHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MasteryCushion extends SandmasteryManifestation
{
	public MasteryCushion(Taldain.Mastery mastery)
	{
		super(mastery);
	}

	@Override
	public boolean tick(ISpiritweb data)
	{
		if(sandmasteryBlocked(data))
		{
			return false;
		}
		if (getMode(data) > 0)
		{
			return performEffectServer(data);
		}
		return false;
	}

	public void tickClient(ISpiritweb data)
	{
		if (MiscHelper.isClient(data))
		{
			performEffectClient(data);
		}
	}

	protected boolean performEffectServer(ISpiritweb data)
	{
		SpiritwebCapability playerSpiritweb = (SpiritwebCapability) data;
		SandmasterySpiritwebSubmodule submodule = (SandmasterySpiritwebSubmodule) playerSpiritweb.getSubmodule(Manifestations.ManifestationTypes.SANDMASTERY);

		LivingEntity living = data.getLiving();
		Vec3 movement = living.getDeltaMovement();
		if (movement.y > 0)
		{
			return false;
		}
		double distFromGround = MiscHelper.distanceFromGround(living);
		if (!(distFromGround > 1 && distFromGround < 10))
		{
			return false;
		}

		if (!submodule.adjustHydration(-SandmasteryConfigs.SERVER.CUSHION_HYDRATION_COST.get(), false))
		{
			return false;
		}
		if (notEnoughChargedSand(data))
		{
			return false;
		}


		living.setDeltaMovement(movement.multiply(1, 0.05, 1));
		living.hurtMarked = true;
		living.resetFallDistance();
		submodule.adjustHydration(-SandmasteryConfigs.SERVER.CUSHION_HYDRATION_COST.get(), true);
		useChargedSand(data);
		return true;
	}
}
