package fund.cyber.markets.connector.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.AlwaysRetryPolicy
import org.springframework.retry.support.RetryTemplate

@Configuration
@EnableRetry
class RetryPolicyConfiguration {

    @Bean
    fun retryTemplate(): RetryTemplate {
        return RetryTemplate().apply {
            setBackOffPolicy(ExponentialBackOffPolicy())
            setRetryPolicy(AlwaysRetryPolicy())
        }
    }
}