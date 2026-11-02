package no.nav.klage.search.api.controller

import io.swagger.v3.oas.annotations.Hidden
import no.nav.klage.search.util.getLogger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Hidden
class FeatureToggleController {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/featuretoggle/{toggleName}")
    fun getToggle(
        @PathVariable("toggleName") toggleName: String,
    ): Boolean = false

    @GetMapping("/aapenfeaturetoggle/{toggleName}")
    fun getUnprotectedToggle(
        @PathVariable("toggleName") toggleName: String,
    ): Boolean = false
}
