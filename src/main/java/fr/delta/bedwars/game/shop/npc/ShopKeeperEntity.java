package fr.delta.bedwars.game.shop.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import fr.delta.bedwars.game.shop.ShopMenu.ShopMenu;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class ShopKeeperEntity extends PathAwareEntity implements PolymerEntity {
    private final ShopMenu shopMenu;
    private ServerPlayerEntity lookTarget = null;

    static public ShopKeeperEntity createEmpty(EntityType<? extends PathAwareEntity> entityType, World world)
    {
        return new ShopKeeperEntity(entityType, world);
    }
    public ShopKeeperEntity(EntityType<? extends PathAwareEntity> entityType, World world, ShopMenu shopMenu) {
        super(entityType, world);
        this.shopMenu = shopMenu;
    }

    protected ShopKeeperEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.shopMenu = null;
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.PLAYER;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return PolymerEntityUtils.createPlayerSpawnPacket(this);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void onBeforeSpawnPacket(Consumer<Packet<?>> packetConsumer) {
        var packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.UPDATE_LISTED));
        var gameProfile = new GameProfile(this.getUuid(), "Test NPC");
        gameProfile.getProperties().put("textures", new Property("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTYxNDk0NDg4ODg4OSwKICAicHJvZmlsZUlkIiA6ICI1N2IzZGZiNWY4YTY0OWUyOGI1NDRlNGZmYzYzMjU2ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYaWthcm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzdmYzc1ZTBlYzAwNDAyMjMyOTZhYTRkMDhiZDI2YmU0ZDE3MmU4ZGUwNzE4NTU4ODgyMmZhZTM2M2QyMjMxOSIKICAgIH0KICB9Cn0=",
                "R/dm6ic4CYbsr66Iz859K5r1MVd7y08FUvOmJgKTE5KRcPdDNe71Vv61jzh0jQ9QeZJXsHe4+58RY2LiXn7LdPKWpNd+ljK2K4n00Yjp/MM9s6ppNOAQj32LY5UuwcXPUkTSQfr2GROM9zvY93lAuILr6xodvUoIrPcbBDHgxuN6FDiE1jKfFF5z2yZIHOVZXqJPJ+0ri1sw3mjMhbO3dPdpzTW24olgR3wqbXgfEwIeiMk1En+wBtce6ZnNHNXIaMj4fFDAsMmFKqvFcPY8SjfjW/jWBDYNFUCMpxTS2XduQGhSoSlNXG+OrI93Ya/iObGeqAp9WCqFvkV8azyG1VTFfegZCFrUwKV+819B8Q3H3JzJOzES9zvhX5CDKYaE4QvWAqGzTOVw7h0NxtOh9alFkbRR2lWFiBhUMT8EqRjkb+OyBVe9vGRJOU448aLQFyuEWLICje9FAmOHRH0JFpMDEKCLvAAZKZAOx9jceQKrcrcAS0f9nnqjWLLrWMK8lWh0CNcPN1P51rQsxMUlWddNEig+RyjOLHIz/fsv3EQ7yycWkeFfkxq0NAVZGajp4T3NhtWG+WlYywafy5Gtys0Mmv4CXu6xzoUdeLhtMjwgmqfdatQlAJGiZCuSMc1KwWis2inI1YDg5jIy8BTViFBGn76mks21iUEpL4JP8FU="
        ));
        packet.getEntries().add(new PlayerListS2CPacket.Entry(this.getUuid(), gameProfile, false, 0, GameMode.ADVENTURE, null, null));
        packetConsumer.accept(packet);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(player instanceof ServerPlayerEntity serverPlayer && shopMenu != null)
            shopMenu.open(serverPlayer);
        return super.interactMob(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        var box = this.getBoundingBox().expand(4.0D);
        List<ServerPlayerEntity> players = this.world.getEntitiesByClass(ServerPlayerEntity.class, box, (empty) -> true);
        if(players.isEmpty()) return;
        if (this.lookTarget == null || this.distanceTo(this.lookTarget) > 5.0D || this.lookTarget.isDisconnected() || !this.lookTarget.isAlive()) {
            this.lookTarget = players.get(this.random.nextInt(players.size()));
        }
        this.lookAtEntity(lookTarget, 60.0F, 60.0F);
        this.setHeadYaw(this.getYaw());
    }
}
