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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * 激光的表示对象. 
 * @author andylizi
 * @version 2.0
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
    Location getSourcePos();
    
    /**
     * 获取作为发射源的虚拟守卫者. 
     * @return 虚拟实体. 
     */
    DummyEntity getGuardian();
    
    /**
     * 获取激光的目标是否为真实存在的. 
     * @return false 表示此激光的目标是用数据包伪造的, true 反之. 
     */
    boolean isTargetReal();
    
    /**
     * 如果激光的目标是真实存在的, 获取此激光的真实目标. 
     * @return 如果 {@link #isTargetReal()} 不为 true, 
     *          此方法永远返回 null. 
     */
    Entity getTarget();
    
    /**
     * 如果激光的目标<b>*不*</b>是真实存在的, 获取此激光的目标的虚拟实体. 
     * @return 如果 {@link #isTargetReal()} 不为 false, 
     *          此方法永远返回 null. 
     */
    DummyEntity getDummyTarget();
    
    /**
     * 获取此激光的目标的位置. 
     */
    Location getTargetPos();
    
    /**
     * 将此激光所需要的相关虚拟实体注册进所在世界的 EntityTracker. 
     * <p>
     * 这并不会改变该对象的内部状态. 此对象还可复用. 
     * 
     * @param range 显示距离. 
     * @throws RuntimeException 如果注册失败. 
     */
    void registerToTracker(int range) throws RuntimeException;
    
    /**
     * 从所在世界的 EntityTracker 解除注册此激光所需要的相关虚拟实体. 
     * <p>
     * 这并不会改变该对象的内部状态. 此对象还可复用. 
     * 
     * @throws RuntimeException 如果解除注册失败.
     */
    void unregisterFromTracker() throws RuntimeException;
    
    /**
     * 向此激光所在世界的所有客户端广播生成此激光的数据包. 
     * @throws InvocationTargetException 如果数据包发送失败.
     * @throws ReflectiveOperationException 如果数据包生成失败. 
     */
    void broadcast() throws InvocationTargetException, ReflectiveOperationException;
    
    /**
     * 向指定客户端发送生成此激光的数据包. 
     * @param player 接收的玩家
     * @throws InvocationTargetException 如果数据包发送失败.
     * @throws ReflectiveOperationException 如果数据包生成失败. 
     */
    void play(Player player) throws InvocationTargetException, ReflectiveOperationException;
    
    /**
     * 向指定客户端发送生成此激光的数据包. 
     * @param players 接收的玩家
     * @throws InvocationTargetException 如果数据包发送失败.
     * @throws ReflectiveOperationException 如果数据包生成失败. 
     */
    void play(Player... players) throws InvocationTargetException, ReflectiveOperationException;
    
    /**
     * 向指定世界的所有客户端发送生成此激光的数据包. 
     * @throws InvocationTargetException 如果数据包发送失败.
     * @throws ReflectiveOperationException 如果数据包生成失败. 
     */
    void play(World world) throws InvocationTargetException, ReflectiveOperationException;
    
    /**
     * 向服务器内的所有客户端广播移除此道激光的数据包. 
     * <p>
     * 这并不会改变该对象的内部状态. 此对象还可复用. 
     */
    void destroy();

    /**
     * 获得用于生成激光的数据包. 
     * @return 不可修改的集合. 
     * @deprecated 不安全 - 破坏封装性. 
     * @throws ReflectiveOperationException 如果数据包生成失败. 
     */
    @Deprecated
    Collection<PacketContainer> _UNSAFE_getPackets() throws ReflectiveOperationException;
}
