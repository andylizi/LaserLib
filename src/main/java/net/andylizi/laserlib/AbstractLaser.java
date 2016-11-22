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
import java.util.Collection;
import java.util.Objects;
import net.andylizi.laserlib.api.DummyEntity;
import net.andylizi.laserlib.api.Laser;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * {@linkplain Laser 激光表示对象}的骨架实现. 
 * @author andylizi
 */
abstract class AbstractLaser implements Laser{
    private final Location startPos;
    private final DummyEntity guardian;

    /**
     * @param start 未经过调整的开始位置. 
     * @param guardian 虚拟守卫者实体. 
     */
    protected AbstractLaser(Location start, DummyEntity guardian) {
        this.startPos = Objects.requireNonNull(start).clone();
        this.guardian = Objects.requireNonNull(guardian);
    }

    @Override
    public void registerToTracker(int range) {
        try {
            guardian.registerToTracker(range, 3, false);
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void unregisterFromTracker() throws RuntimeException {
        try {
            guardian.unregisterFromTracker();
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void broadcast() throws ReflectiveOperationException {
        play(startPos.getWorld());
    }

    @Override
    public void play(World world) throws ReflectiveOperationException {
        for(Player player : world.getPlayers())
            play(player);
    }

    @Override
    public void play(Player... players) throws ReflectiveOperationException {
        for(Player player : players)
            play(player);
    }

    @Override
    public void play(Player player) throws ReflectiveOperationException {
        for(PacketContainer packet : guardian.getPackets())
            NMSUtil.pm.sendServerPacket(player, packet);
    }
    
    @Override
    public void destroy() {
        guardian.destroy();
    }

    @Override public Location getSourcePos() { return startPos; }
    @Override public DummyEntity getGuardian() { return guardian; }
    @Override public Collection<PacketContainer> _UNSAFE_getPackets() throws ReflectiveOperationException 
        { return guardian.getPackets(); }

    @Override
    public String toString() {
        return String.format("Laser[%s]@%s", getGuardian().getEntityId(), Integer.toHexString(hashCode()));
    }
}
