package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.CaseItem;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DropEditorGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final Case caseObj;
    private final CaseItem originalItem;
    private final DropManagementGUI parentGUI;
    private final Inventory inventory;
    private final boolean isNewItem;
    
    // Current editing values
    private Material currentMaterial = Material.DIAMOND;
    private int currentAmount = 1;
    private double currentWeight = 1.0;
    private String currentDisplayName = "";
    private List<String> currentLore = new ArrayList<>();
    private boolean currentGlow = false;
    private Map<Enchantment, Integer> currentEnchantments = new HashMap<>();
    
    // GUI slots
    private static final int MATERIAL_SLOT = 10;
    private static final int AMOUNT_SLOT = 12;
    private static final int WEIGHT_SLOT = 14;
    private static final int NAME_SLOT = 16;
    private static final int LORE_SLOT = 19;
    private static final int GLOW_SLOT = 21;
    private static final int ENCHANTMENTS_SLOT = 23;
    private static final int PREVIEW_SLOT = 25;
    private static final int SAVE_SLOT = 49;
    private static final int CANCEL_SLOT = 45;
    private static final int RESET_SLOT = 53;
    
    public DropEditorGUI(MeklasCasePlugin plugin, Player player, Case caseObj, CaseItem item, DropManagementGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.caseObj = caseObj;
        this.originalItem = item;
        this.parentGUI = parentGUI;
        this.isNewItem = (item == null);
        
        // Initialize values
        if (item != null) {
            this.currentMaterial = item.getMaterial();
            this.currentAmount = item.getAmount();
            this.currentWeight = item.getWeight();
            this.currentDisplayName = item.getDisplayName() != null ? item.getDisplayName() : "";
            this.currentLore = item.getLore() != null ? new ArrayList<>(item.getLore()) : new ArrayList<>();
            this.currentGlow = item.isGlow();
            this.currentEnchantments = item.getEnchantments() != null ? new HashMap<>(item.getEnchantments()) : new HashMap<>();
        }
        
        this.inventory = Bukkit.createInventory(this, 54, 
            "§6§l" + (isNewItem ? "Dodaj Nowy Drop" : "Edytuj Drop"));
        
        setupGUI();
    }
    
    private void setupGUI() {
        ItemStack borderPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        
        ItemStack materialItem = createItem(
            currentMaterial,
            "§a§lMateriał",
            Arrays.asList(
                "§7Obecny: §e" + currentMaterial.name(),
                "",
                "§8Lewy klik: Otwórz selektor materiałów",
                "§8Prawy klik: Ustaw na trzymany przedmiot"
            )
        );
        inventory.setItem(MATERIAL_SLOT, materialItem);
        
        ItemStack amountItem = createItem(
            Material.PAPER,
            "§a§lIlość",
            Arrays.asList(
                "§7Obecna: §e" + currentAmount,
                "",
                "§8Lewy klik: +1",
                "§8Prawy klik: -1",
                "§8Shift+Lewy: +10",
                "§8Shift+Prawy: -10",
                "§8Środkowy klik: Ustaw własną"
            )
        );
        inventory.setItem(AMOUNT_SLOT, amountItem);
        
        ItemStack weightItem = createItem(
            Material.GOLD_INGOT,
            "§a§lWaga (Szansa Dropu)",
            Arrays.asList(
                "§7Obecna: §e" + String.format("%.2f", currentWeight),
                calculateChanceDisplay(),
                "",
                "§8Lewy klik: +0.1",
                "§8Prawy klik: -0.1",
                "§8Shift+Lewy: +1.0",
                "§8Shift+Prawy: -1.0",
                "§8Środkowy klik: Ustaw własną"
            )
        );
        inventory.setItem(WEIGHT_SLOT, weightItem);
        
        ItemStack nameItem = createItem(
            Material.NAME_TAG,
            "§a§lNazwa Wyświetlana",
            Arrays.asList(
                "§7Obecna: " + (currentDisplayName.isEmpty() ? "§8Brak" : "§e" + currentDisplayName),
                "",
                "§8Lewy klik: Ustaw własną nazwę",
                "§8Prawy klik: Wyczyść nazwę"
            )
        );
        inventory.setItem(NAME_SLOT, nameItem);
        
        ItemStack loreItem = createItem(
            Material.WRITABLE_BOOK,
            "§a§lOpis",
            Arrays.asList(
                "§7Linie: §e" + currentLore.size(),
                currentLore.isEmpty() ? "§8Brak opisu" : "§7Podgląd:",
                currentLore.stream().limit(3).collect(Collectors.joining("\n§7")),
                currentLore.size() > 3 ? "§8... i " + (currentLore.size() - 3) + " więcej" : "",
                "",
                "§8Lewy klik: Dodaj linię",
                "§8Prawy klik: Edytuj opis",
                "§8Shift+Prawy: Wyczyść wszystko"
            ).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList())
        );
        inventory.setItem(LORE_SLOT, loreItem);
        
        ItemStack glowItem = createItem(
            currentGlow ? Material.GLOWSTONE : Material.REDSTONE,
            "§a§lEfekt Blasku",
            Arrays.asList(
                "§7Status: " + (currentGlow ? "§aWłączony" : "§cWyłączony"),
                "",
                "§8Kliknij aby przełączyć efekt blasku"
            )
        );
        inventory.setItem(GLOW_SLOT, glowItem);
        
        ItemStack enchantItem = createItem(
            Material.ENCHANTED_BOOK,
            "§a§lZaklęcia",
            Arrays.asList(
                "§7Ilość: §e" + currentEnchantments.size(),
                currentEnchantments.isEmpty() ? "§8Brak zaklęć" : "§7Zaklęcia:",
                currentEnchantments.entrySet().stream()
                    .limit(5)
                    .map(entry -> "§8- §7" + entry.getKey().getKey().getKey() + " " + entry.getValue())
                    .collect(Collectors.joining("\n")),
                currentEnchantments.size() > 5 ? "§8... i " + (currentEnchantments.size() - 5) + " więcej" : "",
                "",
                "§8Lewy klik: Dodaj zaklęcie",
                "§8Prawy klik: Zarządzaj zaklęciami",
                "§8Shift+Prawy: Wyczyść wszystko"
            ).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList())
        );
        inventory.setItem(ENCHANTMENTS_SLOT, enchantItem);
        
        updatePreview();
        
        ItemStack saveButton = createItem(
            Material.EMERALD,
            "§a§lZapisz Drop",
            Arrays.asList(
                "§7Zapisz ten drop do skrzynki",
                "",
                "§8Kliknij aby zapisać i powrócić"
            )
        );
        inventory.setItem(SAVE_SLOT, saveButton);
        
        ItemStack cancelButton = createItem(
            Material.BARRIER,
            "§c§lAnuluj",
            Arrays.asList(
                "§7Odrzuć zmiany i powróć",
                "",
                "§8Kliknij aby anulować"
            )
        );
        inventory.setItem(CANCEL_SLOT, cancelButton);
        
        ItemStack resetButton = createItem(
            Material.YELLOW_CONCRETE,
            "§e§lResetuj",
            Arrays.asList(
                "§7Resetuj do oryginalnych wartości",
                "",
                "§8Kliknij aby zresetować"
            )
        );
        inventory.setItem(RESET_SLOT, resetButton);
    }
    
    private void updatePreview() {
        CaseItem previewItem = new CaseItem(
            currentMaterial,
            currentAmount,
            currentWeight,
            currentDisplayName.isEmpty() ? null : currentDisplayName,
            currentLore.isEmpty() ? null : currentLore,
            currentGlow,
            currentEnchantments.isEmpty() ? null : currentEnchantments
        );
        
        ItemStack preview = previewItem.createItemStack();
        ItemMeta meta = preview.getItemMeta();
        
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore.addAll(meta.getLore());
                lore.add("");
            }
            
            lore.add("§6§lPodgląd Przedmiotu");
            lore.add("§7Tak będzie wyglądał drop");
            
            meta.setLore(lore);
            preview.setItemMeta(meta);
        }
        
        inventory.setItem(PREVIEW_SLOT, preview);
    }
    
    private String calculateChanceDisplay() {
        double totalWeight = caseObj.getItems().stream().mapToDouble(CaseItem::getWeight).sum();
        if (!isNewItem && originalItem != null) {
            totalWeight -= originalItem.getWeight();
        }
        totalWeight += currentWeight;
        
        double chance = (currentWeight / totalWeight) * 100;
        return "§7Szansa: §e" + String.format("%.2f%%", chance);
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        
        switch (slot) {
            case MATERIAL_SLOT:
                if (clickType == ClickType.LEFT) {
                    new MaterialSelectorGUI(plugin, player, this).open();
                } else if (clickType == ClickType.RIGHT) {
                    ItemStack held = player.getInventory().getItemInMainHand();
                    if (held != null && held.getType() != Material.AIR) {
                        currentMaterial = held.getType();
                        setupGUI();
                        MessageUtils.sendMessage(player, "§aMateriał ustawiony na: §e" + currentMaterial.name());
                    }
                }
                break;
                
            case AMOUNT_SLOT:
                handleAmountClick(clickType);
                break;
                
            case WEIGHT_SLOT:
                handleWeightClick(clickType);
                break;
                
            case NAME_SLOT:
                if (clickType == ClickType.LEFT) {
                    startNameConversation();
                } else if (clickType == ClickType.RIGHT) {
                    currentDisplayName = "";
                    setupGUI();
                    MessageUtils.sendMessage(player, "§aNazwa wyświetlana wyczyszczona!");
                }
                break;
                
            case LORE_SLOT:
                if (clickType == ClickType.LEFT) {
                    startLoreAddConversation();
                } else if (clickType == ClickType.RIGHT) {
                    new LoreEditorGUI(plugin, player, this).open();
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    currentLore.clear();
                    setupGUI();
                    MessageUtils.sendMessage(player, "§aOpis wyczyszczony!");
                }
                break;
                
            case GLOW_SLOT:
                currentGlow = !currentGlow;
                setupGUI();
                MessageUtils.sendMessage(player, "§aEfekt blasku: " + (currentGlow ? "§aWłączony" : "§cWyłączony"));
                break;
                
            case ENCHANTMENTS_SLOT:
                if (clickType == ClickType.LEFT) {
                    new EnchantmentSelectorGUI(plugin, player, this).open();
                } else if (clickType == ClickType.RIGHT) {
                    new EnchantmentManagerGUI(plugin, player, this).open();
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    currentEnchantments.clear();
                    setupGUI();
                    MessageUtils.sendMessage(player, "§aWszystkie zaklęcia wyczyszczone!");
                }
                break;
                
            case SAVE_SLOT:
                saveDrop();
                break;
                
            case CANCEL_SLOT:
                parentGUI.open();
                break;
                
            case RESET_SLOT:
                resetValues();
                break;
        }
    }
    
    private void handleAmountClick(ClickType clickType) {
        switch (clickType) {
            case LEFT:
                currentAmount = Math.min(64, currentAmount + 1);
                break;
            case RIGHT:
                currentAmount = Math.max(1, currentAmount - 1);
                break;
            case SHIFT_LEFT:
                currentAmount = Math.min(64, currentAmount + 10);
                break;
            case SHIFT_RIGHT:
                currentAmount = Math.max(1, currentAmount - 10);
                break;
            case MIDDLE:
                startAmountConversation();
                return;
        }
        setupGUI();
    }
    
    private void handleWeightClick(ClickType clickType) {
        switch (clickType) {
            case LEFT:
                currentWeight += 0.1;
                break;
            case RIGHT:
                currentWeight = Math.max(0.01, currentWeight - 0.1);
                break;
            case SHIFT_LEFT:
                currentWeight += 1.0;
                break;
            case SHIFT_RIGHT:
                currentWeight = Math.max(0.01, currentWeight - 1.0);
                break;
            case MIDDLE:
                startWeightConversation();
                return;
        }
        currentWeight = Math.round(currentWeight * 100.0) / 100.0; // Round to 2 decimal places
        setupGUI();
    }
    
    private void saveDrop() {
        CaseItem newItem = new CaseItem(
            currentMaterial,
            currentAmount,
            currentWeight,
            currentDisplayName.isEmpty() ? null : currentDisplayName,
            currentLore.isEmpty() ? null : new ArrayList<>(currentLore),
            currentGlow,
            currentEnchantments.isEmpty() ? null : new HashMap<>(currentEnchantments)
        );
        
        if (isNewItem) {
            caseObj.getItems().add(newItem);
            MessageUtils.sendMessage(player, "§aNowy drop dodany pomyślnie!");
        } else {
            int index = caseObj.getItems().indexOf(originalItem);
            if (index != -1) {
                caseObj.getItems().set(index, newItem);
                MessageUtils.sendMessage(player, "§aDrop zaktualizowany pomyślnie!");
            }
        }
        
        plugin.getCaseManager().saveCase(caseObj);
        parentGUI.open();
    }
    
    private void resetValues() {
        if (originalItem != null) {
            currentMaterial = originalItem.getMaterial();
            currentAmount = originalItem.getAmount();
            currentWeight = originalItem.getWeight();
            currentDisplayName = originalItem.getDisplayName() != null ? originalItem.getDisplayName() : "";
            currentLore = originalItem.getLore() != null ? new ArrayList<>(originalItem.getLore()) : new ArrayList<>();
            currentGlow = originalItem.isGlow();
            currentEnchantments = originalItem.getEnchantments() != null ? new HashMap<>(originalItem.getEnchantments()) : new HashMap<>();
        } else {
            currentMaterial = Material.DIAMOND;
            currentAmount = 1;
            currentWeight = 1.0;
            currentDisplayName = "";
            currentLore.clear();
            currentGlow = false;
            currentEnchantments.clear();
        }
        setupGUI();
        MessageUtils.sendMessage(player, "§aWartości zresetowane!");
    }
    
    // Conversation starters
    private void startNameConversation() {
        player.closeInventory();
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aWpisz nazwę wyświetlaną dla tego dropu (lub 'anuluj' aby anulować):";
                }
                
                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    if ("anuluj".equalsIgnoreCase(input) || "cancel".equalsIgnoreCase(input)) {
                        return END_OF_CONVERSATION;
                    }
                    currentDisplayName = input.replace("&", "§");
                    return END_OF_CONVERSATION;
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    setupGUI();
                    open();
                });
            })
            .buildConversation(player);
        
        conversation.begin();
    }
    
    private void startLoreAddConversation() {
        player.closeInventory();
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter a new lore line (or 'cancel' to cancel):";
                }
                
                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    if ("cancel".equalsIgnoreCase(input)) {
                        return END_OF_CONVERSATION;
                    }
                    currentLore.add(input.replace("&", "§"));
                    return END_OF_CONVERSATION;
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    setupGUI();
                    open();
                });
            })
            .buildConversation(player);
        
        conversation.begin();
    }
    
    private void startAmountConversation() {
        player.closeInventory();
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new NumericPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the amount (1-64):";
                }
                
                @Override
                protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
                    int amount = input.intValue();
                    if (amount >= 1 && amount <= 64) {
                        currentAmount = amount;
                    }
                    return END_OF_CONVERSATION;
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    setupGUI();
                    open();
                });
            })
            .buildConversation(player);
        
        conversation.begin();
    }
    
    private void startWeightConversation() {
        player.closeInventory();
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new NumericPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the weight (0.01 or higher):";
                }
                
                @Override
                protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
                    double weight = input.doubleValue();
                    if (weight >= 0.01) {
                        currentWeight = Math.round(weight * 100.0) / 100.0;
                    }
                    return END_OF_CONVERSATION;
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    setupGUI();
                    open();
                });
            })
            .buildConversation(player);
        
        conversation.begin();
    }
    
    // Getters and setters for sub-GUIs
    public Material getCurrentMaterial() { return currentMaterial; }
    public void setCurrentMaterial(Material material) { this.currentMaterial = material; }
    
    public List<String> getCurrentLore() { return currentLore; }
    public void setCurrentLore(List<String> lore) { this.currentLore = lore; }
    
    public Map<Enchantment, Integer> getCurrentEnchantments() { return currentEnchantments; }
    public void setCurrentEnchantments(Map<Enchantment, Integer> enchantments) { this.currentEnchantments = enchantments; }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}