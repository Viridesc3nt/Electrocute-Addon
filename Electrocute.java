package me.justinjaques.lightningball;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
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

    private static final String AUTHOR = "&2Viridescent_";
    private static final String VERSION = "&21.0.0";
    private static final String NAME = "Electrocute";
    private static final double SPEED = 2;
    private static final long COOLDOWN = 6000;
    private double distanceTravelled;
    private Permission perm;
    public static final long RANGE = 20;
    private static final long DAMAGE = 1;
    private static final int SHOCK_TIME = 70;
    private static final double INCREMENT = .2;

    Random rand = new Random();


    private ElectrocuteListener listener;
    private Location location;
    private Vector direction;


    public Electrocute(Player player) {
        super(player);

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


            int chanceOfStun = rand.nextInt(4);

            if (target instanceof LivingEntity && chanceOfStun == 2) {
                DamageHandler.damageEntity(target, DAMAGE, this);
                ((LivingEntity)target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SHOCK_TIME, 255));


            } else {
                DamageHandler.damageEntity(target, DAMAGE, this);

            }

        }

    }

    private void zigZag() {
        Vector ortho = GeneralMethods.getOrthogonalVector(direction, Math.random(), Math.random() * 360);
        Vector out = direction.clone().add(ortho).multiply(INCREMENT);
        Vector in = direction.clone().subtract(ortho).multiply(INCREMENT);
        for (double d = 0; d < 1; d += INCREMENT) {
            location.add(out);
            playLightningbendingSound(location);
            ParticleEffect.REDSTONE.display(location, 1, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 255 ,255), (float) 1.2));
        }

        for (double d = 0; d < 1; d += INCREMENT) {
            location.add(in);
            playLightningbendingSound(location);
            ParticleEffect.REDSTONE.display(location, 1, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 255 ,255), (float) 1.2));
        }

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
    public void load() {
        ProjectKorra.log.info(this.getName() + " by " + this.getAuthor() + "" + this.getVersion() + "has been loaded!");
        listener = new ElectrocuteListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
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