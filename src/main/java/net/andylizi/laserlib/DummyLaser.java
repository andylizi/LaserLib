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
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * 目标为虚拟实体的激光. 
 * @author andylizi
 */
class DummyLaser extends AbstractLaser{
    private final int targetId;
    private final Location targetPos;

    /**
     * @see AbstractLaser#AbstractLaser(Location, int, Collection) 
     * @param targetPos 未经过调整的目标位置
     * @param targetId 虚拟目标的实体ID
     */
    protected DummyLaser(Location start, int guardianId, Location targetPos, int targetId, Collection<PacketContainer> packets) {
        super(start, guardianId, packets);
        if(targetId <= 0)
            throw new IllegalArgumentException();
        this.targetId = targetId;
        this.targetPos = Objects.requireNonNull(targetPos).clone();
    }

    @Override
    public void sendDestroyPacket() {
        super.sendDestroyPacket();
        NMSUtil.removeEntity(targetId);
    }

    @Override public boolean isTargetReal() { return false; }
    @Override public Entity getTarget() { return null; }
    @Override public int getTargetId() { return targetId; }
    @Override public Location getTargetPos() { return targetPos; }

    @Override
    public String toString() {
        return String.format("DummyLaser[%s -> %s]@%s", 
                getGuardianId(), getTargetId(), Integer.toHexString(hashCode()));
    }
}
