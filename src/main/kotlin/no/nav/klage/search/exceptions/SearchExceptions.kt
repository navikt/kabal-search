package no.nav.klage.search.exceptions

class NotMatchingUserException(msg: String) : RuntimeException(msg)

class FeatureNotEnabledException(msg: String) : RuntimeException(msg)

class NoSaksbehandlerRoleException(msg: String) : RuntimeException(msg)

class NotOwnEnhetException(msg: String) : RuntimeException(msg)

class MissingTilgangException(msg: String) : RuntimeException(msg)
