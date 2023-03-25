package fr.delta.bedwars;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import fr.delta.bedwars.custom.blocks.LaunchPadBlockEntity;
import fr.delta.bedwars.custom.blocks.Launchpad;
import fr.delta.bedwars.game.BedwarsWaiting;
import fr.delta.bedwars.game.shop.data.ShopConfigs;
import fr.delta.bedwars.custom.items.FireBall;
import fr.delta.bedwars.game.shop.npc.ShopKeeperEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.plasmid.game.GameType;

public class Bedwars implements DedicatedServerModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    public static final Block LAUNCH_PAD = new Launchpad(AbstractBlock.Settings.copy(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE).hardness(0.5f).noCollision().resistance(6.f), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
    public static final BlockEntityType<LaunchPadBlockEntity> LAUNCH_PAD_ENTITY = FabricBlockEntityTypeBuilder.create(LaunchPadBlockEntity::new, LAUNCH_PAD).build();
    public static final EntityType<ShopKeeperEntity> SHOP_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier(ID, "shop_entity"), FabricEntityTypeBuilder.create(SpawnGroup.MISC, ShopKeeperEntity::createEmpty).dimensions(EntityDimensions.fixed(0.6F, 1.8F)).build());

    @Override
    public void onInitializeServer() {

        //register items
        Registry.register(Registries.ITEM, new Identifier("bedwars:fire_ball"), new FireBall(new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier(ID, "launch_pad"), new PolymerBlockItem(LAUNCH_PAD, new Item.Settings(), Items.HEAVY_WEIGHTED_PRESSURE_PLATE));
        //register blocks
        Registry.register(Registries.BLOCK, new Identifier(ID, "launch_pad"), LAUNCH_PAD);
        //register block entities
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(ID, "launch_pad"), LAUNCH_PAD_ENTITY);
        //polymer utils
        PolymerBlockUtils.registerBlockEntity(LAUNCH_PAD_ENTITY);
        PolymerEntityUtils.registerType(SHOP_ENTITY);
        FabricDefaultAttributeRegistry.register(SHOP_ENTITY, ShopKeeperEntity.createMobAttributes());
        //register game type
        GameType.register(new Identifier(ID, "bedwars"), BedwarsConfig.CODEC, BedwarsWaiting::open);
        //load all shops categories
        ShopConfigs.register();
    }
}
