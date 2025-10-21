# Rivals for Catan - Introductory Game (Refactored)

## 📋 Overview

This is a fully refactored, object-oriented implementation of the **Rivals for Catan Introductory Game** in Java. The codebase follows SOLID principles, Booch metrics, and implements key design patterns for maintainability, extensibility, and testability.

### Key Features
- ✅ Complete introductory game (94 cards, 2 players)
- ✅ Full dice logic (production + event dice)
- ✅ All event handling
- ✅ Victory condition checking (7 VP)
- ✅ Local and online multiplayer support
- ✅ Clean architecture with design patterns

---

## 🏗️ Architecture Summary

### Package Structure

```
com.catan.rivals/
├── game/           # Game engine and core logic
│   ├── GameEngine.java
│   ├── GamePhase.java
│   ├── GameSetup.java
│   ├── Dice.java
│   └── TurnTemplate.java
├── model/          # Domain models
│   ├── Card.java
│   ├── CardType.java
│   ├── CardEffect.java
│   ├── Deck.java
│   ├── ResourceType.java
│   ├── ResourceBank.java
│   ├── Principality.java
│   └── VictoryCondition.java
├── player/         # Player abstractions
│   ├── Player.java
│   ├── HumanPlayer.java
│   ├── AIPlayer.java
│   └── RemotePlayerProxy.java
├── net/            # Networking
│   ├── GameServer.java
│   └── Connection.java
└── util/           # Utilities
    ├── CardFactory.java
    ├── JsonUtils.java
    └── Observer.java
```

---

## 🎨 Design Patterns Used

### 1. **Strategy Pattern** (Card Effects)
- **Purpose**: Different cards have different effects when played
- **Implementation**: `CardEffect` interface with concrete strategies
- **Benefits**: Easy to add new card types without modifying existing code
- **Location**: `com.catan.rivals.model.CardEffect`

```java
public interface CardEffect {
    boolean apply(Card card, Player activePlayer, Player opponent, int row, int col);
    boolean canApply(Card card, Player activePlayer, Player opponent, int row, int col);
}
```

### 2. **Factory Pattern** (Card Creation)
- **Purpose**: Centralized card creation from JSON
- **Implementation**: `CardFactory` creates cards from JSON data
- **Benefits**: Single point of card creation, easy to modify card loading logic
- **Location**: `com.catan.rivals.util.CardFactory`

```java
public static Deck loadCardsFromJson(String jsonPath) throws IOException
```

### 3. **Observer Pattern** (Event Notifications)
- **Purpose**: Notify observers of game events
- **Implementation**: `Observer` interface for event handling
- **Benefits**: Decoupled event handling, easy to add new observers
- **Location**: `com.catan.rivals.util.Observer`

```java
public interface Observer {
    void onEvent(String event, Object data);
}
```

### 4. **Template Method Pattern** (Turn Execution)
- **Purpose**: Define the skeleton of turn execution
- **Implementation**: `TurnTemplate` with hook methods
- **Benefits**: Consistent turn flow, customizable steps
- **Location**: `com.catan.rivals.game.TurnTemplate`

### 5. **State Pattern** (Game Phases) - Optional
- **Purpose**: Manage game phase transitions
- **Implementation**: `GamePhase` enum/classes
- **Benefits**: Clear phase management, easy to add new phases
- **Location**: `com.catan.rivals.game.GamePhase`

---

## 🔧 SOLID Principles Compliance

### Single Responsibility Principle (SRP)
- ✅ `Card`: Represents card data only
- ✅ `CardFactory`: Creates cards only
- ✅ `ResourceBank`: Manages resources only
- ✅ `VictoryCondition`: Checks victory only
- ✅ Each class has one reason to change

### Open-Closed Principle (OCP)
- ✅ `CardEffect` interface allows new effects without modifying existing code
- ✅ `Observer` interface allows new event handlers without changes
- ✅ Open for extension, closed for modification

### Liskov Substitution Principle (LSP)
- ✅ `HumanPlayer`, `AIPlayer`, `RemotePlayerProxy` all substitute `Player`
- ✅ All `CardEffect` implementations are interchangeable
- ✅ Subtypes maintain supertype contracts

### Interface Segregation Principle (ISP)
- ✅ `CardEffect` has only methods related to card effects
- ✅ `Observer` has only event notification methods
- ✅ Clients not forced to implement unused methods

### Dependency Inversion Principle (DIP)
- ✅ `GameEngine` depends on `Player` abstraction, not concrete players
- ✅ `Card` depends on `CardEffect` interface, not concrete effects
- ✅ High-level modules depend on abstractions

---

## 📊 Booch Metrics

### Coupling (Low)
- ✅ Packages have minimal dependencies
- ✅ Interfaces used to decouple modules
- ✅ `game` package doesn't depend on `net` package

### Cohesion (High)
- ✅ Each class has related responsibilities
- ✅ `ResourceBank` handles all resource operations
- ✅ `Principality` handles all board operations

### Sufficiency
- ✅ All required functionality for introductory game is present
- ✅ Complete game mechanics implemented

### Completeness
- ✅ All components present and interact correctly
- ✅ No missing functionality for base game

### Primitiveness
- ✅ Complex operations built from simple primitives
- ✅ `payCost()` uses `canAfford()` + `removeResources()`

---

## 🎯 Software Quality Attributes

### Modifiability (High)
- Design patterns make changes easy
- Adding new cards: implement `CardEffect`
- Adding new events: implement `Observer`

### Extensibility (High)
- Expansion sets can be added without changing core
- New player types can extend `Player`
- New victory conditions via `VictoryCondition` extension

### Testability (High)
- Interfaces allow mock implementations
- Pure functions for calculations
- Dependency injection for testing

---

## 🚀 How to Compile and Run

### Prerequisites
- Java 8 or higher
- `gson.jar` in the project root (for JSON parsing)
- `cards.json` with card data

### Compilation

```bash
# Compile all Java files
javac -cp ".:gson.jar" -d bin src/com/catan/rivals/**/*.java

# Or use the provided compile script
chmod +x compile.sh
./compile.sh
```

### Running Locally (2 Players, Same Computer)

```bash
# Run with console players
java -cp ".:gson.jar:bin" com.catan.rivals.game.GameEngine

# Run with one bot player
java -cp ".:gson.jar:bin" com.catan.rivals.game.GameEngine bot
```

### Running Online (Client-Server)

```bash
# Terminal 1: Start server
java -cp ".:gson.jar:bin" com.catan.rivals.net.GameServer

# Terminal 2: Start client
java -cp ".:gson.jar:bin" com.catan.rivals.net.GameClient
```

---

## 🧪 Testing and Extensibility

### Unit Tests (JUnit 5)

```bash
# Run tests
java -cp ".:gson.jar:junit-5.jar:bin" org.junit.runner.JUnitCore com.catan.rivals.test.AllTests
```

### Test Coverage
- ✅ Card loading and deck setup
- ✅ Dice rolls and production
- ✅ Event handling (Brigand, Trade, Celebration, Plentiful Harvest)
- ✅ Card play and action phase
- ✅ Victory condition (≥7 VP)

### Adding New Features

#### Add a New Card Effect
```java
public class MyCustomEffect implements CardEffect {
    @Override
    public boolean apply(Card card, Player player, Player opponent, int row, int col) {
        // Your custom logic
        return true;
    }
    
    @Override
    public boolean canApply(Card card, Player player, Player opponent, int row, int col) {
        // Your validation logic
        return true;
    }
}
```

#### Add a New Event Handler
```java
public class MyEventHandler implements Observer {
    @Override
    public void onEvent(String event, Object data) {
        if ("MY_EVENT".equals(event)) {
            // Handle event
        }
    }
}
```

---

## 📁 Project Structure

```
rivals-for-catan/
├── README.md
├── gson.jar
├── cards.json
├── pom.xml (optional, for Maven users)
├── compile.sh
├── run.sh
├── src/
│   └── com/catan/rivals/
│       ├── game/
│       ├── model/
│       ├── player/
│       ├── net/
│       └── util/
├── test/
│   └── com/catan/rivals/
│       ├── GameEngineTest.java
│       └── CardTest.java
└── bin/ (compiled classes)
```

---

## 🎮 Game Rules Summary

### Setup
- 2 players, each starts with 2 settlements, 1 road, 6 regions
- Initial hand: 3 cards from draw stacks
- Goal: Reach 7 victory points

### Turn Structure
1. **Roll Dice**: Production die + Event die
2. **Production**: Regions produce resources (stored on cards, max 3)
3. **Event**: Handle event die result
4. **Actions**: Play cards, build, trade
5. **Replenish**: Draw cards to hand limit (3 + progress points)
6. **Exchange**: Optional card exchange

### Victory Points
- Base VP from cards (settlements, cities, buildings)
- +1 VP for trade advantage (≥3 commerce points ahead)
- +1 VP for strength advantage (≥3 strength points ahead)

---

## 📝 License

This is an educational refactoring project for the d7032e course.
The original "Rivals for Catan" game is © Kosmos/Catan GmbH.

---

## 👥 Contributors

- Refactored by: [Your Name]
- Course: d7032e Software Architecture
- Date: 2025

---

## 🔗 References

- [Rivals for Catan Official Rules](https://www.catan.com/rivals-catan)
- [Original Repository](https://github.com/Spooky-Firefox/d7032e_home_exam)
- [Design Patterns](https://refactoring.guru/design-patterns)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)

---

## 🆘 Troubleshooting

### Common Issues

**Cards not loading:**
- Ensure `cards.json` is in the project root
- Check JSON format is valid
- Verify `gson.jar` is in classpath

**Networking issues:**
- Check firewall settings
- Ensure port 2048 is available
- Verify both client and server use same protocol

**Compilation errors:**
- Verify Java 8+ is installed
- Check all source files are present
- Ensure proper package structure

---

## 📧 Contact

For questions or issues, please refer to the course materials or contact the instructor.
