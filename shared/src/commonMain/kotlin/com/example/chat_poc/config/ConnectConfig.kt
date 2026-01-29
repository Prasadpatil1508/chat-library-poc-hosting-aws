package com.example.chat_poc.config

/**
 * AWS Connect / start-chat API configuration (host → library).
 *
 * **Do not hardcode values.** The host app should load these from its environment
 * (e.g. .env, BuildConfig, xcconfig) and pass [ConnectConfig] when calling the library.
 *
 * @param apiGatewayUrl Base URL of the start-chat API (e.g. from API_GATEWAY env).
 * @param contactFlowId Contact flow ID (e.g. from CONTACT_FLOW_ID env).
 * @param instanceId Connect instance ID (e.g. from INSTANCE_ID env).
 * @param region AWS region (e.g. from REGION env). Used to call AWS Participant Service (participant-connect.{region}.amazonaws.com) for connect and send.
 */
data class ConnectConfig(
    val apiGatewayUrl: String,
    val contactFlowId: String,
    val instanceId: String,
    val region: String,
) {
    fun isValid(): Boolean =
        apiGatewayUrl.isNotBlank() &&
            contactFlowId.isNotBlank() &&
            instanceId.isNotBlank() &&
            region.isNotBlank()
}
