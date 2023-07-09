package fr.delta.bedwars.game.shop.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import fr.delta.bedwars.data.AdditionalDataLoader;
import fr.delta.bedwars.game.shop.ShopMenu.ShopMenu;
import fr.delta.bedwars.mixin.PlayerEntityAccessor;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class ShopKeeperEntity extends PathAwareEntity implements PolymerEntity {
    private final ShopMenu shopMenu;
    private ServerPlayerEntity lookTarget = null;
    private final HolderAttachment attachment;
    private final Property skin;

    static public ShopKeeperEntity createEmpty(EntityType<? extends PathAwareEntity> entityType, World world)
    {
        return new ShopKeeperEntity(entityType, world, null);
    }
    public ShopKeeperEntity(EntityType<? extends PathAwareEntity> entityType, World world, ShopMenu shopMenu) {
        super(entityType, world);
        this.shopMenu = shopMenu;
        var holder = new ElementHolder();
        if(shopMenu != null)
        {
            var name = new TextDisplayElement(shopMenu.getShopKeeperName().formatted(Formatting.AQUA));
            name.setTranslation(new Vector3f(0f, 1f, 0f));
            name.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
            var rightClickToOpen = new TextDisplayElement(Text.translatable("shopkeeper.bedwars.title").formatted(Formatting.YELLOW, Formatting.BOLD));
            rightClickToOpen.setTranslation(new Vector3f(0f, 0.7f, 0f));
            rightClickToOpen.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);

            VirtualEntityUtils.addVirtualPassenger(this, name.getEntityId(), rightClickToOpen.getEntityId());

            holder.addElement(name);
            holder.addElement(rightClickToOpen);
        }

        this.attachment = new EntityAttachment(holder, this, false);
        this.skin = getSkin();
    }

    @Override
    public boolean isImmuneToExplosion() {
        return this.isInvulnerable();
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
    public void onBeforeSpawnPacket(ServerPlayerEntity player, Consumer<Packet<?>> packetConsumer) {
        var packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.UPDATE_LISTED));
        var gameProfile = new GameProfile(this.getUuid(), "NPC");

        gameProfile.getProperties().put("textures", skin);
        packet.getEntries().add(new PlayerListS2CPacket.Entry(this.getUuid(), gameProfile, false, 0, GameMode.ADVENTURE, null, null));
        packetConsumer.accept(packet);
    }

    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(player instanceof ServerPlayerEntity serverPlayer && shopMenu != null)
            shopMenu.open(serverPlayer);
        return ActionResult.FAIL;
    }

    @Override
    public void tick() {
        super.tick();

        var box = this.getBoundingBox().expand(4.0D);
        List<ServerPlayerEntity> players = this.getWorld().getEntitiesByClass(ServerPlayerEntity.class, box, LivingEntity::isPartOfGame);
        if(players.isEmpty()) return;
        if (this.lookTarget == null || this.distanceTo(this.lookTarget) > 5.0D || this.lookTarget.isDisconnected() || !this.lookTarget.isAlive()) {
            this.lookTarget = players.get(this.random.nextInt(players.size()));
        }
        this.lookAtEntity(lookTarget, 60.0F, 60.0F);
        this.setHeadYaw(this.getYaw());
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        var tracked = PlayerEntityAccessor.playerModelParts();
        assert tracked != null;
        data.add(new DataTracker.SerializedEntry<>(tracked.getId(), tracked.getType(), getModelPart()));
    }

    @Override
    public void setVelocity(Vec3d velocity)
    {
        super.setVelocity(Vec3d.ZERO);
    }

    private Property getSkin()
    {
        var keys = AdditionalDataLoader.SKIN_REGISTRY.keySet().stream().toList();
        var index = this.random.nextInt(keys.size());
        var id = keys.get(index);
        return AdditionalDataLoader.SKIN_REGISTRY.get(id);
    }

    private byte getModelPart()
    {
        return (byte) ( PlayerModelPart.CAPE.getBitFlag() |
                PlayerModelPart.JACKET.getBitFlag() |
                PlayerModelPart.LEFT_SLEEVE.getBitFlag() |
                PlayerModelPart.RIGHT_SLEEVE.getBitFlag() |
                PlayerModelPart.LEFT_PANTS_LEG.getBitFlag() |
                PlayerModelPart.RIGHT_PANTS_LEG.getBitFlag() |
                        PlayerModelPart.HAT.getBitFlag());
    }
}
