package no.nav.klage.search.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)

fun getTeamLogger(): Logger = LoggerFactory.getLogger("team-logs")

fun getAuditLogger(): Logger = LoggerFactory.getLogger("audit")