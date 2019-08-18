package net.corda.yo

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor

@CordaService
class VaultListenerService(services: AppServiceHub) : SingletonSerializeAsToken() {

    companion object {
        @JvmStatic
        private val logger = loggerFor<VaultListenerService>()

        const val REQUEST_YO = "Request"
        const val RESPONSE_YO = "Response"
    }
    init {
        services.vaultService.updates.subscribe { update ->
            val yoStateAndRefs = update.produced.filter { it.state.data is YoState && (it.state.data as YoState).yo == REQUEST_YO}
            yoStateAndRefs.forEach {
                val yoState = it.state.data as YoState
                logger.info("Participants: ${yoState.participants}")
                logger.info("YoState: [origin: ${yoState.origin}, target: ${yoState.target}, yo: ${yoState.yo}]")

                try {
                    val yoFlow = YoFlow(yoState.origin, RESPONSE_YO)
                    services.startFlow(yoFlow)
                } catch (e: Exception) {
                    logger.error("Error wile sending yo [$RESPONSE_YO] message to ${yoState.origin}", e)
                }
            }

        }
    }
}