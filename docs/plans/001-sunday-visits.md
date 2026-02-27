# Plan: LVA-2490 — Prevent Visit Creation on Sundays

## Context

**Ticket:** [LVA-2490](https://deliveryhero.atlassian.net/browse/LVA-2490)
**Problem:** Users can currently schedule pet clinic visits on any day of the week, including Sundays. The clinic is closed on Sundays and visits must not be allowed on that day.
**Goal:** Return a validation error when a user submits a visit form with a Sunday date; all other dates should continue to work normally.

---

## Approach: BindingResult check in VisitController

The ticket asked for the simplest approach. A `BindingResult` check in the controller was chosen over a custom `@Constraint` annotation because:
- Only 2 files need to change (controller + test)
- Consistent with the existing `PetValidator` pattern of using `errors.rejectValue()`
- No new classes required

---

## Files Modified

- `src/main/kotlin/org/springframework/samples/petclinic/owner/VisitController.kt` — added Sunday guard in `processNewVisitForm`
- `src/test/kotlin/org/springframework/samples/petclinic/owner/VisitControllerTest.kt` — added `testProcessNewVisitFormOnSundayFails`

---

## Implementation

### VisitController.kt

Added to `processNewVisitForm` before the save:

```kotlin
if (visit.date.dayOfWeek == DayOfWeek.SUNDAY) {
    result.rejectValue("date", "sunday.visit", "Visits cannot be scheduled on Sundays")
}
```

### VisitControllerTest.kt

Added failing test (written first, confirmed RED before implementation):

```kotlin
@Test
fun testProcessNewVisitFormOnSundayFails() {
    val sunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
    mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
            .param("date", sunday.toString())
            .param("description", "Sunday visit")
    )
            .andExpect(model().attributeHasFieldErrors("visit", "date"))
            .andExpect(status().isOk)
            .andExpect(view().name("pets/createOrUpdateVisitForm"))
}
```
