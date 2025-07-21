package no.nav.klage.search.clients.pdl.kafka

import org.apache.avro.generic.GenericRecord

val GenericRecord.opplysningstype get(): String {
    return get("opplysningstype").toString()
}

val GenericRecord.erAdressebeskyttelse get(): Boolean {
    return opplysningstype == OPPLYSNINGSTYPE_ADRESSEBESKYTTELSE
}

const val OPPLYSNINGSTYPE_ADRESSEBESKYTTELSE = "ADRESSEBESKYTTELSE_V1"