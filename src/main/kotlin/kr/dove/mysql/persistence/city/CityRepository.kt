package kr.dove.mysql.persistence.city

import org.springframework.data.r2dbc.repository.R2dbcRepository

interface CityRepository : R2dbcRepository<CityEntity, String> {

}