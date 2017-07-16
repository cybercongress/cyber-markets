package fund.cyber.markets.hitbtc

import org.springframework.stereotype.Component

@Component
open class HitBtcMetaInformation(
        var channelSymbolForTokensPair: Map<String, HitBtcTokensPair> = HashMap()
)