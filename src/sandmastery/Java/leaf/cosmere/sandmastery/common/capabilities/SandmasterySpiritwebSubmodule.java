/*
 * File updated ~ 13 - 2 - 2023 ~ Leaf
 */

package leaf.cosmere.sandmastery.common.capabilities;

import leaf.cosmere.api.CosmereAPI;
import leaf.cosmere.api.ISpiritwebSubmodule;
import leaf.cosmere.api.helpers.CompoundNBTHelper;
import leaf.cosmere.api.helpers.EffectsHelper;
import leaf.cosmere.api.manifestation.Manifestation;
import leaf.cosmere.api.spiritweb.ISpiritweb;
import leaf.cosmere.client.Keybindings;
import leaf.cosmere.sandmastery.client.SandmasteryKeybindings;
import leaf.cosmere.sandmastery.common.Sandmastery;
import leaf.cosmere.sandmastery.common.config.SandmasteryConfigs;
import leaf.cosmere.sandmastery.common.manifestation.SandmasteryManifestation;
import leaf.cosmere.sandmastery.common.network.packets.SyncMasteryBindsMessage;
import leaf.cosmere.sandmastery.common.registries.SandmasteryEffects;
import leaf.cosmere.sandmastery.common.utils.SandmasteryConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedList;
import java.util.List;

public class SandmasterySpiritwebSubmodule implements ISpiritwebSubmodule
{
	private CompoundTag sandmasteryTag = null;
	private int hydrationLevel = SandmasteryConfigs.SERVER.STARTING_HYDRATION.get();
	private int projectileCooldown = 0;
	private final LinkedList<SandmasteryManifestation> ribbonsInUse = new LinkedList<>();

	private int hotkeyFlags = 0;

	@Override
	public void tickClient(ISpiritweb spiritweb)
	{
		if (spiritweb.getSelectedManifestation() instanceof SandmasteryManifestation sandmasteryManifestation)
		{
			final int isActivatedAndActive = Keybindings.MANIFESTATION_USE_ACTIVE.isDown()
					? 1
					: 0;

			final int elevateFlag = SandmasteryKeybindings.SANDMASTERY_ELEVATE.isDown()
					? SandmasteryConstants.ELEVATE_HOTKEY_FLAG
					: 0;
			final int launchFlag =
					SandmasteryKeybindings.SANDMASTERY_LAUNCH.isDown()
							? SandmasteryConstants.LAUNCH_HOTKEY_FLAG
							: 0;

			final int projectileFlag = SandmasteryKeybindings.SANDMASTERY_PROJECTILE.isDown()
					? SandmasteryConstants.PROJECTILE_HOTKEY_FLAG
					: 0;

			final int platformFlag = SandmasteryKeybindings.SANDMASTERY_PLATFORM.isDown()
					? SandmasteryConstants.PLATFORM_HOTKEY_FLAG
					: 0;

			int currentFlags = 0;
			currentFlags = currentFlags + isActivatedAndActive;
			currentFlags = currentFlags + elevateFlag;
			currentFlags = currentFlags + launchFlag;
			currentFlags = currentFlags + projectileFlag;
			currentFlags = currentFlags + platformFlag;

			if (hotkeyFlags != currentFlags)
			{
				sandmasteryTag.putInt(SandmasteryConstants.HOTKEY_TAG, currentFlags);
				hotkeyFlags = currentFlags;
				Sandmastery.packetHandler().sendToServer(new SyncMasteryBindsMessage(currentFlags));
			}
		}
		//if we are only tracking hotkeys when a sandmastery manifestation is selected
		//then things turn off when not selecting one. Would that be correct behaviour?
		//Todo more elegant way of checking if the user is wanting to use sandmastery?
		else if (hotkeyFlags != 0)
		{
			//don't create references unless needed
			//final CompoundTag dataTag = spiritweb.getCompoundTag();
			//reset flag
			hotkeyFlags = 0;
			//save
			sandmasteryTag.putInt(SandmasteryConstants.HOTKEY_TAG, hotkeyFlags);
			//update server
			Sandmastery.packetHandler().sendToServer(new SyncMasteryBindsMessage(hotkeyFlags));
		}
	}

	@Override
	public void tickServer(ISpiritweb spiritweb)
	{
	}

	@Override
	public void deserialize(ISpiritweb spiritweb)
	{
		final CompoundTag compoundTag = spiritweb.getCompoundTag();
		//save a reference to the tag
		sandmasteryTag = CompoundNBTHelper.getOrCreate(compoundTag, Sandmastery.MODID);
		//unload the player specific fields
		hydrationLevel = sandmasteryTag.getInt(SandmasteryConstants.HYDRATION_TAG);
		projectileCooldown = sandmasteryTag.getInt(SandmasteryConstants.PROJECTILE_COOLDOWN_TAG);
		hotkeyFlags = sandmasteryTag.getInt(SandmasteryConstants.HOTKEY_TAG);
	}

	@Override
	public void serialize(ISpiritweb spiritweb)
	{
		final CompoundTag compoundTag = spiritweb.getCompoundTag();

		if (sandmasteryTag == null)
		{
			sandmasteryTag = CompoundNBTHelper.getOrCreate(compoundTag, Sandmastery.MODID);
		}

		sandmasteryTag.putInt(SandmasteryConstants.HYDRATION_TAG, hydrationLevel);
		sandmasteryTag.putInt(SandmasteryConstants.PROJECTILE_COOLDOWN_TAG, projectileCooldown);
		sandmasteryTag.putInt(SandmasteryConstants.HOTKEY_TAG, hotkeyFlags);

		//this shouldn't be necessary, as the spiritweb tag should already have the reference
		//but we are hunting a null ref, so maybe something gets unassigned somewhere
		compoundTag.put(Sandmastery.MODID, sandmasteryTag);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void collectMenuInfo(List<String> m_infoText)
	{
		//todo Localization
		final String text = "Hydration: " + getHydrationLevel();
		m_infoText.add(text);
	}

	@Override
	public void GiveStartingItem(Player player)
	{
	}

	@Override
	public void GiveStartingItem(Player player, Manifestation manifestation)
	{
	}

	public int getHydrationLevel()
	{
		return hydrationLevel;
	}

	public boolean adjustHydration(int amountToAdjust, boolean allowOvermastery, LivingEntity entity)
	{
		int hydration = getHydrationLevel();
		final int newHydrationValue = hydration + amountToAdjust;
		hydrationLevel = Math.max(0, newHydrationValue);

		if (allowOvermastery && newHydrationValue < 0)
		{
			entity.addEffect(EffectsHelper.getNewEffect(SandmasteryEffects.OVERMASTERY_INSTANT_EFFECT.get(), 0, 1));
			entity.addEffect(EffectsHelper.getNewEffect(SandmasteryEffects.OVERMASTERED_EFFECT.get(), 0, SandmasteryConfigs.SERVER.OVERMASTERY_DURATION.get() * 20 * 60)); //  * 20 * 60 to convert minutes to ticks
		}

		return true;
	}

	public void adjustHydration(int amountToAdjust)
	{
		int hydration = getHydrationLevel();
		hydrationLevel = hydration + amountToAdjust;
	}

	public void tickProjectileCooldown()
	{
		this.projectileCooldown -= this.projectileCooldown < 1 ? 0 : 1;
	}

	public void setProjectileCooldown(int cooldown)
	{
		this.projectileCooldown = cooldown;
	}

	public boolean projectileReady()
	{
		return this.projectileCooldown <= 0;
	}


	public void useRibbon(ISpiritweb data, SandmasteryManifestation manifestation)
	{
		int maxRibbons = (int) manifestation.getStrength(data, false);
		if (ribbonsInUse.size() >= maxRibbons)
		{
			SandmasteryManifestation ribbon = ribbonsInUse.getLast();
			data.setMode(ribbon, data.getMode(ribbon) - 1);
		}
		ribbonsInUse.addFirst(manifestation);
		data.syncToClients(null);
	}

	public void releaseRibbon(ISpiritweb data, SandmasteryManifestation manifestation)
	{
		int index = ribbonsInUse.indexOf(manifestation);
		if (index > -1)
		{
			ribbonsInUse.remove(index);
		}
		data.syncToClients(null);
	}

	public void debugRibbonUsage()
	{
		CosmereAPI.logger.info("Ribbons in use ");
		CosmereAPI.logger.info(ribbonsInUse.toString());
	}

	public void updateFlags(int flags)
	{
		this.hotkeyFlags = flags;
		//update the tag value for later serialization.
		this.sandmasteryTag.putInt("hotkeys", hotkeyFlags);
	}
}
