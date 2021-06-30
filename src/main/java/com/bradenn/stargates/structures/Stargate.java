package com.bradenn.stargates.structures;

import com.bradenn.stargates.cosmetics.BlockStand;
import com.bradenn.stargates.Database;
import com.bradenn.stargates.ParticleEffects;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.apache.commons.lang.RandomStringUtils;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Consumer;

public class Stargate extends Structure {

    /* Private variables */

    private final StargateModel model;
    private final UUID uuid;
    private final String name;
    private final String address;

    /**
     * Load stargate from database document.
     *
     * @param document The mongodb BSON document.
     */
    @SuppressWarnings("unchecked")
    public Stargate(Document document) {
        super((Document) document.get("structure"));
        this.name = document.getString("name");
        this.model = StargateModel.deserialize((Map<String, Object>) document.get("model"));
        this.address = document.getString("address");
        this.uuid = UUID.fromString(document.getString("uuid"));
    }

    /* Initialization functions */

    /**
     * Create a new stargate.
     *
     * @param name         Name of the stargate.
     * @param baseLocation The y + 1 location of the stargate.
     * @param orientation  The N W S E orientation of the stargate.
     */
    public Stargate(String name, Location baseLocation, Orientation orientation) {
        super(baseLocation.getBlock().getLocation().add(0.5, -0.5, 0.5), generateBoundingBox(baseLocation, orientation), orientation);

        this.model = StargateModel.MK1;
        this.name = name;

        this.uuid = UUID.randomUUID();
        this.address = RandomStringUtils.randomAlphanumeric(4).toUpperCase();

        build();

        Database.getCollection("stargates").insertOne(getDocument());
        new Dialer(name, this.model, this.uuid, baseLocation, orientation);
    }

    private static BoundingBox generateBoundingBox(Location base, Orientation orientation) {
        Location center = base.getBlock().getLocation().clone().add(0.5, 2.5, 0.5);
        Vector adjusted = orientation.translate(1.75, 1.75, -0.33);
        return BoundingBox.of(center, adjusted.getX(), adjusted.getY(), adjusted.getZ());
    }

    /**
     * Construct a Stargate from its database record.
     *
     * @param uuid Stargate uuid
     * @return Stargate object
     */
    public static Stargate fromUUID(UUID uuid) {
        MongoCollection<Document> stargates = Database.getCollection("stargates");
        Document match = stargates.find(new Document("uuid", uuid.toString())).first();
        if (Objects.isNull(match)) return null;
        return new Stargate(match);
    }

    /**
     * Construct a Stargate from its database record.
     *
     * @param address Stargate address
     * @return Stargate object
     */
    public static Stargate fromAddress(String address) {
        MongoCollection<Document> stargates = Database.getCollection("stargates");
        Document match = stargates.find(new Document("address", address)).first();
        if (Objects.isNull(match)) return null;
        return new Stargate(match);
    }

    /* Getter functions */

    /**
     * Get all stargate objects.
     */
    public static List<Stargate> getAll() {
        List<Stargate> stargates = new ArrayList<>();
        Database.getCollection("stargates").find().map(Stargate::new).forEach((Consumer<? super Stargate>) stargates::add);
        return stargates;
    }

    /**
     * Rebuild all of the stargates.
     */
    public static void rebuildAll() {
        Database.getCollection("stargates").find().forEach((Consumer<? super Document>) stargate -> new Stargate(stargate).rebuild());
    }

    /**
     * Destroy all of the stargate structures and remove them from the database.
     */
    public static void terminateAll() {
        Database.getCollection("stargates").find().forEach((Consumer<? super Document>) stargate -> {
            Stargate stargateRef = new Stargate(stargate);
            stargateRef.terminate();
        });
    }

    /**
     * Serialize the stargate object into a database document.
     */
    public Document getDocument() {
        Document document = new Document();
        document.put("name", name);
        document.put("model", model.serialize());
        document.put("address", address);
        document.put("uuid", uuid.toString());
        document.put("structure", super.getDocument());
        return document;
    }

    /* Macro Getter functions */

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    /* Static Class Constructors */

    public String getAddress() {
        return address;
    }

    public StargateModel getModel() {
        return model;
    }

    /* Bulk Static Class Constructors */

    /**
     * Draw the particle accretion disk of the stargate.
     */
    public void renderPortal() {
        Location center = getLocation().clone().add(0, 3, 0);
        World world = center.getWorld();
        if (Objects.isNull(world)) return;

        Particle.DustOptions blueDust = new Particle.DustOptions(ParticleEffects.ParticleColor.BLUE.getColor(), 2);
        for (double j = 0; j < 3; j += 0.2) {
            double particleCount = 4 * j;
            for (double i = 0; i < particleCount; i += 0.2) {
                double delta = (Math.PI * 2) / particleCount;
                double posX = Math.cos(delta * i) * j;
                double posY = Math.sin(delta * i) * j;
                Vector adjusted = getOrientation().translate(posX, posY, 0);
                world.spawnParticle(Particle.REDSTONE, center.clone().add(adjusted.getX(), adjusted.getY(), adjusted.getZ()), 1, blueDust);
            }
        }
    }

    /* Local functions */

    /**
     * Draw the particle accretion disk of the stargate.
     */
    public void renderOpeningPortal() {
//        Location center = getLocation().clone().add(0, 3, 0);
//        World world = center.getWorld();
//        if (Objects.isNull(world)) return;
//        double itr = 0.8;
//        Particle.DustOptions blueDust = new Particle.DustOptions(ParticleEffects.ParticleColor.PURPLE.getColor(), 2);
//        for (double j = 0; j < 3; j += 0.2) {
//            double particleCount = 4 * j;
//            for (double i = 0; i < particleCount; i += 0.2) {
//                double delta = (Math.PI * 2) / particleCount;
//                double posX = Math.cos(delta * i) * j;
//                double posY = Math.sin(delta * i) * j;
//                double posZ = -Math.abs(itr * Math.pow(j, 2)) + itr * 9;
//                Vector adjusted = getOrientation().translate(posX, posY, posZ);
//                world.spawnParticle(Particle.REDSTONE, center.clone().add(adjusted.getX(), adjusted.getY(), adjusted.getZ()), 1, blueDust);
//            }
//        }
    }

    /**
     * Destroy the stargate structure and remove it from the database.
     */
    public void summonPlayer(Player player) {
        Location safeTeleport = getLocation().clone().add(0, 1, 0);
        safeTeleport.getChunk().load();
        safeTeleport.setYaw(getOrientation().playerYaw());

        PotionEffect potionEffect = new PotionEffect(PotionEffectType.CONFUSION, 100, 10, true);
        player.addPotionEffect(potionEffect);
        player.teleport(safeTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setVelocity(getOrientation().translate(0, 0, 0.2));
    }

    /* Structure functions */

    public void setModel(StargateModel stargateModel) {

        Database.getCollection("stargates").findOneAndUpdate(new Document("uuid", this.getUUID().toString()),  Updates.set("model", stargateModel.serialize()));
        Database.getCollection("dialers").findOneAndUpdate(new Document("stargateUUID", this.getUUID().toString()),  Updates.set("model", stargateModel.serialize()));

        Dialer dialer = Dialer.fromStargate(getUUID());
        assert dialer != null;
        dialer.rebuild();
        Stargate stargate = Stargate.fromUUID(getUUID());
        assert stargate != null;
        stargate.rebuild();
    }

    /**
     * Construct the stargate structure.
     */
    public void build() {
        double yOffset = 1.8125;

        World world = getWorld();
        Location centerLocation = getLocation().clone().add(0, yOffset, 0);
        int outer = 34;
        for (int i = 0; i < outer; i++) {

            double unitCircle = (2 * Math.PI);

            double outerDelta = unitCircle / 34;
            double outerAngle = outerDelta * i + outerDelta * 8.5;

            double outerX = Math.cos(outerDelta * i) * 3.25;
            double outerY = Math.sin(outerDelta * i) * 3.25;

            BlockStand outerRing = new BlockStand(uuid, world);

            Location outerLocation = centerLocation.clone().add(getOrientation().rotate(outerX, outerY, (i % 2 == 0 ? 0.005 : -0.005)));
            outerRing.setMaterial(Material.DEEPSLATE_TILE_SLAB);
            Vector outerRotation = getOrientation().rotate(0, 0, -outerAngle);
            outerRing.largeBlockAt(outerLocation, new EulerAngle(outerRotation.getX(), outerRotation.getY(), outerRotation.getZ()));


            if (i > 32) continue;
            double innerDelta = unitCircle / 32;
            double innerAngle = innerDelta * i + innerDelta * 8;

            double innerX = Math.cos(innerDelta * i) * 3;
            double innerY = Math.sin(innerDelta * i) * 3;

            BlockStand innerRing = new BlockStand(uuid, world);
            Location innerLocation = centerLocation.clone().add(getOrientation().rotate(innerX, innerY, (i % 2 == 0 ? 0.002 : -0.002)));
            innerRing.setMaterial(Material.POLISHED_DEEPSLATE_SLAB);
            Vector innerRotation = getOrientation().rotate(0, 0, -innerAngle);
            innerRing.largeBlockAt(innerLocation, new EulerAngle(innerRotation.getX(), innerRotation.getY(), innerRotation.getZ()));

            int chevronCount = 8;
            if (getModel().equals(StargateModel.MK2)) {
                chevronCount = 16;
            }
            if (i > chevronCount * 2) continue;
            double chevronDelta = unitCircle / chevronCount;

            double chevronAngle = chevronDelta * i + chevronDelta * (getModel().equals(StargateModel.MK2) ? 4 : 2);

            double chevronX = Math.cos(chevronDelta * i) * 3.125;
            double chevronY = Math.sin(chevronDelta * i) * 3.125;

            BlockStand chevronRing = new BlockStand(uuid, world);
            Location chevronLocationA = centerLocation.clone().add(getOrientation().rotate(chevronX, chevronY, (i % 2 == 0 ? 0.025 : -0.025)));
            Location chevronLocationB = centerLocation.clone().add(getOrientation().rotate(chevronX, chevronY, (i % 2 == 0 ? -0.025 : 0.025)));
            if (getModel().equals(StargateModel.MK1)) {
                chevronRing.setMaterial(Material.WAXED_CUT_COPPER_SLAB);
            } else {
                chevronRing.setMaterial(Material.DARK_PRISMARINE_SLAB);
            }

            Vector chevronRotation = getOrientation().rotateSpin(0, 0, -chevronAngle);
            chevronRing.largeBlockAt(chevronLocationA, new EulerAngle(chevronRotation.getX(), chevronRotation.getY(), chevronRotation.getZ()));
            chevronRing.largeBlockAt(chevronLocationB, new EulerAngle(chevronRotation.getX(), chevronRotation.getY(), chevronRotation.getZ()));
        }
    }

    /**
     * Find and remove all structure-related objects.
     */
    public void destroy() {
        Collection<Entity> nearbyEntities = getWorld().getNearbyEntities(getBoundingBox().clone().expand(3, 3, 3), e -> BlockStand.isArmorStand(e, uuid));
        nearbyEntities.forEach(Entity::remove);
    }

    /**
     * Destroy the stargate structure and remove it from the database.
     */
    public void terminate() {
        this.destroy();
        Database.getCollection("stargates").findOneAndDelete(new Document("uuid", getUUID().toString()));
        Document dialer = Database.getCollection("dialers").findOneAndDelete(new Document("stargateUUID", getUUID().toString()));
        assert dialer != null;
        new Dialer(dialer).destroy();
    }


}