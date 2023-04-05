package fr.delta.bedwars.game.map;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.map_templates.BlockBounds;

public class RawTeamData {
    public RawTeamData(DyeColor color, BlockBounds spawnLocation, BlockBounds bedLocation, BlockBounds forge, BlockBounds effectPool)
    {
        this.color = color;
        this.spawnLocation = spawnLocation;
        this.bedLocation = bedLocation;
        this.forge = forge;
        this.effectPool = effectPool;
    }
    public DyeColor color;
    public BlockBounds bedLocation;
    public BlockBounds spawnLocation;
    public BlockBounds forge;
    public BlockBounds effectPool;
}
