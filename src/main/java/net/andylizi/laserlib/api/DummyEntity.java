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
import org.bukkit.entity.Player;

/**
 * 一个虚拟实体的表示对象. 
 * @author andylizi
 * @version 1.0
 */
public interface DummyEntity {
    /**
     * 向指定玩家发送生成此虚拟实体的数据包. 
     * @param player
     * @throws InvocationTargetException 如果发送失败. 
     */
    void play(Player player) throws InvocationTargetException;

    /**
     * 将此虚拟实体注册进所在世界的 EntityTracker. 
     * @param range 显示距离. 
     * @param updateFrequency 更新间隔. 
     * @param sendVelocityUpdates 是否发送速度向量更新. 
     * @return EntityTrackerEntry
     * @throws IllegalStateException 如果此虚拟实体已经被注册过了. 
     * @throws ReflectiveOperationException 如果注册失败. 
     */
    Object registerToTracker(int range, int updateFrequency, boolean sendVelocityUpdates) 
            throws IllegalStateException, ReflectiveOperationException;
    
    /**
     * 从所在世界的 EntityTracker 解除注册此虚拟实体. 
     * @throws ReflectiveOperationException 如果解除失败. 
     */
    void unregisterFromTracker() throws ReflectiveOperationException;
    
    /**
     * 向服务器内所有玩家广播移除此虚拟实体的数据包, 并从 EntityTracker 解除注册(如果有的话). 
     * <p>
     * 此方法不会改变对象的内部状态, 此对象还可复用. 
     */
    void destroy();

    /**
     * 获得此虚拟实体的实体ID. 
     * @return 实体ID. 此ID可能只被客户端承认. 
     */
    int getEntityId();

    /**
     * 获得或延迟创建用于创建此虚拟实体的数据包. 
     * @throws ReflectiveOperationException 如果执行延迟创建时失败. 
     */
    Collection<PacketContainer> getPackets() throws ReflectiveOperationException;
    
    /**
     * 获得此虚拟实体的 NMS 表示对象的引用. 
     * @return NMS Entity. 
     * @deprecated 不安全 - 破坏封装性. 
     */
    @Deprecated
    Object _UNSAFE_getEntity();
}
