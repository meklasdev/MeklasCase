package pl.meklas.meklascase.hologram;

import pl.meklas.meklascase.utils.MessageUtils;

import java.util.Random;

/**
 * Advanced visual effects for animated holograms
 */
public class HologramEffects {
    
    private static final Random random = new Random();
    
    // Rainbow colors for special effects
    private static final String[] RAINBOW_COLORS = {
        "<gradient:#ff0000:#ff8800>", // Red to Orange
        "<gradient:#ff8800:#ffff00>", // Orange to Yellow  
        "<gradient:#ffff00:#88ff00>", // Yellow to Green
        "<gradient:#88ff00:#00ff88>", // Green to Cyan
        "<gradient:#00ff88:#0088ff>", // Cyan to Blue
        "<gradient:#0088ff:#8800ff>", // Blue to Purple
        "<gradient:#8800ff:#ff0088>"  // Purple to Pink
    };
    
    // Particle effects using Unicode characters
    private static final String[] PARTICLES = {
        "✨", "⭐", "🌟", "💫", "✦", "✧", "✩", "✪", "✫", "✬", "✭", "✮", "✯", "✰"
    };
    
    // Weather effects
    private static final String[] SNOW_EFFECTS = {
        "❄", "❅", "❆", "🌨", "⛄"
    };
    
    private static final String[] RAIN_EFFECTS = {
        "💧", "💦", "🌧", "☔", "🌦"
    };
    
    // Magic effects
    private static final String[] MAGIC_SYMBOLS = {
        "✦", "✧", "✩", "✪", "✫", "✬", "✭", "✮", "✯", "✰", "◆", "◇", "◈", "◉", "◊"
    };
    
    // Geometric patterns
    private static final String[] GEOMETRIC_PATTERNS = {
        "▲ ▼ ▲", "◆ ◇ ◆", "● ○ ●", "■ □ ■", "★ ☆ ★"
    };
    
    /**
     * Creates a rainbow text effect
     */
    public static String createRainbowText(String text, int offset) {
        if (text.length() == 0) return text;
        
        StringBuilder rainbow = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                rainbow.append(' ');
                continue;
            }
            
            int colorIndex = (i + offset) % RAINBOW_COLORS.length;
            rainbow.append(RAINBOW_COLORS[colorIndex])
                   .append(c)
                   .append("</gradient>");
        }
        
        return rainbow.toString();
    }
    
    /**
     * Creates floating particles effect
     */
    public static String createParticleField(int width, int density, int animationFrame) {
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < width; i++) {
            if (random.nextInt(100) < density) {
                // Add animated particle
                int particleIndex = (i + animationFrame) % PARTICLES.length;
                String particle = PARTICLES[particleIndex];
                
                // Random color for particle
                int colorIndex = (i + animationFrame) % RAINBOW_COLORS.length;
                field.append(RAINBOW_COLORS[colorIndex])
                     .append(particle)
                     .append("</gradient>");
            } else {
                field.append(" ");
            }
        }
        
        return field.toString();
    }
    
    /**
     * Creates a pulsing border effect
     */
    public static String createPulsingBorder(int length, int pulsePhase) {
        StringBuilder border = new StringBuilder();
        
        // Choose border character based on pulse phase
        String borderChar;
        String color;
        
        switch (pulsePhase % 4) {
            case 0:
                borderChar = "▬";
                color = MessageUtils.PRIMARY_GRADIENT;
                break;
            case 1:
                borderChar = "▭";
                color = MessageUtils.SUCCESS_GRADIENT;
                break;
            case 2:
                borderChar = "▬";
                color = MessageUtils.WARNING_GRADIENT;
                break;
            default:
                borderChar = "▭";
                color = MessageUtils.ERROR_GRADIENT;
                break;
        }
        
        border.append(color);
        for (int i = 0; i < length; i++) {
            border.append(borderChar);
        }
        border.append("</gradient>");
        
        return border.toString();
    }
    
    /**
     * Creates a wave animation effect
     */
    public static String createWaveAnimation(String text, int wavePhase) {
        if (text.length() == 0) return text;
        
        StringBuilder wave = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Calculate wave position
            double wavePos = Math.sin((i + wavePhase) * 0.5) * 2;
            
            // Apply different effects based on wave position
            if (wavePos > 1) {
                // Peak - bright color
                wave.append(MessageUtils.SUCCESS_GRADIENT)
                    .append(c)
                    .append("</gradient>");
            } else if (wavePos < -1) {
                // Valley - dim color
                wave.append("<gray>")
                    .append(c)
                    .append("</gray>");
            } else {
                // Normal - standard color
                wave.append(MessageUtils.PRIMARY_GRADIENT)
                    .append(c)
                    .append("</gradient>");
            }
        }
        
        return wave.toString();
    }
    
    /**
     * Creates a matrix-style digital rain effect
     */
    public static String createDigitalRain(int width, int animationFrame) {
        StringBuilder rain = new StringBuilder();
        
        String[] digits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        
        rain.append(MessageUtils.SUCCESS_GRADIENT);
        
        for (int i = 0; i < width; i++) {
            if ((i + animationFrame) % 3 == 0) {
                int digitIndex = (i + animationFrame) % digits.length;
                rain.append(digits[digitIndex]);
            } else {
                rain.append(" ");
            }
        }
        
        rain.append("</gradient>");
        return rain.toString();
    }
    
    /**
     * Creates a spinning loading effect
     */
    public static String createSpinner(int frame) {
        String[] spinChars = {"|", "/", "-", "\\", "|", "/", "-", "\\"};
        String spinChar = spinChars[frame % spinChars.length];
        
        return MessageUtils.WARNING_GRADIENT + "[" + spinChar + "]</gradient>";
    }
    
    /**
     * Creates a typewriter effect
     */
    public static String createTypewriterEffect(String fullText, int charactersShown, int cursorBlink) {
        if (charactersShown >= fullText.length()) {
            return MessageUtils.PRIMARY_GRADIENT + fullText + "</gradient>";
        }
        
        String visibleText = fullText.substring(0, charactersShown);
        String cursor = cursorBlink % 2 == 0 ? "|" : " ";
        
        return MessageUtils.PRIMARY_GRADIENT + visibleText + cursor + "</gradient>";
    }
    
    /**
     * Creates a glitch effect
     */
    public static String createGlitchEffect(String text, int glitchFrame) {
        if (glitchFrame % 10 != 0) {
            return MessageUtils.PRIMARY_GRADIENT + text + "</gradient>";
        }
        
        // Apply glitch
        StringBuilder glitched = new StringBuilder();
        String[] glitchChars = {"█", "▓", "▒", "░", "▀", "▄", "▌", "▐"};
        
        for (int i = 0; i < text.length(); i++) {
            if (random.nextInt(5) == 0) {
                // Glitch this character
                String glitchChar = glitchChars[random.nextInt(glitchChars.length)];
                glitched.append(MessageUtils.ERROR_GRADIENT)
                        .append(glitchChar)
                        .append("</gradient>");
            } else {
                glitched.append(text.charAt(i));
            }
        }
        
        return MessageUtils.PRIMARY_GRADIENT + glitched.toString() + "</gradient>";
    }
    
    /**
     * Creates a breathing effect (expand/contract)
     */
    public static String createBreathingEffect(String text, int breathPhase) {
        double breathScale = Math.sin(breathPhase * 0.2) * 0.5 + 1.0; // Scale between 0.5 and 1.5
        
        if (breathScale > 1.2) {
            // Expanded - add spaces
            return MessageUtils.PRIMARY_GRADIENT + " " + text + " </gradient>";
        } else if (breathScale < 0.8) {
            // Contracted - remove spaces if any
            return MessageUtils.SECONDARY_GRADIENT + text.trim() + "</gradient>";
        } else {
            // Normal
            return MessageUtils.PRIMARY_GRADIENT + text + "</gradient>";
        }
    }
    
    /**
     * Creates a neon glow effect
     */
    public static String createNeonGlow(String text, int glowPhase) {
        String[] neonColors = {
            "<gradient:#ff00ff:#ff0080>", // Pink neon
            "<gradient:#00ffff:#0080ff>", // Cyan neon  
            "<gradient:#ffff00:#ff8000>", // Yellow neon
            "<gradient:#00ff00:#80ff00>"  // Green neon
        };
        
        String color = neonColors[glowPhase % neonColors.length];
        
        // Add glow effect with special characters
        return color + "▓▒░ " + text + " ░▒▓</gradient>";
    }
    
    /**
     * Creates a fire effect
     */
    public static String createFireEffect(int intensity, int animationFrame) {
        String[] fireChars = {"🔥", "🔥🔥", "🔥🔥🔥"};
        String[] fireColors = {
            MessageUtils.ERROR_GRADIENT,
            MessageUtils.WARNING_GRADIENT,
            "<gradient:#ff4500:#ff6347>"
        };
        
        StringBuilder fire = new StringBuilder();
        
        for (int i = 0; i < intensity; i++) {
            int charIndex = (i + animationFrame) % fireChars.length;
            int colorIndex = (i + animationFrame) % fireColors.length;
            
            fire.append(fireColors[colorIndex])
                .append(fireChars[charIndex])
                .append("</gradient>");
        }
        
        return fire.toString();
    }
    
    /**
     * Creates a water wave effect
     */
    public static String createWaterWave(int width, int waveFrame) {
        StringBuilder wave = new StringBuilder();
        String[] waveChars = {"~", "∼", "≈", "∽"};
        
        wave.append("<gradient:#0077be:#00a8e8>");
        
        for (int i = 0; i < width; i++) {
            int charIndex = (i + waveFrame) % waveChars.length;
            wave.append(waveChars[charIndex]);
        }
        
        wave.append("</gradient>");
        return wave.toString();
    }
    
    /**
     * Creates a geometric pattern animation
     */
    public static String createGeometricPattern(int patternFrame) {
        String pattern = GEOMETRIC_PATTERNS[patternFrame % GEOMETRIC_PATTERNS.length];
        
        return MessageUtils.SECONDARY_GRADIENT + pattern + "</gradient>";
    }
    
    /**
     * Creates a constellation effect
     */
    public static String createConstellation(int width, int frame) {
        StringBuilder constellation = new StringBuilder();
        String[] stars = {"✦", "✧", "✩", "✪", "✫"};
        
        constellation.append("<gradient:#000080:#4169e1>");
        
        for (int i = 0; i < width; i++) {
            if ((i + frame) % 7 == 0) {
                int starIndex = (i + frame) % stars.length;
                constellation.append(stars[starIndex]);
            } else {
                constellation.append(" ");
            }
        }
        
        constellation.append("</gradient>");
        return constellation.toString();
    }
}