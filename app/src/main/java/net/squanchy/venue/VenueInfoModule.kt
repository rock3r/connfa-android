package net.squanchy.venue

import dagger.Module
import dagger.Provides
import net.squanchy.service.firebase.FirestoreDbService
import net.squanchy.service.repository.AuthService

@Module
internal class VenueInfoModule {

    @Provides
    fun venueInfoService(dbService: FirestoreDbService, authService: AuthService): VenueInfoService = FirestoreVenueInfoService(
        dbService,
        authService
    )
}
