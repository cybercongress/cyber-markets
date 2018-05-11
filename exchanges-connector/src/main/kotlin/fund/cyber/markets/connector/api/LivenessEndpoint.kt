package fund.cyber.markets.connector.api

import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LivenessEndpoint(
        private val connectorRunner: ConnectorRunner
) {

    @GetMapping("/liveness")
    fun isAlive(): ResponseEntity<Any> {

        connectorRunner.tradesConnectors.forEach { entry ->
            if (!entry.value.isAlive()) {
                return ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE)
            }
        }

        return ResponseEntity(HttpStatus.OK)
    }

}