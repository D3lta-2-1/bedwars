package fr.delta.bedwars.event;

import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface RecipeClickedEvent {

    StimulusEvent<RecipeClickedEvent> EVENT = StimulusEvent.create(RecipeClickedEvent.class, ctx -> (player, recipe, craftAll) -> {
        try{
            for(var listener : ctx.getListeners())
            {
                var result = listener.onClicked(player, recipe, craftAll);
                if(result == ActionResult.SUCCESS || result == ActionResult.FAIL) return result;
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return ActionResult.SUCCESS;
    });

    ActionResult onClicked(ServerPlayerEntity player, @Nullable Recipe<?> recipe, boolean craftAll);
}
