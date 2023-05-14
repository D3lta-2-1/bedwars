package fr.delta.bedwars.game.shop.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.stream.Stream;

public class ShopEntryConfig extends MapCodec<Pair<Identifier, ShopEntry>> {

    public static Codec<Pair<Identifier, ShopEntry>> CODEC = new ShopEntryConfig().codec();

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.concat(Stream.of(ops.createString("type")), Stream.of(ops.createString("id")) );
    }

    @Override
    public <T> DataResult<Pair<Identifier, ShopEntry>> decode(DynamicOps<T> ops, MapLike<T> input) {
        var result = EntryRegistry.SHOP_ENTRY_CODECS.decode(ops, input.get("type") ).map(Pair::getFirst);
        result.error().ifPresent(error -> Bedwars.LOGGER.error(error.message(), error));
        if(result.result().isEmpty()) throw new RuntimeException("Unknown shop entry type");
        return this.decodeConfig(ops, input, result.result().get()).map(entry ->
            getEntryID(ops, input, (ShopEntry) entry)
        );
    }

    private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, Codec<?> configCodec) {
        if (configCodec instanceof MapCodecCodec<?> mapCodec) {
            return mapCodec.codec().decode(ops, input).map(Function.identity());
        } else {
            return configCodec.decode(ops, input.get("config")).map(Pair::getFirst);
        }
    }

    private <T> Pair<Identifier, ShopEntry> getEntryID(DynamicOps<T> ops, MapLike<T> input, ShopEntry entry)
    {
        var result = Identifier.CODEC.decode(ops, input.get("id")).map(Pair::getFirst);
        result.error().ifPresent(error -> Bedwars.LOGGER.error(error.message(), error));
        if(result.result().isEmpty()) throw new RuntimeException("Unknown ID for shop");
        return new Pair<>(result.result().get(), entry);
    }

    @Override
    public <T> RecordBuilder<T> encode(Pair<Identifier, ShopEntry> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return null;
    }
}
