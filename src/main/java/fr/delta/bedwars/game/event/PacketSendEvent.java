package fr.delta.bedwars.game.event;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface PacketSendEvent {
    StimulusEvent<PacketSendEvent> EVENT = StimulusEvent.create(PacketSendEvent.class, ctx -> (packet, handler) -> {
        try {
            for (var listener : ctx.getListeners())
            {
                 if(listener.onPacketSendEvent(packet, handler) == ActionResult.FAIL) return ActionResult.FAIL;
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return ActionResult.SUCCESS;
    });
    ActionResult onPacketSendEvent(Packet<?> packet, ServerPlayNetworkHandler handler);

}
