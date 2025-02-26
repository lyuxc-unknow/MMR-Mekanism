package es.degrassi.mmreborn.mekanism.common.crafting.requirement.jei;


import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedSizedRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.jei.JeiComponent;
import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import es.degrassi.mmreborn.common.integration.jei.category.MMRRecipeCategory;
import es.degrassi.mmreborn.common.integration.jei.category.drawable.DrawableWrappedText;
import es.degrassi.mmreborn.common.util.Utils;
import es.degrassi.mmreborn.mekanism.common.crafting.requirement.RequirementChemical;
import es.degrassi.mmreborn.mekanism.common.machine.component.ChemicalComponent;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.recipe_viewer.jei.ChemicalStackRenderer;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JeiChemicalComponent extends JeiComponent<ChemicalStack, RecipeRequirement<ChemicalComponent, RequirementChemical>> {
  public JeiChemicalComponent(RecipeRequirement<ChemicalComponent, RequirementChemical> requirement) {
    super(requirement, 0, 0);
  }

  @Override
  public int getWidth() {
    return 16;
  }

  @Override
  public int getHeight() {
    return 16;
  }

  @Override
  public List<ChemicalStack> ingredients() {
    return Lists.newArrayList(requirement.requirement().required.copy());
  }

  @Override
  @SuppressWarnings("removal")
  public @NotNull List<Component> getTooltip(@NotNull ChemicalStack ingredient, @NotNull TooltipFlag tooltipFlag) {
    List<Component> tooltip = super.getTooltip(ingredient, tooltipFlag);
    String mode = requirement.requirement().getMode().isInput() ? "input" : "output";
    tooltip.add(Component.translatable("modular_machinery_reborn.jei.ingredient.chemical." + mode, ingredient.getTextComponent(), ingredient.getAmount()));
    if (requirement.chance() < 1F && requirement.chance() >= 0F) {
      String keyNever = requirement.requirement().getMode().isInput() ? "tooltip.machinery.chance.in.never" : "tooltip.machinery" +
          ".chance.out.never";
      String keyChance = requirement.requirement().getMode().isInput() ? "tooltip.machinery.chance.in" : "tooltip" +
          ".machinery.chance.out";

      String chanceStr = String.valueOf(Mth.floor(requirement.chance() * 100F));
      if (requirement.chance() == 0F) {
        tooltip.add(Component.translatable(keyNever));
      } else {
        if (requirement.chance() < 0.01F) {
          chanceStr = "< 1";
        }
        chanceStr += "%";
        tooltip.add(Component.translatable(keyChance, chanceStr));
      }
    }
    return tooltip;
  }

  @Override
  public void setRecipe(MMRRecipeCategory category, IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
    Component component = Component.empty();
    String chance = Utils.decimalFormat(requirement.chance() * 100);
    if (requirement.chance() > 0 && requirement.chance() < 1)
      component = Component.translatable("modular_machinery_reborn.ingredient.chance", chance, "%").withColor(Config.chanceColor);
    else if (requirement.chance() == 0)
      component = Component.translatable("modular_machinery_reborn.ingredient.chance.nc").withColor(Config.chanceColor);
    Font font = Minecraft.getInstance().font;
    recipe.chanceTexts.add(
        Pair.of(
            new PositionedSizedRequirement(
                getPosition().x(),
                getPosition().y(),
                getWidth(),
                font.wordWrapHeight(component, getWidth())
            ),
            new DrawableWrappedText(List.of(component), getWidth() + 2, true)
                .transform(DrawableWrappedText.Operation.SET, DrawableWrappedText.State.TRANSLATEX, getPosition().x())
                .transform(DrawableWrappedText.Operation.SET, DrawableWrappedText.State.TRANSLATEY, getPosition().y())
                .transform(DrawableWrappedText.Operation.SET, DrawableWrappedText.State.SCALE, 0.75)
                .transform(DrawableWrappedText.Operation.SET, DrawableWrappedText.State.TRANSLATEZ, 500)
                .transform(DrawableWrappedText.Operation.SET, DrawableWrappedText.State.TRANSLATEX, (double) (getWidth() - 16) / 2)
                .transform(DrawableWrappedText.Operation.ADD, DrawableWrappedText.State.TRANSLATEX, 17)
                .transform(DrawableWrappedText.Operation.REMOVE, DrawableWrappedText.State.TRANSLATEX, Math.min(14, font.width(component)))
                .transform(DrawableWrappedText.Operation.SET, DrawableWrappedText.State.TRANSLATEY, (double) (getHeight() - 16) / 2)
                .transform(DrawableWrappedText.Operation.REMOVE, DrawableWrappedText.State.TRANSLATEY, 1)
                .transform(DrawableWrappedText.Operation.ADD, DrawableWrappedText.State.TRANSLATEY, (double) font.lineHeight * 3/2)
        )
    );
    builder
        .addSlot(role(), getPosition().x(), getPosition().y())
        .setOverlay(
            MMRJeiPlugin.jeiHelpers.getGuiHelper().createDrawable(texture(), getUOffset(), getVOffset(), getWidth() + 2, getHeight() + 2),
            -1,
            -1
        )
        .setCustomRenderer(MekanismJEI.TYPE_CHEMICAL, new ChemicalStackRenderer(getRequirement().requirement().amount, getWidth(), getHeight()))
        .addIngredient(MekanismJEI.TYPE_CHEMICAL, new ChemicalStack(getRequirement().requirement().required.getChemical(), getRequirement().requirement().amount))
        .addRichTooltipCallback((slot, tooltip) -> tooltip.addAll(getTooltip(ingredients().get(0), TooltipFlag.NORMAL)));
  }
}
