# Emulator Czujników IoT i Generator Pomiarów (F13 & F11)

Narzędzie konsolowe emulujące autonomiczne czujniki pomiarowe IoT.
Generator przesyła wyniki wyłącznie przez publiczne API aplikacji (`POST /api/measurements` z nagłówkiem `X-API-Key`). Zgodnie z wymaganiami, klucz API czujnika oraz adres API są parametryzowane i nie są zapisane na stałe w kodzie.

## Wymagania
- Python 3.10+ (skrypt korzysta wyłącznie ze standardowych bibliotek: `urllib`, `json`, `argparse`, `math`, `time`). Nie wymaga instalowania pakietów przez `pip`.

## Tryby pracy

### 1. Inicjalizacja danych demonstracyjnych F11 (`--seed`)
Automatycznie tworzy 3 serie pomiarowe, rejestruje dla nich czujniki i przesyła po 20 pomiarów ze znacznikami czasu z przeszłości (spełnia wymóg co najmniej 3 serii po co najmniej 15 punktów):
```bash
python generator.py --seed --api http://localhost:8080 --user admin --pass Admin123!
```

### 2. Emulacja czujnika – przebieg sinusoidalny (`--mode sine`)
Generuje przebieg zmienny wg funkcji sinus w zadanym przedziale `[min, max]`:
```bash
python generator.py --api http://localhost:8080 --key <KLUCZ_API_CZUJNIKA> --mode sine --min 18.0 --max 26.0 --count 30 --interval 2.0
```

### 3. Emulacja czujnika – wartości losowe (`--mode random`)
Generuje losowe punkty pomiarowe z rozkładu jednostajnego:
```bash
python generator.py --api http://localhost:8080 --key <KLUCZ_API_CZUJNIKA> --mode random --min 0.0 --max 100.0 --count 15 --interval 1.0
```

### 4. Generowanie serii historycznej (`--historical`)
Wysyła wyniki ze znacznikami czasu z przeszłości (np. 1 punkt na godzinę wstecz):
```bash
python generator.py --api http://localhost:8080 --key <KLUCZ_API_CZUJNIKA> --mode sine --min 10.0 --max 30.0 --count 24 --interval 3600 --historical
```

### 5. Pobieranie rzeczywistych danych pogodowych na żywo (`--mode openmeteo`)
Pobiera rzeczywiste pomiary atmosferyczne dla Warszawy z publicznego serwisu Open-Meteo i przesyła je do API czujnika (rozszerzenie punktowe):
```bash
python generator.py --api http://localhost:8080 --key <KLUCZ_API_CZUJNIKA> --mode openmeteo --metric temperature --count 10 --interval 5.0
```
Dostępne metryki (`--metric`):
- `temperature` (temperatura w °C)
- `humidity` (wilgotność względna w %)
- `pressure` (ciśnienie na poziomie gruntu w hPa)

## Parametry wiersza poleceń

| Flaga | Domyślnie | Opis |
| --- | --- | --- |
| `--api` | `http://localhost:8080` (lub zmienna `API_URL`) | Adres bazowy serwera API |
| `--key` | `None` (lub zmienna `SENSOR_API_KEY`) | Klucz API czujnika otrzymany przy rejestracji |
| `--mode` | `sine` | Wzorzec generowania: `sine`, `random`, `openmeteo` |
| `--min` | `15.0` | Minimalna dopuszczalna wartość |
| `--max` | `25.0` | Maksymalna dopuszczalna wartość |
| `--count` | `20` | Liczba pomiarów do wysłania |
| `--interval`| `1.0` | Odstęp czasu w sekundach między kolejnymi pomiarami |
| `--historical`| `false` | Flaga generowania znaczników czasu z przeszłości |
| `--metric` | `temperature` | Metryka dla trybu Open-Meteo (`temperature`, `humidity`, `pressure`) |
| `--seed` | `false` | Tryb zasilania danymi F11 (wymaga podania `--user` i `--pass`) |
| `--user` | `admin` | Login administratora dla trybu `--seed` |
| `--pass` | `Admin123!` | Hasło administratora dla trybu `--seed` |
