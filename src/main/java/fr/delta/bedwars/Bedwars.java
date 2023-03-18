package fr.delta.bedwars;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import fr.delta.bedwars.custom.blocks.LaunchPadBlockEntity;
import fr.delta.bedwars.custom.blocks.Launchpad;
import fr.delta.bedwars.game.BedwarsWaiting;
import fr.delta.bedwars.game.shop.data.ShopConfigs;
import fr.delta.bedwars.custom.items.FireBall;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public class Bedwars implements DedicatedServerModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final GameRuleType BED_INTERACTION = GameRuleType.create();
    public static final GameRuleType BLAST_PROOF_GLASS_RULE = GameRuleType.create();

    public static final Block LAUNCH_PAD = new Launchpad(AbstractBlock.Settings.copy(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE).hardness(0.5f).noCollision().resistance(6.f), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
    public static final BlockEntityType<LaunchPadBlockEntity> LAUNCH_PAD_ENTITY = FabricBlockEntityTypeBuilder.create(LaunchPadBlockEntity::new, LAUNCH_PAD).build();

    @Override
    public void onInitializeServer() {
        Registry.register(Registries.ITEM, new Identifier("bedwars:fire_ball"), new FireBall(new FabricItemSettings()));
        Registry.register(Registries.BLOCK, new Identifier(ID, "launch_pad"), LAUNCH_PAD);
        Registry.register(Registries.ITEM, new Identifier(ID, "launch_pad"), new PolymerBlockItem(LAUNCH_PAD, new Item.Settings(), Items.HEAVY_WEIGHTED_PRESSURE_PLATE));
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(ID, "launch_pad"), LAUNCH_PAD_ENTITY);
        PolymerBlockUtils.registerBlockEntity(LAUNCH_PAD_ENTITY);
        //register game type
        GameType.register(new Identifier(ID, "bedwars"), BedwarsConfig.CODEC, BedwarsWaiting::open);
        //load all shops categories
        ShopConfigs.register();
    }
}
