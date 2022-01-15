package me.justinjaques.lightningball;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public final class Electrocute extends LightningAbility implements AddonAbility {


    private static final String AUTHOR = ChatColor.RED + "Viridescent_";
    private static final String VERSION = ChatColor.RED + "1.0.0";
    private static final String NAME = "Electrocute";
    private double SPEED;
    private long COOLDOWN;
    private double distanceTravelled;
    private Permission perm;
    public long RANGE;
    private long DAMAGE;
    private int SHOCK_TIME;
    private double INCREMENT;
    static String path = "ExtraAbilities.Viridescent_.Lightning.Electrocute.";


    private ElectrocuteListener listener;
    private Location location;
    private Vector direction;

    private void setFields() {
        SPEED = ConfigManager.defaultConfig.get().getLong(path+"SPEED");
        COOLDOWN = ConfigManager.defaultConfig.get().getLong(path+"COOLDOWN");
        RANGE = ConfigManager.defaultConfig.get().getLong(path+"RANGE");
        DAMAGE = ConfigManager.defaultConfig.get().getLong(path+"DAMAGE");
        SHOCK_TIME = ConfigManager.defaultConfig.get().getInt(path+"SHOCK_TIME");
        INCREMENT = ConfigManager.defaultConfig.get().getDouble(path+"INCREMENT");
    }


    public Electrocute(Player player) {
        super(player);
        setFields();
        location = player.getEyeLocation();
        direction = player.getEyeLocation().getDirection();
        direction.multiply(SPEED);
        distanceTravelled = 0;
        if(!bPlayer.isOnCooldown(this)) {

            start();
            bPlayer.addCooldown(this);

        }

    }

    private void affectTargets() {
        List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(location, 2);

        for (Entity target : targets) {
            if (target.getUniqueId() == player.getUniqueId()) {
                continue;
            }

            if (target instanceof LivingEntity) {
                DamageHandler.damageEntity(target, DAMAGE, this);
                ((LivingEntity)target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SHOCK_TIME, 255));
            }

        }

    }

    private void zigZag() {
        Vector ortho = GeneralMethods.getOrthogonalVector(direction, Math.random() * 360, 0.25 + Math.random() * 0.5);
        Vector out = direction.clone().add(ortho).multiply(INCREMENT);
        Vector in = direction.clone().subtract(ortho).multiply(INCREMENT);
        double distance = SPEED / Math.cos(direction.angle(out));
        for (double d = 0; d < distance; d += INCREMENT) {
            location.add(out);
            ParticleEffect.REDSTONE.display(location, 1, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 230 ,255), (float) 1.2));
        }
        for (double d = 0; d < distance; d += INCREMENT) {
            location.add(in);
            ParticleEffect.REDSTONE.display(location, 1, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 230 ,255), (float) 1.2));
        }

    }



    @Override
    public String getDescription() {
        return ChatColor.RED + "Electrocute is a LightningBending technique that allows the user to build up charge in their body and \n release it from their fingertips. This ability also disables your opponents Chakra network temporarily, paralyzing them in the process.";

    }




    @Override
    public void progress() {
        if(!bPlayer.canBendIgnoreCooldowns(this)) {
            remove();
            return;
        }
       if(location.getBlock().getType().isSolid()) {
           remove();
           return;
       }

       if(distanceTravelled > RANGE) {
           remove();
           return;
       }
       playLightningbendingSound(location);
       affectTargets();
       zigZag();
       distanceTravelled += direction.length();

    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getInstructions() {

        return ChatColor.RED + "LEFT-CLICK at an opponent to shoot a bolt of Lightning at them. Upon impact, your opponent will be stunned.";
    }

    @Override
    public void load() {
        ProjectKorra.log.info(this.getName() + " by " + this.getAuthor() + "" + this.getVersion() + "has been loaded!");
        listener = new ElectrocuteListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        ConfigManager.defaultConfig.get().addDefault(path+"INCREMENT", .02);
        ConfigManager.defaultConfig.get().addDefault(path+"COOLDOWN", 9000);
        ConfigManager.defaultConfig.get().addDefault(path+"RANGE", 15);
        ConfigManager.defaultConfig.get().addDefault(path+"SPEED", 2);
        ConfigManager.defaultConfig.get().addDefault(path+"SHOCK_TIME", 70);
        ConfigManager.defaultConfig.save();
        perm = new Permission("bending.ability.Electrocution");
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);

    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}
