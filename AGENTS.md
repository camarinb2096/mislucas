# Repository Guidelines

## Project Structure & Module Organization

This repository currently contains the project overview in `README.md` and the game specification in `docs/`. Read `docs/game.md`, `docs/rules.md`, and `docs/model.md` before changing behavior. The planned single Maven module should place production Java in `src/main/java`, JUnit tests in `src/test/java`, and future JavaFX assets in `src/main/resources`. Keep financial rules in the model; the JavaFX interface belongs to the third submission.

## Build, Test, and Development Commands

The Maven project has not been scaffolded yet, so these commands apply after `pom.xml` is added:

- `mvn test` — compile the model and run JUnit 5 tests.
- `mvn clean test` — verify the project from a clean build.
- `mvn javadoc:javadoc` — generate the Javadoc files required for submission.

Use JDK 21. Do not add a JavaFX run command until an application entry point exists.

## Coding Style & Naming Conventions

Use four spaces for Java indentation, one public top-level type per file, `PascalCase` for classes, `camelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants. Store money as integer Colombian pesos; do not use `double` for balances, interest, or returns. Keep jars nonnegative and track debt separately. Add multiline Javadoc comments to model classes and members, including parameters and outcomes where relevant. No formatter or linter is configured yet; keep formatting consistent within each package.

## Testing Guidelines

Use JUnit 5. Name test classes `*Test.java` and test methods after the rule and expected outcome, such as `rejectsAllocationAboveAllowance`. Cover normal behavior, boundaries, invalid decisions, debt installments, rounding, and an entire eight-week game with a fixed card order. No numeric coverage threshold is specified; tests must run without JavaFX.

## Commit & Pull Request Guidelines

GitHub history contains `Initial commit` and documentation commits using a `docs:` prefix; it does not yet establish a convention for code. Use short imperative subjects with an area prefix, such as `model: Validate jar withdrawals`. Name work branches with Gitflow-style prefixes and the task ID, such as `feature/T04-jar-balances`, `bugfix/T08-installments`, or `release/second-submission`. Open one pull request per GitHub Project task, write its title and description in English, link its issue, describe the rule changed, and include the verification commands and results. Update `docs/rules.md` when behavior changes. Request review from another team member; screenshots are needed only for future interface changes.
