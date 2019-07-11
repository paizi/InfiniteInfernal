package cat.nyaa.infiniteinfernal.bossbar;

import cat.nyaa.infiniteinfernal.mob.IMob;
import cat.nyaa.infiniteinfernal.mob.MobManager;
import cat.nyaa.nyaacore.Pair;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class BossbarManager {

    private static class BossbarRefreshTask extends BukkitRunnable {
        @Override
        public void run() {
            Collection<IMob> mobs = MobManager.instance().getMobs();
            Map<Player, List<Pair<BossBar, AngledEntity>>> playerCounter = new HashMap<>();
            if (!mobs.isEmpty()) {
                mobs.forEach(iMob -> {
                    LivingEntity entity = iMob.getEntity();
                    List<AngledEntity> nearbyPlayers = entity.getNearbyEntities(50, 25, 50).stream()
                            .filter(entity1 -> entity1 instanceof Player)
                            .map(player -> AngledEntity.of(entity, (Player) player))
                            .sorted(AngledEntity::compareTo)
                            .collect(Collectors.toList());
                    KeyedBossBar bossBar = iMob.getBossBar();
                    bossBar.removeAll();
                    if (!nearbyPlayers.isEmpty()) {
                        nearbyPlayers.forEach(angledEntity -> {
                            LivingEntity player = angledEntity.livingEntity;
                            if (player instanceof Player) {
                                List<Pair<BossBar, AngledEntity>> pairs = playerCounter.get(player);
                                Pair<BossBar, AngledEntity> bar = new Pair<>(bossBar, AngledEntity.of(entity, player));
                                if (pairs.size() < 5) {
                                    add(pairs, bar);
                                } else {
                                    for (int i = 0; i < pairs.size(); i++) {
                                        if (bar.getValue().compareTo(pairs.get(i).getValue()) < 0) {
                                            replace(pairs, i, bar);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    }
                    updateProgress(bossBar, entity);
                });
            }
        }

        private void updateProgress(KeyedBossBar bossBar, LivingEntity entity) {
            double health = entity.getHealth();
            double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double progress = health / maxHealth;
            bossBar.setProgress(progress);
            if (progress < 0.33) {
                bossBar.setColor(BarColor.RED);
            } else if (progress < 0.66) {
                bossBar.setColor(BarColor.YELLOW);
            } else {
                bossBar.setColor(BarColor.BLUE);
            }
        }

        void add(List<Pair<BossBar, AngledEntity>> pairs, Pair<BossBar, AngledEntity> bar) {
            bar.getKey().addPlayer(((Player) bar.getValue().livingEntity));
            pairs.add(bar);
        }

        void replace(List<Pair<BossBar, AngledEntity>> pairs, int origin, Pair<BossBar, AngledEntity> replacement) {
            replacement.getKey().addPlayer(((Player) replacement.getValue().livingEntity));
            pairs.add(origin, replacement);
            Pair<BossBar, AngledEntity> remove = pairs.remove(pairs.size() - 1);
            remove.getKey().removePlayer(((Player) remove.getValue().livingEntity));
        }
    }

    static class AngledEntity implements Comparable<AngledEntity> {
        private static int CLOSE_DISTANCE = (25 * 25) / 4;
        double angle;
        double distance;
        LivingEntity livingEntity;

        public AngledEntity(double angle, double distance, LivingEntity currentMobEntity) {
            this.angle = angle;
            this.distance = distance;
            livingEntity = currentMobEntity;
        }

        public static AngledEntity of(LivingEntity entity, LivingEntity player) {
            double angle = player.getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(player.getLocation().toVector()));
            double distance = player.getLocation().distance(entity.getLocation());
            return new AngledEntity(angle, distance, player);
        }

        @Override
        public int hashCode() {
            return (int) (angle + livingEntity.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AngledEntity && angle == ((AngledEntity) obj).angle && livingEntity.equals(((AngledEntity) obj).livingEntity);
        }

        @Override
        public int compareTo(AngledEntity o) {
            double c1, c2;
            c1 = angle;
            c2 = o.angle;
            double distanceShift = 1000000d;
            if (distance > CLOSE_DISTANCE) {
                c1 += distanceShift;
            }
            if (o.distance > CLOSE_DISTANCE) {
                c2 += distanceShift;
            }
            if (c1 - c2 > 0) return 1;
            if (c1 == c2) return 0;
            return -1;
        }
    }
}