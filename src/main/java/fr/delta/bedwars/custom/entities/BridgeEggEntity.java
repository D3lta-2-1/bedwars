package fr.delta.bedwars.custom.entities;

import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class BridgeEggEntity extends EggEntity {
    private final BlockState trailBlock;
    private final BedwarsActive game;
    private Vec3d lastLeftPos = null;
    private Vec3d lastRightPos = null;
    private int blockLeft = 64;

    private BlockPos rightLineEnd = null;

    public BridgeEggEntity(World world, LivingEntity thrower, BlockState trailBlock, BedwarsActive game)
    {
        super(world, thrower);
        this.trailBlock = trailBlock;
        this.game = game;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.getWorld().isClient()) {
            return;
        }

        BlockPos pos = this.getBlockPos();
        if(!game.couldABlockBePlacedHere(pos))
        {
            this.getWorld().sendEntityStatus(this, (byte)3);
            this.discard();
        }
        else
        {
            for (BlockPos blockPos : this.getBlockPositions())
            {
                this.tryPlaceAt(blockPos);
            }
        }
    }

    private void tryPlaceAt(BlockPos pos)
    {
        if (this.getWorld().getBlockState(pos).isAir() && game.couldABlockBePlacedHere(pos) && this.getWorld().getEntitiesByClass(LivingEntity.class, new Box(pos), LivingEntity::isPartOfGame).isEmpty() && blockLeft > 0)
        {
            this.getWorld().setBlockState(pos, this.trailBlock);
            blockLeft--;
            if(blockLeft == 0)
            {
                this.getWorld().sendEntityStatus(this, (byte)3);
                this.discard();
            }
        }
    }

    /*@Override
    protected void onCollision(HitResult hitResult) {
        // ignore self-collisions
        if (hitResult.getType() == HitResult.Type.BLOCK)
        {
            if(Arrays.asList(lastBlockPlaced).contains(((BlockHitResult)hitResult).getBlockPos()))
                return;
        }
        this.world.sendEntityStatus(this, (byte)3);
        this.discard();
    }*/

    private Set<BlockPos> getBlockPositions()
    {
        var vel = this.getVelocity();
        var unit = vel.normalize();
        var pos = getPos().add(0, -1.8, 0);
        var yaw = Math.atan2(unit.getZ(), unit.getX());
        var leftYaw = yaw + Math.PI / 2;
        var rightYaw = yaw - Math.PI / 2;

        //get positions offsets
        var leftVec = new Vec3d(Math.cos(leftYaw), 0, Math.sin(leftYaw)).multiply(0.5);
        var rightVec = new Vec3d(Math.cos(rightYaw), 0, Math.sin(rightYaw)).multiply(0.5);
        var leftPos = pos.add(leftVec);
        var rightPos = pos.add(rightVec);
        var listOfPos = new HashSet<BlockPos>();

        if(lastLeftPos != null)
        {
            var leftLine = drawLine(from(lastLeftPos), from(leftPos));
            var rightLine = drawLine(from(lastRightPos), from(rightPos));

            //file the gap between the two lines in diagonal
            if(rightLineEnd != null) //start from the end of the last line
            {
                var left = leftLine.get(0);
                var right = rightLineEnd;
                drawLine(listOfPos, left, right);
            }
            for(int i = 1; i < leftLine.size(); i++) //draw line with 1 block offset
            {
                var i2 = i - 1;
                var left = leftLine.get(i);
                var right = rightLine.get(i2);
                drawLine(listOfPos, left, right);
            }
            rightLineEnd = rightLine.get(rightLine.size() - 1);
            listOfPos.addAll(leftLine);
            listOfPos.addAll(rightLine);
        }
        lastLeftPos = leftPos;
        lastRightPos = rightPos;

        return listOfPos;
    }

    static private BlockPos from(Vec3d vec)
    {
        return from(vec.x, vec.y, vec.z);
    }
    static private BlockPos from(double x, double y, double z)
    {
        return new BlockPos((int)x, (int)y, (int)z);
    }

    public static List<BlockPos> drawLine(BlockPos pos1, BlockPos pos2)
    {
        var listOfPoints = new ArrayList<BlockPos>();
        listOfPoints.add(pos1);
        int x1 = pos1.getX();
        int y1 = pos1.getY();
        int z1 = pos1.getZ();
        int x2 = pos2.getX();
        int y2 = pos2.getY();
        int z2 = pos2.getZ();
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);
        int xs;
        int ys;
        int zs;
        if (x2 > x1) {
            xs = 1;
        } else {
            xs = -1;
        }
        if (y2 > y1) {
            ys = 1;
        } else {
            ys = -1;
        }
        if (z2 > z1) {
            zs = 1;
        } else {
            zs = -1;
        }

        // Driving axis is X-axis
        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;
            while (x1 != x2) {
                x1 += xs;
                if (p1 >= 0) {
                    y1 += ys;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    z1 += zs;
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;
                listOfPoints.add(new BlockPos(x1, y1, z1));
            }

            // Driving axis is Y-axis
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;
            while (y1 != y2) {
                y1 += ys;
                if (p1 >= 0) {
                    x1 += xs;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    z1 += zs;
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx;
                p2 += 2 * dz;
                listOfPoints.add(new BlockPos(x1, y1, z1));
            }

            // Driving axis is Z-axis
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;
            while (z1 != z2) {
                z1 += zs;
                if (p1 >= 0) {
                    y1 += ys;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    x1 += xs;
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
                listOfPoints.add(new BlockPos(x1, y1, z1));
            }
        }
        return listOfPoints;
    }

    public static void drawLine(Set<BlockPos> listOfPoints,BlockPos pos1, BlockPos pos2)
    {
        listOfPoints.add(pos1);
        int x1 = pos1.getX();
        int y1 = pos1.getY();
        int z1 = pos1.getZ();
        int x2 = pos2.getX();
        int y2 = pos2.getY();
        int z2 = pos2.getZ();
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);
        int xs;
        int ys;
        int zs;
        if (x2 > x1) {
            xs = 1;
        } else {
            xs = -1;
        }
        if (y2 > y1) {
            ys = 1;
        } else {
            ys = -1;
        }
        if (z2 > z1) {
            zs = 1;
        } else {
            zs = -1;
        }

        // Driving axis is X-axis
        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;
            while (x1 != x2) {
                x1 += xs;
                if (p1 >= 0) {
                    y1 += ys;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    z1 += zs;
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;
                listOfPoints.add(new BlockPos(x1, y1, z1));
            }

            // Driving axis is Y-axis
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;
            while (y1 != y2) {
                y1 += ys;
                if (p1 >= 0) {
                    x1 += xs;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    z1 += zs;
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx;
                p2 += 2 * dz;
                listOfPoints.add(new BlockPos(x1, y1, z1));
            }

            // Driving axis is Z-axis
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;
            while (z1 != z2) {
                z1 += zs;
                if (p1 >= 0) {
                    y1 += ys;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    x1 += xs;
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
                listOfPoints.add(new BlockPos(x1, y1, z1));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        //disable chicken spawning
    }
}
