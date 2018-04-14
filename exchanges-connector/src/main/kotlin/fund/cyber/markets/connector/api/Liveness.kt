package fund.cyber.markets.connector.api

import fund.cyber.markets.connector.ConnectorRunner
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Liveness(
        private val connectorRunner: ConnectorRunner
) {

    @GetMapping("/liveness")
    fun isAlive(): ResponseEntity<Any> {

        connectorRunner.connectors.forEach { connector ->
            if (!connector.isAlive()) {
                return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

        return ResponseEntity(HttpStatus.OK)
    }

}