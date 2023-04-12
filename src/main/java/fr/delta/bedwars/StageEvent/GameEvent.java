package fr.delta.bedwars.StageEvent;

import com.mojang.serialization.Codec;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public interface GameEvent {
    TinyRegistry<Codec<? extends GameEvent>> REGISTRY = register();
    static private TinyRegistry<Codec<? extends GameEvent>> register() {
        TinyRegistry<Codec<? extends GameEvent>> registry = TinyRegistry.create();
        registry.register(new Identifier(Bedwars.ID, "change_generator_tier"), UpdateGeneratorTier.CODEC);
        registry.register(new Identifier(Bedwars.ID, "bed_destruction"), BedDestruction.CODEC);
        return registry;
    }

    int getTimeToWait();
    Text getStageName(BedwarsActive game);
    void run(BedwarsActive game);


}
