package kr.dove.mysql.persistence.continent

import org.springframework.data.r2dbc.repository.R2dbcRepository

interface ContinentRepository : R2dbcRepository<ContinentEntity, String> {
}