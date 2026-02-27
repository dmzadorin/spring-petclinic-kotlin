# Plan: Prevent Visit Creation on Sundays (LVA-2490)

## Context
Users can currently schedule pet clinic visits on any day of the week. The requirement is to block Sunday bookings. The ticket asked to decide between a custom Jakarta `@Constraint` annotation or a `BindingResult` check in `VisitController`.

## Decision: Spring Validator (matches existing PetValidator pattern)

The codebase already uses the Spring `Validator` interface in `PetValidator` (`src/main/kotlin/org/springframework/samples/petclinic/owner/PetValidator.kt`), registered via `@InitBinder` in `PetController`. A dedicated `VisitValidator` is the simplest and most consistent approach.

A custom Jakarta `@Constraint` annotation was ruled out — it introduces a new pattern for a single-field rule with no added benefit.

## Files Changed

| File | Change |
|------|--------|
| `src/main/kotlin/org/springframework/samples/petclinic/visit/VisitValidator.kt` | **Created** — validates that visit date is not a Sunday |
| `src/main/kotlin/org/springframework/samples/petclinic/owner/VisitController.kt` | **Modified** — registers `VisitValidator` via `@InitBinder("visit")` |
| `src/test/kotlin/org/springframework/samples/petclinic/owner/VisitControllerTest.kt` | **Modified** — added Sunday-rejection and weekday-success test cases |

## Implementation

### VisitValidator
```kotlin
class VisitValidator : Validator {
    override fun supports(clazz: Class<*>) = Visit::class.java.isAssignableFrom(clazz)

    override fun validate(obj: Any, errors: Errors) {
        val visit = obj as Visit
        if (visit.date.dayOfWeek == DayOfWeek.SUNDAY) {
            errors.rejectValue("date", "sunday.not.allowed", "Visits on Sundays are not allowed")
        }
    }
}
```

### VisitController registration
```kotlin
@InitBinder("visit")
fun initVisitBinder(dataBinder: WebDataBinder) {
    dataBinder.addValidators(VisitValidator())
}
```

`addValidators` (not `validator =`) preserves existing `@Valid` / `@NotEmpty` bean validation on the `description` field.

## Verification
```bash
./gradlew test --tests VisitControllerTest
./gradlew clean build -x test
```
