# AdventOfCode

This is a collection of my Advent of Code solutions. Each year may have a different build structure/language.

## 2022 (Kotlin)

To build/run solutions from 2022, Gradle is used.

### Build:
```
cd 2022
./gradlew build
```

### Run:
```
cd 2022
./gradlew run
```

By default, the runner will execute each day in sequence. To run an individual day, pass an integer representing the day to run as a command line argument.
For more details on the various execution configurations, see the documentation on the main function in [AdventRunner.kt](/2022/src/main/kotlin/advent/AdventRunner.kt)
