# Object-oriented model

The second submission implements the game domain as **one Maven module using Java 21**, with JUnit 5 tests. Domain classes do not import JavaFX. The third submission's interface will call model operations and display their results.

## Package structure

The domain lives under `org.mislucas.model`. Domain tests use corresponding packages under `src/test/java`.

| Subpackage | Responsibility |
|---|---|
| `config` | Game parameters, resource scenarios, and savings goals. |
| `game` | Game state, turn progression, jars, and money. |
| `events` | Cards and their effects. |
| `rules` | Coordination of financial rules such as allocation, payments, and returns. |
| `result` | The completed game's outcome and indicators. |

The third submission applies Model–View–Controller (MVC) to the JavaFX interface. Classes in `org.mislucas.controller` will translate player actions into model calls. Screens in `org.mislucas.view` will render the resulting state and feedback. The model owns the game rules and can run independently of JavaFX.

## Domain responsibilities

| Element | Responsibility |
|---|---|
| `Scenario` and `Goal` | Provide the allowance, fixed expense, and selected goal price. |
| `Jar` | Encapsulate a nonnegative balance and validate deposits and withdrawals. Each game has Spending, Goal, and Cushion jars. |
| `Game` | Hold setup, current week and phase, jars, deck, received income, Goal jar usage, and final state. Reject actions out of order. |
| `Card` and its types | Represent and resolve want, need, emergency, opportunity, and quiet-week events. Distinct effects justify polymorphism. |
| `Debt` and `Installment` | Track financed shortfalls, payment choice, total interest, due weeks, and outstanding balance. |
| `RulesEngine` | Coordinate allocation, payment priorities, installment collection, returns, and closing without depending on the interface. |
| `GameResult` | Expose balances, remaining debt, net balance, victory, stars, and savings percentage. |

The player is an actor, so no account or authentication class is needed.

## Invariants and test seams

- Store money as integer Colombian pesos; do not use `double` for balances, interest, or returns.
- Jars cannot be negative. Debt is tracked separately.
- Reject an invalid choice before changing state; a rejected allocation changes no jar.
- Allow a fixed card order in tests. A normal game shuffles the deck once.
- A complete eight-week game, including credit and outcome, must be testable without JavaFX.
- Model classes and members need multiline documentation comments, and the required Javadoc files must be generated.
