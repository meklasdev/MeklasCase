package pl.meklas.meklascase.trail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Trail {
    
    private UUID id;
    private String name;
    private String description;
    private TrailType type;
    private List<Location> points;
    private Material displayMaterial;
    private Particle particle;
    private double speed;
    private int duration;
    private boolean active;
    private UUID owner;
    private long createdAt;
    private long lastModified;
    
    // Trail properties for table management
    private String category;
    private int priority;
    private boolean publicTrail;
    
    public Trail(String name, TrailType type, UUID owner) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.points = new ArrayList<>();
        this.active = false;
        this.speed = 1.0;
        this.duration = 60;
        this.displayMaterial = Material.DIAMOND;
        this.particle = Particle.FLAME;
        this.category = "Default";
        this.priority = 0;
        this.publicTrail = false;
        this.createdAt = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.lastModified = System.currentTimeMillis();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.lastModified = System.currentTimeMillis();
    }
    
    public TrailType getType() { return type; }
    public void setType(TrailType type) { 
        this.type = type;
        this.lastModified = System.currentTimeMillis();
    }
    
    public List<Location> getPoints() { return points; }
    public void setPoints(List<Location> points) { 
        this.points = points;
        this.lastModified = System.currentTimeMillis();
    }
    
    public void addPoint(Location location) {
        this.points.add(location);
        this.lastModified = System.currentTimeMillis();
    }
    
    public void removePoint(int index) {
        if (index >= 0 && index < points.size()) {
            this.points.remove(index);
            this.lastModified = System.currentTimeMillis();
        }
    }
    
    public Material getDisplayMaterial() { return displayMaterial; }
    public void setDisplayMaterial(Material displayMaterial) { 
        this.displayMaterial = displayMaterial;
        this.lastModified = System.currentTimeMillis();
    }
    
    public Particle getParticle() { return particle; }
    public void setParticle(Particle particle) { 
        this.particle = particle;
        this.lastModified = System.currentTimeMillis();
    }
    
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { 
        this.speed = speed;
        this.lastModified = System.currentTimeMillis();
    }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { 
        this.duration = duration;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { 
        this.active = active;
        this.lastModified = System.currentTimeMillis();
    }
    
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { 
        this.owner = owner;
        this.lastModified = System.currentTimeMillis();
    }
    
    public long getCreatedAt() { return createdAt; }
    public long getLastModified() { return lastModified; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.lastModified = System.currentTimeMillis();
    }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { 
        this.priority = priority;
        this.lastModified = System.currentTimeMillis();
    }
    
    public boolean isPublicTrail() { return publicTrail; }
    public void setPublicTrail(boolean publicTrail) { 
        this.publicTrail = publicTrail;
        this.lastModified = System.currentTimeMillis();
    }
    
    // Utility methods
    public double getLength() {
        if (points.size() < 2) return 0.0;
        
        double length = 0.0;
        for (int i = 1; i < points.size(); i++) {
            Location prev = points.get(i - 1);
            Location curr = points.get(i);
            if (prev.getWorld().equals(curr.getWorld())) {
                length += prev.distance(curr);
            }
        }
        return length;
    }
    
    public boolean canEdit(Player player) {
        return player.hasPermission("meklascase.trail.admin") || 
               player.getUniqueId().equals(owner);
    }
    
    @Override
    public String toString() {
        return String.format("Trail{name='%s', type=%s, points=%d, active=%s}", 
                           name, type, points.size(), active);
    }
}