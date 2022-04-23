package kr.dove.mysql.service

import kr.dove.mysql.api.*
import kr.dove.mysql.config.MasterDataSourceProperties
import kr.dove.mysql.config.SlaveDataSourceProperties
import kr.dove.mysql.persistence.city.CityEntity
import kr.dove.mysql.persistence.city.CityRepository
import kr.dove.mysql.persistence.continent.ContinentEntity
import kr.dove.mysql.persistence.continent.ContinentRepository
import kr.dove.mysql.persistence.country.CountryEntity
import kr.dove.mysql.persistence.country.CountryRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import java.util.*

@RestController
class ServiceImpl(
    private val continentRepository: ContinentRepository,
    private val countryRepository: CountryRepository,
    private val cityRepository: CityRepository,
) : ContinentService, CountryService, CityService {

    @Transactional(
        value = SlaveDataSourceProperties.transactionManager,
        readOnly = true
    )
    override fun continents(): Flux<Continent> {
        return continentRepository.findAll()
            .flatMap {
                Mono.just(it.castToApi())
            }
    }

    @Transactional(
        value = MasterDataSourceProperties.transactionManager,
        rollbackFor = [OptimisticLockingFailureException::class]
    )
    override fun post(name: String): Mono<Continent> {
        return continentRepository.save(
            ContinentEntity(
                UUID.randomUUID().toString(),
                name
            )
        ).flatMap {
            Mono.just(it.castToApi())
        }
    }

    @Transactional(
        value = SlaveDataSourceProperties.transactionManager,
        readOnly = true
    )
    override fun countries(): Flux<Country> {
        return countryRepository.findAll()
            .flatMap {
                Mono.zip(
                    continentRepository.findById(it.continent),
                    Mono.just(it)
                )
            }.flatMap { tuple ->
                val (continent, country) = tuple
                Mono.just(country.castToApi(
                    continent.castToApi()
                ))
            }
    }

    @Transactional(
        value = MasterDataSourceProperties.transactionManager,
        rollbackFor = [OptimisticLockingFailureException::class]
    )
    override fun post(country: Country): Mono<Country> {
        return country.continent.id ?. let { continentId ->
            countryRepository.save(
                CountryEntity(
                    UUID.randomUUID().toString(),
                    country.name,
                    continentId
                )
            ).flatMap { en ->
                Mono.just(
                    country.apply {
                        this.id = en.id
                        this.version = en.version
                    }
                )
            }
        } ?: run { Mono.just(country) }
    }

    @Transactional(
        value = SlaveDataSourceProperties.transactionManager,
        readOnly = true
    )
    override fun cities(): Flux<City> {
        return cityRepository.findAll()
            .flatMap {
                Mono.zip(
                    countryRepository.findById(it.country),
                    Mono.just(it)
                )
            }.flatMap {
                val continent = continentRepository.findById(it.t1.continent)
                Mono.zip(
                    continent,
                    Mono.just(it.t1),
                    Mono.just(it.t2)
                )
            }.flatMap { tuple ->
                val (continent, country, city) = tuple
                Mono.just(
                    city.castToApi(
                        country.castToApi(
                            continent.castToApi()
                        )
                    )
                )
            }
    }

    @Transactional(
        value = MasterDataSourceProperties.transactionManager,
        rollbackFor = [OptimisticLockingFailureException::class]
    )
    override fun post(city: City): Mono<City> {
        return city.country.id ?. let { countryId ->
            cityRepository.save(
                CityEntity(
                    UUID.randomUUID().toString(),
                    city.name,
                    city.population,
                    countryId
                )
            ).flatMap { en ->
                Mono.just(
                    city.apply {
                        this.id = en.id
                        this.version = en.version
                    }
                )
            }

        } ?: run { Mono.just(city) }
    }
}