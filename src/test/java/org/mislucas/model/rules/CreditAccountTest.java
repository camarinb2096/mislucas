package org.mislucas.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mislucas.model.game.Jar;
import org.mislucas.model.game.JarType;
import org.mislucas.model.game.Money;

class CreditAccountTest {
    private final Jar spending = new Jar(JarType.SPENDING);
    private final Jar goal = new Jar(JarType.GOAL);
    private final Jar cushion = new Jar(JarType.CUSHION);
    private final CreditAccount account = new CreditAccount(new ExpensePayment(spending, goal, cushion));

    @Test
    void financesEightThousandInOneInstallmentWithoutInterest() {
        Debt debt = account.finance(3, new Money(8_000), CreditPlan.ONE_INSTALLMENT);

        assertEquals(CreditPlan.ONE_INSTALLMENT, debt.plan());
        assertEquals(new Money(8_000), debt.principal());
        assertEquals(new Money(0), debt.interest());
        assertSchedule(debt, List.of(4), List.of(new Money(8_000)));
        assertEquals(new Money(8_000), account.outstandingBalance());
    }

    @Test
    void prioritizesDueWeekAcrossDebtsBeforeTheirCreationOrder() {
        Debt first = account.finance(1, new Money(8_000), CreditPlan.TWO_INSTALLMENTS);
        Debt second = account.finance(1, new Money(2_000), CreditPlan.ONE_INSTALLMENT);
        spending.deposit(new Money(5_400));

        account.collectDueInstallments(3);

        assertEquals(new Money(0), first.installments().getFirst().remaining());
        assertEquals(new Money(1_000), second.outstandingBalance());
        assertEquals(new Money(4_400), first.installments().getLast().remaining());
        assertEquals(new Money(0), spending.balance());
    }

    @Test
    void financesEightThousandInTwoWeeklyInstallmentsWithTenPercentInterest() {
        Debt debt = account.finance(3, new Money(8_000), CreditPlan.TWO_INSTALLMENTS);

        assertEquals(CreditPlan.TWO_INSTALLMENTS, debt.plan());
        assertEquals(new Money(800), debt.interest());
        assertSchedule(debt, List.of(4, 5), List.of(new Money(4_400), new Money(4_400)));
        assertEquals(new Money(8_800), debt.outstandingBalance());
    }

    @Test
    void roundsHalfPesoInterestUpAndPutsTheExtraPesoInTheSecondInstallment() {
        Debt debt = account.finance(1, new Money(8_015), CreditPlan.TWO_INSTALLMENTS);

        assertEquals(new Money(802), debt.interest());
        assertSchedule(debt, List.of(2, 3), List.of(new Money(4_408), new Money(4_409)));
    }

    @Test
    void doesNotCollectFutureInstallmentsAndPaysDueOnesInJarPriorityOrder() {
        Debt debt = account.finance(1, new Money(8_000), CreditPlan.TWO_INSTALLMENTS);
        spending.deposit(new Money(1_000));
        cushion.deposit(new Money(2_000));
        goal.deposit(new Money(5_000));

        assertFalse(account.hasDueInstallments(1));
        assertEquals(new Money(0), account.collectDueInstallments(1));
        assertEquals(new Money(8_800), debt.outstandingBalance());

        assertTrue(account.hasDueInstallments(2));
        assertEquals(new Money(1_400), account.collectDueInstallments(2));
        assertEquals(new Money(0), spending.balance());
        assertEquals(new Money(0), cushion.balance());
        assertEquals(new Money(3_600), goal.balance());
        assertEquals(new Money(4_400), debt.outstandingBalance());
        assertFalse(account.hasDueInstallments(2));
        assertTrue(account.hasDueInstallments(3));
    }

    @Test
    void retriesAnOverduePartialPaymentBeforeNewInstallmentsWithoutExtraInterest() {
        Debt older = account.finance(1, new Money(8_000), CreditPlan.TWO_INSTALLMENTS);
        Debt newer = account.finance(2, new Money(5_000), CreditPlan.ONE_INSTALLMENT);
        spending.deposit(new Money(3_000));

        account.collectDueInstallments(2);

        assertEquals(new Money(1_400), older.installments().getFirst().remaining());
        assertEquals(new Money(10_800), account.outstandingBalance());
        assertEquals(new Money(0), spending.balance());
        assertEquals(new Money(0), cushion.balance());
        assertEquals(new Money(0), goal.balance());

        cushion.deposit(new Money(2_000));
        account.collectDueInstallments(3);

        assertEquals(new Money(0), older.installments().getFirst().remaining());
        assertEquals(new Money(3_800), older.installments().getLast().remaining());
        assertEquals(new Money(5_000), newer.outstandingBalance());
        assertEquals(new Money(800), older.interest());

        goal.deposit(new Money(8_800));
        assertEquals(new Money(8_800), account.collectDueInstallments(4));
        assertEquals(new Money(0), account.outstandingBalance());
        assertFalse(account.hasDueInstallments(4));
    }

    @Test
    void keepsLateInstallmentsInTheOutstandingBalanceAtTheEndOfWeekEight() {
        Debt weekSeven = account.finance(7, new Money(8_000), CreditPlan.TWO_INSTALLMENTS);
        Debt weekEight = account.finance(8, new Money(5_000), CreditPlan.TWO_INSTALLMENTS);
        spending.deposit(new Money(6_000));

        account.collectDueInstallments(8);

        assertSchedule(weekSeven, List.of(8, 9), List.of(new Money(4_400), new Money(4_400)));
        assertSchedule(weekEight, List.of(9, 10), List.of(new Money(2_750), new Money(2_750)));
        assertEquals(new Money(9_900), account.outstandingBalance());
        assertEquals(new Money(1_600), spending.balance());
        assertFalse(account.hasDueInstallments(8));
    }

    @Test
    void settlesFutureInstallmentsFromGoalOnlyAndKeepsTheUnpaidPortion() {
        Debt debt = account.finance(8, new Money(8_000), CreditPlan.TWO_INSTALLMENTS);
        goal.deposit(new Money(6_000));
        spending.deposit(new Money(12_000));
        cushion.deposit(new Money(2_000));

        assertEquals(new Money(6_000), account.settleFromGoal());

        assertEquals(new Money(0), goal.balance());
        assertEquals(new Money(12_000), spending.balance());
        assertEquals(new Money(2_000), cushion.balance());
        assertEquals(new Money(0), debt.installments().getFirst().remaining());
        assertEquals(new Money(2_800), debt.installments().getLast().remaining());
        assertEquals(new Money(2_800), account.outstandingBalance());
    }

    @Test
    void stopsSettlingWhenTheDebtIsFullyPaid() {
        account.finance(8, new Money(1_000), CreditPlan.ONE_INSTALLMENT);
        goal.deposit(new Money(41_000));

        assertEquals(new Money(1_000), account.settleFromGoal());
        assertEquals(new Money(0), account.settleFromGoal());
        assertEquals(new Money(40_000), goal.balance());
        assertEquals(new Money(0), account.outstandingBalance());
    }

    @Test
    void rejectsInvalidFinancingWithoutAddingDebt() {
        assertThrows(IllegalArgumentException.class, () -> account.finance(0, new Money(8_000), CreditPlan.ONE_INSTALLMENT));
        assertThrows(IllegalArgumentException.class, () -> account.finance(2, new Money(0), CreditPlan.ONE_INSTALLMENT));
        assertThrows(NullPointerException.class, () -> account.finance(2, new Money(8_000), null));
        assertThrows(ArithmeticException.class, () ->
                account.finance(2, new Money(Integer.MAX_VALUE), CreditPlan.TWO_INSTALLMENTS));
        assertEquals(new Money(0), account.outstandingBalance());
    }

    private static void assertSchedule(Debt debt, List<Integer> dueWeeks, List<Money> amounts) {
        assertEquals(dueWeeks, debt.installments().stream().map(Installment::dueWeek).toList());
        assertEquals(amounts, debt.installments().stream().map(Installment::amount).toList());
    }
}
