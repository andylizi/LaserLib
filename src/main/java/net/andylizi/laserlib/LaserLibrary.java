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

import net.andylizi.laserlib.api.LaserManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LaserLib主类与入口点. 
 * @author andylizi
 */
public final class LaserLibrary extends JavaPlugin{
    private static LaserManager manager;

    @Override
    public void onEnable() {
        manager = new LaserManagerImpl();
//        new Test();
    }

    /**
     * 获得守卫者激光API. 
     * @return 如果插件未加载完成则返回 null.
     */
    public static LaserManager getLaserManager() {
        return manager;
    }

//    private class Test implements org.bukkit.event.Listener {
//        {
//            getServer().getPluginManager().registerEvents(this, LaserLibrary.this);
//        }
//        
//        @org.bukkit.event.EventHandler
//        public void onChat(org.bukkit.event.player.PlayerChatEvent event) {
//            org.bukkit.Location ploc = event.getPlayer().getLocation();
////            manager.createLaser(ploc.clone().add(5, 0, 0), event.getPlayer(), false).broadcast();
//            buildCircle(ploc.clone().add(0, -10, 0), 8, 0.1)
//                    .forEach(loc -> {
//                        manager.createLaserAndBroadcast(ploc, loc, false);
//                    });
//        }
//
//        public java.util.Collection<org.bukkit.Location> buildCircle(org.bukkit.Location center, double r, double p) {
//            java.util.List<org.bukkit.Location> s = new java.util.LinkedList<>();
//            for(double i = 0; i <= Math.PI * 2; i += p)
//                s.add(center.clone().add(Math.cos(i) * r, 0, Math.sin(i) * r));
//            return s;
//        }
//    }
}
