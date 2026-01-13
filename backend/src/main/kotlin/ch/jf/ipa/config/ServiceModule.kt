package ch.jf.ipa.config

import ch.jf.ipa.service.CriteriaProvider
import ch.jf.ipa.service.CriteriaService
import ch.jf.ipa.service.EvaluationService
import ch.jf.ipa.service.GradingService
import ch.jf.ipa.service.IpaPersonCriteriaProvider
import ch.jf.ipa.service.IpaService
import ch.jf.ipa.service.MetadataService
import ch.jf.ipa.service.PdfCriteriaParser
import ch.jf.ipa.service.PdfImportService
import ch.jf.ipa.service.PersonCriteriaProvider
import org.koin.dsl.bind
import org.koin.dsl.module

val serviceModule = module {
    single { CriteriaService(get()) } bind CriteriaProvider::class
    single { MetadataService(get()) }
    single { IpaService(get()) }
    single { PdfCriteriaParser() }
    single { PdfImportService(get(), get(), get(), get()) }

    single { IpaPersonCriteriaProvider(get()) } bind PersonCriteriaProvider::class
    single { EvaluationService(get(), get()) }
    single { GradingService(get(), get()) }
}
