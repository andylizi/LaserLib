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

import java.util.*;
import net.andylizi.laserlib.NMSUtil.DummyGuardianRecord;
import net.andylizi.laserlib.api.DummyEntity;
import net.andylizi.laserlib.api.Laser;
import net.andylizi.laserlib.api.LaserManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * {@linkplain LaserManager 激光管理器}的内部实现. 
 * @author andylizi
 */
class LaserManagerImpl implements LaserManager{
    /**
     * 守卫者坐标与其眼睛的坐标的偏移量. 
     */
    private static final float GUARDIAN_EYE_HEIGHT = 0.85f * 0.5f;
    
    /**
     * 盔甲架坐标与其眼睛的坐标的偏移量. 
     */
    private static final float ARMORSTAND_EYE_HEIGHT = 1.975f * 0.5f;

    protected LaserManagerImpl() { }

    @Override
    public Laser createLaser(Location source, Entity target, boolean elder) throws RuntimeException {
        Objects.requireNonNull(source, "source cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
        Validate.isTrue(source.getWorld().getUID().equals(target.getWorld().getUID()), 
                "source and target must in the same world");
        Location guardianPos = source.clone().add(0, -GUARDIAN_EYE_HEIGHT, 0);
        try{
            DummyEntity guardian = NMSUtil.createDummyGuardian(guardianPos, elder, target.getEntityId());
            NMSUtil.dummyGuardians.add(new DummyGuardianRecord(target.getEntityId(), guardian, null));
            return new RealLaser(source, guardian, target);
        }catch(ReflectiveOperationException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Laser createLaser(Location start, Location end, boolean elder) throws RuntimeException {
        Objects.requireNonNull(start, "startloc cannot be null");
        Objects.requireNonNull(end, "endloc cannot be null");
        Validate.isTrue(start.getWorld().getUID().equals(end.getWorld().getUID()), 
                "startLoc and endLoc must in the same world");
        Location guardianPos = start.clone().add(0, -GUARDIAN_EYE_HEIGHT, 0);
        Location asPos = end.clone().add(0, -ARMORSTAND_EYE_HEIGHT, 0);
        try{
            DummyEntity dummyTarget = NMSUtil.createDummyArmorStand(asPos);
            DummyEntity dummyGuardian = NMSUtil.createDummyGuardian(guardianPos, elder, dummyTarget.getEntityId());
            NMSUtil.dummyGuardians.add(new DummyGuardianRecord(dummyTarget.getEntityId(), dummyGuardian, dummyTarget));
            return new DummyLaser(start, dummyGuardian, end, dummyTarget);
        }catch(ReflectiveOperationException ex){
            throw new RuntimeException(ex);
        }
    }
}
