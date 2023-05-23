package dev.janssenbatista.shopping.list.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShoppingListApiApplication

fun main(args: Array<String>) {
	runApplication<ShoppingListApiApplication>(*args)
}
