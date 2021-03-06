package cat.nyaa.infiniteinfernal.mob;

import cat.nyaa.infiniteinfernal.ability.IAbilitySet;
import cat.nyaa.infiniteinfernal.configs.MobConfig;
import cat.nyaa.infiniteinfernal.controler.Aggro;
import cat.nyaa.infiniteinfernal.loot.ILootItem;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Map;

public interface IMob {
    Map<ILootItem, Integer> getLoots();
    Map<ILootItem, Integer> getSpecialLoots();
    List<IAbilitySet> getAbilities();
    LivingEntity getEntity();
    EntityType getEntityType();
    KeyedBossBar getBossBar();
    int getLevel();
    double getDamage();
    double getMaxHealth();
    double getSpecialChance();
    void makeInfernal(LivingEntity entity);
    boolean isAutoSpawn();
    boolean dropVanilla();
    String getName();
    String getTaggedName();

    void showParticleEffect();

    void autoRetarget();
    void retarget(LivingEntity entity);

    LivingEntity getTarget();
    Map<LivingEntity, Aggro> getNonPlayerTargets();
    boolean isTarget(LivingEntity target);

    MobConfig getConfig();
    void onDeath();

    int getExp();

    EntityDamageEvent getLastDamageCause();

    void setLastDamageCause(EntityDamageEvent event);
}
