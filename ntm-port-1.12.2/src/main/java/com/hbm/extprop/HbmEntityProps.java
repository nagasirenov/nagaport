package com.hbm.extprop;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Native owner/persistence lifecycle for the two existing NTM property payloads. */
public final class HbmEntityProps {
    public static final ResourceLocation LIVING_ID = new ResourceLocation("hbm", "living_props");
    public static final ResourceLocation PLAYER_ID = new ResourceLocation("hbm", "player_props");

    @CapabilityInject(HbmLivingProps.class)
    public static Capability<HbmLivingProps> LIVING;
    @CapabilityInject(HbmPlayerProps.class)
    public static Capability<HbmPlayerProps> PLAYER;

    private HbmEntityProps() { }

    public static void register() {
        CapabilityManager.INSTANCE.register(HbmLivingProps.class, new Capability.IStorage<HbmLivingProps>() {
            @Override
            public NBTBase writeNBT(Capability<HbmLivingProps> capability, HbmLivingProps value, EnumFacing side) {
                NBTTagCompound tag = new NBTTagCompound();
                value.saveNBTData(tag);
                return tag;
            }

            @Override
            public void readNBT(Capability<HbmLivingProps> capability, HbmLivingProps value, EnumFacing side, NBTBase tag) {
                value.loadNBTData((NBTTagCompound) tag);
            }
        }, () -> new HbmLivingProps(null));
        CapabilityManager.INSTANCE.register(HbmPlayerProps.class, new Capability.IStorage<HbmPlayerProps>() {
            @Override
            public NBTBase writeNBT(Capability<HbmPlayerProps> capability, HbmPlayerProps value, EnumFacing side) {
                NBTTagCompound tag = new NBTTagCompound();
                value.saveNBTData(tag);
                return tag;
            }

            @Override
            public void readNBT(Capability<HbmPlayerProps> capability, HbmPlayerProps value, EnumFacing side, NBTBase tag) {
                value.loadNBTData((NBTTagCompound) tag);
            }
        }, () -> new HbmPlayerProps(null));
        MinecraftForge.EVENT_BUS.register(new HbmEntityProps());
    }

    static HbmLivingProps getLiving(EntityLivingBase entity) {
        return Objects.requireNonNull(entity.getCapability(Objects.requireNonNull(LIVING,
                "NTM living capability has not been registered"), null),
                "NTM living capability is not attached; do not query it during EntityConstructing");
    }

    static HbmPlayerProps getPlayer(EntityPlayer player) {
        return Objects.requireNonNull(player.getCapability(Objects.requireNonNull(PLAYER,
                "NTM player capability has not been registered"), null),
                "NTM player capability is not attached; do not query it during EntityConstructing");
    }

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if(entity instanceof EntityLivingBase) {
            event.addCapability(LIVING_ID, new Provider<>(LIVING, new HbmLivingProps((EntityLivingBase) entity)));
        }
        if(entity instanceof EntityPlayer) {
            event.addCapability(PLAYER_ID, new Provider<>(PLAYER, new HbmPlayerProps((EntityPlayer) entity)));
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        // Match the original handler for death AND End return: copy only the wire
        // subset, not all persisted player fields and not the living properties.
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        try {
            HbmPlayerProps.getData(event.getOriginal()).serialize(buffer);
            HbmPlayerProps.getData(event.getEntityPlayer()).deserialize(buffer);
        } finally {
            buffer.release();
        }
    }

    /** Each provider and payload belongs to exactly one entity. Forge owns its NBT. */
    private static final class Provider<T> implements ICapabilitySerializable<NBTTagCompound> {
        private final Capability<T> capability;
        private final T value;

        private Provider(Capability<T> capability, T value) {
            this.capability = Objects.requireNonNull(capability, "NTM capability was not injected before attachment");
            this.value = value;
        }

        @Override
        public boolean hasCapability(Capability<?> requested, EnumFacing side) {
            return requested == capability;
        }

        @Override
        public <V> V getCapability(Capability<V> requested, EnumFacing side) {
            return requested == capability ? capability.cast(value) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return (NBTTagCompound) capability.writeNBT(value, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag) {
            capability.readNBT(value, null, tag);
        }
    }
}
