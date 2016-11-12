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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Location;

/**
 * 核心操作的实现. 
 * @author andylizi
 */
class NMSUtil {
    /**
     * ProtocolLib 提供的 {@link ProtocolManager}. 
     */
    static final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
    
    /**
     * ProtocolLib 提供的 {@link BukkitUnwrapper}.
     */
    static final BukkitUnwrapper bukkitUnwrapper = BukkitUnwrapper.getInstance();
    
    /**
     * 服务器当前版本. 
     */
    static final MinecraftVersion currentVersion = pm.getMinecraftVersion();
    
    /**
     * 服务器版本是否高于 1.8 (缤纷更新). 
     */
    static final boolean ABOVE_BOUNTIFUL_UPDATE = currentVersion.compareTo(MinecraftVersion.COMBAT_UPDATE) >= 0;
    
    static final Constructor<?> CONST_ENTITY_GURADIAN;
    
    static final Constructor<?> CONST_ENTITY_ARMORSTAND;
    
    /**
     * DataWatcher中Entity类的Flags属性.
     */
    static final WrappedDataWatcherObject WOBJ_ENTITY_FLAGS;
    
    /**
     * DataWatcher中EntityArmorStand类的Flags属性.
     */
    static final WrappedDataWatcherObject WOBJ_ARMORSTAND_FLAGS;
    
    /**
     * DataWatcher中EntityGuardian类的Flags属性. 
     */
    static final WrappedDataWatcherObject WOBJ_GUARDIAN_FLAGS;
    
    /**
     * DataWatcher中EntityGuardian类的TargetEntity属性. 
     */
    static final WrappedDataWatcherObject WOBJ_GUARDIAN_TARGET;
    
    static{
        try {
            CONST_ENTITY_GURADIAN = MinecraftReflection.getMinecraftClass("EntityGuardian")
                    .getConstructor(MinecraftReflection.getNmsWorldClass());
            CONST_ENTITY_GURADIAN.setAccessible(true);
        } catch(ReflectiveOperationException ex) {
            throw new UnsupportedOperationException("init constructor of EntityGuardian", ex);
        }
        try {
            CONST_ENTITY_ARMORSTAND = MinecraftReflection.getMinecraftClass("EntityArmorStand")
                    .getConstructor(MinecraftReflection.getNmsWorldClass());
            CONST_ENTITY_ARMORSTAND.setAccessible(true);
        } catch(ReflectiveOperationException ex) {
            throw new UnsupportedOperationException("init constructor of EntityArmorStand", ex);
        }
        if(ABOVE_BOUNTIFUL_UPDATE){
            try{
                Object obj = null;
                for(Field field : MinecraftReflection.getMinecraftClass("Entity").getDeclaredFields())
                    if(MinecraftReflection.getDataWatcherObjectClass() == field.getType()){
                        field.setAccessible(true);
                        obj = field.get(null);
                        break;
                    }
                if(obj == null) throw new NoSuchFieldException("flags");
                WOBJ_ENTITY_FLAGS = new WrappedDataWatcherObject(obj);
            }catch(ReflectiveOperationException ex){
                throw new UnsupportedOperationException("init DataWatcherObject of Entity", ex);
            }
            try{
                Object obj = null;
                for(Field field : MinecraftReflection.getMinecraftClass("EntityArmorStand").getDeclaredFields())
                    if(MinecraftReflection.getDataWatcherObjectClass() == field.getType()){
                        field.setAccessible(true);
                        obj = field.get(null);
                        break;
                    }
                if(obj == null) throw new NoSuchFieldException("flags");
                WOBJ_ARMORSTAND_FLAGS = new WrappedDataWatcherObject(obj);
            }catch(ReflectiveOperationException ex){
                throw new UnsupportedOperationException("init DataWatcherObject of EntityArmorStand", ex);
            }
            try{
                Object first = null;
                Object secound = null;
                for(Field field : MinecraftReflection.getMinecraftClass("EntityGuardian").getDeclaredFields())
                    if(MinecraftReflection.getDataWatcherObjectClass() == field.getType()){
                        field.setAccessible(true);
                        if(first == null)
                            first = field.get(null);
                        else{
                            secound = field.get(null);
                            break;
                        }
                    }
                if(first == null) throw new NoSuchFieldException("flags");
                if(secound == null) throw new NoSuchFieldError("target entity");
                WOBJ_GUARDIAN_FLAGS = new WrappedDataWatcherObject(first);
                WOBJ_GUARDIAN_TARGET = new WrappedDataWatcherObject(secound);
            }catch(ReflectiveOperationException ex){
                throw new UnsupportedOperationException("init DataWatcherObjects of EntityGuardian", ex);
            }
        }else{
            WOBJ_ENTITY_FLAGS = new WrappedIndexDataWatcherObject(0);      // 0 - Entity - Flags
            WOBJ_ARMORSTAND_FLAGS = new WrappedIndexDataWatcherObject(10); // 10 - ArmorStand - Flags
            WOBJ_GUARDIAN_FLAGS = new WrappedIndexDataWatcherObject(16);   // 16 - Guardian - Flags
            WOBJ_GUARDIAN_TARGET = new WrappedIndexDataWatcherObject(17);  // 17 - Guardian - Target EID
        }
    }

    /**
     * 生成用于创建虚拟守卫者的数据包. 
     * @param pos 生成位置. 
     * @param isElder 是否为远古守卫者. 
     * @param target 守卫者的目标的实体ID. 
     * @param output 数据包输出. 
     * @return 创建的守卫者的实体ID. 
     * @throws ReflectiveOperationException 如果创建失败.
     */
    static int createDummyGuardian(Location pos, boolean isElder, int target, Consumer<PacketContainer> output) 
            throws ReflectiveOperationException{
        Object entity = CONST_ENTITY_GURADIAN.newInstance(bukkitUnwrapper.unwrapItem(pos.getWorld()));
        int entityId = getEntityId(entity);
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher(FieldUtils.readField(entity, "datawatcher", true));
        updateDataWatcherValue(dataWatcher, WOBJ_ENTITY_FLAGS, (byte) 0x20);    // 0x20 Invisible
        updateDataWatcherValue(dataWatcher, WOBJ_GUARDIAN_TARGET, target);      // Target EID
        if(isElder)
            if(ABOVE_BOUNTIFUL_UPDATE){
                updateDataWatcherValue(dataWatcher, 
                        WOBJ_GUARDIAN_FLAGS, (byte) (0x2 | 0x4));               // 0x2 RetractingSpikes | 0x4 Elder
            }else{
                updateDataWatcherValue(dataWatcher, 
                        WOBJ_GUARDIAN_FLAGS, 0x4 | 0x2);                        // 0x4 Elder | 0x2 RetractingSpikes
            }

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet.getIntegers().write(0, entityId);                                // Entity Id
        packet.getIntegers().write(1, 68);                                      // EntityType 68 - Guardian
        if(ABOVE_BOUNTIFUL_UPDATE){                                             // *************** > 1.8
            packet.getUUIDs().write(0, UUID.randomUUID());                      // Entity UUID
            
            packet.getDoubles().write(0, pos.getX());                           // PosX
            packet.getDoubles().write(1, pos.getY());                           // PosY
            packet.getDoubles().write(2, pos.getZ());                           // PosZ
        }else{                                                                  // *************** = 1.8
            packet.getIntegers().write(2, (int) Math.floor(pos.getX() * 32d));  // PosX
            packet.getIntegers().write(3, (int) Math.floor(pos.getY() * 32d));  // PosY
            packet.getIntegers().write(4, (int) Math.floor(pos.getZ() * 32d));  // PosZ
        }
        packet.getDataWatcherModifier().write(0, dataWatcher);
        
        output.accept(packet);
        return entityId;
    }
    
    /**
     * 生成用于创建虚拟隐形盔甲架的数据包. 
     * @param pos 生成位置. 
     * @param output 数据包输出. 
     * @return 创建的盔甲架的实体ID. 
     * @throws ReflectiveOperationException 如果创建失败.
     */
    static int createDummyArmorStand(Location pos, Consumer<PacketContainer> output) 
            throws ReflectiveOperationException{
        Object entity = CONST_ENTITY_ARMORSTAND.newInstance(bukkitUnwrapper.unwrapItem(pos.getWorld()));
        int entityId = getEntityId(entity);
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher(FieldUtils.readField(entity, "datawatcher", true));
        updateDataWatcherValue(dataWatcher, WOBJ_ENTITY_FLAGS, 
                (byte) 0x20);                   // 0 - 0x20 Invisible
        updateDataWatcherValue(dataWatcher, WOBJ_ARMORSTAND_FLAGS, 
                (byte) (0x1 | 0x8 | 0x10));     // 11 - (0x1 Small | 0x8 NoBasePlate | 0x10 Marker)
        int pitch = (int) (pos.getPitch() * 256f / 360f);
        int yaw = (int) (pos.getYaw() * 256f / 360f);

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getIntegers().write(0, entityId);                                  // Entity Id
        if(ABOVE_BOUNTIFUL_UPDATE){                                               // *************** > 1.8
            packet.getUUIDs().write(0, UUID.randomUUID());                        // Entity UUID
            packet.getDoubles().write(0, pos.getX());                             // PosX
            packet.getDoubles().write(1, pos.getY());                             // PosY
            packet.getDoubles().write(2, pos.getZ());                             // PosZ

            packet.getIntegers().write(4, pitch);                                 // Pitch
            packet.getIntegers().write(5, yaw);                                   // Yaw
            
            packet.getIntegers().write(6, 78);                                    // ObjectType 78 - ArmorStand
        }else{                                                                    // *************** = 1.8
            packet.getIntegers().write(1, (int) Math.floor(pos.getX() * 32d));    // PosX
            packet.getIntegers().write(2, (int) Math.floor(pos.getY() * 32d));    // PosY
            packet.getIntegers().write(3, (int) Math.floor(pos.getZ() * 32d));    // PosZ

            packet.getIntegers().write(7, pitch);                                 // Pitch
            packet.getIntegers().write(8, yaw);                                   // Yaw

            packet.getIntegers().write(9, 78);                                    // ObjectType 78 - ArmorStand
        }

        PacketContainer packet2 = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entityId);
        packet2.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        output.accept(packet);
        output.accept(packet2);
        return entityId;
    }
    
    /**
     * 获取NMS实体的实体ID. 
     * @param entity NMS实体
     * @return 实体ID.
     * @throws ReflectiveOperationException 如果反射操作失败. 
     */
    static int getEntityId(Object entity) throws ReflectiveOperationException{
        return (int) FieldUtils.readField(entity, "id", true);
    }

    /**
     * 向所有在线客户端广播一个移除指定实体的数据包. 
     * @param id 实体ID. 
     */
    static void removeEntity(int id) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0, new int[] { id });
        pm.broadcastServerPacket(packet);
    }

    /**
     * 以版本兼容的方式更新 DataWatcher 中的值. 
     * @param <T> 值的类型. 
     * @param dataWatcher 要更新的 DataWatcher
     * @param object 键
     * @param value 值
     */
    static <T> void updateDataWatcherValue(WrappedDataWatcher dataWatcher, WrappedDataWatcherObject object, T value){
        if(object.getSerializer() != null)
            dataWatcher.setObject(object, value, false);
        else dataWatcher.setObject(object.getIndex(), value, false);
    }
    
    /**
     * 兼容低版本中使用的数字索引方式. 
     * @author andylizi
     */
    static class WrappedIndexDataWatcherObject extends WrappedDataWatcherObject{
        private final int index;

        /**
         * @param index 索引
         */
        public WrappedIndexDataWatcherObject(int index) {
            this.index = index;
        }

        @Override public int getIndex() { return index; }
        @Override public Serializer getSerializer() { return null; }
    }
    
    /**
     * 封闭构造器. 
     * @throws AssertionError 一定会抛出. 
     */
    private NMSUtil() throws AssertionError{ throw new AssertionError(); }
}
