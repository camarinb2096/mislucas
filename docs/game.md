# Game type, actor, and flow

## Type and purpose

Mis Lucas is a **single-player educational turn-based simulation**. Each turn represents one week; a game lasts eight weeks. The player practices budgeting, distinguishing needs from wants, saving toward a goal, building an emergency reserve, and comparing the cost of financing an expense. Money and returns are fictional; the game does not represent a real bank account or credit card.

The intended audience is teenagers aged 12 to 16 in Colombia. The third submission will add a JavaFX desktop interface. In the second submission, the Java model must work without a graphical interface.

## Actor and system behavior

| Role | Actions or responsibilities |
|---|---|
| Player | Chooses a resource scenario and savings goal; allocates each income; decides whether to buy optional items; chooses one or two installments when mandatory expenses exceed available funds; reviews feedback and the final outcome. |
| Game system | Grants allowances, shuffles and reveals cards, validates choices, charges expenses and installments, calculates returns, tracks debt, and determines the outcome and stars. |

The **player is the only external actor** in the game. The instructor and development team assess or build the project but do not participate in a game. There are no user accounts, multiplayer mode, or connections to financial services.

## Game flow

1. **Setup:** choose a resource scenario and savings goal. Normal difficulty uses an eight-card deck.
2. **Weeks 1–8:** receive an allowance, allocate it among the Spending jar (*Para gastar*), Goal jar (*Mi meta*), and Cushion jar (*Colchón*); pay installments and expenses; resolve one card; and see an explanation. Opportunity income is allocated immediately. Returns are credited in weeks 4 and 8.
3. **Outcome:** settle outstanding debt from the Goal jar, check whether the savings goal was met, and show jar balances, remaining debt, savings percentage, and stars.

The future interface has three planned screens: setup, weekly game, and outcome. The model owns the rules; visual controls only collect actions and display results.

## Scope of the second submission

Three scenarios, three goals, three jars, the Normal deck, payments, educational credit, returns, final outcome, JUnit tests, and Javadoc. Easy and Hard difficulties, more player profiles, saved games, and visual effects are outside this submission's minimum scope.
