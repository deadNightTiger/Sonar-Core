package sonar.core.common.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import sonar.core.utils.SonarAPI;
import sonar.core.utils.helpers.NBTHelper;
import sonar.core.utils.helpers.NBTHelper.SyncType;
import cofh.api.energy.EnergyStorage;
import cpw.mods.fml.common.Optional.Method;

public class TileEntityEnergy extends TileEntitySonar implements IEnergyTile {

	public EnergyStorage storage;
	public int maxTransfer;

	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		if (type == SyncType.SAVE || type == SyncType.SYNC) {
			NBTHelper.readEnergyStorage(storage, nbt);
		}

		if (type == SyncType.DROP) {
			this.storage.setEnergyStored(nbt.getInteger("energy"));
		}
	}

	public void writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		if (type == SyncType.SAVE || type == SyncType.SYNC) {
			NBTHelper.writeEnergyStorage(storage, nbt);
		}
		if (type == SyncType.DROP) {
			nbt.setInteger("energy", this.storage.getEnergyStored());

		}
	}

	public void onLoaded() {
		if (!this.worldObj.isRemote && SonarAPI.ic2Loaded()) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
		}
	}

	@Method(modid = "IC2")
	@Override
	public void invalidate() {
		super.invalidate();
		if (!this.worldObj.isRemote) {
			if (SonarAPI.ic2Loaded()) {
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}
		}
	}

}
