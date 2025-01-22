[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/M0kyOMLZ)
# Mastermind

## Authors
- Karol Baciński (@karolus162 on GitHub)
- Marta Hering-Zagrocka (@mhz47 on GitHub)

## Description
Mastermind to prosta gra planszowa, w której gracz próbuje odgadnąć ukryty kod składający się z kolorowych pionków. W podstawowej wersji gry gracz będzie miał do dyspozycji 8 kolorów, z których będzie mógł wybrać 4, aby stworzyć swój kod. Po każdym ruchu gracz będzie otrzymywał informacje zwrotne, które pomogą mu odgadnąć kod. Gra kończy się, gdy gracz odgadnie kod lub skończy się mu liczba prób.

## Features
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

## Game v2
- multiplayer mode
- improved GUI
- more tests

## Libraries
- Compose 
- JUnit
- kotlinx
- sqlite-jdbc
- ktLint


## How to run
Dodtkowo, można uruchamiać grę poprzez gradelw:

- Aplikacja gry z GUI:

Właściwa gra (z gui) może być włączona poprzez ./gradlew runClient. 
Domyślnie uruchomi się z założeniem, że serwer znajduje się na locahoście na porcie 12345. 
Można to zmienić argumentami uruchomienia. 

Sposób podawania argumetów: -Pargs"flaga, wartość, flaga2, wartość...".

Dostępne flagi:
  -host,
  -port.

Przykładowe wywołanie: ./gradlew runClient -Pargs="-host, 'localhost', -port, 12346"

- Serwer (dla trybu multiplayer):

Serwer można uruchomić poprzez ./gradlew runServer na domyślnym porcie 12345 lub podając port jako argument wywołania, np. ./gradlew runServer -Pargs="12346".


- Wersja terminalowa (tylko single player):

Zostawiliśmy dodatkowo wersję terminalową, którą można uruchomić poprzez ./gradlew runTerminal,
gdzie można dołączyć flagi w ten sam sposób co w przypadku GUI.

Dostępne flagi:
-l (sequence length) - od 3 do 6,
-a (max attmpts) - od 3 do 20,
-c (color list, jako string kolorów oddzielonych spacjami) - długość od 3 do 8.

Przykładowe wywołanie: ./gradlew runTerminal -Pargs="-l, 4, -a, 10, -c, red blue green yellow"
Flagi są opcjonalne, domyślne wartości to: -l 4, -a 10, -c A B C D E F.

- Testy:

Testy można uruchomić poprzez ./gradlew runTests.

#### Uruchamianie gry z poziomu IDE:
- Aplikacja gry z GUI: App.kt
- Serwer: GameServer.kt
- Aplikacja gry z terminala (tylko single player): Main.kt