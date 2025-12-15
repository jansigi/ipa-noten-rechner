package ch.jf.ipa.config

import ch.jf.ipa.repository.CriterionProgressRepository
import ch.jf.ipa.repository.CriterionProgressRepositoryImpl
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.repository.PersonRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<PersonRepository> { PersonRepositoryImpl() }
    single<CriterionProgressRepository> { CriterionProgressRepositoryImpl() }
}

