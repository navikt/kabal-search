package no.nav.klage.search.clients

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.search.clients.pdl.graphql.HentPersonResponse
import no.nav.klage.search.clients.pdl.graphql.PdlClient
import no.nav.klage.search.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.search.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PdlClientTest {

    @MockK
    lateinit var tokenUtilMock: TokenUtil

    @BeforeEach
    fun before() {
        every { tokenUtilMock.getAppAccessTokenWithPdlScope() } returns "abc"
        every { tokenUtilMock.getSaksbehandlerAccessTokenWithPdlScope() } returns "abc"
    }

    @Test
    fun `pdl response kan mappes selv med tomme arrays`() {
        val hentPersonResponse = getHentPersonResponse(pdlResponse())
        assertThat(hentPersonResponse.data).isNotNull
        assertThat(hentPersonResponse.data!!.hentPerson!!.navn.first().fornavn).isEqualTo("AREMARK")
    }

    @Test
    fun `personsøk ok`() {
        val personsoekResponse = getSoekPersonResponse(pdlSoekResponse())
        assertThat(personsoekResponse.data).isNotNull
    }

    fun getHentPersonResponse(jsonResponse: String): HentPersonResponse {
        val pdlClient = PdlClient(
            createShortCircuitWebClient(jsonResponse),
            tokenUtilMock
        )

        return pdlClient.getPersonInfo("fnr")
    }

    fun getSoekPersonResponse(jsonResponse: String): SoekPersonResponse {
        val pdlClient = PdlClient(
            createShortCircuitWebClient(jsonResponse),
            tokenUtilMock
        )

        return pdlClient.personsok("fnr")
    }

    @Language("json")
    fun pdlResponse() = """
        {
          "data": {
            "hentPerson": {
              "navn": [
                {
                  "fornavn": "AREMARK",
                  "mellomnavn": null,
                  "etternavn": "TESTFAMILIEN"
                }
              ],
              "kjoenn": [
                {
                  "kjoenn": "MANN"
                }
              ],
              "adressebeskyttelse": [],
              "sivilstand": []
            }
          }
        }
    """

    @Language("json")
    fun pdlSoekResponse() = """
        {
          "data": {
            "sokPerson": {
              "pageNumber": 1,
              "totalHits": 38,
              "totalPages": 2,
              "hits": [
                {
                  "score": 37.505756,
                  "person": {
                    "folkeregisteridentifikator": [
                      {
                        "identifikasjonsnummer": "23051668235"
                      }
                    ],
                    "navn": [
                      {
                        "fornavn": "LITEN",
                        "etternavn": "SAKS",
                        "mellomnavn": null
                      }
                    ],
                    "adressebeskyttelse": [],
                    "foedsel": [
                      {
                        "foedselsdato": "2016-05-23"
                      }
                    ]
                  }
                }
              ]
            }
          }
        }
    """


}
