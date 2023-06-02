package dev.janssenbatista.shopping.list.api.exceptions.handlers

import dev.janssenbatista.shopping.list.api.exceptions.UnauthorizedException
import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(RuntimeException::class)
    fun handleException(ex: RuntimeException): ResponseEntity<Any> {
        return when (ex) {
            is UserNotFoundException -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
            is UserAlreadyExistsException -> ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message)
            is UnauthorizedException -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }


}