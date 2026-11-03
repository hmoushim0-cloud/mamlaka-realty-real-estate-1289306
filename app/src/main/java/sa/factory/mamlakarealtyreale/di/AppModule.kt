package sa.factory.mamlakarealtyreale.di

import sa.factory.mamlakarealtyreale.data.repository.MamlakaRealtyRepositoryImpl
import sa.factory.mamlakarealtyreale.domain.repository.MamlakaRealtyRepository
import sa.factory.mamlakarealtyreale.domain.usecase.*
import sa.factory.mamlakarealtyreale.hardware.HardwareSensorDriver
import sa.factory.mamlakarealtyreale.ui.viewmodel.MamlakaRealtyViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: MamlakaRealtyRepository = MamlakaRealtyRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): MamlakaRealtyViewModel = MamlakaRealtyViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
