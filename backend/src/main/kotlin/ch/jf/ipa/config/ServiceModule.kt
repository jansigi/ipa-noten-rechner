package ch.jf.ipa.config

import ch.jf.ipa.service.CriteriaProvider
import ch.jf.ipa.service.CriteriaService
import ch.jf.ipa.service.GradingService
import org.koin.dsl.module

val serviceModule = module {
    single<CriteriaProvider> { CriteriaService() }
    single { GradingService(get(), get()) }
}

