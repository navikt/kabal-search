package no.nav.klage.search.service

import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.search.domain.*
import no.nav.klage.search.domain.elasticsearch.*
import no.nav.klage.search.domain.elasticsearch.EsStatus.*
import no.nav.klage.search.domain.saksbehandler.Saksbehandler
import no.nav.klage.search.repositories.AnonymeBehandlingerSearchHits
import no.nav.klage.search.repositories.BehandlingerSearchHits
import no.nav.klage.search.repositories.EsBehandlingRepository
import no.nav.klage.search.repositories.SearchHits
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getMedian
import org.opensearch.common.unit.TimeValue
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.index.query.TermQueryBuilder
import org.opensearch.search.aggregations.AggregationBuilder
import org.opensearch.search.aggregations.AggregationBuilders
import org.opensearch.search.aggregations.bucket.range.ParsedDateRange
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.SortBuilders
import org.opensearch.search.sort.SortOrder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


open class ElasticsearchService(private val esBehandlingRepository: EsBehandlingRepository) :
    ApplicationListener<ContextRefreshedEvent> {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

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

    fun save(klagebehandlinger: List<EsBehandling>) {
        esBehandlingRepository.save(klagebehandlinger)
    }

    fun save(klagebehandling: EsBehandling) {
        logger.debug("Skal indeksere fra kabal-search, klage med id ${klagebehandling.id}")
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

    open fun findOppgaveByBehandlingId(criteria: BehandlingIdSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlersFerdigstilteOppgaverByCriteria(criteria: SaksbehandlersFerdigstilteOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlersUferdigeOppgaverByCriteria(criteria: SaksbehandlersUferdigeOppgaverSearchCriteria): BehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.addPaging(criteria)
        searchSourceBuilder.addSorting(criteria)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))

        val searchHits = esBehandlingRepository.search(searchSourceBuilder)
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlersOppgaverPaaVentByCriteria(criteria: SaksbehandlersOppgaverPaaVentSearchCriteria): BehandlingerSearchHits {
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

    open fun findSaksbehandlereByEnhetCriteria(criteria: SaksbehandlereByEnhetSearchCriteria): SortedSet<Saksbehandler> {
        val searchHits: SearchHits<EsBehandling> = esBehandlingRepository.search(criteria.toEsQuery())

        //Sort results by etternavn
        return searchHits.map {
            Saksbehandler(
                navIdent = it.content.tildeltSaksbehandlerident
                    ?: throw RuntimeException("tildeltSaksbehandlerident is null. Can't happen"),
                navn = it.content.tildeltSaksbehandlernavn ?: "Navn mangler"
            )
        }.toSortedSet(compareBy<Saksbehandler> { it.navn.split(" ").last() })
    }

    open fun countIkkeTildelt(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(IKKE_TILDELT, ytelse, type)
    }

    open fun countTildelt(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(TILDELT, ytelse, type)
    }

    open fun countSendtTilMedunderskriver(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(SENDT_TIL_MEDUNDERSKRIVER, ytelse, type)
    }

    open fun countMedunderskriverValgt(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(MEDUNDERSKRIVER_VALGT, ytelse, type)
    }

    open fun countReturnertTilSaksbehandler(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(RETURNERT_TIL_SAKSBEHANDLER, ytelse, type)
    }

    open fun countAvsluttet(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(FULLFOERT, ytelse, type)
    }

    open fun countSattPaaVent(ytelse: Ytelse, type: Type): Long {
        return countByStatusYtelseAndType(SATT_PAA_VENT, ytelse, type)
    }

    private fun countByStatusYtelseAndType(status: EsStatus, ytelse: Ytelse, type: Type): Long {
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.must(QueryBuilders.termQuery("status", status))
        baseQuery.must(QueryBuilders.termQuery("ytelseId", ytelse.id))
        baseQuery.must(QueryBuilders.termQuery("type", type.id))
        return esBehandlingRepository.count(baseQuery)
    }

    open fun countAntallSaksdokumenterIAvsluttedeBehandlingerMedian(ytelse: Ytelse, type: Type): Double {
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.must(QueryBuilders.termQuery("status", FULLFOERT))
        baseQuery.must(QueryBuilders.termQuery("ytelseId", ytelse.id))
        baseQuery.must(QueryBuilders.termQuery("type", type.id))
        val searchHits = esBehandlingRepository.search(baseQuery)
        val saksdokumenterPerAvsluttetBehandling = searchHits.map { e -> e.content }
            .map { e ->
                e.saksdokumenter.size
            }
            .toList()

        return getMedian(saksdokumenterPerAvsluttetBehandling)
    }

    open fun countLedigeOppgaverMedUtgaattFristByCriteria(criteria: CountLedigeOppgaverMedUtgaattFristSearchCriteria): Int {
        return esBehandlingRepository.count(criteria.toEsQuery()).toInt()
    }

    private fun SearchSourceBuilder.addSorting(criteria: SortableSearchCriteria) {
        fun sortField(criteria: SortableSearchCriteria): String =
            if (criteria.sortField == SortField.MOTTATT) {
                "mottattKlageinstans"
            } else if (criteria.sortField == SortField.PAA_VENT_FROM) {
                "sattPaaVent"
            } else if (criteria.sortField == SortField.PAA_VENT_TO) {
                "sattPaaVentExpires"
            } else {
                "frist"
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
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)

        baseQuery.must(haveSakenGjelder(fnr))

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun SaksbehandlereByEnhetSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)

        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.must(QueryBuilders.termQuery("tildeltEnhet", enhet))
        baseQuery.must(beTildeltSaksbehandler())

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun LedigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beTildeltSaksbehandler())
        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun CountLedigeOppgaverMedUtgaattFristSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beTildeltSaksbehandler())
        baseQuery.must(haveFristMellom(fristFom, fristTom))
        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun SaksbehandlersFerdigstilteOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        //baseQuery.must(beAvsluttetAvSaksbehandler())
        baseQuery.must(beAvsluttetAvSaksbehandlerEtter(ferdigstiltFom))
        baseQuery.must(beTildeltSaksbehandler(saksbehandler))

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun BehandlingIdSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.must(QueryBuilders.idsQuery().addIds(behandlingId))

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun SaksbehandlersUferdigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beSattPaaVent())
        baseQuery.must(beTildeltSaksbehandlerOrMedunderskriver(saksbehandler))

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun SaksbehandlersOppgaverPaaVentSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.must(beSattPaaVent())
        baseQuery.must(beTildeltSaksbehandlerOrMedunderskriver(saksbehandler))

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun EnhetensFerdigstilteOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        //baseQuery.must(beAvsluttetAvSaksbehandler())
        baseQuery.must(beAvsluttetAvSaksbehandlerEtter(ferdigstiltFom))
        baseQuery.must(beTildeltEnhet(enhetId))
        if (saksbehandlere.isNotEmpty()) {
            baseQuery.must(beTildeltSaksbehandler(saksbehandlere))
        }

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun EnhetensOppgaverPaaVentSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.must(beSattPaaVent())
        baseQuery.must(beTildeltEnhet(enhetId))
        if (saksbehandlere.isNotEmpty()) {
            baseQuery.must(beTildeltSaksbehandler(saksbehandlere))
        }

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun EnhetensUferdigeOppgaverSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.addSecurityFilters(this)
        baseQuery.addBasicFilters(this)
        baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        baseQuery.mustNot(beSattPaaVent())
        baseQuery.must(beTildeltEnhet(enhetId))
        if (saksbehandlere.isNotEmpty()) {
            //TODO: Skal man her ta med oppgaver hvor en saksbehandler i enheten er medunderskriver på en annen enhets oppgave?
            // Det er ikke med nå, jeg inkluderer ikke medunderskrivere i det hele tatt
            baseQuery.must(beTildeltSaksbehandler(saksbehandlere))
        }

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun BoolQueryBuilder.addBasicFilters(basicSearchCriteria: BasicSearchCriteria) {
        if (basicSearchCriteria.typer.isNotEmpty()) {
            val innerQueryType = QueryBuilders.boolQuery()
            this.must(innerQueryType)
            basicSearchCriteria.typer.forEach {
                innerQueryType.should(QueryBuilders.termQuery("type", it.id))
            }
        }

        if (basicSearchCriteria.ytelser.isNotEmpty()) {
            val innerQueryYtelse = QueryBuilders.boolQuery()
            this.must(innerQueryYtelse)
            basicSearchCriteria.ytelser.forEach {
                innerQueryYtelse.should(QueryBuilders.termQuery("ytelseId", it.id))
            }
        }

        if (basicSearchCriteria.hjemler.isNotEmpty()) {
            val innerQueryHjemler = QueryBuilders.boolQuery()
            this.must(innerQueryHjemler)
            basicSearchCriteria.hjemler.forEach {
                innerQueryHjemler.should(QueryBuilders.termQuery("hjemler", it.id))
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
                filterQuery.should(QueryBuilders.termQuery("strengtFortrolig", true))
                filterQuery.should(QueryBuilders.termQuery("fortrolig", true))
            }

            !kanBehandleEgenAnsatt && kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 2
                //Er i praksis det samme som case 1
                filterQuery.should(QueryBuilders.termQuery("strengtFortrolig", true))
                filterQuery.should(QueryBuilders.termQuery("fortrolig", true))
            }

            kanBehandleEgenAnsatt && !kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 3
                //tolker dette som kun egen ansatt som også er strengt fortrolig
                //Skipper de normale, altså de som ikke har noe.
                filterQuery.must(QueryBuilders.termQuery("strengtFortrolig", true))
                filterQuery.mustNot(QueryBuilders.termQuery("fortrolig", true))
            }

            kanBehandleEgenAnsatt && kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 4
                //Skal inkludere de normale
                //Skal inkludere egen ansatt
                //Skal inkludere fortrolig
                filterQuery.mustNot(QueryBuilders.termQuery("strengtFortrolig", true))
            }

            !kanBehandleEgenAnsatt && !kanBehandleFortrolig && kanBehandleStrengtFortrolig -> {
                //Case 5.
                //Er i praksis det samme som case 3. Inkluderer egen ansatte som også har strengt fortrolig, strengt fortrolig trumfer egen ansatt
                filterQuery.must(QueryBuilders.termQuery("strengtFortrolig", true))
                filterQuery.mustNot(QueryBuilders.termQuery("fortrolig", true))
            }

            !kanBehandleEgenAnsatt && kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 6
                //Skal inkludere de normale
                //Skal inkludere fortrolig
                //Skal inkludere fortrolige som også er egen ansatt, men ikke egen ansatte som ikke er fortrolige
                val egenAnsattAndNotFortrolig = QueryBuilders.boolQuery()
                egenAnsattAndNotFortrolig.must(QueryBuilders.termQuery("egenAnsatt", true))
                egenAnsattAndNotFortrolig.mustNot(QueryBuilders.termQuery("fortrolig", true))

                filterQuery.mustNot(egenAnsattAndNotFortrolig)
                filterQuery.mustNot(QueryBuilders.termQuery("strengtFortrolig", true))
            }

            kanBehandleEgenAnsatt && !kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 7
                //Skal inkludere de normale
                //Skal inkludere egen ansatt
                filterQuery.mustNot(QueryBuilders.termQuery("strengtFortrolig", true))
                filterQuery.mustNot(QueryBuilders.termQuery("fortrolig", true))
            }

            !kanBehandleEgenAnsatt && !kanBehandleFortrolig && !kanBehandleStrengtFortrolig -> {
                //Case 8
                filterQuery.mustNot(QueryBuilders.termQuery("strengtFortrolig", true))
                filterQuery.mustNot(QueryBuilders.termQuery("fortrolig", true))
                filterQuery.mustNot(QueryBuilders.termQuery("egenAnsatt", true))
            }
        }
    }

    fun deleteAll() {
        esBehandlingRepository.deleteAll()
    }

    fun findAllIdAndModified(): Map<String, LocalDateTime> {
        val searchHits = esBehandlingRepository.search(QueryBuilders.matchAllQuery())
        return searchHits.map { it.id to it.content.modified }.toMap()
    }

    open fun findRelatedKlagebehandlinger(
        fnr: String,
        saksreferanse: String,
    ): RelatedKlagebehandlinger {
        val aapneByFnr = klagebehandlingerMedFoedselsnummer(fnr, true)
        val aapneBySaksreferanse = klagebehandlingerMedSaksreferanse(saksreferanse, true)
        val avsluttedeByFnr = klagebehandlingerMedFoedselsnummer(fnr, false)
        val avsluttedeBySaksreferanse = klagebehandlingerMedSaksreferanse(saksreferanse, false)
        //TODO: Vi trenger vel neppe returnere hele klagebehandlingen.. Hva trenger vi å vise i gui?
        return RelatedKlagebehandlinger(
            aapneByFnr,
            avsluttedeByFnr,
            aapneBySaksreferanse,
            avsluttedeBySaksreferanse,
        )
    }

    private fun klagebehandlingerMedFoedselsnummer(fnr: String, aapen: Boolean): List<EsBehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termQuery("sakenGjelderFnr", fnr)), aapen
        )
    }

    private fun klagebehandlingerMedSaksreferanse(saksreferanse: String, aapen: Boolean): List<EsBehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termQuery("kildeReferanse", saksreferanse)), aapen
        )
    }

    private fun findWithBaseQueryAndAapen(baseQuery: BoolQueryBuilder, aapen: Boolean): List<EsBehandling> {
        if (aapen) {
            baseQuery.mustNot(beAvsluttetAvSaksbehandler())
        } else {
            baseQuery.must(beAvsluttetAvSaksbehandler())
        }
        return try {
            esBehandlingRepository.search(baseQuery)
                .searchHits.map { it.content }
        } catch (e: Exception) {
            logger.error("Failed to search ES for related klagebehandlinger", e)
            emptyList()
        }
    }

    open fun statistikkQuery(): KlageStatistikk {

        val baseQueryInnsendtOgAvsluttet: QueryBuilder = QueryBuilders.matchAllQuery()
        val aggregationsForInnsendtAndAvsluttet = addAggregationsForInnsendtAndAvsluttet()

        val searchHitsInnsendtOgAvsluttet =
            esBehandlingRepository.search(baseQueryInnsendtOgAvsluttet, aggregationsForInnsendtAndAvsluttet)

        val innsendtOgAvsluttetAggs = searchHitsInnsendtOgAvsluttet.aggregations
        val sumInnsendtYesterday =
            innsendtOgAvsluttetAggs!!.get<ParsedDateRange>("innsendt_yesterday").buckets.firstOrNull()?.docCount ?: 0
        val sumInnsendtLastSevenDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("innsendt_last7days").buckets.firstOrNull()?.docCount ?: 0
        val sumInnsendtLastThirtyDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("innsendt_last30days").buckets.firstOrNull()?.docCount ?: 0
        val sumAvsluttetYesterday =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("avsluttet_yesterday").buckets.firstOrNull()?.docCount ?: 0
        val sumAvsluttetLastSevenDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("avsluttet_last7days").buckets.firstOrNull()?.docCount ?: 0
        val sumAvsluttetLastThirtyDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("avsluttet_last30days").buckets.firstOrNull()?.docCount
                ?: 0

        val baseQueryOverFrist: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQueryOverFrist.mustNot(beAvsluttetAvSaksbehandler())
        val aggregationsForOverFrist = addAggregationsForOverFrist()

        val searchHitsOverFrist =
            esBehandlingRepository.search(baseQueryOverFrist, aggregationsForOverFrist)
        val sumOverFrist =
            searchHitsOverFrist.aggregations!!.get<ParsedDateRange>("over_frist").buckets.firstOrNull()?.docCount
                ?: 0
        searchHitsOverFrist.aggregations.get<ParsedDateRange>("over_frist").buckets.forEach {
            logger.debug("from clause in over_frist is ${it.from}")
            logger.debug("to clause in over_frist is ${it.to}")
        }
        val sumUbehandlede = searchHitsOverFrist.totalHits
        return KlageStatistikk(
            sumUbehandlede,
            sumOverFrist,
            sumInnsendtYesterday,
            sumInnsendtLastSevenDays,
            sumInnsendtLastThirtyDays,
            sumAvsluttetYesterday,
            sumAvsluttetLastSevenDays,
            sumAvsluttetLastThirtyDays
        )
    }

    private fun addAggregationsForOverFrist(): List<AggregationBuilder> {
        return listOf(
            AggregationBuilders.dateRange("over_frist").field("frist")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addUnboundedTo("now/d")
                .format(ISO8601)
        )
    }

    private fun addAggregationsForInnsendtAndAvsluttet(): List<AggregationBuilder> {
        return listOf(
            AggregationBuilders.dateRange("innsendt_yesterday").field("innsendt")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addRange("now-1d/d", "now/d")
                .format(ISO8601),
            AggregationBuilders.dateRange("innsendt_last7days").field("innsendt")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addRange("now-7d/d", "now/d")
                .format(ISO8601),
            AggregationBuilders.dateRange("innsendt_last30days").field("innsendt")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addRange("now-30d/d", "now/d")
                .format(ISO8601),
            AggregationBuilders.dateRange("avsluttet_yesterday").field("avsluttetAvSaksbehandler")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addRange("now-1d/d", "now/d")
                .format(ISO8601),
            AggregationBuilders.dateRange("avsluttet_last7days").field("avsluttetAvSaksbehandler")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addRange("now-7d/d", "now/d")
                .format(ISO8601),
            AggregationBuilders.dateRange("avsluttet_last30days").field("avsluttetAvSaksbehandler")
                .timeZone(ZoneId.of(ZONEID_UTC))
                .addRange("now-30d/d", "now/d")
                .format(ISO8601)
        )
    }

    private fun beAvsluttetAvSaksbehandler() = QueryBuilders.existsQuery("avsluttetAvSaksbehandler")

    private fun beSattPaaVent() = QueryBuilders.existsQuery("sattPaaVent")

    private fun beTildeltSaksbehandler() = QueryBuilders.existsQuery("tildeltSaksbehandlerident")

    private fun beAvsluttetAvSaksbehandlerEtter(ferdigstiltFom: LocalDate) =
        QueryBuilders.rangeQuery("avsluttetAvSaksbehandler").gte(ferdigstiltFom).format(ISO8601).timeZone(ZONEID_UTC)

    private fun haveFristEtter(fristFom: LocalDate) =
        QueryBuilders.rangeQuery("frist").gte(fristFom).format(ISO8601).timeZone(ZONEID_UTC)

    private fun haveFristFoer(fristTom: LocalDate) =
        QueryBuilders.rangeQuery("frist").lte(fristTom).format(ISO8601).timeZone(ZONEID_UTC)

    private fun haveFristMellom(fristFom: LocalDate, fristTom: LocalDate) =
        QueryBuilders.rangeQuery("frist").gte(fristFom).lte(fristTom).format(ISO8601).timeZone(ZONEID_UTC)

    private fun beTildeltSaksbehandler(saksbehandlere: List<String>): BoolQueryBuilder {
        val innerQuerySaksbehandler = QueryBuilders.boolQuery()
        saksbehandlere.forEach {
            innerQuerySaksbehandler.should(QueryBuilders.termQuery("tildeltSaksbehandlerident", it))
        }
        return innerQuerySaksbehandler
    }

    private fun beTildeltSaksbehandler(navIdent: String) =
        QueryBuilders.termQuery("tildeltSaksbehandlerident", navIdent)

    private fun beTildeltMedunderskriver(navIdent: String): BoolQueryBuilder {
        val innerQueryMedunderskriver = QueryBuilders.boolQuery()
        innerQueryMedunderskriver.must(QueryBuilders.termQuery("medunderskriverident", navIdent))
        innerQueryMedunderskriver.must(
            QueryBuilders.termQuery(
                "medunderskriverFlyt",
                MedunderskriverFlyt.OVERSENDT_TIL_MEDUNDERSKRIVER.name
            )
        )
        return innerQueryMedunderskriver
    }

    private fun beTildeltSaksbehandlerOrMedunderskriver(navIdent: String): BoolQueryBuilder {
        val innerQuery = QueryBuilders.boolQuery()
        innerQuery.should(beTildeltSaksbehandler(navIdent))
        innerQuery.should(beTildeltMedunderskriver(navIdent))
        return innerQuery
    }

    private fun beTildeltEnhet(enhetId: String): TermQueryBuilder =
        QueryBuilders.termQuery("tildeltEnhet", enhetId)

    private fun haveSakenGjelder(fnr: String): TermQueryBuilder =
        QueryBuilders.termQuery("sakenGjelderFnr", fnr)

    fun deleteBehandling(behandlingId: UUID) {
        esBehandlingRepository.deleteBehandling(behandlingId)
    }

    //TODO: Har beholdt dette fordi det bare er her koden for relaterte personer og sånt er dokumentert.
    /*
   private fun KlagebehandlingerSearchCriteria.toEsQuery(): QueryBuilder {

       val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
       logger.debug("Search criteria: {}", this)

       baseQuery.addSecurityFilters(this)

       val combinedInnerFnrAndYtelseQuery = QueryBuilders.boolQuery()
       baseQuery.must(combinedInnerFnrAndYtelseQuery)

       val innerFnrAndYtelseQuery = QueryBuilders.boolQuery()
       combinedInnerFnrAndYtelseQuery.should(innerFnrAndYtelseQuery)

       val innerQueryFnr = QueryBuilders.boolQuery()
       innerFnrAndYtelseQuery.must(innerQueryFnr)
       foedselsnr?.let {
           innerQueryFnr.should(QueryBuilders.termQuery("sakenGjelderFnr", it))
       }

       val innerQueryYtelse = QueryBuilders.boolQuery()
       innerFnrAndYtelseQuery.must(innerQueryYtelse)
       ytelser.forEach {
           innerQueryYtelse.should(QueryBuilders.termQuery("ytelseId", it.id))
       }

       extraPersonWithYtelser?.let { extraPerson ->
           val innerFnrAndYtelseEktefelleQuery = QueryBuilders.boolQuery()
           combinedInnerFnrAndYtelseQuery.should(innerFnrAndYtelseEktefelleQuery)

           innerFnrAndYtelseEktefelleQuery.must(
               QueryBuilders.termQuery(
                   "sakenGjelderFnr",
                   extraPerson.foedselsnr
               )
           )

           val innerYtelseEktefelleQuery = QueryBuilders.boolQuery()
           innerFnrAndYtelseEktefelleQuery.must(innerYtelseEktefelleQuery)
           extraPerson.ytelser.forEach { ytelse ->
               innerYtelseEktefelleQuery.should(QueryBuilders.termQuery("ytelseId", ytelse.id))
           }
       }

       when (statuskategori) {
           Statuskategori.AAPEN -> baseQuery.mustNot(beAvsluttetAvSaksbehandler())
           Statuskategori.AVSLUTTET -> baseQuery.must(beAvsluttetAvSaksbehandler())
           Statuskategori.ALLE -> Unit
       }

       enhetId?.let {
           baseQuery.must(QueryBuilders.termQuery("tildeltEnhet", enhetId))
       }

       val innerQueryBehandlingtype = QueryBuilders.boolQuery()
       baseQuery.must(innerQueryBehandlingtype)
       if (typer.isNotEmpty()) {
           typer.forEach {
               innerQueryBehandlingtype.should(QueryBuilders.termQuery("type", it.id))
           }
       } else {
           innerQueryBehandlingtype.should(QueryBuilders.termQuery("type", Type.KLAGE.id))
       }

       erTildeltSaksbehandler?.let {
           if (erTildeltSaksbehandler) {
               baseQuery.must(beTildeltSaksbehandler())
           } else {
               baseQuery.mustNot(beTildeltSaksbehandler())
           }
       }
       if (saksbehandlere.isNotEmpty()) {
           val innerQuerySaksbehandler = QueryBuilders.boolQuery()
           saksbehandlere.forEach {
               innerQuerySaksbehandler.should(QueryBuilders.termQuery("tildeltSaksbehandlerident", it))
           }

           if (statuskategori == Statuskategori.AAPEN) {
               saksbehandlere.forEach {
                   val innerMedunderskriverQuery = QueryBuilders.boolQuery()
                   innerMedunderskriverQuery.must(QueryBuilders.termQuery("medunderskriverident", it))
                   innerMedunderskriverQuery.must(
                       QueryBuilders.termQuery(
                           "medunderskriverFlyt",
                           MedunderskriverFlyt.OVERSENDT_TIL_MEDUNDERSKRIVER.name
                       )
                   )
                   innerQuerySaksbehandler.should(innerMedunderskriverQuery)
               }
           }

           baseQuery.must(innerQuerySaksbehandler)
       }

       opprettetFom?.let {
           baseQuery.must(
               QueryBuilders.rangeQuery("mottattKlageinstans").gte(it).format(ISO8601).timeZone(ZONEID_UTC)
           )
       }
       opprettetTom?.let {
           baseQuery.must(
               QueryBuilders.rangeQuery("mottattKlageinstans").lte(it).format(ISO8601).timeZone(ZONEID_UTC)
           )
       }
       ferdigstiltFom?.let {
           baseQuery.must(
               QueryBuilders.rangeQuery("avsluttetAvSaksbehandler").gte(it).format(ISO8601).timeZone(ZONEID_UTC)
           )
       }
       ferdigstiltTom?.let {
           baseQuery.must(
               QueryBuilders.rangeQuery("avsluttetAvSaksbehandler").lte(it).format(ISO8601).timeZone(ZONEID_UTC)
           )
       }
       fristFom?.let {
           baseQuery.must(
               QueryBuilders.rangeQuery("frist").gte(it).format(ISO8601).timeZone(ZONEID_UTC)
           )
       }
       fristTom?.let {
           baseQuery.must(
               QueryBuilders.rangeQuery("frist").lte(it).format(ISO8601).timeZone(ZONEID_UTC)
           )
       }

       if (hjemler.isNotEmpty()) {
           val innerQueryHjemler = QueryBuilders.boolQuery()
           baseQuery.must(innerQueryHjemler)
           hjemler.forEach {
               innerQueryHjemler.should(QueryBuilders.termQuery("hjemler", it.id))
           }
       }

       logger.debug("Making search request with query {}", baseQuery.toString())
       return baseQuery
   }

    */
}
