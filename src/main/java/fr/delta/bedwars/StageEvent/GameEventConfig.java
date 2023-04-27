package fr.delta.bedwars.StageEvent;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class GameEventConfig extends MapCodec<StageEvent> {

    public static Codec<StageEvent> CODEC = new GameEventConfig().codec();

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString("type"));
    }

    @Override
    public <T> DataResult<StageEvent> decode(DynamicOps<T> ops, MapLike<T> input) {
        var result = StageEvent.REGISTRY.decode(ops, input.get("type")).map(Pair::getFirst);
        result.error().ifPresent(System.out::println);
        return (DataResult<StageEvent>) this.decodeConfig(ops, input, result.result().get());
    }

    private <T> DataResult<?> decodeConfig(DynamicOps<T> ops, MapLike<T> input, Codec<?> configCodec) {
        if (configCodec instanceof MapCodecCodec<?> mapCodec) {
            return mapCodec.codec().decode(ops, input).map(Function.identity());
        } else {
            return configCodec.decode(ops, input.get("config")).map(Pair::getFirst);
        }
    }

    @Override
    public <T> RecordBuilder<T> encode(StageEvent input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return null;
    }
}
