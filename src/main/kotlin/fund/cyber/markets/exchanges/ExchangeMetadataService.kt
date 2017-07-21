package fund.cyber.markets.exchanges

import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.model.ExchangeMetadataInitializedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled


/**
 * Base class for exchanges metadata services.
 *
 *  Service working lifecycle
 *  1) Spring context initialized
 *  2) [updateMetadataJob] method scheduled
 *  3) on first invocation [updateMetadataJob] try to initialize [ExchangeMetadata] from exchange
 *  4) if both unavailable, than step 3 will repeated till succeed initialization
 *  5) after succeed initialization [ExchangeMetadataInitializedEvent] is send
 *  6) [ExchangeMetadataInitializedEvent] will be pickuped by websocket handlers to initialize connection
 *  7) after that [updateMetadataJob] will be used to update exchange metadata
 *
 */
abstract class ExchangeMetadataService<out M : ExchangeMetadata>(val exchange: String) {

    private val LOG = LoggerFactory.getLogger(ExchangeMetadataService::class.java)

    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher

    private var isInitialized = false

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 5 * 1000)
    private fun updateMetadataJob() {
        if (!isInitialized) {
            internalInitializeMetadata()
        } else {
            updateMetadata()
        }
    }

    private fun internalInitializeMetadata() {
        try {
            LOG.info("Initialize $exchange metadata job is started")
            initializeMetadata()
            isInitialized = true
            LOG.info("Initialize $exchange metadata job completed")
            eventPublisher.publishEvent(ExchangeMetadataInitializedEvent(exchange))
        } catch (exception: Exception) {
            LOG.info("Initialize $exchange metadata job failed")
        }
    }

    private fun internalUpdateMetadata() {
        try {
            LOG.info("Update $exchange metadata job is started")
            updateMetadata()
            LOG.info("Update $exchange metadata job completed")
            //todo send update event
        } catch (exception: Exception) {
            LOG.info("Update $exchange metadata job failed")
        }
    }

    protected abstract fun initializeMetadata()
    protected abstract fun updateMetadata()
    abstract fun getMetadata(): M
}
