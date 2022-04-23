package kr.dove.mysql.persistence.continent

import kr.dove.mysql.api.Continent
import kr.dove.mysql.persistence.Times
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table

@Table(value = "continent")
data class ContinentEntity(
    @Id val id: String,
    val name: String,
    @Version val version: Int = 0
) : Times() {

    fun castToApi(): Continent =
        Continent(
            id,
            name,
            version
        )
}
