package no.nav.klage.search.service

import org.springframework.stereotype.Service

@Service
class AdminService(private val indexService: IndexService) {

    companion object {
        private const val TWO_SECONDS = 2000L
    }

    /*
    fun syncEsWithDb() {
        indexService.reindexAllKlagebehandlinger()
        Thread.sleep(TWO_SECONDS)
        indexService.findAndLogOutOfSyncKlagebehandlinger()
    }
     */

    fun deleteAllInES() {
        indexService.deleteAllKlagebehandlinger()
        Thread.sleep(TWO_SECONDS)
    }

    fun recreateEsIndex() {
        indexService.recreateIndex()
    }

    /*
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    @SchedulerLock(name = "findAndLogOutOfSyncKlagebehandlinger")
    fun findAndLogOutOfSyncKlagebehandlinger() =
        indexService.findAndLogOutOfSyncKlagebehandlinger()
    */
}