# Plan: Prevent Visit Scheduling on Sundays

## Context

The vet clinic is closed on Sundays. Currently `VisitController.processNewVisitForm` accepts any `LocalDate` for a visit, including Sundays. This plan adds a guard to reject Sunday bookings and display an error message back to the user.

## Recommendation: BindingResult check in VisitController

### Why not a custom @Constraint annotation?

- No custom `@Constraint` annotations exist anywhere in the project. The only precedent for programmatic validation is `PetValidator` (a Spring `Validator`).
- Sunday-checking is domain logic specific to visit scheduling—it won't be reused on other entities.
- A custom annotation requires two new files (annotation + `ConstraintValidator` impl) for a one-liner rule.
- The task is explicitly described as a "quick feature addition".

### Why BindingResult check wins?

- The pattern already exists in `VisitController.processNewVisitForm` — `result` is already in scope.
- One `if` block + one `result.rejectValue(...)` call. Zero new files.
- Consistent with how `PetValidator` works (programmatic `errors.rejectValue`).
- Easy to read, easy to test.

---

## Files to Modify

| File | Change |
|------|--------|
| `src/test/kotlin/org/springframework/samples/petclinic/owner/VisitControllerTest.kt` | **Step 1 (TDD red):** Add failing Sunday test |
| `src/main/kotlin/org/springframework/samples/petclinic/owner/VisitController.kt` | **Step 2 (TDD green):** Add Sunday check to make test pass |

---

## TDD Steps

### Step 1 — Write the failing test (red)

Add to `VisitControllerTest.kt`:

```kotlin
@Test
fun testProcessNewVisitFormRejectsSunday() {
    val nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
    mockMvc.perform(
        post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
            .param("description", "Sunday visit attempt")
            .param("date", nextSunday.toString())
    )
        .andExpect(model().attributeHasFieldErrors("visit", "date"))
        .andExpect(status().isOk)
        .andExpect(view().name("pets/createOrUpdateVisitForm"))
}
```

Imports to add at the top of `VisitControllerTest.kt`:
```kotlin
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
```

Run `./gradlew test --tests VisitControllerTest` — the new test must **fail** at this point.

---

### Step 2 — Implement the fix (green)

In `VisitController.kt`, update `processNewVisitForm` to add a Sunday check **before** inspecting `result.hasErrors()`:

```kotlin
@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
fun processNewVisitForm(@Valid visit: Visit, result: BindingResult): String {
    if (visit.date.dayOfWeek == DayOfWeek.SUNDAY) {
        result.rejectValue("date", "sunday", "Visits cannot be scheduled on Sundays")
    }
    return if (result.hasErrors()) {
        "pets/createOrUpdateVisitForm"
    } else {
        visits.save(visit)
        "redirect:/owners/{ownerId}"
    }
}
```

Import to add:
```kotlin
import java.time.DayOfWeek
```

Run `./gradlew test --tests VisitControllerTest` — all tests must now **pass**.

---

## Verification

1. **Red phase:** `./gradlew test --tests VisitControllerTest` — `testProcessNewVisitFormRejectsSunday` fails.
2. **Green phase:** After implementing the fix, rerun — all tests pass.
3. **Manual smoke test:** `./gradlew bootRun`, navigate to add a visit, pick a Sunday — expect a date field error. Pick a weekday — expect redirect to owner page.

---

## Output Artefact

Per project conventions, once approved this plan will be saved to `docs/plans/001-sunday-visits.md`.
