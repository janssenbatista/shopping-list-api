package dev.janssenbatista.shopping.list.api.exceptions

class UserNotFoundException(override val message: String?) : RuntimeException(message)