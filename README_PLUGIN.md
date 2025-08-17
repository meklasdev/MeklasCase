# 🎰 meklasCase - Advanced Case Plugin

**meklasCase** to zaawansowany plugin skrzynek dla serwerów Minecraft Paper/Spigot 1.20.6–1.21. Oferuje piękne animacje, system rotacji co 24h, integrację z hologramami i wiele więcej!

## ✨ Główne Funkcje

### 🎁 System Skrzynek
- **Klasyczne typy**: LOOTBOX i LUCKBLOCK
- **Piękne animacje**: Płynne GUI z efektami spin
- **Szybkie otwieranie**: Shift+PPM dla natychmiastowego otwarcia
- **Ładne efekty**: Dźwięki, tytuły, fajerwerki

### 🔄 System Rotacji
- **Automatyczna rotacja**: Co 24h lub o stałej godzinie
- **Profile dnia**: Różne boosty i przedmioty każdego dnia
- **Boosty**: Zwiększone szanse na konkretne itemy
- **Override**: Całkowicie inne przedmioty w specjalne dni

### 🌟 Hologramy (fHolo)
- **Automatyczne hologramy**: Nad każdą skrzynką
- **Informacje na żywo**: Aktualny boost, czas do rotacji
- **TOP DROP**: Wyświetlanie ostatnich najlepszych wygranych
- **Piękny design**: Kolorowe gradienty i emotikony

### 📢 System Ogłoszeń
- **Broadcast wygranych**: Automatyczne ogłaszanie wygranych
- **TOP DROP alerts**: Specjalne efekty dla najrzadszych przedmiotów
- **Rotacja**: Informowanie o zmianach profilu dnia

## 🚀 Instalacja

1. **Pobierz** plik `meklasCase-1.0.0.jar`
2. **Wrzuć** do folderu `plugins/` na serwerze
3. **Zrestartuj** serwer
4. **Opcjonalnie**: Zainstaluj `HolographicDisplays` dla hologramów

## ⚙️ Konfiguracja

### Główny plik - `config.yml`

```yaml
# Ustawienia rotacji
resetAtFixedTime: true  # true = o stałej godzinie, false = co X godzin
fixedTime: "04:00"     # Godzina rotacji (format 24h)
windowHours: 24        # Godziny między rotacjami

# Funkcje
quickOpen: true        # Shift+PPM = szybkie otwarcie

# Dźwięki
sounds:
  noKey: ENTITY_VILLAGER_NO
  spin: UI_BUTTON_CLICK
  win: ENTITY_PLAYER_LEVELUP

# Ogłoszenia
broadcast:
  enabled: true
  messages:
    win: "{player} wygrał {item} x{amount} z {case}"
    top: "{player} pobił TOP DROP! {item} x{amount}"
    rotation: "Nowy dzień! Dziś wysoka szansa na {boost_item}"
```

### Skrzynki - `cases/nazwa.yml`

```yaml
type: LOOTBOX  # LOOTBOX lub LUCKBLOCK

# Konfiguracja klucza
key:
  material: TRIPWIRE_HOOK
  name: "&a&lKlucz do Skrzynki"
  lore:
    - "&7Użyj na skrzynce"
  glow: true

# Przedmioty do wygrania
items:
  - item: DIAMOND
    amount: 1
    weight: 10      # Im mniejsza waga, tym rzadszy
    name: "&b&lDiament"
    lore:
      - "&7Rzadki diament!"
    glow: true

# Profile rotacji
rotation:
  profiles:
    day1:
      description: "Dzień diamentów!"
      boosts:
        - item: DIAMOND
          multiplier: 3.0  # 3x większa szansa
    
    day2:
      description: "Specjalne przedmioty!"
      override:  # Całkowicie inne przedmioty
        - item: NETHERITE_INGOT
          amount: 1
          weight: 50
```

### Lokalizacje - `locations.yml`

```yaml
cases:
  example:
    world: world
    x: 100
    y: 65
    z: 200
    hologram:
      enabled: true
```

## 🎮 Komendy

| Komenda | Opis | Uprawnienia |
|---------|------|-------------|
| `/meklascase help` | Wyświetla pomoc | - |
| `/meklascase create <nazwa> [typ]` | Tworzy skrzynkę | `meklascase.admin` |
| `/meklascase delete <nazwa>` | Usuwa skrzynkę | `meklascase.admin` |
| `/meklascase setlocation <nazwa>` | Ustawia lokalizację | `meklascase.admin` |
| `/meklascase removelocation <nazwa>` | Usuwa lokalizację | `meklascase.admin` |
| `/meklascase give <gracz> <case> <ilość>` | Daje klucze | `meklascase.admin` |
| `/meklascase giveall <case> <ilość>` | Daje klucze wszystkim | `meklascase.admin` |
| `/meklascase reload` | Przeładowuje plugin | `meklascase.admin` |
| `/meklascase enable <nazwa>` | Włącza skrzynkę | `meklascase.admin` |
| `/meklascase disable <nazwa>` | Wyłącza skrzynkę | `meklascase.admin` |
| `/meklascase rotate now` | Wymusza rotację | `meklascase.rotate.admin` |
| `/meklascase info <case>` | Informacje o skrzynce | `meklascase.admin` |

**Aliasy**: `/mcase`, `/case`

## 🔐 Uprawnienia

| Uprawnienie | Opis | Domyślnie |
|-------------|------|-----------|
| `meklascase.use` | Używanie skrzynek | `true` |
| `meklascase.admin` | Wszystkie komendy admin | `op` |
| `meklascase.rotate.admin` | Wymuszanie rotacji | `op` |
| `meklascase.boost.admin` | Zarządzanie boostami | `op` |

## 🎯 Jak używać?

### Dla Graczy
1. **Zdobądź klucz** od administratora
2. **Znajdź skrzynkę** na mapie (z hologramem)
3. **Kliknij PPM** z kluczem w ręce
4. **Oglądaj animację** i ciesz się nagrodą!
5. **Shift+PPM** = szybkie otwarcie bez animacji

### Dla Administratorów

#### Tworzenie skrzynki:
```
/meklascase create mojaskrzynka LOOTBOX
/meklascase setlocation mojaskrzynka  (patrz na blok)
```

#### Dawanie kluczy:
```
/meklascase give gracz123 mojaskrzynka 5
/meklascase giveall mojaskrzynka 1
```

#### Zarządzanie:
```
/meklascase info mojaskrzynka
/meklascase rotate now
/meklascase reload
```

## 🎨 System Kolorów

Plugin używa pięknych gradientów i kolorów:
- 🟢 **Zielony gradient**: Sukces, wygrane
- 🔴 **Czerwony gradient**: Błędy, TOP DROP
- 🟡 **Złoty gradient**: Specjalne przedmioty, czas
- 🔵 **Niebieski gradient**: Informacje, opisy
- 🟣 **Fioletowy gradient**: Ostrzeżenia, boosty

## 🏗️ Struktura Plików

```
plugins/meklasCase/
├── config.yml           # Główna konfiguracja
├── locations.yml        # Lokalizacje skrzynek
├── rotation_state.yml   # Stan rotacji (auto)
└── cases/              # Konfiguracje skrzynek
    ├── example.yml
    ├── premium.yml
    └── ...
```

## 🔧 Zaawansowane Funkcje

### Wagi Przedmiotów
- **Im mniejsza waga, tym rzadszy przedmiot**
- Przykład: waga 5 = 5% szansy, waga 50 = 50% szansy
- Boosty mnożą wagę (boost x3 = 3x większa szansa)

### Profile Rotacji
- **Boosts**: Zwiększają szanse na konkretne przedmioty
- **Override**: Całkowicie zastępują standardowe przedmioty
- **Cykliczność**: Profile zmieniają się cyklicznie

### Hologramy
- **Automatyczne**: Tworzą się nad każdą skrzynką
- **Aktualizacja na żywo**: Czas, boosty, TOP DROP
- **Wyłączanie**: `hologram.enabled: false` w locations.yml

## 🐛 Rozwiązywanie Problemów

### Plugin się nie ładuje?
- Sprawdź wersję Javy (wymagana 17+)
- Sprawdź wersję serwera (Paper/Spigot 1.20.6+)
- Sprawdź logi w konsoli

### Hologramy nie działają?
- Zainstaluj HolographicDisplays
- Sprawdź uprawnienia pluginu
- Przeładuj plugin: `/meklascase reload`

### Skrzynka nie działa?
- Sprawdź czy jest włączona: `/meklascase info nazwa`
- Sprawdź lokalizację: `/meklascase setlocation nazwa`
- Sprawdź uprawnienia gracza: `meklascase.use`

## 📊 Statystyki i Monitoring

Plugin automatycznie zapisuje:
- **Stany rotacji**: Kiedy ostatnia rotacja, aktywny profil
- **TOP DROP**: Ostatnie najlepsze wygrane
- **Konfiguracje**: Wszystko w plikach YAML

## 🎉 Przykładowy Gameplay

1. **Rano (04:00)**: Rotacja! Nowy profil "Dzień Diamentów"
2. **Hologram**: Pokazuje boost x3 na diamenty
3. **Gracz**: Używa klucza, widzi piękną animację
4. **Wygrana**: Diament z boosted szansą!
5. **TOP DROP**: Jeśli to najrzadszy przedmiot, wszyscy widzą ogłoszenie
6. **Następny dzień**: Nowy profil, nowe szanse!

## 💝 Wsparcie

- **Autor**: meklas
- **Wersja**: 1.0.0
- **Licencja**: MIT
- **Java**: 17+
- **Minecraft**: 1.20.6-1.21

---

**Enjoy your beautiful cases! 🎰✨**