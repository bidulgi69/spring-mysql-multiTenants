package kr.dove.mysql.api

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CountryService {

    @GetMapping(
        value = ["/countries"],
        produces = [MediaType.APPLICATION_NDJSON_VALUE]
    )
    fun countries(): Flux<Country>

    @PostMapping(
        value = ["/country"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun post(@RequestBody country: Country): Mono<Country>
}