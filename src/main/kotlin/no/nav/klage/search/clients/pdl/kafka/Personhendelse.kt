package no.nav.klage.search.clients.pdl.kafka

import org.apache.avro.generic.GenericRecord

val GenericRecord.opplysningstype get(): String {
    return get("opplysningstype").toString()
}