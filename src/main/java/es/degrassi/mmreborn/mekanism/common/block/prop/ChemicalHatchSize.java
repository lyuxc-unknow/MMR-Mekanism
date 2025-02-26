package es.degrassi.mmreborn.mekanism.common.block.prop;

import es.degrassi.mmreborn.common.block.prop.ConfigLoaded;
import es.degrassi.mmreborn.mekanism.common.data.MMRConfig;
import es.degrassi.mmreborn.mekanism.common.entity.base.ChemicalTankEntity;
import es.degrassi.mmreborn.mekanism.common.network.server.component.SUpdateChemicalComponentPacket;
import lombok.Getter;
import lombok.Setter;
import mekanism.api.AutomationType;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ChemicalHatchSize implements StringRepresentable, ConfigLoaded {
  TINY(100),
  SMALL(400),
  NORMAL(1000),
  REINFORCED(2000),
  BIG(4500),
  HUGE(8000),
  LUDICROUS(16000),
  VACUUM(32000);

  @Getter
  @Setter
  private long size;

  public final long defaultConfigurationValue;

  ChemicalHatchSize(long defaultConfigurationValue) {
    this.defaultConfigurationValue = defaultConfigurationValue;
  }

  public static ChemicalHatchSize value(String value) {
    return switch(value.toUpperCase(Locale.ROOT)) {
      case "SMALL" -> SMALL;
      case "NORMAL" -> NORMAL;
      case "REINFORCED" -> REINFORCED;
      case "BIG" -> BIG;
      case "HUGE" -> HUGE;
      case "LUDICROUS" -> LUDICROUS;
      case "VACUUM" -> VACUUM;
      default -> TINY;
    };
  }

  public BasicChemicalTank buildTank(ChemicalTankEntity tileEntity, boolean canFill, boolean canDrain) {
    return (BasicChemicalTank) BasicChemicalTank.create(
      size,
      ((chemical, automationType) -> canDrain || automationType == AutomationType.INTERNAL),
      ((chemical, automationType) -> canFill || automationType == AutomationType.INTERNAL),
      ConstantPredicates.alwaysTrue(),
      ChemicalAttributeValidator.ALWAYS_ALLOW,
      () -> {
        if (tileEntity.getLevel() instanceof ServerLevel l)
          PacketDistributor.sendToPlayersTrackingChunk(
            l,
            new ChunkPos(tileEntity.getBlockPos()),
            new SUpdateChemicalComponentPacket(tileEntity.getTank().getStack(), tileEntity.getBlockPos())
          );
      }
    );
  }

  @Override
  public @NotNull String getSerializedName() {
    return name().toLowerCase();
  }
}
