/*
 * Copyright (C) 2016 andylizi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.andylizi.laserlib;

import net.andylizi.laserlib.api.DummyEntity;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.utility.MinecraftReflection;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * {@linkplain DummyEntity 虚拟实体表示对象}的内部实现. 
 * @author andylizi
 */
class DummyEntityImpl implements DummyEntity {
    private final int entityId;
    private final Object entity;
    private final boolean living;
    private Collection<PacketContainer> packets;

    public DummyEntityImpl(Object entity) throws IllegalArgumentException, ReflectiveOperationException{
        this.entity = Objects.requireNonNull(entity);
        if(!MinecraftReflection.isMinecraftEntity(entity))
            throw new IllegalArgumentException();
        this.entityId = NMSUtil.readEntityId(entity);
        this.living = MinecraftReflection.getMinecraftClass("EntityLiving").isInstance(entity);
    }
    
    @Override
    public void play(Player player) throws InvocationTargetException{
        for(PacketContainer packet : packets) 
            NMSUtil.pm.sendServerPacket(player, packet);
    }
    
    @Override
    public void destroy(){
        NMSUtil.removeEntity(entityId);
        try {
            unregisterFromTracker();
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object registerToTracker(int range, int updateFrequency, boolean sendVelocityUpdates) 
            throws IllegalStateException, ReflectiveOperationException{
        return NMSUtil.addToTracker(FieldUtils.readField(entity, "world"), entity, range, updateFrequency, sendVelocityUpdates);
    }
    
    @Override
    public void unregisterFromTracker() throws ReflectiveOperationException{
        NMSUtil.removeFromTracker(entity);
    }

    @Override public int getEntityId() { return entityId; }
    
    @Override
    public Collection<PacketContainer> getPackets() throws ReflectiveOperationException {
        if(packets != null)
            return packets;
        return (this.packets = living ? NMSUtil.createLivingEntitySpawnPacket(entity) : 
                                        NMSUtil.createEntitySpawnPacket(entity, 78, 0));  // 78 - ArmorStand
    }

    @Override
    @Deprecated
    public Object _UNSAFE_getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final DummyEntityImpl other = (DummyEntityImpl) obj;
        if (this.entityId != other.entityId) return false;
        if (!Objects.equals(this.entity, other.entity)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.entityId;
        hash = 89 * hash + Objects.hashCode(this.entity);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Dummy%s", entity.toString());
    }
}
