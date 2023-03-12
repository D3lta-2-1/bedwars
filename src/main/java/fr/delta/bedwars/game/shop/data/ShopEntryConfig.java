package fr.delta.bedwars.game.shop.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import fr.delta.bedwars.game.shop.articles.ShopEntry;

import java.util.function.Function;
import java.util.stream.Stream;

public class ShopEntryConfig extends MapCodec<ShopEntry> {

    public static Codec<ShopEntry> CODEC = new ShopEntryConfig().codec();

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString("type"));
    }

    @Override
    public <T> DataResult<ShopEntry> decode(DynamicOps<T> ops, MapLike<T> input) {
        var result = EntryRegistry.SHOP_ENTRY_CODECS.decode(ops, input.get("type") ).map(Pair::getFirst);
        result.error().ifPresent(System.out::println);
        return (DataResult<ShopEntry>) this.decodeConfig(ops, input, result.result().get());
    }

    @Override
    public <T> RecordBuilder<T> encode(ShopEntry input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return null;
    }

    private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, Codec<?> configCodec) {
        if (configCodec instanceof MapCodecCodec<?> mapCodec) {
            return mapCodec.codec().decode(ops, input).map(Function.identity());
        } else {
            return configCodec.decode(ops, input.get("config")).map(Pair::getFirst);
        }
    }
}
