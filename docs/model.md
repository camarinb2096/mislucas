# Object-oriented model

The second submission implements the game domain as one Maven module using Java 21 and JUnit 5. Domain classes do not depend on JavaFX. The third submission's interface will call model operations and display their results.

## Package structure

The domain lives under `org.mislucas.model`. Domain tests use corresponding packages under `src/test/java`.

| Subpackage | Responsibility |
|---|---|
| `config` | Shared game parameters, resource scenarios, and savings goals. |
| `game` | Game state, turn progression, jars, and integer-peso amounts. |
| `events` | Normal-deck card data, card kinds and types, and deck creation and shuffling. |
| `rules` | Financial rules: income allocation, expense payment, credit and installments, returns, and outcome calculation. |
| `result` | The immutable final game result and its indicators. |

The third submission applies Model–View–Controller (MVC) to the JavaFX interface. Classes in `org.mislucas.controller` will translate player actions into model calls. Screens in `org.mislucas.view` will render the resulting state and feedback. The model owns the game rules and runs independently of JavaFX.

## Domain responsibilities

| Element | Responsibility |
|---|---|
| `GameParameters` | Defines shared constants, including the number of weeks in a game. |
| `Scenario` and `Goal` | Provide the allowance, fixed expense, card amounts, and selected goal price. |
| `Money` and `Jar` | Represent nonnegative whole-peso amounts and validate jar deposits and withdrawals. Each game has Spending, Goal, and Cushion jars. |
| `Game` | Owns the setup, week and phase, jars, deck, income total, Goal-jar usage, and final result. It enforces the turn order and delegates calculations to rule objects. |
| `Card`, `CardKind`, and `CardType` | Represent card data and classify its event. `Game` dispatches resolution by card type; the current model uses enums and a switch, not card subclasses or polymorphic effects. |
| `NormalDeck` | Builds one card of each Normal kind, either in a supplied order for repeatable runs or shuffled once for a new game. |
| `IncomeAllocation` and `ExpensePayment` | Validate and apply income allocations, and pay expenses according to their priorities. |
| `CreditAccount`, `Debt`, and `Installment` | Track financed shortfalls, interest, due weeks, collections, and outstanding balances. |
| `ReturnCredit` | Applies and records the scheduled savings returns. |
| `GameOutcome` | Calculates victory, stars, net balance, and savings percentage after debt settlement. |
| `GameResult` | Exposes the final jar balances, remaining debt, net balance, goal status, victory, stars, and savings percentage. |

The player is the only external actor, so the model does not need an account or authentication class.

## Invariants and test seams

- Store money as integer Colombian pesos; do not use `double` for balances, interest, or returns.
- Jars cannot be negative. Debt is tracked separately.
- Reject an invalid choice before changing state; a rejected allocation leaves every jar unchanged.
- Allow a fixed card order in tests. A normal game shuffles the deck once.
- A complete eight-week game, including credit and outcome, must be testable without JavaFX.
- Document model classes and members with multiline Javadoc comments, and generate the required Javadoc files.
