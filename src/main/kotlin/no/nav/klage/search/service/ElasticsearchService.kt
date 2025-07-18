package no.nav.klage.search.service

import no.nav.klage.kodeverk.FlowState
import no.nav.klage.kodeverk.SattPaaVentReason
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.search.domain.*
import no.nav.klage.search.domain.elasticsearch.EsBehandling
import no.nav.klage.search.domain.elasticsearch.EsStatus
import no.nav.klage.search.domain.elasticsearch.EsStatus.*
import no.nav.klage.search.domain.saksbehandler.Saksbehandler
import no.nav.klage.search.repositories.AnonymeBehandlingerSearchHits
import no.nav.klage.search.repositories.BehandlingerSearchHits
import no.nav.klage.search.repositories.EsBehandlingRepository
import no.nav.klage.search.repositories.SearchHits
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getMedian
import no.nav.klage.search.util.getTeamLogger
import org.opensearch.common.unit.TimeValue
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.index.query.TermQueryBuilder
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.SortBuilders
import org.opensearch.search.sort.SortOrder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.lang.System.currentTimeMillis
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit


open class ElasticsearchService(private val esBehandlingRepository: EsBehandlingRepository) :
    ApplicationListener<ContextRefreshedEvent> {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()

        private const val ISO8601 = "yyyy-MM-dd"
        private const val ZONEID_UTC = "Z"
    }

    fun recreateIndex() {
        esBehandlingRepository.recreateIndex()
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            esBehandlingRepository.createIndex()
        } catch (e: Exception) {
            logger.error("Unable to initialize OpenSearch", e)
        }
    }

    fun save(klagebehandling: EsBehandling) {
        logger.debug("Skal indeksere fra kabal-search, klage med id ${klagebehandling.behandlingId}")
        esBehandlingRepository.save(klagebehandling)
    }

    open fun findOppgaverOmPersonByCriteria(criteria: OppgaverOmPersonSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findLedigeOppgaverByCriteria(criteria: LedigeOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findLedigeROLOppgaverByCriteria(criteria: LedigeOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toROLEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlersFerdigstilteOppgaverByCriteria(criteria: FerdigstilteOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findROLsReturnerteOppgaverByCriteria(criteria: ReturnerteROLOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toROLEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlersUferdigeOppgaverByCriteria(criteria: UferdigeOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findROLsUferdigeOppgaverByCriteria(criteria: UferdigeOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toROLEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlersOppgaverPaaVentByCriteria(criteria: OppgaverPaaVentSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findEnhetensFerdigstilteOppgaverByCriteria(criteria: EnhetensFerdigstilteOppgaverSearchCriteria): AnonymeBehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits.anonymize()
    }

    open fun findKrolsReturnerteOppgaverByCriteria(criteria: KrolsReturnerteOppgaverSearchCriteria): AnonymeBehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits.anonymize()
    }

    open fun findEnhetensOppgaverPaaVentByCriteria(criteria: EnhetensOppgaverPaaVentSearchCriteria): AnonymeBehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits.anonymize()
    }

    open fun findEnhetensUferdigeOppgaverByCriteria(criteria: EnhetensUferdigeOppgaverSearchCriteria): AnonymeBehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits.anonymize()
    }

    open fun findKrolsUferdigeOppgaverByCriteria(criteria: KrolsUferdigeOppgaverSearchCriteria): AnonymeBehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits.anonymize()
    }

    open fun findSaksbehandlereByEnhetCriteria(criteria: SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria): Set<Saksbehandler> {
        val searchHits: SearchHits<EsBehandling> = esBehandlingRepository.search(criteria.toEsQuery())

        return searchHits.map {
            Saksbehandler(
                navIdent = it.content.tildeltSaksbehandlerident
                    ?: throw RuntimeException("tildeltSaksbehandlerident is null. Can't happen"),
                navn = it.content.tildeltSaksbehandlernavn ?: "Navn mangler"
            )
        }.toSet()
    }

    open fun findMedunderskrivereByEnhetCriteria(criteria: SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria): Set<Saksbehandler> {
        val searchHits: SearchHits<EsBehandling> = esBehandlingRepository.search(criteria.toEsQuery())

        return searchHits.mapNotNull {
            if (it.content.medunderskriverident == null) {
                null
            } else {
                Saksbehandler(
                    navIdent = it.content.medunderskriverident,
                    navn = it.content.medunderskriverNavn ?: "Navn mangler"
                )
            }
        }.toSet()
    }

    open fun findROLListByEnhetCriteria(criteria: ROLListSearchCriteria): Set<Saksbehandler> {
        val searchHits: SearchHits<EsBehandling> = esBehandlingRepository.search(criteria.toEsQuery())

        return searchHits.mapNotNull {
            if (it.content.rolIdent == null) {
                null
            } else {
                Saksbehandler(
                    navIdent = it.content.rolIdent,
                    navn = it.content.rolNavn ?: "Navn mangler"
                )
            }
        }.toSet()
    }

    open fun countIkkeTildelt(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countIkkeTildelt.name) {
            countByStatusYtelseAndType(IKKE_TILDELT, ytelse, type)
        }
    }

    open fun countTildelt(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countTildelt.name) {
            countByStatusYtelseAndType(TILDELT, ytelse, type)
        }
    }

    open fun countSendtTilMedunderskriver(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countSendtTilMedunderskriver.name) {
            countByStatusYtelseAndType(SENDT_TIL_MEDUNDERSKRIVER, ytelse, type)
        }
    }

    open fun countMedunderskriverValgt(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countMedunderskriverValgt.name) {
            countByStatusYtelseAndType(MEDUNDERSKRIVER_VALGT, ytelse, type)
        }
    }

    open fun countReturnertTilSaksbehandler(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countReturnertTilSaksbehandler.name) {
            countByStatusYtelseAndType(RETURNERT_TIL_SAKSBEHANDLER, ytelse, type)
        }
    }

    open fun countAvsluttet(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countAvsluttet.name) {
            countByStatusYtelseAndType(FULLFOERT, ytelse, type)
        }
    }

    open fun countSattPaaVent(ytelse: Ytelse, type: Type): Long {
        return runWithTiming(method = ::countSattPaaVent.name) {
            countByStatusYtelseAndType(SATT_PAA_VENT, ytelse, type)
        }
    }

    private fun <T> runWithTiming(method: String, block: () -> T): T {
        val start = currentTimeMillis()
        try {
            return block.invoke()
        } finally {
            val end = currentTimeMillis()
            logger.debug("Time it took to call $method: ${end - start} millis")
        }
    }

    private fun countByStatusYtelseAndType(status: EsStatus, ytelse: Ytelse, type: Type): Long {
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.must(QueryBuilders.termQuery(EsBehandling::status.name, status))
        baseQuery.must(QueryBuilders.termQuery(EsBehandling::ytelseId.name, ytelse.id))
        baseQuery.must(QueryBuilders.termQuery(EsBehandling::typeId.name, type.id))
        return esBehandlingRepository.count(baseQuery)
    }

    open fun countAntallSaksdokumenterIAvsluttedeBehandlingerMedian(ytelse: Ytelse, type: Type): Double {
        val start = currentTimeMillis()
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.must(QueryBuilders.termQuery(EsBehandling::status.name, FULLFOERT))
        baseQuery.must(QueryBuilders.termQuery(EsBehandling::ytelseId.name, ytelse.id))
        baseQuery.must(QueryBuilders.termQuery(EsBehandling::typeId.name, type.id))
        val searchHits = esBehandlingRepository.search(baseQuery)
        val saksdokumenterPerAvsluttetBehandling = searchHits.map { e -> e.content }
            .map { e ->
                e.saksdokumenter.size
            }
            .toList()

        val medianStart = currentTimeMillis()
        val median = getMedian(saksdokumenterPerAvsluttetBehandling)
        val medianEnd = currentTimeMillis()
        logger.debug("Time it took to calculate median for list with ${saksdokumenterPerAvsluttetBehandling.size} elements: ${medianEnd - medianStart} millis")

        val end = currentTimeMillis()
        logger.debug("Time it took to call ${::countAntallSaksdokumenterIAvsluttedeBehandlingerMedian.name}: ${end - start} millis")
        return median
    }

    open fun countLedigeOppgaverMedUtgaattFristByCriteria(criteria: CountLedigeOppgaverMedUtgaattFristSearchCriteria): Int {
        return esBehandlingRepository.count(criteria.toEsQuery()).toInt()
    }

    private fun SearchSourceBuilder.addSorting(criteria: SortableSearchCriteria) {
        fun sortField(criteria: SortableSearchCriteria): String =
            when (criteria.sortField) {
                SortField.MOTTATT -> {
                    EsBehandling::sakMottattKaDato.name
                }

                SortField.PAA_VENT_FROM -> {
                    EsBehandling::sattPaaVent.name
                }

                SortField.PAA_VENT_TO -> {
                    EsBehandling::sattPaaVentExpires.name
                }

                SortField.AVSLUTTET_AV_SAKSBEHANDLER -> {
                    EsBehandling::avsluttetAvSaksbehandler.name
                }

                SortField.RETURNERT_FRA_ROL -> {
                    EsBehandling::returnertFraROL.name
                }

                SortField.VARSLET_FRIST -> {
                    EsBehandling::varsletFrist.name
                }

                else -> {
                    EsBehandling::frist.name
                }
            }

        fun mapOrder(criteria: SortableSearchCriteria): SortOrder {
            return criteria.order.let {
                when (it) {
                    Order.ASC -> SortOrder.ASC
                    Order.DESC -> SortOrder.DESC
                }
            }
        }

        this.sort(SortBuilders.fieldSort(sortField(criteria)).order(mapOrder(criteria)))
    }

    private fun SearchSourceBuilder.addPaging(criteria: PageableSearchCriteria) {
        this.from(criteria.offset)
        this.size(criteria.limit)
    }

    private fun OppgaverOmPersonSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)

        baseQuery.must(haveSakenGjelder(fnr))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun SaksbehandlereAndMedunderskrivereByEnhetSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)

        baseQuery.mustNot(beAvsluttetAvSaksbehandler())

        val innerQuery = QueryBuilders.boolQuery()
        innerQuery.should(beTildeltEnhet(enhet))
        innerQuery.should(beSendtTilMedunderskriverIEnhet(enhet))
        baseQuery.must(innerQuery)

        baseQuery.must(beTildeltSaksbehandler())
        baseQuery.mustNot(beFeilregistrert())

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun ROLListSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)

        baseQuery.mustNot(beAvsluttetAvSaksbehandler())

        baseQuery.must(beAssignedToROL())
        baseQuery.must(beTildeltSaksbehandler())
        baseQuery.mustNot(beFeilregistrert())

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun LedigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beTildeltSaksbehandler())
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))
        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun LedigeOppgaverSearchCriteria.toROLEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.must(beTildeltSaksbehandler())
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(beSentToROL())
        baseQuery.mustNot(beAssignedToROL())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))
        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun CountLedigeOppgaverMedUtgaattFristSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beTildeltSaksbehandler())
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))
        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun FerdigstilteOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        //baseQuery.must(beAvsluttetAvSaksbehandler())
        baseQuery.must(beAvsluttetAvSaksbehandlerEtter(ferdigstiltFom))
        baseQuery.must(beAvsluttetAvSaksbehandlerFoer(ferdigstiltTom))
        baseQuery.must(beTildeltSaksbehandler(navIdent))
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun ReturnerteROLOppgaverSearchCriteria.toROLEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.must(beReturnedFromROL())
        baseQuery.must(beReturnertFraROLEtter(returnertFom))
        baseQuery.must(beReturnertFraROLFoer(returnertTom))
        baseQuery.must(beAssignedToROL(navIdent = navIdent))
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun UferdigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beSattPaaVent())
        baseQuery.must(beTildeltSaksbehandlerOrMedunderskriver(navIdent))
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun UferdigeOppgaverSearchCriteria.toROLEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(beSentToROL())
        baseQuery.must(beAssignedToROL(navIdent = navIdent))
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun OppgaverPaaVentSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.must(beSattPaaVent())
        baseQuery.must(beTildeltSaksbehandlerOrMedunderskriver(navIdent))
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        if (sattPaaVentReasons.isNotEmpty()) {
            baseQuery.must(beSattPaaVentReasons(sattPaaVentReasons))
        }

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun EnhetensFerdigstilteOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        //baseQuery.must(beAvsluttetAvSaksbehandler())
        baseQuery.must(beAvsluttetAvSaksbehandlerEtter(ferdigstiltFom))
        baseQuery.must(beAvsluttetAvSaksbehandlerFoer(ferdigstiltTom))
        baseQuery.must(beTildeltEnhet(enhetId))
        if (saksbehandlere.isNotEmpty()) {
            baseQuery.must(beTildeltSaksbehandlere(saksbehandlere))
        }
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun EnhetensOppgaverPaaVentSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.must(beSattPaaVent())

        val innerQuery = QueryBuilders.boolQuery()

        val enhetQuery = QueryBuilders.boolQuery()
        enhetQuery.should(beTildeltEnhet(enhetId))
        enhetQuery.should(beSendtTilMedunderskriverIEnhet(enhetId))
        innerQuery.must(enhetQuery)

        if (saksbehandlere.isNotEmpty()) {
            innerQuery.must(beTildeltSaksbehandlere(saksbehandlere))
        }

        if (medunderskrivere.isNotEmpty()) {
            innerQuery.must(beSentToMedunderskriver())
            innerQuery.must(beTildeltMedunderskrivere(medunderskrivere))
        }

        if (sattPaaVentReasons.isNotEmpty()) {
            innerQuery.must(beSattPaaVentReasons(sattPaaVentReasons))
        }

        baseQuery.must(innerQuery)
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun EnhetensUferdigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beSattPaaVent())

        val innerQuery = QueryBuilders.boolQuery()

        val enhetQuery = QueryBuilders.boolQuery()
        enhetQuery.should(beTildeltEnhet(enhetId))
        enhetQuery.should(beSendtTilMedunderskriverIEnhet(enhetId))
        innerQuery.must(enhetQuery)

        if (saksbehandlere.isNotEmpty()) {
            innerQuery.must(beTildeltSaksbehandlere(saksbehandlere))
        }

        if (medunderskrivere.isNotEmpty()) {
            innerQuery.must(beSentToMedunderskriver())
            innerQuery.must(beTildeltMedunderskrivere(medunderskrivere))
        }

        if (muFlowStates.isNotEmpty()) {
            innerQuery.must(beMUFlowStates(muFlowStates))
        }

        if (rolFlowStates.isNotEmpty()) {
            innerQuery.must(beROLFlowStates(rolFlowStates))
        }

        baseQuery.must(innerQuery)
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun KrolsUferdigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.must(beSentToROL())

        if (rolList.isNotEmpty()) {
            baseQuery.must(beAssignedToROL(rolList))
        }

        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun KrolsReturnerteOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        teamLogger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.must(beReturnedFromROL())
        baseQuery.must(beReturnertFraROLEtter(returnertFom))
        baseQuery.must(beReturnertFraROLFoer(returnertTom))
        baseQuery.must(beAssignedToROL())
        baseQuery.mustNot(beFeilregistrert())
        baseQuery.must(haveFristBetween(fristFrom, fristTo))
        baseQuery.must(haveVarsletFristBetween(varsletFristFrom, varsletFristTo))

        teamLogger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun BoolQueryBuilder.addBasicFilters(basicSearchCriteria: BasicSearchCriteria) {
        if (basicSearchCriteria.typer.isNotEmpty()) {
            val innerQueryType = QueryBuilders.boolQuery()
            this.must(innerQueryType)
            basicSearchCriteria.typer.forEach {
                innerQueryType.should(QueryBuilders.termQuery(EsBehandling::typeId.name, it.id))
            }
        }

        if (basicSearchCriteria.ytelser.isNotEmpty()) {
            val innerQueryYtelse = QueryBuilders.boolQuery()
            this.must(innerQueryYtelse)
            basicSearchCriteria.ytelser.forEach {
                innerQueryYtelse.should(QueryBuilders.termQuery(EsBehandling::ytelseId.name, it.id))
            }
        }

        if (basicSearchCriteria.hjemler.isNotEmpty()) {
            val innerQueryHjemler = QueryBuilders.boolQuery()
            this.must(innerQueryHjemler)
            basicSearchCriteria.hjemler.forEach {
                innerQueryHjemler.should(QueryBuilders.termQuery(EsBehandling::hjemmelIdList.name, it.id))
            }
        }
    }

    private fun BoolQueryBuilder.addSecurityFilters(securitySearchCriteria: SecuritySearchCriteria) {
        val filterQuery = QueryBuilders.boolQuery()
        this.filter(filterQuery)

        val kanBehandleEgenAnsatt = securitySearchCriteria.kanBehandleEgenAnsatt
        val kanBehandleFortrolig = securitySearchCriteria.kanBehandleFortrolig
        val kanBehandleStrengtFortrolig = securitySearchCriteria.kanBehandleStrengtFortrolig
        when {
            kanBehandleEgenAnsatt && kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 1
                //Skipper de normale, altså de som ikke har noe.
                //fortrolig og strengt fortrolig trumfer egen ansatt
                //tolker dette som kun egen ansatt som også er strengt fortrolig eller fortrolig
                filterQuery.should(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
                filterQuery.should(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))
            }

            !kanBehandleEgenAnsatt && kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 2
                //Er i praksis det samme som case 1
                filterQuery.should(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
                filterQuery.should(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))
            }

            kanBehandleEgenAnsatt && !kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 3
                //tolker dette som kun egen ansatt som også er strengt fortrolig
                //Skipper de normale, altså de som ikke har noe.
                filterQuery.must(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))
            }

            kanBehandleEgenAnsatt && kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 4
                //Skal inkludere de normale
                //Skal inkludere egen ansatt
                //Skal inkludere fortrolig
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
            }

            !kanBehandleEgenAnsatt && !kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 5.
                //Er i praksis det samme som case 3. Inkluderer egen ansatte som også har strengt fortrolig, strengt fortrolig trumfer egen ansatt
                filterQuery.must(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))
            }

            !kanBehandleEgenAnsatt && kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 6
                //Skal inkludere de normale
                //Skal inkludere fortrolig
                //Skal inkludere fortrolige som også er egen ansatt, men ikke egen ansatte som ikke er fortrolige
                val egenAnsattAndNotFortrolig = QueryBuilders.boolQuery()
                egenAnsattAndNotFortrolig.must(QueryBuilders.termQuery(EsBehandling::egenAnsatt.name, true))
                egenAnsattAndNotFortrolig.mustNot(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))

                filterQuery.mustNot(egenAnsattAndNotFortrolig)
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
            }

            kanBehandleEgenAnsatt && !kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 7
                //Skal inkludere de normale
                //Skal inkludere egen ansatt
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))
            }

            !kanBehandleEgenAnsatt && !kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 8
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::strengtFortrolig.name, true))
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::fortrolig.name, true))
                filterQuery.mustNot(QueryBuilders.termQuery(EsBehandling::egenAnsatt.name, true))
            }
        }
    }

    private fun beSentToROL(): BoolQueryBuilder {
        val queryBeSentToROL = QueryBuilders.boolQuery()
        queryBeSentToROL.must(
            QueryBuilders.termQuery(
                EsBehandling::rolFlowStateId.name,
                FlowState.SENT.id
            )
        )
        return queryBeSentToROL
    }

    private fun beReturnedFromROL(): BoolQueryBuilder {
        val queryBeReturnedFromROL = QueryBuilders.boolQuery()
        queryBeReturnedFromROL.must(
            QueryBuilders.termQuery(
                EsBehandling::rolFlowStateId.name,
                FlowState.RETURNED.id
            )
        )
        return queryBeReturnedFromROL
    }

    private fun beAssignedToROL() = QueryBuilders.existsQuery(EsBehandling::rolIdent.name)

    private fun beAssignedToROL(navIdent: String) = QueryBuilders.termQuery(EsBehandling::rolIdent.name, navIdent)

    private fun beAvsluttetAvSaksbehandler() = QueryBuilders.existsQuery(EsBehandling::avsluttetAvSaksbehandler.name)

    private fun beFeilregistrert() = QueryBuilders.existsQuery(EsBehandling::feilregistrert.name)

    private fun beSattPaaVent() = QueryBuilders.existsQuery(EsBehandling::sattPaaVent.name)

    private fun beTildeltSaksbehandler() = QueryBuilders.existsQuery(EsBehandling::tildeltSaksbehandlerident.name)

    private fun beAvsluttetAvSaksbehandlerEtter(ferdigstiltFom: LocalDate) =
        QueryBuilders.rangeQuery(EsBehandling::avsluttetAvSaksbehandler.name).gte(ferdigstiltFom).format(ISO8601)
            .timeZone(ZONEID_UTC)

    private fun beAvsluttetAvSaksbehandlerFoer(ferdigstiltTom: LocalDate) =
        QueryBuilders.rangeQuery(EsBehandling::avsluttetAvSaksbehandler.name).lte(ferdigstiltTom).format(ISO8601)
            .timeZone(ZONEID_UTC)

    private fun beReturnertFraROLEtter(returnertFom: LocalDate) =
        QueryBuilders.rangeQuery(EsBehandling::returnertFraROL.name).gte(returnertFom).format(ISO8601)
            .timeZone(ZONEID_UTC)

    private fun beReturnertFraROLFoer(returnertTom: LocalDate) =
        QueryBuilders.rangeQuery(EsBehandling::returnertFraROL.name).lte(returnertTom).format(ISO8601)
            .timeZone(ZONEID_UTC)

    private fun haveFristBetween(fristFom: LocalDate, fristTom: LocalDate): BoolQueryBuilder {
        val innerQuery = QueryBuilders.boolQuery()

        innerQuery.should(
            QueryBuilders.rangeQuery(EsBehandling::frist.name).gte(fristFom).lte(fristTom).format(ISO8601)
                .timeZone(ZONEID_UTC)
        )
        innerQuery.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(EsBehandling::frist.name)))

        return innerQuery
    }

    private fun haveVarsletFristBetween(varsletFristFom: LocalDate, varsletFristTom: LocalDate): BoolQueryBuilder {
        val innerQuery = QueryBuilders.boolQuery()

        innerQuery.should(
            QueryBuilders.rangeQuery(EsBehandling::varsletFrist.name).gte(varsletFristFom).lte(varsletFristTom)
                .format(ISO8601)
                .timeZone(ZONEID_UTC)
        )
        innerQuery.should(
            QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(EsBehandling::varsletFrist.name))
        )

        return innerQuery
    }

    private fun beTildeltSaksbehandlere(saksbehandlere: List<String>): BoolQueryBuilder {
        val innerQuerySaksbehandler = QueryBuilders.boolQuery()
        saksbehandlere.forEach {
            innerQuerySaksbehandler.should(QueryBuilders.termQuery(EsBehandling::tildeltSaksbehandlerident.name, it))
        }
        return innerQuerySaksbehandler
    }

    private fun beAssignedToROL(rolList: List<String>): BoolQueryBuilder {
        val innerQueryRol = QueryBuilders.boolQuery()
        rolList.forEach {
            innerQueryRol.should(QueryBuilders.termQuery(EsBehandling::rolIdent.name, it))
        }
        return innerQueryRol
    }

    private fun beTildeltMedunderskrivere(medunderskrivere: List<String>): BoolQueryBuilder {
        val innerQueryMedunderskriver = QueryBuilders.boolQuery()
        medunderskrivere.forEach {
            innerQueryMedunderskriver.should(QueryBuilders.termQuery(EsBehandling::medunderskriverident.name, it))
        }
        return innerQueryMedunderskriver
    }

    private fun beSattPaaVentReasons(sattPaaVentReasons: List<SattPaaVentReason>): BoolQueryBuilder? {
        return if (sattPaaVentReasons.isNotEmpty()) {
            val innerQueryMedunderskriver = QueryBuilders.boolQuery()
            sattPaaVentReasons.forEach {
                innerQueryMedunderskriver.should(QueryBuilders.termQuery(EsBehandling::sattPaaVentReasonId.name, it.id))
            }
            innerQueryMedunderskriver
        } else null
    }

    private fun beROLFlowStates(rolFlowStates: List<FlowState>): BoolQueryBuilder? {
        return if (rolFlowStates.isNotEmpty()) {
            val innerQueryMedunderskriver = QueryBuilders.boolQuery()
            rolFlowStates.forEach {
                innerQueryMedunderskriver.should(QueryBuilders.termQuery(EsBehandling::rolFlowStateId.name, it.id))
            }
            innerQueryMedunderskriver
        } else null
    }

    private fun beMUFlowStates(muFlowStates: List<FlowState>): BoolQueryBuilder? {
        return if (muFlowStates.isNotEmpty()) {
            val innerQueryMedunderskriver = QueryBuilders.boolQuery()
            muFlowStates.forEach {
                innerQueryMedunderskriver.should(QueryBuilders.termQuery(EsBehandling::medunderskriverFlowStateId.name, it.id))
            }
            innerQueryMedunderskriver
        } else null
    }

    private fun beTildeltSaksbehandler(navIdent: String) =
        QueryBuilders.termQuery(EsBehandling::tildeltSaksbehandlerident.name, navIdent)


    private fun beTildeltMedunderskriver(navIdent: String): BoolQueryBuilder {
        val innerQueryMedunderskriver = QueryBuilders.boolQuery()
        innerQueryMedunderskriver.must(QueryBuilders.termQuery(EsBehandling::medunderskriverident.name, navIdent))
        innerQueryMedunderskriver.must(beSentToMedunderskriver())

        return innerQueryMedunderskriver
    }

    private fun beTildeltSaksbehandlerOrMedunderskriver(navIdent: String): BoolQueryBuilder {
        val innerQuery = QueryBuilders.boolQuery()
        innerQuery.should(beTildeltSaksbehandler(navIdent))
        innerQuery.should(beTildeltMedunderskriver(navIdent))
        return innerQuery
    }

    private fun beTildeltEnhet(enhetId: String): TermQueryBuilder =
        QueryBuilders.termQuery(EsBehandling::tildeltEnhet.name, enhetId)

    private fun beSentToMedunderskriver(): TermQueryBuilder =
        QueryBuilders.termQuery(
            EsBehandling::medunderskriverFlowStateId.name,
            FlowState.SENT.id
        )

    private fun beSendtTilMedunderskriverIEnhet(enhetsnummer: String): BoolQueryBuilder {
        val innerQueryMedunderskriver = QueryBuilders.boolQuery()
        innerQueryMedunderskriver.must(QueryBuilders.termQuery(EsBehandling::medunderskriverEnhet.name, enhetsnummer))
        innerQueryMedunderskriver.must(beSentToMedunderskriver())
        return innerQueryMedunderskriver
    }

    private fun haveSakenGjelder(fnr: String): TermQueryBuilder =
        QueryBuilders.termQuery(EsBehandling::sakenGjelderFnr.name, fnr)

    fun deleteBehandling(behandlingId: UUID) {
        esBehandlingRepository.deleteBehandling(behandlingId)
    }
}
