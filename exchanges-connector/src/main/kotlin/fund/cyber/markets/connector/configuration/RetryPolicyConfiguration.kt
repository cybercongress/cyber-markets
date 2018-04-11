package fund.cyber.markets.connector.configuration

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.listener.RetryListenerSupport
import org.springframework.retry.policy.AlwaysRetryPolicy
import org.springframework.retry.support.RetryTemplate

private val log = LoggerFactory.getLogger(DefaultRetryListenerSupport::class.java)!!

class DefaultRetryListenerSupport: RetryListenerSupport() {

    override fun <T : Any?, E : Throwable?> onError(context: RetryContext, callback: RetryCallback<T, E>?,
                                                    throwable: Throwable) {
        if (context.retryCount == 1) log.error("Error occurred. Start retrying...", throwable)
        super.onError(context, callback, throwable)
    }
}

@Configuration
@EnableRetry
class RetryPolicyConfiguration {

    @Bean
    fun retryTemplate(): RetryTemplate {
        return RetryTemplate().apply {
            setBackOffPolicy(ExponentialBackOffPolicy())
            setRetryPolicy(AlwaysRetryPolicy())
            registerListener(DefaultRetryListenerSupport())
        }
    }
}