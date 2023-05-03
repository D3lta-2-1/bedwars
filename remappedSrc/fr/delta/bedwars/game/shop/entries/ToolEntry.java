package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.player.ToolManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolEntry extends ShopEntry{
    public static Codec<ToolEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Tier.CODEC.listOf().fieldOf("tiers").forGetter(ToolEntry::getAvailableTiers)
    ).apply(instance, ToolEntry::new));

    public record Tier(Item item, Cost cost, Map<Enchantment, Integer> enchantments) {
        public static Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Registries.ITEM.getCodec().fieldOf("item").forGetter(Tier::item),
                Cost.CODEC.fieldOf("cost").forGetter(Tier::cost),
                Codec.unboundedMap(Registries.ENCHANTMENT.getCodec(), Codec.INT).optionalFieldOf("enchantments", new HashMap<>()).forGetter(Tier::enchantments)
        ).apply(instance, Tier::new));
    }

    public ToolEntry(List<Tier> availableTiers) {
        this.availableTiers = availableTiers;
    }

    public List<Tier> getAvailableTiers() {
        return availableTiers;
    }

    List<Tier> availableTiers;

    private ToolManager getOrCreateManager(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var toolManagers = bedwarsGame.getInventoryManager().getToolManagers(PlayerRef.of(player));
        for (var manager : toolManagers) {

            if (manager.getAvailableTiers() == this.availableTiers) {
                return manager;
            }
        }

        //should not happen but just in case
        var toolManager = new ToolManager(availableTiers);
        toolManagers.add(toolManager);
        return toolManager;
    }

    @Override
    public void setup(BedwarsActive bedwarsGame) {
        for(var player : bedwarsGame.getTeamPlayersMap().values())
        {
            if(player == null)
                continue;
            bedwarsGame.getInventoryManager().getToolManagers(player).add(new ToolManager(availableTiers));
        }
        super.setup(bedwarsGame);
    }

    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return Text.translatable(getDisplay(bedwarsGame, player).getTranslationKey());
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var manager = getOrCreateManager(bedwarsGame, player);
        if(manager.isMaxed())
        {
            return availableTiers.get(availableTiers.size() - 1).item;
        }
        else
        {
            return availableTiers.get(manager.concurrentTier() + 1).item;
        }
    }

    @Override
    public Map<Enchantment, Integer> enchantment(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var manager = getOrCreateManager(bedwarsGame, player);
        if(manager.isMaxed())
        {
            return availableTiers.get(availableTiers.size() - 1).enchantments;
        }
        else
        {
            return availableTiers.get(manager.concurrentTier() + 1).enchantments;
        }
    }

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var manager = getOrCreateManager(bedwarsGame, player);
        if(manager.isMaxed())
        {
            return null;
        }
        else
        {
            return availableTiers.get(manager.concurrentTier() + 1).cost;
        }
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var manager = getOrCreateManager(bedwarsGame, player);
        return new BuyOffer(!manager.isMaxed(), Text.literal("tool Maxed").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var manager = getOrCreateManager(bedwarsGame, player);
        if(manager.isEmpty()) BedwarsEvents.ensureInventoryIsNotFull(player, bedwarsGame.getActivity());
        return manager.upgrade(player);
    }
}
