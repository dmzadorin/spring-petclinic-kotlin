# Project: Spring PetClinic Kotlin

## Core Commands
- **Build**: `./gradlew clean build -x test`
- **Test**: `./gradlew test`
- **Specific Test**: `./gradlew test --tests {TestClassName}`

## Architecture & Style
- **Framework**: Spring Boot 4 + Kotlin 2.3
- **Injection**: Use Constructor Injection.
- **Data**: Spring Data JPA.
- **Conventions**:
    - Prefer `val` (immutable).
    - Use standard Spring stereotypes (@Controller, @Service).

## Planning Workflow (IMPORTANT)
- **Docs**: Store all design decisions in `docs/plans/`.
- **Process**:
    1. Create a markdown plan.
    2. Wait for user confirmation.
    3. Write a failing test.
    4. Implement code.
- **Mandatory Output**: 
  - When asked to create a plan or technical design, you MUST copy the approved plan file in the `docs/plans/` directory. 
  - Create the directory if it does not exist. Do not just print the plan to the console.
