package no.nav.klage.search.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdminService(
    private val indexService: IndexService,
) {
    fun recreateEsIndex() {
        indexService.recreateIndex()
    }

    fun deleteBehandling(behandlingId: UUID) {
        indexService.deleteBehandling(behandlingId)
    }
}
