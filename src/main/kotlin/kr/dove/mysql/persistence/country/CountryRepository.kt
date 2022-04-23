package kr.dove.mysql.persistence.country

import org.springframework.data.r2dbc.repository.R2dbcRepository

interface CountryRepository : R2dbcRepository<CountryEntity, String> {
}