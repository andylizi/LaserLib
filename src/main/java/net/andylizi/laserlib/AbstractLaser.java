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

import com.comphenix.protocol.events.PacketContainer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import net.andylizi.laserlib.api.Laser;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * 激光表示对象的骨架实现. 
 * @author andylizi
 */
abstract class AbstractLaser implements Laser{
    private final Collection<PacketContainer> packets;
    private final Location startPos;
    private final int guardianId;

    /**
     * @param start 未经过调整的开始位置. 
     * @param guardianId 守卫者的实体ID. 
     * @param packets 用于生成激光的数据包. 
     */
    protected AbstractLaser(Location start, int guardianId, Collection<PacketContainer> packets) {
        this.startPos = Objects.requireNonNull(start).clone();
        if(guardianId <= 0)
            throw new IllegalArgumentException();
        this.guardianId = guardianId;
        Validate.notEmpty(packets);
        this.packets = Collections.unmodifiableCollection(packets);
    }

    @Override
    public void broadcast() throws RuntimeException {
        play(startPos.getWorld());
    }

    @Override
    public void play(World world) throws RuntimeException {
        world.getPlayers().forEach(this::play);
    }

    @Override
    public void play(Player... players) throws RuntimeException {
        for(Player player : players)
            play(player);
    }

    @Override
    public void play(Player player) throws RuntimeException {
        packets.forEach(packet -> {
            try {
                NMSUtil.pm.sendServerPacket(player, packet, true);
            } catch(InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    
    @Override
    public void sendDestroyPacket() {
        NMSUtil.removeEntity(guardianId);
    }

    @Override public Location getSourcePos() { return startPos; }
    @Override public int getGuardianId() { return guardianId; }
    @Override public Collection<PacketContainer> _UNSAFE_getPackets() { return packets; }

    @Override
    public String toString() {
        return String.format("Laser[%s]@%s", getGuardianId(), Integer.toHexString(hashCode()));
    }
}
