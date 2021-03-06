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

import java.util.Objects;
import net.andylizi.laserlib.api.DummyEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * 目标为真实实体的激光. 
 * @author andylizi
 */
class RealLaser extends AbstractLaser{
    private final Entity target;

    /**
     * @see AbstractLaser#AbstractLaser(Location, DummyEntity) 
     * @param target 目标. 
     */
    protected RealLaser(Location start, DummyEntity guardian, Entity target) {
        super(start, guardian);
        this.target = Objects.requireNonNull(target);
        if(!target.isValid())
            destroy();
    }

    @Override public boolean isTargetReal() { return true; }
    @Override public Entity getTarget() { return target; }
    @Override public DummyEntity getDummyTarget() { return null; }
    @Override public Location getTargetPos() { return target.getLocation(); }

    @Override
    public String toString() {
        return String.format("RealLaser[%s -> %s]@%s", 
                getGuardian().getEntityId(), getDummyTarget(), Integer.toHexString(hashCode()));
    }
}
