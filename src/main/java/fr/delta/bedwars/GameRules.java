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

    public static final GameRuleType REDUCED_EXPLOSION_DAMAGE = GameRuleType.create();
    public static final GameRuleType AMPLIFIED_EXPLOSION_KNOCKBACK = GameRuleType.create();
    public static final GameRuleType FIRE_SPREAD = GameRuleType.create();
    public static final GameRuleType REDUCED_FALL_DAMAGE = GameRuleType.create();
}
