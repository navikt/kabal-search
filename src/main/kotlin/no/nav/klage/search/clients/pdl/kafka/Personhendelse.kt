package no.nav.klage.search.clients.pdl.kafka

import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

val GenericRecord.opplysningstype get(): String {
    return get("opplysningstype").toString()
}

val GenericRecord.personidenter: List<String>
    get(): List<String> {
    return (get("personidenter") as GenericData.Array<*>)
        .map { it.toString() }
}

val GenericRecord.fnr get(): String {
    return (get("personidenter") as GenericData.Array<*>)
        .map { it.toString() }
        .first { it.length == 11 }
}

val GenericRecord.erAdressebeskyttelse get(): Boolean {
    return opplysningstype == OPPLYSNINGSTYPE_ADRESSEBESKYTTELSE
}

const val OPPLYSNINGSTYPE_ADRESSEBESKYTTELSE = "ADRESSEBESKYTTELSE_V1"