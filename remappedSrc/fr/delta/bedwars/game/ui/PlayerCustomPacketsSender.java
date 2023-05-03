package fr.delta.bedwars.game.ui;

import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PlayerCustomPacketsSender {
    //Fade - Ticks to spend fading in.
    //Stay - Ticks to keep the title displayed.
    //Fade Out - Ticks to spend fading out, not when to start fading out.
    static public void showTitle(ServerPlayerEntity player, Text title, Text subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }

    static public void showTitle(ServerPlayerEntity player, Text title, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks));
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
    }

    static public void changeSubtitle(ServerPlayerEntity player, Text subtitle)
    {
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }

    static public void showOverlay(ServerPlayerEntity player, Text overlay)
    {
        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(overlay));
    }

    static public void clearTitleAndOverLays(ServerPlayerEntity player)
    {
        player.networkHandler.sendPacket(new ClearTitleS2CPacket(false));
    }
}
