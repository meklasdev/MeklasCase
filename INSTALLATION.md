# 🚀 Instalacja meklasCase

## 📋 Wymagania

### Serwer Minecraft
- **Paper/Spigot**: 1.20.6 - 1.21
- **Java**: 17 lub nowsza
- **RAM**: Minimum 1GB (zalecane 2GB+)

### Opcjonalne Pluginy
- **HolographicDisplays**: Dla hologramów (zalecane)

## 🔧 Kompilacja

### 1. Zainstaluj narzędzia
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk maven

# Windows
# Pobierz i zainstaluj JDK 17+ i Maven z oficjalnych stron
```

### 2. Sklonuj repozytorium
```bash
git clone <repository-url>
cd meklascase
```

### 3. Kompiluj
```bash
mvn clean package
```

### 4. Znajdź JAR
Skompilowany plik będzie w: `target/meklasCase-1.0.0.jar`

## 📦 Instalacja na serwerze

### 1. Upload
```bash
# Skopiuj JAR do folderu plugins
cp target/meklasCase-1.0.0.jar /path/to/server/plugins/
```

### 2. Restart serwera
```bash
# Zrestartuj serwer Minecraft
./start.sh  # lub jak uruchamiasz serwer
```

### 3. Sprawdź logi
```
[INFO] [meklasCase] Plugin został pomyślnie włączony!
[INFO] [meklasCase] Autor: meklas | Wersja: 1.0.0
```

## ⚙️ Pierwsza konfiguracja

### 1. Sprawdź pliki
Po pierwszym uruchomieniu sprawdź folder:
```
plugins/meklasCase/
├── config.yml
├── locations.yml
├── rotation_state.yml
└── cases/
    └── example.yml
```

### 2. Utwórz pierwszą skrzynkę
```
/meklascase create testowa LOOTBOX
/meklascase setlocation testowa
/meklascase give [nick] testowa 5
```

### 3. Sprawdź działanie
- Znajdź blok gdzie ustawiłeś skrzynkę
- Kliknij PPM z kluczem w ręce
- Ciesz się animacją! 🎰

## 🔍 Rozwiązywanie problemów

### Plugin nie startuje
```bash
# Sprawdź wersję Javy
java -version

# Sprawdź logi serwera
tail -f logs/latest.log
```

### Błędy kompilacji
```bash
# Wyczyść cache Maven
mvn clean

# Sprawdź dependencje
mvn dependency:tree

# Przebuduj
mvn clean compile package
```

### Hologramy nie działają
```bash
# Zainstaluj HolographicDisplays
# Pobierz z: https://dev.bukkit.org/projects/holographicdisplays

# Sprawdź czy jest załadowany
/plugins

# Przeładuj meklasCase
/meklascase reload
```

## 🎯 Quick Start

### Dla niecierpliwych:
```bash
# 1. Kompiluj
mvn clean package

# 2. Upload JAR
cp target/*.jar /server/plugins/

# 3. Restart serwera

# 4. Utwórz skrzynkę
/meklascase create mojaskrzynka LOOTBOX
/meklascase setlocation mojaskrzynka
/meklascase give gracz123 mojaskrzynka 1

# 5. Gotowe! 🎉
```

## 📞 Wsparcie

Jeśli masz problemy:
1. Sprawdź logi serwera
2. Sprawdź wersję Javy i serwera
3. Sprawdź czy wszystkie dependencje są zainstalowane
4. Przeczytaj dokumentację w README_PLUGIN.md

---

**Powodzenia z instalacją! 🎰✨**