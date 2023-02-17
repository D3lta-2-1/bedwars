package me.verya.bedwars.game.map;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.map_templates.BlockBounds;

public class RawTeamData {
    public RawTeamData(DyeColor color, BlockBounds spawnLocation, BlockBounds bedLocation, BlockBounds forge)
    {
        this.color = color;
        this.spawnLocation = spawnLocation;
        this.bedLocation = bedLocation;
        this.forge = forge;
    }
    public DyeColor color;
    public BlockBounds bedLocation;
    public BlockBounds spawnLocation;
    public BlockBounds forge;
}
