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
import com.comphenix.protocol.reflect.MethodUtils;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import net.andylizi.laserlib.api.DummyEntity;
import org.bukkit.Location;
import org.bukkit.World;

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
    
    /**
     * 服务器版本是否高于 1.11 (探险更新). 
     */
    static final boolean ABOVE_EXPLORATION_UPDATE = currentVersion.compareTo(new MinecraftVersion(1, 11, 0)) >= 0;

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
    
    /**
     * 被创建过的虚拟守卫者与其目标的记录集合. 
     */
    static final Set<DummyGuardianRecord> dummyGuardians = new HashSet<>();
    
    private static Constructor<?> CONST_ENTITY_GURADIAN;
    private static Constructor<?> CONST_ENTITY_ELDER_GUARDIAN;
    private static Constructor<?> CONST_ENTITY_ARMORSTAND;
    private static Constructor<?> CONST_PACKET_SPAWN_ENTITY;
    private static Constructor<?> CONST_PACKET_SPAWN_ENTITY_LIVING;
    private static Constructor<?> CONST_PACKET_ENTITY_METADATA;
    private static Constructor<?> CONST_ENTITY_TRACKER_ENTRY;
    private static MethodAccessor METHOD_INTHASHMAP_CONTAINSITEM;
    private static MethodAccessor METHOD_INTHASHMAP_ADDKEY;
    private static FieldAccessor FIELD_TRACKEDENTITIES;
    private static Field FIELD_ENTITY_ID;
    private static Method METHOD_ON_ENTITY_REMOVED;

    static{
        if(ABOVE_BOUNTIFUL_UPDATE){
            try{
                FieldAccessor[] accessors = Accessors.getFieldAccessorArray
                    (MinecraftReflection.getEntityClass(), MinecraftReflection.getDataWatcherObjectClass(), true);
                if(accessors.length == 0) throw new NoSuchFieldException("flags");
                WOBJ_ENTITY_FLAGS = new WrappedDataWatcherObject(accessors[0].get(null));
            }catch(ReflectiveOperationException ex){
                throw new UnsupportedOperationException("init DataWatcherObject of Entity", ex);
            }
            try{
                FieldAccessor[] accessors = Accessors.getFieldAccessorArray
                    (MinecraftReflection.getMinecraftClass("EntityArmorStand"), MinecraftReflection.getDataWatcherObjectClass(), true);
                if(accessors.length == 0) throw new NoSuchFieldException("flags");
                WOBJ_ARMORSTAND_FLAGS = new WrappedDataWatcherObject(accessors[0].get(null));
            }catch(ReflectiveOperationException ex){
                throw new UnsupportedOperationException("init DataWatcherObject of EntityArmorStand", ex);
            }
            try{
                FieldAccessor[] accessors = Accessors.getFieldAccessorArray(MinecraftReflection
                        .getMinecraftClass("EntityGuardian"), 
                        MinecraftReflection.getDataWatcherObjectClass(), true);
                if(accessors.length < 1) throw new NoSuchFieldException("flags");
                if(accessors.length < 2) throw new NoSuchFieldException("target entity");
                WOBJ_GUARDIAN_FLAGS = new WrappedDataWatcherObject(accessors[0].get(null));
                WOBJ_GUARDIAN_TARGET = new WrappedDataWatcherObject(accessors[1].get(null));
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
     * 创建用于生成实体对象的数据包. 
     * @param entity NMS Entity.
     * @param objectType 对象类型. 
     * @param objectData 对象附加数据. 
     * @return 包含所用数据包的集合. 
     * @throws ReflectiveOperationException 如果创建失败. 
     */
    static Collection<PacketContainer> createEntitySpawnPacket(Object entity, int objectType, int objectData) throws ReflectiveOperationException{
        if(CONST_PACKET_SPAWN_ENTITY == null){
            CONST_PACKET_SPAWN_ENTITY = PacketType.Play.Server.SPAWN_ENTITY.getPacketClass()
                    .getConstructor(MinecraftReflection.getMinecraftClass("Entity"), int.class, int.class);
            CONST_PACKET_SPAWN_ENTITY.setAccessible(true);
        }
        
        return Arrays.asList(new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY, 
                CONST_PACKET_SPAWN_ENTITY.newInstance(entity, objectType, objectData)));
    }
    
    /**
     * 创建用于生成生物实体的数据包. 
     * @param entity NMS EntityLiving.
     * @return 包含所用数据包的集合. 
     * @throws ReflectiveOperationException 如果创建失败. 
     */
    static Collection<PacketContainer> createLivingEntitySpawnPacket(Object entity) throws ReflectiveOperationException{
        if(CONST_PACKET_SPAWN_ENTITY_LIVING == null){
            CONST_PACKET_SPAWN_ENTITY_LIVING = PacketType.Play.Server.SPAWN_ENTITY_LIVING.getPacketClass()
                    .getConstructor(MinecraftReflection.getMinecraftClass("EntityLiving"));
            CONST_PACKET_SPAWN_ENTITY_LIVING.setAccessible(true);
        }
        if(CONST_PACKET_ENTITY_METADATA == null){
            CONST_PACKET_ENTITY_METADATA = PacketType.Play.Server.ENTITY_METADATA.getPacketClass()
                    .getConstructor(int.class, MinecraftReflection.getDataWatcherClass(), boolean.class);
            CONST_PACKET_ENTITY_METADATA.setAccessible(true);
        }
        return Arrays.asList(
                new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING, 
                CONST_PACKET_SPAWN_ENTITY_LIVING.newInstance(entity)), 
                new PacketContainer(PacketType.Play.Server.ENTITY_METADATA, 
                CONST_PACKET_ENTITY_METADATA.newInstance(readEntityId(entity), readDataWatcher(entity), true)));
    }

    /**
     * 构造一个虚拟守卫者的实例. 
     * @param pos 生成位置. 
     * @param isElder 是否为远古守卫者. 
     * @param target 守卫者的目标的实体ID. 
     * @return 虚拟守卫者的表示对象. 
     * @throws ReflectiveOperationException 如果创建失败.
     */
    static DummyEntity createDummyGuardian(Location pos, boolean isElder, int target) 
            throws ReflectiveOperationException{
        boolean useIndependentElderGuardian = isElder && ABOVE_EXPLORATION_UPDATE;
        if(CONST_ENTITY_GURADIAN == null){
            CONST_ENTITY_GURADIAN = MinecraftReflection.getMinecraftClass("EntityGuardian")
                    .getConstructor(MinecraftReflection.getNmsWorldClass());
            CONST_ENTITY_GURADIAN.setAccessible(true);
        }
        if(useIndependentElderGuardian && CONST_ENTITY_ELDER_GUARDIAN == null){
            CONST_ENTITY_ELDER_GUARDIAN = MinecraftReflection.getMinecraftClass("EntityGuardianElder")
                    .getConstructor(MinecraftReflection.getNmsWorldClass());
            CONST_ENTITY_ELDER_GUARDIAN.setAccessible(true);
        }
        Object entity = (useIndependentElderGuardian ? CONST_ENTITY_ELDER_GUARDIAN : CONST_ENTITY_GURADIAN)
                .newInstance(bukkitUnwrapper.unwrapItem(pos.getWorld()));
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher(readDataWatcher(entity));
        updateDataWatcherValue(dataWatcher, WOBJ_ENTITY_FLAGS, (byte) 0x20);    // 0x20 Invisible
        updateDataWatcherValue(dataWatcher, WOBJ_GUARDIAN_TARGET, target);      // Target EID
        if(isElder)
            if(ABOVE_BOUNTIFUL_UPDATE){
                if(ABOVE_EXPLORATION_UPDATE){
                    updateDataWatcherValue(dataWatcher, 
                            WOBJ_GUARDIAN_FLAGS, true);                         // true RetractingSpikes
                }else{
                    updateDataWatcherValue(dataWatcher, 
                            WOBJ_GUARDIAN_FLAGS, (byte) (0x2 | 0x4));           // 0x2 RetractingSpikes | 0x4 Elder
                }
            }else{
                updateDataWatcherValue(dataWatcher, 
                        WOBJ_GUARDIAN_FLAGS, 0x2 | 0x4);                        // 0x2 RetractingSpikes | 0x4 Elder
            }
        writeEntityData(entity, pos, dataWatcher.getHandle());
        return new DummyEntityImpl(entity);
    }
    
    /**
     * 构造一个虚拟隐形盔甲架的实例. 
     * @param pos 生成位置. 
     * @return 虚拟盔甲架的表示对象. 
     * @throws ReflectiveOperationException 如果创建失败.
     */
    static DummyEntity createDummyArmorStand(Location pos) 
            throws ReflectiveOperationException{
        if(CONST_ENTITY_ARMORSTAND == null){
            CONST_ENTITY_ARMORSTAND = MinecraftReflection.getMinecraftClass("EntityArmorStand")
                    .getConstructor(MinecraftReflection.getNmsWorldClass());
            CONST_ENTITY_ARMORSTAND.setAccessible(true);
        }
        Object entity = CONST_ENTITY_ARMORSTAND.newInstance(bukkitUnwrapper.unwrapItem(pos.getWorld()));
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher(FieldUtils.readField(entity, "datawatcher", true));
        updateDataWatcherValue(dataWatcher, WOBJ_ENTITY_FLAGS, 
                (byte) 0x20);                   // 0 - 0x20 Invisible
        updateDataWatcherValue(dataWatcher, WOBJ_ARMORSTAND_FLAGS, 
                (byte) (0x1 | 0x8 | 0x10));     // 11 - (0x1 Small | 0x8 NoBasePlate | 0x10 Marker)
        writeEntityData(entity, pos, dataWatcher.getHandle());
        return new DummyEntityImpl(entity);
    }
    
    /**
     * 将实体注册进指定 EntityTracker. 
     * @param world 所属世界. 
     * @param tracker EntityTracker. 
     * @param entity 要注册的实体.
     * @param range 显示范围. 
     * @param updateFrequency 更新间隔. 
     * @param sendVelocityUpdates 是否发送速度向量更新. 
     * @return EntityTrackerEntry. 
     * @throws IllegalStateException 如果指定实体已被注册. 
     * @throws ReflectiveOperationException 如果注册失败. 
     */
    static Object addToTracker(Object world, Object tracker, Object entity, 
            int range, int updateFrequency, boolean sendVelocityUpdates) 
            throws IllegalStateException, ReflectiveOperationException{
        if(METHOD_INTHASHMAP_CONTAINSITEM == null){
            for(Method m : MinecraftReflection.getMinecraftClass("IntHashMap").getMethods())
                if(m.getReturnType() == boolean.class && 
                        m.getParameterCount() == 1 && 
                        m.getParameters()[0].getType() == int.class){
                    m.setAccessible(true);
                    METHOD_INTHASHMAP_CONTAINSITEM = Accessors.getMethodAccessor(m, true);
                }
            if(METHOD_INTHASHMAP_CONTAINSITEM == null)
                throw new NoSuchMethodException("containsItem in IntHashMap");
        }
        Object trackedEntityHashTable;
        int entityId;
        if((boolean) METHOD_INTHASHMAP_CONTAINSITEM
                .invoke(trackedEntityHashTable = FieldUtils.readField(tracker, "trackedEntities", true), 
                        entityId = readEntityId(entity))){
            throw new IllegalStateException("Entity is already tracked!");
        }
        if(CONST_ENTITY_TRACKER_ENTRY == null){
            CONST_ENTITY_TRACKER_ENTRY = MinecraftReflection
                    .getMinecraftClass("EntityTrackerEntry")
                    .getConstructor(ABOVE_BOUNTIFUL_UPDATE ? 
                            new Class[]{ MinecraftReflection.getEntityClass(), int.class, int.class, int.class, boolean.class } : 
                            new Class[]{ MinecraftReflection.getEntityClass(), int.class, int.class, boolean.class });
            CONST_ENTITY_TRACKER_ENTRY.setAccessible(true);
        }
        if(METHOD_INTHASHMAP_ADDKEY == null){
            for(Method m : MinecraftReflection.getMinecraftClass("IntHashMap").getMethods())
                if(m.getReturnType() == void.class && 
                        m.getParameterCount() == 2 && 
                        m.getParameters()[0].getType() == int.class && 
                        m.getParameters()[1].getType() == Object.class){
                    m.setAccessible(true);
                    METHOD_INTHASHMAP_ADDKEY = Accessors.getMethodAccessor(m, true);
                }
            if(METHOD_INTHASHMAP_ADDKEY == null)
                throw new NoSuchMethodException("addKey in IntHashMap");
        }
        if(FIELD_TRACKEDENTITIES == null){
            FIELD_TRACKEDENTITIES = Accessors.getFieldAccessor(tracker.getClass(), Set.class, true);
        }
        Object entry;
        if(ABOVE_BOUNTIFUL_UPDATE){
            entry = CONST_ENTITY_TRACKER_ENTRY.newInstance(entity, range, range, updateFrequency, sendVelocityUpdates);
        }else{
            entry = CONST_ENTITY_TRACKER_ENTRY.newInstance(entity, range, updateFrequency, sendVelocityUpdates);
        }
        ((Collection) FIELD_TRACKEDENTITIES.get(tracker)).add(entry);
        METHOD_INTHASHMAP_ADDKEY.invoke(trackedEntityHashTable, entityId, entry);
        MethodUtils.invokeMethod(entry, "scanPlayers", FieldUtils.readField(world, "players"));
        return entry;
    }

    /**
     * 将实体注册进指定世界的 EntityTracker. 
     * @param world 所属世界. 
     * @param entity 要注册的实体.
     * @param range 显示范围. 
     * @param updateFrequency 更新间隔. 
     * @param sendVelocityUpdates 是否发送速度向量更新. 
     * @return EntityTrackerEntry. 
     * @throws IllegalStateException 如果指定实体已被注册. 
     * @throws ReflectiveOperationException 如果注册失败. 
     */
    static Object addToTracker(Object world, Object entity, 
            int range, int updateFrequency, boolean sendVelocityUpdates) 
            throws IllegalStateException, ReflectiveOperationException{
        return addToTracker(world, FieldUtils.readField(world, "tracker"), entity, range, updateFrequency, sendVelocityUpdates);
    }

    /**
     * 从指定 EntityTracker 中解除注册指定实体. 
     * @param tracker EntityTracker.
     * @param entity 要解除注册的实体. 
     * @throws ReflectiveOperationException 如果解除注册失败. 
     */
    static void removeFromTracker(Object tracker, Object entity) throws ReflectiveOperationException{
        MethodUtils.invokeMethod(tracker, "untrackEntity", entity);
    }
    
    /**
     * 从指定实体所在世界的 EntityTracker 中解除注册. 
     * @param entity 解除注册的实体. 
     * @throws ReflectiveOperationException 如果解除注册失败. 
     */
    static void removeFromTracker(Object entity) throws ReflectiveOperationException{
        MethodUtils.invokeMethod(FieldUtils.readField(FieldUtils.readField(entity, "world"), "tracker"), "untrackEntity", entity);
    }

    /**
     * 向NMS实体中写入数据. 
     * @param entity 要写入的 NMS Entity. 
     * @param pos 实体坐标. 
     * @param dataWatcher 实体的 DataWatcher 对象. 
     * @throws IllegalAccessException 如果写入失败. 
     */
    static void writeEntityData(Object entity, Location pos, Object dataWatcher) throws IllegalAccessException{
        FieldUtils.writeField(entity, "locX", pos.getX(), false);
        FieldUtils.writeField(entity, "locY", pos.getY(), false);
        FieldUtils.writeField(entity, "locZ", pos.getZ(), false);
        FieldUtils.writeField(entity, "yaw", pos.getYaw(), false);
        FieldUtils.writeField(entity, "pitch", pos.getPitch(), false);
        FieldUtils.writeField(entity, "datawatcher", dataWatcher, true);
        FieldUtils.writeField(entity, "attachedToPlayer", true);
    }
    
    /**
     * 获取NMS实体的实体ID. 
     * @param entity NMS实体
     * @return 实体ID.
     * @throws ReflectiveOperationException 如果反射操作失败. 
     */
    static int readEntityId(Object entity) throws ReflectiveOperationException{
        if(FIELD_ENTITY_ID != null)
            return FIELD_ENTITY_ID.getInt(entity);
        return (int) FieldUtils.readField(entity, "id", true);
    }
    
    /**
     * 获取NMS实体的DataWatcher. 
     * @param entity NMS实体. 
     * @return DataWatcher.
     * @throws ReflectiveOperationException 如果反射操作失败. 
     */
    static Object readDataWatcher(Object entity) throws ReflectiveOperationException{
        return FieldUtils.readField(entity, "datawatcher", true);
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
     * 向指定世界中注册移除实体事件的监听器.
     * @param bukkitWorld 指定世界. 
     * @throws ReflectiveOperationException 如果操作失败. 
     */
    static void injectWorldEventListener(World bukkitWorld) throws ReflectiveOperationException{
        if(FIELD_ENTITY_ID == null){
            FIELD_ENTITY_ID = MinecraftReflection.getEntityClass().getDeclaredField("id");
            FIELD_ENTITY_ID.setAccessible(true);
        }
        
        Object world = bukkitUnwrapper.unwrapItem(bukkitWorld);
        Class<?> listenerClass = MinecraftReflection.getMinecraftClass("IWorldAccess");
        Method addListener = null;
        for(Method method : MinecraftReflection.getNmsWorldClass().getMethods())
            if(method.getParameterTypes().length == 1 && 
                    method.getParameters()[0].getType() == listenerClass){
                method.setAccessible(true);
                addListener = method;
                break;
            }
        if(addListener == null)
            throw new NoSuchFieldException("Cannot found method `addIWorldAccess`");
        if(METHOD_ON_ENTITY_REMOVED == null){
            boolean first = false;
            for(Method method : listenerClass.getMethods())
                if(method.getParameters().length == 1 && 
                        method.getParameters()[0].getType() == MinecraftReflection.getEntityClass()){
                    if(!first){
                        first = true;
                    }else{
                        method.setAccessible(true);
                        METHOD_ON_ENTITY_REMOVED = method;
                    }
                }
            if(METHOD_ON_ENTITY_REMOVED == null)
                throw new NoSuchMethodException("Cannot find method `onEntityRemoved`");
        }

        addListener.invoke(world, Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), 
                new Class[] { listenerClass }, 
                (obj, method, args) -> {
                    if(METHOD_ON_ENTITY_REMOVED.equals(method)){
                        onEntityRemoved(readEntityId(args[0]));
                    }
                    return null;
                }));
    }
    
    /**
     * 当一个实体从世界中被移除时触发. 
     * @param entityId 被移除的实体ID. 
     */
    static void onEntityRemoved(int entityId){
        dummyGuardians.removeIf(record -> {
            if(record.targetId == entityId){
                record.destroy();
                return true;
            }
            return false;
        });
    }
    
    /**
     * 移除所有虚拟守卫者与其虚拟的目标(如果有).
     */
    static void removeAllLaser(){
        dummyGuardians.removeIf(record -> {
            record.destroy();
            return true;
        });
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
         * @param index 索引编号
         */
        public WrappedIndexDataWatcherObject(int index) {
            this.index = index;
        }

        @Override public int getIndex() { return index; }
        @Override public Serializer getSerializer() { return null; }

        /**
         * 违背自反性. 
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof WrappedDataWatcherObject)) return false;
            if (this.index != ((WrappedDataWatcherObject) obj).getIndex()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.index;
            return hash;
        }
    }
    
    /**
     * 用于保存创建的虚拟守卫者与其虚拟目标(如果有)的值类. 
     */
    static class DummyGuardianRecord{
        final int targetId;
        final DummyEntity guardian;
        final DummyEntity target;

        public DummyGuardianRecord(int targetId, DummyEntity guardian) {
            this.targetId = targetId;
            this.guardian = guardian;
            this.target = null;
        }

        public DummyGuardianRecord(int targetId, DummyEntity guardian, DummyEntity target) {
            this.targetId = targetId;
            this.guardian = guardian;
            this.target = target;
        }
        
        /**
         * 移除此虚拟守卫者与其目标(如果有). 
         */
        public void destroy(){
            guardian.destroy();
            if(target != null)
                try{
                    target.destroy();
                }catch(Exception ex) {}
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final DummyGuardianRecord other = (DummyGuardianRecord) obj;
            if (this.targetId != other.targetId) return false;
            if (!Objects.equals(this.guardian, other.guardian)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + this.targetId;
            hash = 61 * hash + Objects.hashCode(this.guardian);
            return hash;
        }
    }
    
    /**
     * 封闭构造器. 
     * @throws AssertionError 一定会抛出. 
     */
    private NMSUtil() throws AssertionError{ throw new AssertionError(); }
}
