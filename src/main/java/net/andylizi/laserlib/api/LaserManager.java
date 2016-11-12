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
package net.andylizi.laserlib.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * 守卫者激光相关操作的主API. 
 * @author andylizi
 * @version 1.0
 */
public interface LaserManager {
    /**
     * 创建一道对准真实实体目标的激光的表示对象. 
     * <p>
     * <b>注意:</b> 此方法并不会真正把激光显示给客户端. 
     * 
     * @param source 激光的发射源位置. 
     *              此方法会对其进行调整, 确保与客户端显示的位置一致
     * @param target 激光对准的目标. 此位置在客户端渲染时会有一定偏差. 
     * @param elder 激光来源是否为远古守卫者. 远古守卫者的激光变色时间较短. 
     * @return 生成的激光对象. 
     * @throws RuntimeException 如果生成失败. 
     * @see Laser#play(org.bukkit.entity.Player)
     */
    public Laser createLaser(Location source, Entity target, boolean elder) throws RuntimeException;
    
    /**
     * 创建一道对准真实实体目标的激光的表示对象, 
     * 并显示给所在世界中的所有客户端.
     * 
     * @throws RuntimeException 如果生成失败. 
     * @see #createLaser(Location, Entity, boolean)
     */
    default public Laser createLaserAndBroadcast(Location source, Entity target, boolean elder) throws RuntimeException{
        Laser laser = createLaser(source, target, elder);
        laser.broadcast();
        return laser;
    }
    
    /**
     * 创建一道对准虚拟实体目标的激光的表示对象. 
     * <p>
     * <b>注意:</b> 此方法并不会真正把激光显示给客户端. 
     * 
     * @param start 激光的发射源位置. 
     *              此方法会对其进行调整, 确保与客户端显示的位置一致
     * @param end 激光对准的目标. 
     *              此方法会对其进行调整, 确保与客户端显示的位置一致
     * @param elder 激光来源是否为远古守卫者. 远古守卫者的激光变色时间较短. 
     * @return 生成的激光对象. 
     * @throws RuntimeException 如果生成失败. 
     * @see Laser#play(org.bukkit.entity.Player)
     */
    public Laser createLaser(Location start, Location end, boolean elder) throws RuntimeException;
    
    /**
     * 创建一道对准虚拟实体目标的激光的表示对象, 
     * 并显示给所在世界中的所有客户端.
     * 
     * @throws RuntimeException 如果生成失败. 
     * @see #createLaser(Location, Location, boolean) 
     */
    default public Laser createLaserAndBroadcast(Location start, Location end, boolean elder) throws RuntimeException{
        Laser laser = createLaser(start, end, elder);
        laser.broadcast();
        return laser;
    }
}
