package kr.dove.mysql.api

data class City(
    var id: String? = null,
    val name: String,
    val population: Int,
    val country: Country,
    var version: Int? = null
)

data class Country(
    var id: String? = null,
    val name: String,
    val continent: Continent,
    var version: Int? = null
)

data class Continent(
    var id: String? = null,
    val name: String,
    var version: Int? = null
)