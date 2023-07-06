package fr.delta.bedwars;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.custom.items.BridgeEgg;
import fr.delta.bedwars.custom.items.PopupItem;
import fr.delta.bedwars.game.BedwarsWaiting;
import fr.delta.bedwars.data.AdditionalDataLoader;
import fr.delta.bedwars.custom.items.FireBall;
import fr.delta.bedwars.game.shop.npc.ShopKeeperEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.plasmid.game.GameType;

public class Bedwars implements DedicatedServerModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final EntityType<ShopKeeperEntity> SHOP_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier(ID, "shop_entity"), FabricEntityTypeBuilder.create(SpawnGroup.MISC, ShopKeeperEntity::createEmpty).dimensions(EntityDimensions.fixed(0.6F, 1.8F)).build());
    public static Item FIRE_BALL = Registry.register(Registries.ITEM, new Identifier("bedwars:fire_ball"), new FireBall(new FabricItemSettings()));
    public static Item BRIDGE_EGG = Registry.register(Registries.ITEM, new Identifier("bedwars:bridge_egg"), new BridgeEgg(new FabricItemSettings()));
    public static Item POPUP_TOWER = Registry.register(Registries.ITEM, new Identifier("bedwars:popup_tower"), new PopupItem(new FabricItemSettings(), new Identifier("bedwars", "popup_tower"), PopupItem::CircularIterable));
    public static Item POPUP_WALL = Registry.register(Registries.ITEM, new Identifier("bedwars:popup_wall"), new PopupItem(new FabricItemSettings(), new Identifier("bedwars", "popup_wall"), PopupItem::LinearIterable));

    @Override
    public void onInitializeServer()
    {
        PolymerEntityUtils.registerType(SHOP_ENTITY);
        FabricDefaultAttributeRegistry.register(SHOP_ENTITY, ShopKeeperEntity.createMobAttributes());
        //register game type
        GameType.register(new Identifier(ID, "bedwars"), BedwarsConfig.CODEC, BedwarsWaiting::open);
        //load all shops categories
        AdditionalDataLoader.register();
    }
}
