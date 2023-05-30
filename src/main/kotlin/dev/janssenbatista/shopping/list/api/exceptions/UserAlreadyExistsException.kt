package dev.janssenbatista.shopping.list.api.exceptions

class UserAlreadyExistsException(override val message: String?) : RuntimeException(message)