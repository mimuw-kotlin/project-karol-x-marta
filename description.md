# Mastermind

## Authors
- Karol Baciński (@karolus162 on GitHub)
- Marta Hering-Zagrocka (@mhz47 on GitHub)

## Description
Mastermind to prosta gra planszowa, w której gracz próbuje odgadnąć ukryty kod składający się z kolorowych pionków. W podstawowej wersji gry gracz będzie miał do dyspozycji 8 kolorów, z których będzie mógł wybrać 4, aby stworzyć swój kod. Po każdym ruchu gracz będzie otrzymywał informacje zwrotne, które pomogą mu odgadnąć kod. Gra kończy się, gdy gracz odgadnie kod lub skończy się mu liczba prób.

## Game v1
- single player mode
- several options for game settings
- GUI
- terminal mode
- tests
- database with time scores

## Libraries
- Compose (zgodnie z sugestią skorzystaliśmy z compose zamiast TornadoFX)
- JUnit
- kotlinx
- sqlite-jdbc

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