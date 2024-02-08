package com.mrmelon54.WirelessRedstone.packet;

import com.mrmelon54.WirelessRedstone.WirelessRedstone;
import com.mrmelon54.WirelessRedstone.block.entity.WirelessReceiverBlockEntity;
import com.mrmelon54.WirelessRedstone.block.entity.WirelessTransmitterBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.core.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public record BlockFrequencyChangeC2SPacket(boolean isReceiver, BlockPos blockPos, int freq) {
    public static BlockFrequencyChangeC2SPacket decode(FriendlyByteBuf byteBuf) {
        return new BlockFrequencyChangeC2SPacket(byteBuf.readBoolean(), byteBuf.readBlockPos(), byteBuf.readInt());
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(isReceiver);
        byteBuf.writeBlockPos(blockPos);
        byteBuf.writeInt(freq);
    }

    public void apply(Supplier<NetworkManager.PacketContext> packetContextSupplier) {
        Player player = packetContextSupplier.get().getPlayer();

        @SuppressWarnings("resource")
        Level level = player.level();
        if (isReceiver) {
            Optional<WirelessReceiverBlockEntity> blockEntity = level.getBlockEntity(blockPos, WirelessRedstone.WIRELESS_RECEIVER_BLOCK_ENTITY);
            blockEntity.ifPresentOrElse(x -> x.setFrequency(freq), () -> WirelessRedstone.LOGGER.warn("Failed to apply BlockFrequencyChangeC2SPacket(true, {" + blockPos.toShortString() + "}, " + freq + ")"));
        } else {
            Optional<WirelessTransmitterBlockEntity> blockEntity = level.getBlockEntity(blockPos, WirelessRedstone.WIRELESS_TRANSMITTER_BLOCK_ENTITY);
            blockEntity.ifPresentOrElse(x -> x.setFrequency(freq), () -> WirelessRedstone.LOGGER.warn("Failed to apply BlockFrequencyChangeC2SPacket(true, {" + blockPos.toShortString() + "}, " + freq + ")"));
        }
        level.scheduleTick(blockPos, isReceiver ? WirelessRedstone.WIRELESS_RECEIVER : WirelessRedstone.WIRELESS_TRANSMITTER, 0);
    }
}
