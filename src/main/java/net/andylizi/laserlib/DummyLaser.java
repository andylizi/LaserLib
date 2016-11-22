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
import java.util.Objects;
import net.andylizi.laserlib.api.DummyEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * 目标为虚拟实体的激光. 
 * @author andylizi
 */
class DummyLaser extends AbstractLaser{
    private final DummyEntity target;
    private final Location targetPos;

    /**
     * @see AbstractLaser#AbstractLaser(Location, DummyEntity) 
     * @param targetPos 未经过调整的目标位置
     * @param target 虚拟目标的实体ID
     */
    protected DummyLaser(Location start, DummyEntity guardian, Location targetPos, DummyEntity target) {
        super(start, guardian);
        this.target = Objects.requireNonNull(target);
        this.targetPos = Objects.requireNonNull(targetPos).clone();
    }

    @Override
    public void registerToTracker(int range) {
        super.registerToTracker(range);
        try {
            target.registerToTracker(range, Integer.MAX_VALUE, false);
        } catch(IllegalStateException ex) {
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void unregisterFromTracker() throws RuntimeException {
        super.unregisterFromTracker();
        try {
            target.unregisterFromTracker();
        } catch(ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void play(Player player) throws ReflectiveOperationException {
        super.play(player);
        for(PacketContainer packet : target.getPackets())
            NMSUtil.pm.sendServerPacket(player, packet);
    }

    @Override
    public void destroy() {
        super.destroy();
        target.destroy();
    }

    @Override public boolean isTargetReal() { return false; }
    @Override public Entity getTarget() { return null; }
    @Override public DummyEntity getDummyTarget() { return target; }
    @Override public Location getTargetPos() { return targetPos; }

    @Override
    public String toString() {
        return String.format("DummyLaser[%s -> %s]@%s", 
                getGuardian().getEntityId(), getDummyTarget(), Integer.toHexString(hashCode()));
    }
}
