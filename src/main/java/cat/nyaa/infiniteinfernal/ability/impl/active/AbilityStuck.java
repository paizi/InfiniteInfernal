package cat.nyaa.infiniteinfernal.ability.impl.active;

import cat.nyaa.infiniteinfernal.I18n;
import cat.nyaa.infiniteinfernal.InfPlugin;
import cat.nyaa.infiniteinfernal.ability.ActiveAbility;
import cat.nyaa.infiniteinfernal.mob.IMob;
import cat.nyaa.infiniteinfernal.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AbilityStuck extends ActiveAbility {
    @Serializable
    public int duration = 60;

    private static Listener listener;
    private static List<UUID> stucked = new ArrayList<>();
    private boolean inited = false;

    @Override
    public void active(IMob iMob) {
        if(!inited)init();

        List<LivingEntity> candidates = Utils.getValidTargets(iMob, iMob.getEntity().getNearbyEntities(20, 20, 20))
                .collect(Collectors.toList());
        LivingEntity victim = Utils.randomPick(candidates);
        if (victim == null)return;
        victim.addPotionEffect(PotionEffectType.SLOW.createEffect(duration, 10), true);
        stucked.add(victim.getUniqueId());
        new BukkitRunnable(){
            @Override
            public void run() {
                stucked.remove(victim.getUniqueId());
            }
        }.runTaskLater(InfPlugin.plugin, duration);
    }

    private void init(){
        listener = new Listener() {
            @EventHandler
            void onEntityMove(PlayerMoveEvent e){
                if (stucked.contains(e.getPlayer().getUniqueId())){
                    Location to = e.getTo();
                    if (to != null){
                        e.setTo(e.getFrom().setDirection(to.getDirection()));
                    }
                }
            }

            @EventHandler
            void onEntityTeleport(EntityTeleportEvent e) {
                if (stucked.contains(e.getEntity().getUniqueId())) {
                    e.setCancelled(true);
                }
            }

            @EventHandler
            void onPlayerTeleport(PlayerTeleportEvent e) {
                if (stucked.contains(e.getPlayer().getUniqueId())) {
                    if (e.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
                        e.getPlayer().sendMessage(I18n.format("message.stuck"));
                        e.setCancelled(true);
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, InfPlugin.plugin);
        inited = true;
    }


    @Override
    public String getName() {
        return "Stuck";
    }
}