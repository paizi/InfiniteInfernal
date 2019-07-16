package cat.nyaa.infiniteinfernal.loot;

import cat.nyaa.infiniteinfernal.configs.MessageConfig;
import cat.nyaa.infiniteinfernal.mob.IMob;
import cat.nyaa.infiniteinfernal.utils.Utils;
import cat.nyaa.nyaacore.Message;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class InfMessager implements IMessager {
    private List<String> drop;
    private List<String> mobKill;
    private List<String> noDrop;
    private List<String> playerKill;
    private List<String> specialDrop;

    public InfMessager(MessageConfig config){
        setupFromConfig(config);
    }

    private void setupFromConfig(MessageConfig config) {
        drop = config.drop;
        mobKill = config.mobKill;
        noDrop = config.noDrop;
        playerKill = config.playerKill;
        specialDrop = config.specialDrop;
    }

    @Override
    public void broadcastToWorld(IMob deadMob, LivingEntity killer, ILootItem item) {
        String s = Utils.randomPick(playerKill);
        new Message("").append(buildString(s, deadMob, killer, item), killer.getEquipment().getItemInMainHand())
                .send(killer);
        if (item !=null){

        }
    }

    private String buildString(String s, IMob deadMob, LivingEntity killer, ILootItem item) {
        return s.replaceAll("\\{player\\.name}", killer.getName())
                .replaceAll("\\{mob\\.name}", deadMob.getTaggedName())
                .replaceAll("\\{player\\.item}", "{itemName}");
    }

    @Override
    public void broadcastExtraToWorld(IMob deadMob, LivingEntity killer, ILootItem item) {

    }

}