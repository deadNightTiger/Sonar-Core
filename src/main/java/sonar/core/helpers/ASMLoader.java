package sonar.core.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import sonar.core.SonarCore;
import sonar.core.api.asm.EnergyContainerHandler;
import sonar.core.api.asm.EnergyHandler;
import sonar.core.api.asm.FluidHandler;
import sonar.core.api.asm.InventoryHandler;
import sonar.core.api.energy.ISonarEnergyContainerHandler;
import sonar.core.api.energy.ISonarEnergyHandler;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.inventories.ISonarInventoryHandler;
import sonar.core.utils.Pair;
import sonar.core.utils.SortingDirection;

public class ASMLoader {

	public enum ASMLog {
		LOADED, ERROR, MODID;
	}

	public static void load(@Nonnull ASMDataTable asmDataTable) {
		SonarCore.inventoryHandlers = getInventoryHandlers(asmDataTable);
		SonarCore.energyHandlers = getEnergyHandlers(asmDataTable);
		SonarCore.fluidHandlers = getFluidHandlers(asmDataTable);
		SonarCore.energyContainerHandlers = getEnergyContainerHandlers(asmDataTable);
	}

	public static List<ISonarInventoryHandler> getInventoryHandlers(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, InventoryHandler.class, ISonarInventoryHandler.class, true, true);
	}

	public static List<ISonarEnergyHandler> getEnergyHandlers(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, EnergyHandler.class, ISonarEnergyHandler.class, true, true);
	}

	public static List<ISonarFluidHandler> getFluidHandlers(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, FluidHandler.class, ISonarFluidHandler.class, true, true);
	}

	public static List<ISonarEnergyContainerHandler> getEnergyContainerHandlers(@Nonnull ASMDataTable asmDataTable) {
		return ASMLoader.getInstances(asmDataTable, EnergyContainerHandler.class, ISonarEnergyContainerHandler.class, true, true);
	}

	public static void log(ASMLog log, Class type, ASMData asm, String modid) {
		switch (log) {
		case ERROR:
			SonarCore.logger.error(type.getSimpleName() + " couldn't be loaded: {}", asm.getClassName());
			break;
		case LOADED:
			SonarCore.logger.info(type.getSimpleName() + " loaded successfully: {}", asm.getClassName());
			break;
		case MODID:
			SonarCore.logger.error(String.format("Couldn't load " + type.getSimpleName() + "%s for modid %s", asm.getClassName(), modid));
			break;
		default:
			break;
		}
	}

	public static <T> List<T> getInstances(@Nonnull ASMDataTable asmDataTable, Class annotation, Class<T> instanceClass, boolean checkModid, boolean sortPriority) {
		String annotationClassName = annotation.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		ArrayList<ASMDataTable.ASMData> data = new ArrayList();
		asmDatas.forEach(asmData -> data.add(asmData));

		if (sortPriority) {
			data.sort(new Comparator<ASMDataTable.ASMData>() {
				public int compare(ASMDataTable.ASMData str1, ASMDataTable.ASMData str2) {
					return SonarHelper.compareWithDirection((int) str1.getAnnotationInfo().get("priority"), (int) str2.getAnnotationInfo().get("priority"), SortingDirection.UP);
				}
			});
		}

		List<T> instances = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : data) {
			String modid = checkModid ? (String) asmData.getAnnotationInfo().get("modid") : "";
			if (!checkModid || Loader.isModLoaded(modid) || Loader.isModLoaded(modid.toLowerCase())) {
				try {
					Class<?> asmClass = Class.forName(asmData.getClassName());
					Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
					T instance = asmInstanceClass.newInstance();
					instances.add(instance);
					log(ASMLog.LOADED, instanceClass, asmData, modid);
					continue;
				} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
					log(ASMLog.ERROR, instanceClass, asmData, modid);
					continue;
				}
			} else {
				log(ASMLog.MODID, instanceClass, asmData, modid);
			}
		}

		return instances;
	}

	public static <T> List<Pair<ASMDataTable.ASMData, Class<? extends T>>> getClasses(@Nonnull ASMDataTable asmDataTable, Class annotation, Class<T> instanceClass, boolean checkModid) {
		String annotationClassName = annotation.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		List<Pair<ASMDataTable.ASMData, Class<? extends T>>> classes = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : asmDatas) {
			String modid = checkModid ? (String) asmData.getAnnotationInfo().get("modid") : "";
			if (!checkModid || Loader.isModLoaded(modid) || Loader.isModLoaded(modid.toLowerCase())) {
				try {
					Class<?> asmClass = Class.forName(asmData.getClassName());
					Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
					classes.add(new Pair(asmData, asmInstanceClass));
					log(ASMLog.LOADED, instanceClass, asmData, modid);
					continue;
				} catch (ClassNotFoundException e) {
					log(ASMLog.ERROR, instanceClass, asmData, modid);
					continue;
				}
			} else {
				log(ASMLog.MODID, instanceClass, asmData, modid);
			}
		}
		return classes;
	}
}
