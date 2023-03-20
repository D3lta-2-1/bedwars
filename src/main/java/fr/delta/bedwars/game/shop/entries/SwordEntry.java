package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class SwordEntry extends ShopEntry {

    public static Codec<SwordEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(SwordEntry::getDisplayNoArgument),
            Cost.CODEC.fieldOf("cost").forGetter(SwordEntry::getCostNoArgument)
    ).apply(instance, SwordEntry::new));
    public SwordEntry(Item item, Cost cost) {
        this.item = item;
        this.cost = cost;
    }

    final private Item item;
    final private Cost cost;

    public Cost getCostNoArgument() {
        return cost;
    }
    public Item getDisplayNoArgument() {
        return item;
    }

    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return cost;
    }
    @Override
    public MutableText getName(BedwarsActive BedwarsGame, ServerPlayerEntity player)
    {
        return Text.translatable(item.getTranslationKey());
    }

    @Override
    public Item getDisplay(BedwarsActive BedwarsGame, ServerPlayerEntity player)
    {
        return item;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        bedwarsGame.getDefaultSwordManager().removeDefaultSword(player);
        return ItemStackBuilder.of(item).setUnbreakable().build();
    }
}
