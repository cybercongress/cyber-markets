package fund.cyber.markets.service

import fund.cyber.markets.model.ExchangeMetadata
import fund.cyber.markets.storage.RethinkDbService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled

/**
 * Base class for exchanges metadata services.
 *
 *  Service working lifecycle
 *  1) Spring context initialized
 *  2) [updateMetadataJob] method scheduled
 *  3) on first invocation [updateMetadataJob] try to initialize [ExchangeMetadata] firstly from http, and if exchange
 *      due to some reason is unavailable, than from rethink db
 *  4) if both unavailable, than step 3 will repeated till succeed initialization
 *  5) after succeed initialization [ExchangeMetadataInitializedEvent] is send
 *  6) [ExchangeMetadataInitializedEvent] will be pickuped by websocket handlers to initialize connection
 *  7) after that [updateMetadataJob] will be used to update exchange metainfo
 *
 */
abstract class ExchangeMetadataService<out M : ExchangeMetadata>(
        val exchange: String,
        private val metadataClass: Class<M>,
        private val rethinkDbService: RethinkDbService,
        private val eventPublisher: ApplicationEventPublisher
) {

    private val LOG = LoggerFactory.getLogger(ExchangeMetadataService::class.java)

    private var cachedMetadata: M? = null

    @Scheduled(fixedDelay = 1 * 60 * 1000, initialDelay = 5 * 1000)
    private fun updateMetadataJob() {
        LOG.info("Update $exchange metadata job is started")
        if (cachedMetadata == null) {
            cachedMetadata = initializeMetadata()
            cachedMetadata?.let { eventPublisher.publishEvent(ExchangeMetadataInitializedEvent(metadata = it)) }

        } else {
            //todo update mechanism
        }
    }

    fun initializeMetadata(): M? {
        LOG.info("Going to initialize $exchange metadata")
        var metadata = internalGetMetadataFromExchange()
        if (metadata == null) {
            LOG.info("Fallback into accessing $exchange metadata cached in rethink")
            metadata = getMetadataFromDb()
        }
        if (metadata == null) LOG.info("$exchange metadata was not initialized")
        return metadata
    }

    private fun getMetadataFromDb(): M? {
        try {
            return rethinkDbService.getExchangeMetadata(exchange, clazz = metadataClass)
        } catch (exception: Exception) {
            LOG.error("Cant access $exchange metadata in rethinkdb", exception)
            return null
        }
    }

    private fun internalGetMetadataFromExchange(): M? {
        try {
            LOG.info("Accessing $exchange metadata by api")
            return getMetadataFromExchange()
        } catch (exception: Exception) {
            LOG.error("Cant access $exchange metadata by api", exception)
            return null
        }
    }

    protected abstract fun getMetadataFromExchange(): M
}

class ExchangeMetadataInitializedEvent<out M : ExchangeMetadata>(
        val metadata: M
) : ApplicationEvent(metadata)



