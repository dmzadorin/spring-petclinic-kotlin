package org.springframework.samples.petclinic.visit

import org.springframework.validation.Errors
import org.springframework.validation.Validator
import java.time.DayOfWeek

class VisitValidator : Validator {

    override fun supports(clazz: Class<*>) = Visit::class.java.isAssignableFrom(clazz)

    override fun validate(obj: Any, errors: Errors) {
        val visit = obj as Visit
        if (visit.date.dayOfWeek == DayOfWeek.SUNDAY) {
            errors.rejectValue("date", "sunday.not.allowed", "Visits on Sundays are not allowed")
        }
    }
}
