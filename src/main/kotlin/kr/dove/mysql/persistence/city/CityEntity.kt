package kr.dove.mysql.persistence.city

import kr.dove.mysql.api.City
import kr.dove.mysql.api.Country
import kr.dove.mysql.persistence.Times
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@Table(value = "city")
data class CityEntity(
    @Id val id: String,
    val name: String,
    val population: Int,
    val country: String,    //  fk
    @Version val version: Int = 0
) : Times() {

    fun castToApi(country: Country): City =
        City(
            id,
            name,
            population,
            country,
            version
        )
}
