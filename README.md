[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/M0kyOMLZ)
# Mastermind

## Authors
- Karol Baciński (@karolus162 on GitHub)
- Marta Hering-Zagrocka (@mhz47 on GitHub)

## Description
Mastermind to prosta gra planszowa, w której gracz próbuje odgadnąć ukryty kod składający się z kolorowych pionków. W podstawowej wersji gry gracz będzie miał do dyspozycji 8 kolorów, z których będzie mógł wybrać 4, aby stworzyć swój kod. Po każdym ruchu gracz będzie otrzymywał informacje zwrotne, które pomogą mu odgadnąć kod. Gra kończy się, gdy gracz odgadnie kod lub skończy się mu liczba prób.

## Features (released and planned)
- single player mode
- several difficulty levels
- multiplayer 1v1 web mode
- GUI

## Game v1
- single player mode
- several options for game settings
- GUI
- terminal mode
- tests
- database with time scores

## Game v2 - plan
W drugiej części dodamy możliwość gry wieloosobowej, w której gracze będą mogli rywalizować ze sobą na sieciowo, dodatkowe poziomy trudności i dopracujemy wygląd.

## Libraries
- Compose 
- JUnit
- kotlinx
- sqlite-jdbc
- ktor (planowane w Game v2)


## How to run
Właściwa gra (z gui) może być włączona poprzez ./gradlew run.

Zostawiliśmy dodatkowo wersję terminalową, którą można uruchomić poprzez ./gradlew runTerminal,
gdzie można dołączyć flagi w sposób -Pargs"flaga, wartość, flaga2, wartość...".

Dostępne flagi:
-l (sequence length) - od 3 do 6,
-a (max attmpts) - od 3 do 20,
-c (color list, jako string kolorów oddzielonych spacjami) - długość od 3 do 8.

Przykładowe wywołanie: ./gradlew runTerminal -Pargs="-l, 4, -a, 10, -c, red blue green yellow"
Flagi są opcjonalne, domyślne wartości to: -l 4, -a 10, -c A B C D E F.

Uruchamianie gry w wersji terminalowej ze względów estetycznych zalecamy jednak z poziomu IDE.

Testy można uruchomić poprzez ./gradlew runTests.