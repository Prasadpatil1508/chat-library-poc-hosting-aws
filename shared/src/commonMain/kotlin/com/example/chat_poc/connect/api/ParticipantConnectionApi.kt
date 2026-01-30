package com.example.chat_poc.connect.api

import com.example.chat_poc.connect.model.ConnectionDetails

/**
 * Abstraction for participant connection (AWS CreateParticipantConnection or proxy).
 * Use [AwsParticipantConnectionApi] to call AWS directly with participant token + region.
 */
interface ParticipantConnectionApi {
    suspend fun getConnection(region: String, participantToken: String): Result<ConnectionDetails>
}
