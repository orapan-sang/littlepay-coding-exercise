# lp-exercise

## How to build & run
**Build in terminal**

Command: `./gradlew clean build`

**Run in terminal**

Command: `./gradlew run`

## How to run tests

**Run in terminal**

Command: `./gradlew test`

## Assumption
- Credit card and bus fare are in the same currency.
- Now supports only one route (from stopId 1 to 3) and all buses from any companies are using the same fare rules.
- Taps in taps.csv will be in chronological order and trips will be exported in chronological order too.
- For a valid trip, tap ON must happen before tap OFF and are on the same day.
- If at the same stop, tap ON and OFF happen at the same time, consider it as a CANCELLED trip.