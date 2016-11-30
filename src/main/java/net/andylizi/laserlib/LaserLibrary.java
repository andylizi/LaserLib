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

import java.io.*;
import java.util.logging.Level;
import net.andylizi.laserlib.api.LaserManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

/**
 * LaserLib主类与入口点. 
 * @author andylizi
 */
public final class LaserLibrary extends JavaPlugin implements Listener {
    private static final LaserManager manager = new LaserManagerImpl();;
    
    /**
     * 获得守卫者激光API. 
     * @return API. 
     */
    public static LaserManager getLaserManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskLater(this, () -> {
            Bukkit.getWorlds().forEach(world -> {
                try {
                    NMSUtil.injectWorldEventListener(world);
                } catch(ReflectiveOperationException ex) {
                    getLogger().log(Level.WARNING, "Unable to register eventlisteners of world \"" + world.getName() + "\"", ex);
                }
            });
        }, 1L);
        startMetrics();
        
//        new Test();
    }

    @Override
    public void onDisable() {
        NMSUtil.removeAllLaser();
    }
    
    /**
     * 1.9 - Fix.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event){
        NMSUtil.onEntityRemoved(event.getPlayer().getEntityId());
    }

    /**
     * 启动 {@linkplain MetricsLite Metrics}.
     */
    private void startMetrics(){
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch(IOException ex) {
            getLogger().log(Level.WARNING, "Unable to load metrics config", ex);
        }
    }

//    private class Test implements org.bukkit.event.Listener {
//        {
//            getServer().getPluginManager().registerEvents(this, LaserLibrary.this);
//        }
//        
//        @org.bukkit.event.EventHandler
//        public void onChat(org.bukkit.event.player.PlayerChatEvent event) throws ReflectiveOperationException {
//            org.bukkit.Location ploc = event.getPlayer().getLocation();
//            manager.createLaser(ploc.clone().add(5, 0, 0), event.getPlayer(), true).registerToTracker(60);
//            for(org.bukkit.Location loc : buildCircle(ploc.clone().add(0, -10, 0), 8, 0.1)){
//                manager.createLaser(ploc, loc, false).broadcast();
//            }
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
