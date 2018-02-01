package fund.cyber.markets.rest.task

import fund.cyber.markets.cassandra.repository.SupplyRepository
import fund.cyber.markets.rest.configuration.AppContext
import fund.cyber.markets.rest.util.CryptoProxy
import org.slf4j.LoggerFactory

class SupplyResolverTask(
        val supplyRepository: SupplyRepository = AppContext.supplyRepository
): Runnable {

    private val log = LoggerFactory.getLogger(SupplyResolverTask::class.java)!!

    override fun run() {
        log.info("Updating supplies")

        try {
            supplyRepository.saveAll(CryptoProxy.getSupplies())
        } catch (e: Exception) {
            log.error("Error during update supplies", e)
        }
    }

}