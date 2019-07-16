package cat.nyaa.infiniteinfernal.abilitiy.impl.active;

import cat.nyaa.infiniteinfernal.InfPlugin;
import cat.nyaa.infiniteinfernal.abilitiy.AbilityAttack;
import cat.nyaa.infiniteinfernal.abilitiy.ActiveAbility;
import cat.nyaa.infiniteinfernal.mob.IMob;
import cat.nyaa.infiniteinfernal.utils.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AbilityClearEffect extends ActiveAbility implements AbilityAttack {
    private static List<UUID> affected = new ArrayList<>();
    private static final String CACHE_EFFECT = "EFFECT";

    @Serializable
    public double tickChance = 0.5;
    @Serializable
    public double attackChance = 0.5;
    @Serializable
    public int duration = 60;
    @Serializable
    public List<String> effects = new ArrayList<>();
    private boolean inited = false;

    private static Listener listener;
    private int durationWatcher = duration;

    @Override
    public void onAttack(IMob mob, LivingEntity target) {
        if (!Utils.possibility(attackChance))return;
        if (!inited){
            init();
        }
        affect(target);
    }

    private void affect(LivingEntity target) {
        UUID uniqueId = target.getUniqueId();
        affected.add(uniqueId);
        new BukkitRunnable(){
            @Override
            public void run() {
                affected.remove(uniqueId);
            }
        }.runTaskLater(InfPlugin.plugin, duration);
        createCacheAndClearEffect(target);
    }

    private void createCacheAndClearEffect(LivingEntity target) {
        if (duration != durationWatcher){
            cache = cacheBuilder.expireAfterAccess((long) (((double) duration) / 20d), TimeUnit.SECONDS ).build();
        }
        List<PotionEffectType> peT = new ArrayList<>(effects.size());
        if (!effects.isEmpty()) {
            effects.forEach(s->{
                PotionEffectType potionEffect = Utils.parseEffect(s, getName());
                peT.add(potionEffect);
                clearEffect(target, potionEffect);
            });
        }
        cache.put(CACHE_EFFECT, peT);
    }
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .initialCapacity(100)
            .expireAfterAccess((long) (((double) duration) / 20d), TimeUnit.SECONDS);

    Cache<String, List<PotionEffectType>> cache = cacheBuilder.build();

    public void clearEffect(LivingEntity entity, PotionEffectType type){
        entity.removePotionEffect(type);
    }

    public void init(){
           listener = new Listener() {
               @EventHandler
               public void onPotion(EntityPotionEffectEvent ev){
                   List<PotionEffectType> potions = cache.getIfPresent(CACHE_EFFECT);
                   if (potions != null && !potions.isEmpty()){
                       if (potions.contains(ev.getModifiedType()))
                           ev.setCancelled(true);
                   }else {
                       Entity entity = ev.getEntity();
                       if (entity instanceof LivingEntity) {
                           createCacheAndClearEffect((LivingEntity) entity);
                       }
                   }
               }
           };
        Bukkit.getPluginManager().registerEvents(listener, InfPlugin.plugin);
    }

    public void disable(){
        inited = false;
    }

    @Override
    public void active(IMob iMob) {
        if (!Utils.possibility(tickChance))return;
        if (!inited){
            init();
        }
        LivingEntity livingEntity = Utils.randomPick(Utils.getValidTargets(iMob, iMob.getEntity().getNearbyEntities(30, 30, 30)).collect(Collectors.toList()));
        affect(livingEntity);
    }

    @Override
    public String getName() {
        return "ClearEffect";
    }
}