package no.nav.klage.search.service

import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.kodeverk.Type
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.search.domain.KlagebehandlingerSearchCriteria.Statuskategori.*
import no.nav.klage.search.domain.SaksbehandlereByEnhetSearchCriteria
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.search.domain.elasticsearch.EsKlagebehandling.Status.*
import no.nav.klage.search.domain.elasticsearch.KlageStatistikk
import no.nav.klage.search.domain.elasticsearch.RelatedKlagebehandlinger
import no.nav.klage.search.domain.saksbehandler.Saksbehandler
import no.nav.klage.search.repositories.EsKlagebehandlingRepository
import no.nav.klage.search.repositories.KlagebehandlingerSearchHits
import no.nav.klage.search.repositories.SearchHits
import no.nav.klage.search.util.getLogger
import no.nav.klage.search.util.getMedian
import org.opensearch.common.unit.TimeValue
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilder
import org.opensearch.index.query.QueryBuilders
import org.opensearch.search.aggregations.AggregationBuilder
import org.opensearch.search.aggregations.AggregationBuilders
import org.opensearch.search.aggregations.bucket.range.ParsedDateRange
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.search.sort.SortBuilders
import org.opensearch.search.sort.SortOrder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit


open class ElasticsearchService(
    private val esKlagebehandlingRepository: EsKlagebehandlingRepository,
) :
    ApplicationListener<ContextRefreshedEvent> {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        private const val ISO8601 = "yyyy-MM-dd"
        private const val ZONEID_UTC = "Z"
    }

    fun recreateIndex() {
        esKlagebehandlingRepository.recreateIndex()
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            esKlagebehandlingRepository.createIndex()
        } catch (e: Exception) {
            logger.error("Unable to initialize OpenSearch", e)
        }
    }

    fun save(klagebehandlinger: List<EsKlagebehandling>) {
        esKlagebehandlingRepository.save(klagebehandlinger)
    }

    fun save(klagebehandling: EsKlagebehandling) {
        logger.debug("Skal indeksere fra kabal-search, klage med id ${klagebehandling.id}")
        esKlagebehandlingRepository.save(klagebehandling)
    }

    open fun findByCriteria(criteria: KlagebehandlingerSearchCriteria): KlagebehandlingerSearchHits {
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(criteria.toEsQuery())
        searchSourceBuilder.from(criteria.offset)
        searchSourceBuilder.size(criteria.limit)
        searchSourceBuilder.timeout(TimeValue(60, TimeUnit.SECONDS))
        searchSourceBuilder.sort(SortBuilders.fieldSort(sortField(criteria)).order(mapOrder(criteria.order)));

        val searchHits =
            esKlagebehandlingRepository.search(searchSourceBuilder, emptyList())
        logger.debug("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun findSaksbehandlereByEnhetCriteria(criteria: SaksbehandlereByEnhetSearchCriteria): SortedSet<Saksbehandler> {
        val searchHits: SearchHits<EsKlagebehandling> = esKlagebehandlingRepository.search(criteria.toEsQuery())

        //Sort results by etternavn
        return searchHits.map {
            Saksbehandler(
                navIdent = it.content.tildeltSaksbehandlerident
                    ?: throw RuntimeException("tildeltSaksbehandlerident is null. Can't happen"),
                navn = it.content.tildeltSaksbehandlernavn ?: "Navn mangler"
            )
        }.toSortedSet(compareBy<Saksbehandler> { it.navn.split(" ").last() })
    }

    open fun countIkkeTildelt(): Long {
        return countByStatus(IKKE_TILDELT)
    }

    open fun countTildelt(): Long {
        return countByStatus(TILDELT)
    }

    open fun countSendtTilMedunderskriver(): Long {
        return countByStatus(SENDT_TIL_MEDUNDERSKRIVER)
    }

    open fun countMedunderskriverValgt(): Long {
        return countByStatus(MEDUNDERSKRIVER_VALGT)
    }

    open fun countReturnertTilSaksbehandler(): Long {
        return countByStatus(RETURNERT_TIL_SAKSBEHANDLER)
    }

    open fun countAvsluttet(): Long {
        return countByStatus(FULLFOERT)
    }

    private fun countByStatus(status: EsKlagebehandling.Status): Long {
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.must(QueryBuilders.termQuery("status", status))
        return esKlagebehandlingRepository.count(baseQuery)
    }

    open fun countAntallSaksdokumenterIAvsluttedeBehandlingerMedian(): Double {
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQuery.should(QueryBuilders.termQuery("status", FULLFOERT))
        val searchHits = esKlagebehandlingRepository.search(baseQuery)
        val saksdokumenterPerAvsluttetBehandling = searchHits.map { e -> e.content }
            .map { e -> e.saksdokumenter.size }.toList()

        return getMedian(saksdokumenterPerAvsluttetBehandling)
    }

    open fun countByCriteria(criteria: KlagebehandlingerSearchCriteria): Int {
        return esKlagebehandlingRepository.count(criteria.toEsQuery()).toInt()
    }

    private fun sortField(criteria: KlagebehandlingerSearchCriteria): String =
        if (criteria.sortField == KlagebehandlingerSearchCriteria.SortField.MOTTATT) {
            "mottattKlageinstans"
        } else {
            "frist"
        }

    private fun mapOrder(order: KlagebehandlingerSearchCriteria.Order?): SortOrder {
        return order.let {
            when (it) {
                null -> SortOrder.ASC
                KlagebehandlingerSearchCriteria.Order.ASC -> SortOrder.ASC
                KlagebehandlingerSearchCriteria.Order.DESC -> SortOrder.DESC
            }
        }
    }

    private fun SaksbehandlereByEnhetSearchCriteria.toEsQuery(): QueryBuilder {
        logger.debug("Search criteria: {}", this)
        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        addSecurityFilters(
            baseQuery = baseQuery,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
            kanBehandleFortrolig = kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig
        )

        baseQuery.mustNot(QueryBuilders.existsQuery("avsluttetAvSaksbehandler"))
        baseQuery.must(QueryBuilders.termQuery("tildeltEnhet", enhet))
        baseQuery.must(QueryBuilders.existsQuery("tildeltSaksbehandlerident"))

        logger.debug("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    private fun KlagebehandlingerSearchCriteria.toEsQuery(): QueryBuilder {

        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        logger.debug("Search criteria: {}", this)

        addSecurityFilters(
            baseQuery = baseQuery,
            kanBehandleEgenAnsatt = kanBehandleEgenAnsatt,
            kanBehandleFortrolig = kanBehandleFortrolig,
            kanBehandleStrengtFortrolig = kanBehandleStrengtFortrolig
        )

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
            AAPEN -> baseQuery.mustNot(QueryBuilders.existsQuery("avsluttetAvSaksbehandler"))
            AVSLUTTET -> baseQuery.must(QueryBuilders.existsQuery("avsluttetAvSaksbehandler"))
            ALLE -> Unit
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
                baseQuery.must(QueryBuilders.existsQuery("tildeltSaksbehandlerident"))
            } else {
                baseQuery.mustNot(QueryBuilders.existsQuery("tildeltSaksbehandlerident"))
            }
        }
        if (saksbehandlere.isNotEmpty()) {
            val innerQuerySaksbehandler = QueryBuilders.boolQuery()
            saksbehandlere.forEach {
                innerQuerySaksbehandler.should(QueryBuilders.termQuery("tildeltSaksbehandlerident", it))
            }

            if (statuskategori == AAPEN) {
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

    private fun addSecurityFilters(
        baseQuery: BoolQueryBuilder,
        kanBehandleEgenAnsatt: Boolean,
        kanBehandleFortrolig: Boolean,
        kanBehandleStrengtFortrolig: Boolean
    ) {
        val filterQuery = QueryBuilders.boolQuery()
        baseQuery.filter(filterQuery)

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
        esKlagebehandlingRepository.deleteAll()
    }

    fun findAllIdAndModified(): Map<String, LocalDateTime> {
        val searchHits = esKlagebehandlingRepository.search(QueryBuilders.matchAllQuery())
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

    private fun klagebehandlingerMedFoedselsnummer(fnr: String, aapen: Boolean): List<EsKlagebehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termQuery("sakenGjelderFnr", fnr)), aapen
        )
    }

    private fun klagebehandlingerMedSaksreferanse(saksreferanse: String, aapen: Boolean): List<EsKlagebehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termQuery("kildeReferanse", saksreferanse)), aapen
        )
    }

    private fun findWithBaseQueryAndAapen(baseQuery: BoolQueryBuilder, aapen: Boolean): List<EsKlagebehandling> {
        if (aapen) {
            baseQuery.mustNot(QueryBuilders.existsQuery("avsluttetAvSaksbehandler"))
        } else {
            baseQuery.must(QueryBuilders.existsQuery("avsluttetAvSaksbehandler"))
        }
        return try {
            esKlagebehandlingRepository.search(baseQuery)
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
            esKlagebehandlingRepository.search(baseQueryInnsendtOgAvsluttet, aggregationsForInnsendtAndAvsluttet)

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
        baseQueryOverFrist.mustNot(QueryBuilders.existsQuery("avsluttetAvSaksbehandler"))
        val aggregationsForOverFrist = addAggregationsForOverFrist()

        val searchHitsOverFrist =
            esKlagebehandlingRepository.search(baseQueryOverFrist, aggregationsForOverFrist)
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
}