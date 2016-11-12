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

import com.comphenix.protocol.events.PacketContainer;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * 激光的表示对象. 
 * @author andylizi
 * @version 1.0
 */
public abstract interface Laser {
    /**
     * 获取激光发射源的位置. 
     * <p>
     * 此方法返回的位置与创建此激光时传入的一直, 
     * 没有经过{@linkplain LaserManager#createLaser(Location, Location, boolean) 
     *          此方法的}调整. 
     * @return 发射源. 由于MC渲染问题, 此位置有一定偏差. 
     */
    public Location getSourcePos();
    
    /**
     * 获取作为发射源的虚拟守卫者的实体ID. 
     * @return 实体ID. 
     */
    public int getGuardianId();
    
    /**
     * 获取激光的目标是否为真实存在的. 
     * @return false表示此激光的目标是用数据包伪造的, true反之. 
     */
    public boolean isTargetReal();
    
    /**
     * 如果激光的目标是真实存在的, 获取此激光的真实目标. 
     * @return 如果 {@link #isTargetReal()} 不为true, 此方法返回null. 
     *          由于MC渲染问题, 此位置有一定偏差. 
     */
    public Entity getTarget();
    
    /**
     * 获取此激光的目标的实体ID. 
     * @return 实体ID. 若 {@link #isTargetReal()} 返回false, 
     *          此实体ID所对应的实体是伪造的. 
     */
    public int getTargetId();
    
    /**
     * 获取此激光的目标的位置. 
     */
    public Location getTargetPos();
    
    /**
     * 向服务器内的所有客户端广播移除此道激光的数据包. 
     * <p>
     * 这并不会改变此对象的内部状态. 此对象还可复用. 
     */
    public void sendDestroyPacket();
    
    /**
     * 向此激光所在世界的所有客户端广播生成此激光的数据包. 
     * @throws RuntimeException 如果数据包发送失败. 
     */
    public void broadcast() throws RuntimeException;
    
    /**
     * 向指定客户端发送生成此激光的数据包. 
     * @param player 接收的玩家
     * @throws RuntimeException 如果数据包发送失败. 
     */
    public void play(Player player) throws RuntimeException;
    
    /**
     * 向指定客户端发送生成此激光的数据包. 
     * @param players 接收的玩家
     * @throws RuntimeException 如果数据包发送失败. 
     */
    public void play(Player... players) throws RuntimeException;
    
    /**
     * 向指定世界的所有客户端发送生成此激光的数据包. 
     * @throws RuntimeException 如果数据包发送失败. 
     */
    public void play(World world) throws RuntimeException;
    
    /**
     * 获得用于生成激光的数据包. 
     * @return 不可修改的集合. 
     * @deprecated 不安全 - 破坏封装性. 
     */
    @Deprecated
    public Collection<PacketContainer> _UNSAFE_getPackets();
}
