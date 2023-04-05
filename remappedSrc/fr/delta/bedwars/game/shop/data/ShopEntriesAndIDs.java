package fr.delta.bedwars.game.shop.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.util.Identifier;
import java.util.List;

public record ShopEntriesAndIDs(List<Pair<Identifier, ShopEntry>> entries) {
    static public Codec<ShopEntriesAndIDs> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ShopEntryConfig.CODEC.listOf().fieldOf("entries").forGetter(ShopEntriesAndIDs::entries)
            ).apply(instance, ShopEntriesAndIDs::new)
        );
}
