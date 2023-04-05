package fr.delta.bedwars.game.shop.entries;

import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EmptyEntry extends ShopEntry{
    public static EmptyEntry INSTANCE = new EmptyEntry();


    @Override
    public MutableText getName(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return Text.literal("Empty Entry").setStyle(Style.EMPTY.withFormatting(Formatting.RED));
    }

    @Override
    public Item getDisplay(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return Items.BARRIER;
    }

    @Override
    public boolean hasGlint(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return true;
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return new BuyOffer(false, Text.literal("this entry doesn't exist").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
    }

    @Override
    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return null;
    }
}
