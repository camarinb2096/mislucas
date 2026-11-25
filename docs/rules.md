# Business rules

These rules are the reference for the model and its tests. Amounts are **simulation assumptions** and may be calibrated after a complete game works.

## Setup and money

All jars start at COP 0. Store balances as integer pesos; no jar may become negative. Income allocations must be in increments of COP 1,000. Returns and installments may leave jar balances that are not multiples of COP 1,000.

Jar deposits and withdrawals accept nonnegative whole pesos. A withdrawal can equal the full available balance. Reject a withdrawal above the available balance and leave the balance unchanged.

| Scenario | Weekly allowance | Fixed expense in weeks 1 and 5 | Headphones | Sneakers | Class trip |
|---|---:|---:|---:|---:|---:|
| Tight (*Ajustado*) | COP 10,000 | COP 6,000 | COP 40,000 | COP 60,000 | COP 80,000 |
| Medium (*Medio*) | COP 20,000 | COP 12,000 | COP 80,000 | COP 120,000 | COP 160,000 |
| Comfortable (*Holgado*) | COP 40,000 | COP 24,000 | COP 160,000 | COP 240,000 | COP 320,000 |

The Normal deck contains one card per week. Shuffle it once at game start and do not repeat cards. Tests may supply a fixed order.

| Card | Tight | Medium | Comfortable |
|---|---:|---:|---:|
| Want 1 | COP 5,000 | COP 10,000 | COP 20,000 |
| Want 2 | COP 6,000 | COP 12,000 | COP 24,000 |
| Want 3 | COP 10,000 | COP 20,000 | COP 40,000 |
| Need | COP 8,000 | COP 16,000 | COP 32,000 |
| Emergency 1 | COP 8,000 | COP 16,000 | COP 32,000 |
| Emergency 2 | COP 10,000 | COP 20,000 | COP 40,000 |
| Opportunity | +COP 10,000 | +COP 20,000 | +COP 40,000 |
| Quiet week | COP 0 | COP 0 | COP 0 |

## Weekly sequence

1. Grant the weekly allowance and allocate **all of it** among the Spending (*Para gastar*), Goal (*Mi meta*), and Cushion (*Colchón*) jars. Validate all three allocations before crediting any jar. Reject negative amounts, an incorrect total, or allocations outside COP 1,000 increments. If an allocation is rejected, every jar keeps its previous balance. Apply these same rules to opportunity income.
2. Collect installments due this week after allocation and before other expenses, using Spending → Cushion → Goal. Collect overdue installments first. Any unpaid part remains due without extra interest.
3. Charge the fixed expense only in weeks 1 and 5. Finance any mandatory shortfall.
4. Reveal and resolve one card. A want is bought only if the player accepts and Spending has enough money; it never creates debt. A need or fixed expense uses Spending → Cushion → Goal. An emergency uses Cushion → Spending → Goal. Finance mandatory shortfalls. Allocate opportunity income immediately as a new income. A quiet-week card has no direct effect.
5. In weeks 4 and 8, credit Goal and Cushion with a return of 2% of each jar's balance, rounded **separately for each jar** to the nearest COP 100; round a tie upward. Spending earns no return.
6. Show balances, debt, and an explanation of the week's events. Close the game after week 8; no further turns are allowed.

The turn model starts in week 1 and accepts one successful allowance allocation per week. If installments are due, it requires collection before the next action. In weeks 1 and 5 it then charges the scenario's fixed expense once. A fixed-expense shortfall must be financed before card reveal. A revealed want requires a decision, a need or emergency requires payment and financing of any shortfall, and an opportunity requires a complete allocation. A quiet week is resolved on reveal. The week can close after the card is resolved. A rejected action preserves the phase, week, and jar balances. Closing weeks 1 through 7 starts the next week, and closing week 8 ends the game. A fixed reveal order must contain each of the eight Normal card kinds exactly once and uses the amounts for the selected scenario.

A mandatory payment reduces the available balances in priority order and reports any remaining shortfall for financing. It also reports the amount drawn from Goal for the final star calculation.

## Educational credit

When a fixed expense, need, or emergency creates a shortfall, the player chooses:

| Choice | Total owed | Due date |
|---|---|---|
| One installment | The shortfall, with no interest | Following week |
| Two installments | The shortfall plus 10% total interest, calculated once | Half in each of the next two weeks |

Round the interest to the nearest whole peso, with ties rounded up. If the total in pesos is odd, round the first installment down and assign the remaining peso to the second. Collect installments by their original due week, starting with the oldest. For installments with the same due week, collect the older debt first. A partial payment reduces the debt. Attempt to collect the unpaid part the following week without adding interest. Installments due after week 8 still count as debt at closing. Explain the financed amount, interest, and payment weeks to the player. **Example:** financing COP 8,000 costs COP 8,000 in one installment or COP 8,800 in two installments of COP 4,400.

## Savings returns

At the end of weeks 4 and 8, credit each eligible jar using its balance after all weekly payments and card effects. Record the sum of the two rounded credits as returns received. The week-4 credit remains in the jar and can contribute to the week-8 balance. A balance of COP 2,499 earns COP 0 after rounding, while a balance of COP 2,500 earns COP 100.

## Closing and outcome

After the week-8 return, pay outstanding debt from Goal until that jar is exhausted. Display any remainder as a **separate negative debt amount**; jars stay at zero or above.

- **Final net balance** = Spending + Goal + Cushion − remaining debt. A negative value means defeat.
- **Victory** requires Goal to cover the chosen price after debt settlement and a nonnegative final net balance.
- **Stars:** 0 without victory; 1 for victory; 2 if Goal was never used for mandatory expenses or installments; 3 if Cushion also ends with at least one weekly allowance for the chosen scenario. Using Goal for final debt settlement also prevents the second star.
- **Savings percentage** = (final Goal balance + final Cushion balance) ÷ (allowances + opportunities + returns received) × 100. The denominator includes returns by team decision. Round the percentage to two decimal places, with ties rounded up.

Checkable examples: 2% of COP 7,500 is COP 150, rounded to a COP 200 credit. If all three jars end at COP 0 and COP 2,000 of debt remains, the final net balance is −COP 2,000 and the player loses.
