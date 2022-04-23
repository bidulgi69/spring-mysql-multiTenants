package kr.dove.mysql.persistence.country

import kr.dove.mysql.api.Continent
import kr.dove.mysql.api.Country
import kr.dove.mysql.persistence.Times
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@Table(value = "country")
data class CountryEntity(
    @Id val id: String,
    val name: String,
    val continent: String,  //  fk
    @Version val version: Int = 0
) : Times() {

    fun castToApi(continent: Continent): Country =
        Country(
            id,
            name,
            continent,
            version
        )
}
