package ch.jf.ipa.config

import ch.jf.ipa.repository.CriteriaRepository
import ch.jf.ipa.repository.CriteriaRepositoryImpl
import ch.jf.ipa.repository.CriterionProgressRepository
import ch.jf.ipa.repository.CriterionProgressRepositoryImpl
import ch.jf.ipa.repository.IpaRepository
import ch.jf.ipa.repository.IpaRepositoryImpl
import ch.jf.ipa.repository.MetadataRepository
import ch.jf.ipa.repository.MetadataRepositoryImpl
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.repository.PersonRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<PersonRepository> { PersonRepositoryImpl() }
    single<CriterionProgressRepository> { CriterionProgressRepositoryImpl() }
    single<CriteriaRepository> { CriteriaRepositoryImpl() }
    single<MetadataRepository> { MetadataRepositoryImpl() }
    single<IpaRepository> { IpaRepositoryImpl() }
}

