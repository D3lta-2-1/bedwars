package fr.delta.bedwars;

import fr.delta.bedwars.event.RecipeClickedEvent;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public class GameRules {
    public static final GameRuleType BED_INTERACTION = GameRuleType.create();
    public static final GameRuleType BLAST_PROOF_GLASS_RULE = GameRuleType.create();
    public static final GameRuleType ENDER_PEARL_DAMAGE = GameRuleType.create();

    public static final GameRuleType RECIPE_BOOK_USAGE = GameRuleType.create()
            .enforces(RecipeClickedEvent.EVENT, result -> (player, recipe, craftAll) -> result == ActionResult.SUCCESS ? ActionResult.PASS : result);

    /* public static GameRuleType CRAFTING_INVENTORIES_USAGE = GameRuleType.create()
            .enforces(SlotInteractionEvent.BEFORE, result -> (player, handler, slotIndex, button, actionType) -> {
                System.out.println("called");
                if (result == ActionResult.SUCCESS) return ActionResult.PASS;
                var screenHandler = handler instanceof AbstractRecipeScreenHandler ? (AbstractRecipeScreenHandler<?>) handler : null;
                if (screenHandler == null) return ActionResult.PASS;
                if (slotIndex >= screenHandler.getCraftingResultSlotIndex() && slotIndex <= screenHandler.getCraftingSlotCount())
                    return ActionResult.FAIL;
                return ActionResult.PASS;
            });
     */
}
