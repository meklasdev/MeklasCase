
---

# meklasCase

meklasCase to plugin crate dla Paper/Spigot 1.20.6–1.21.
Umożliwia tworzenie skrzynek z kluczami, animacjami, rotacją dropów co 24h i integracją z fHolo.

Funkcje

Klasyczne skrzynki typu LOOTBOX i LUCKBLOCK.

Rotacja dropów co 24h: zmiana listy nagród albo zmiana szans.

System boost dnia: wybrane itemy mają większą szansę i są wyświetlane w hologramie.

Hologramy z fHolo automatycznie nad skrzynkami:

nazwa i typ skrzynki,

aktualny boost dnia,

czas do końca rotacji,

ostatni TOP DROP.


Animacja spin w inventory (27 slotów) lub szybkie otwarcie (Shift+PPM).

Broadcast wygranych i zmian profilu dnia.

Wszystko w YAML, bez bazy danych.



---

Instalacja

1. Pobierz jar meklasCase i wrzuć do folderu plugins/.


2. Zrestartuj serwer.


3. Skonfiguruj pliki w plugins/meklasCase/.


4. Jeśli chcesz hologramy, zainstaluj fHolo.




---

Pliki konfiguracyjne

config.yml

resetAtFixedTime: true
fixedTime: "04:00"
windowHours: 24

quickOpen: true

sounds:
  noKey: ENTITY_VILLAGER_NO
  spin: UI_BUTTON_CLICK
  win: ENTITY_PLAYER_LEVELUP

broadcast:
  enabled: true
  messages:
    win: "{player} wygrał {item} x{amount} z {case}"
    top: "{player} pobił TOP DROP! {item} x{amount}"
    rotation: "Nowy dzień! Dziś wysoka szansa na {boost_item}"


---

cases/example.yml

type: LOOTBOX
key:
  material: TRIPWIRE_HOOK
  name: "&aKlucz do ExampleCase"
  lore:
    - "&7Użyj na skrzynce"
  glow: true

items:
  - item: DIAMOND
    amount: 1
    weight: 10
  - item: EMERALD
    amount: 2
    weight: 30
  - item: GOLD_INGOT
    amount: 8
    weight: 60

rotation:
  profiles:
    day1:
      description: "Dziś diamenty lecą częściej!"
      boosts:
        - item: DIAMOND
          multiplier: 3.0
    day2:
      description: "Zielony dzień"
      boosts:
        - item: EMERALD
          multiplier: 2.0
    day3:
      override:
        - item: ENCHANTED_GOLDEN_APPLE
          amount: 1
          weight: 100


---

locations.yml

cases:
  example:
    world: world
    x: 100
    y: 65
    z: 200
    hologram:
      enabled: true


---

rotation_state.yml

cases:
  example:
    lastRotationAt: 2025-08-17T04:00:00Z
    activeProfile: day1
    lastTopDrop: "DIAMOND x2"


---

Komendy

Komenda	Opis

/meklascase create <nazwa>	Tworzy skrzynkę
/meklascase delete <nazwa>	Usuwa skrzynkę
/meklascase setlocation <nazwa>	Ustawia blok jako skrzynkę
/meklascase removelocation <nazwa>	Usuwa lokalizację skrzynki
/meklascase give <gracz> <case> <ilość>	Daje klucze graczowi
/meklascase giveall <case> <ilość>	Daje klucze wszystkim
/meklascase reload	Przeładowuje plugin
/meklascase enable <nazwa>	Włącza skrzynkę
/meklascase disable <nazwa>	Wyłącza skrzynkę
/meklascase rotate now	Wymusza rotację
/meklascase boost set <case> <profil> <item> <multiplier>	Ustawia boost dnia
/meklascase info <case>	Pokazuje aktywny profil i czas do końca



---

Uprawnienia

Permission	Opis

meklascase.admin	Dostęp do wszystkich komend
meklascase.rotate.admin	Wymuszanie rotacji
meklascase.boost.admin	Zarządzanie boostami



---

Logika rotacji

fixedTime: reset o stałej godzinie serwera (np. 04:00).

windowHours: reset co 24h od ostatniego.

Po resecie aktywuje się kolejny profil dnia.

Profil dnia może podbić szanse (multiplier) albo całkiem nadpisać tabelę.



---

Hologramy z fHolo

Domyślne linie hologramu:

1. Nazwa skrzynki


2. Typ skrzynki


3. Boost dnia: np. “Dziś wysoka szansa na DIAMOND x3”


4. Czas do końca: “Pozostało 12:34:56”


5. Ostatni TOP DROP



Placeholdery: {case}, {type}, {boost_item}, {boost_mult}, {time_left}, {top_item}, {top_amount}.


---

Edge Case

Brak klucza: komunikat + dźwięk.

Brak miejsca w ekwipunku: item dropi obok gracza.

Brak fHolo: plugin działa, tylko bez hologramów.

Zmiana czasu systemowego: plugin używa UTC.



---

Efekt końcowy

Gracze używają kluczy do skrzynek.

Co 24h zmienia się profil dnia: boost na konkretne itemy albo nowa pula nagród.

Hologram pokazuje boost dnia i czas do końca.

Prosty system YAML, zero bazy danych.



---

Chcesz, żebym ci do tego README dorzucił jeszcze sekcję z przykładowym gameplay flow (jak gracz widzi cały proces w praktyce, krok po kroku)?

