package ph.project.active.di

import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(FakeDataModule, ViewModelModule, platformModule())
