package fr.delta.bedwars.game.shop.npc;
import fr.delta.bedwars.game.behavior.ClaimManager;
import fr.delta.bedwars.game.shop.ShopMenu.ShopMenu;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import xyz.nucleoid.map_templates.BlockBounds;

public class ShopKeeper {

    public static TaterzenNPC createShopKeeper(ServerWorld world, BlockBounds pos, ClaimManager claimManager, ShopMenu menu)
    {
        var npc = TaterzensAPI.createTaterzen(world, "ItemShopNPC", pos.centerBottom(), new float[]{0,0,0});
        npc.addProfession(ShopProfession.ID, new ShopProfession(menu));
        claimManager.addRegion(pos);
        npc.setInvulnerable(true);
        npc.setMovement(NPCData.Movement.FORCED_LOOK);
        npc.setCustomName(Text.translatable("shop.bedwars.itemShop").setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW).withBold(true)));
        world.getChunk(npc.getBlockPos());
        world.spawnEntity(npc);
        return npc;
    }
}
