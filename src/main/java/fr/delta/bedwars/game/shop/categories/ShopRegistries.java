package fr.delta.bedwars.game.shop.categories;

import fr.delta.bedwars.game.shop.articles.ShopEntry;
import fr.delta.bedwars.game.shop.articles.armors.ChainMailArmor;
import fr.delta.bedwars.game.shop.articles.armors.IronArmor;
import fr.delta.bedwars.game.shop.articles.blocks.EndStone;
import fr.delta.bedwars.game.shop.articles.blocks.Ladder;
import fr.delta.bedwars.game.shop.articles.blocks.Wood;
import fr.delta.bedwars.game.shop.articles.blocks.Wool;
import fr.delta.bedwars.game.shop.articles.swords.IronSword;
import fr.delta.bedwars.game.shop.articles.swords.StoneSword;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import java.util.Arrays;
import java.util.List;

public class ShopRegistries {
    public record ShopCategory(MutableText Name, Item display, List<ShopEntry> Entries) {}
    //todo add translation keys
    static ShopCategory BLOCKS = new ShopCategory(Text.literal("Blocks"), Items.ORANGE_TERRACOTTA, Arrays.asList(
            Wool.INSTANCE,
            EndStone.INSTANCE,
            Wood.INSTANCE,
            Ladder.INSTANCE
    ));

    static ShopCategory SWORDS = new ShopCategory(Text.literal("Swords"), Items.GOLDEN_SWORD, Arrays.asList(
            IronSword.INSTANCE,
            StoneSword.INSTANCE
    ));

    static ShopCategory ARMORS = new ShopCategory(Text.literal("Armors"), Items.IRON_CHESTPLATE, Arrays.asList(
            IronArmor.INSTANCE,
            ChainMailArmor.INSTANCE
    ));
    static public final List<ShopCategory> ItemShopCategories = Arrays.asList(
            BLOCKS,
            SWORDS,
            ARMORS
    );
}
