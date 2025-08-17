# 🎭 Animacje Hologramów meklasCase

Plugin oferuje zaawansowane animowane hologramy z wieloma spektakularnymi efektami wizualnymi!

## ✨ Dostępne Efekty

### 🌈 Rainbow Text
```
Nazwa skrzynki wyświetlana w tęczowych kolorach z płynną animacją
M - czerwony → pomarańczowy
e - pomarańczowy → żółty  
k - żółty → zielony
l - zielony → cyjan
a - cyjan → niebieski
s - niebieski → fioletowy
```

### ⭐ Particle Field
```
✨ ⭐ 🌟 💫 ✦ ✧ ✩ ✪ ✫ ✬ ✭ ✮ ✯ ✰
Losowo rozmieszczone animowane cząsteczki w różnych kolorach
```

### 🔥 Fire Effects (dla boostów)
```
🔥 🔥🔥 🔥🔥🔥 🔥🔥 🔥
▓▒░ BOOST AKTYWNY ░▒▓
Intensywny efekt ognia z neon glow dla aktywnych boostów
```

### 🎯 Glitch Effects (specjalne przedmioty)
```
S█EC▓AL▒E ░RZ▐DM█OT▓
Efekt zakłócenia dla override items z losowymi glitch charakterami
```

### 💎 TOP DROP Effects
```
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
💎 T O P   D R O P 💎
D~i~a~m~e~n~t~ ~x~2
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
```

### ⏰ Time Progress Bar
```
🕐 Rotacja za: 12:34:56
████████████████░░░░ 80%
Animowany pasek postępu z zmieniającymi się kolorami
```

### 🌌 Constellation Effects
```
✦   ✧     ✩ ✪   ✫
  ✬ ✭   ✮     ✯ ✰
Gwiazdy tworzące animowane konstelacje
```

### 💻 Digital Rain (urgentne wiadomości)
```
0 1 A F 3 7 B E 2 9 C 4
Matrix-style cyfrowy deszcz dla pilnych powiadomień
```

### 🌊 Wave Animation
```
R O T A C J A   W K R Ó T C E
Tekst poruszający się jak fala z różnymi kolorami
```

## ⚙️ Konfiguracja Efektów

### config.yml
```yaml
holograms:
  enabled: true
  animations:
    enabled: true
    updateInterval: 10  # ticks (0.5 sekundy)
    effects:
      rainbow: true      # Rainbow tekst dla nazw
      particles: true    # Floating particles
      fire: true         # Fire efekty dla boostów
      glitch: true       # Glitch dla override items
      neon: true         # Neon glow efekty
      constellation: true # Konstelacje gwiazd
      digitalRain: true  # Matrix rain
      pulsingBorder: true # Pulsujące ramki
      waveAnimation: true # Wave animacja
```

## 🎮 Komendy Zarządzania

### Sprawdzanie statusu efektów
```bash
/meklascase hologram effects
```
Wyświetla:
```
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
        Status efektów hologramów
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬

rainbow: ✓ Włączony
particles: ✓ Włączony
fire: ✓ Włączony
glitch: ✓ Włączony
neon: ✓ Włączony
constellation: ✓ Włączony
digitalRain: ✓ Włączony
pulsingBorder: ✓ Włączony
waveAnimation: ✓ Włączony

Animacje: ✓
Interwał aktualizacji: 10 ticków
```

### Zarządzanie hologramami
```bash
/meklascase hologram toggle przykład    # Włącz/wyłącz hologram
/meklascase hologram reload             # Przeładuj wszystkie
```

## 🎨 Przykłady Animowanych Hologramów

### Standardowa Skrzynka
```
✨ P r z y k ł a d ✨
📦 LOOTBOX

🔥🔥🔥
▓▒░ BOOST AKTYWNY ░▒▓
Dziś diamenty lecą częściej!

🕒 Rotacja za: 23:45:12
████████████████████ 100%

✦   ✧     ✩ ✪   ✫

💎 Czekamy na TOP DROP...

👆 Kliknij prawym przyciskiem!
🔑 Potrzebujesz klucza

✨ ⭐ 🌟 💫 ✦ ✧ ✩ ✪ ✫
✦   ✧     ✩ ✪   ✫
▲ ▼ ▲
```

### Skrzynka z TOP DROP
```
🌟 S u p e r C a s e 🌟
📋 LUCKBLOCK

S█EC▓AL▒E ░RZ▐DM█OT▓
⭐ Specjalne przedmioty na dziś!

⚠️ [|] ⚠️
0 1 A F 3 7 B E 2 9 C 4
R█OT▓AC▒A ░WK▐RÓT█CE

▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
💎 T O P   D R O P 💎
D~i~a~m~e~n~t~ ~P~r~e~m~i~u~m~ ~x~1
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬

☝️ Kliknij prawym przyciskiem!
🗝️ Potrzebujesz klucza

💫 ✦ ✧ 🌟 ✩ ✪ ✫ ⭐ ✬
    ✦ ✧     ✩ ✪   ✫
◆ ◇ ◆
```

## 🔧 Optymalizacja Performance

### Ustawienia wydajności
```yaml
holograms:
  animations:
    updateInterval: 20  # Wolniej = mniej lag (1 sekunda)
    effects:
      # Wyłącz ciężkie efekty na słabszych serwerach
      particles: false
      digitalRain: false
      glitch: false
```

### Rekomendacje
- **Małe serwery** (< 50 graczy): updateInterval: 20
- **Średnie serwery** (50-100 graczy): updateInterval: 15  
- **Duże serwery** (100+ graczy): updateInterval: 10
- **Potężne serwery**: updateInterval: 5 (ultra smooth)

## 🎪 Specjalne Efekty dla Różnych Stanów

### 🔥 Boost Aktywny
```
🔥 🔥🔥 🔥🔥🔥 🔥🔥 🔥
▓▒░ BOOST AKTYWNY ░▒▓
Fire animation + Neon glow + Pulsing colors
```

### ⭐ Override Items
```
S█EC▓AL▒E ░RZ▐DM█OT▓
Glitch effect + Rainbow text + Star particles
```

### ⚠️ Rotacja Wkrótce
```
0 1 A F 3 7 B E 2 9 C 4
⚠️ [/] ⚠️
R█OT▓AC▒A ░WK▐RÓT█CE
Digital rain + Spinner + Glitch + Urgent blinking
```

### 💎 TOP DROP
```
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
💎 T O P   D R O P 💎
D~i~a~m~e~n~t~ ~x~2
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
Pulsing borders + Rainbow text + Wave animation + Diamond effects
```

## 🎯 Tips & Tricks

### Wyłączanie lagowych efektów
```yaml
effects:
  particles: false     # Wyłącz jeśli lag
  digitalRain: false   # Ciężki efekt
  glitch: false        # Może powodować lag
```

### Dostosowanie do motywu serwera
```yaml
effects:
  # Serwer fantasy
  constellation: true
  particles: true
  
  # Serwer sci-fi  
  digitalRain: true
  glitch: true
  neon: true
  
  # Serwer klasyczny
  rainbow: true
  waveAnimation: true
  pulsingBorder: true
```

---

**Ciesz się spektakularnymi animowanymi hologramami! 🎭✨**