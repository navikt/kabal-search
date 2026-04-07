package no.nav.klage.search.exceptions

class UserNotFoundException(msg: String) : RuntimeException(msg)

class GroupNotFoundException(msg: String) : RuntimeException(msg)

class MissingTilgangException(msg: String) : RuntimeException(msg)
