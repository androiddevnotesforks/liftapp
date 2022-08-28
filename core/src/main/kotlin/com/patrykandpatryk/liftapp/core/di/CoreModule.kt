package com.patrykandpatryk.liftapp.core.di

import com.patrykandpatryk.liftapp.core.mapper.BodyEntriesToChartEntriesMapper
import com.patrykandpatryk.liftapp.core.text.StringProviderImpl
import com.patrykandpatryk.liftapp.core.ui.name.NameResolverImpl
import com.patrykandpatryk.liftapp.core.validation.HigherThanZero
import com.patrykandpatryk.liftapp.core.validation.HigherThanZeroValidator
import com.patrykandpatryk.liftapp.domain.body.BodyEntry
import com.patrykandpatryk.liftapp.domain.mapper.Mapper
import com.patrykandpatryk.liftapp.domain.model.NameResolver
import com.patrykandpatryk.liftapp.domain.text.StringProvider
import com.patrykandpatryk.liftapp.domain.validation.Validator
import com.patrykandpatryk.vico.core.entry.ChartEntry
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import java.text.Collator
import java.text.DecimalFormat

@Module
@InstallIn(SingletonComponent::class)
internal interface CoreModule {

    @Binds
    fun bindNameSolver(solver: NameResolverImpl): NameResolver

    @Binds
    fun bindBodyEntriesToChartEntriesMapper(
        mapper: BodyEntriesToChartEntriesMapper,
    ): Mapper<List<BodyEntry>, List<List<ChartEntry>>>

    @Binds
    fun bindStringProvider(provider: StringProviderImpl): StringProvider

    companion object {

        @Provides
        fun provideCollator(): Collator = Collator.getInstance()

        @Provides
        @HigherThanZero
        fun provideHigherThanZeroValidator(): Validator<Float> = HigherThanZeroValidator

        @Provides
        @Decimal
        fun provideDecimalFormat(): DecimalFormat = DecimalFormat("#.##")

        @Provides
        @Integer
        fun provideIntegerFormat(): DecimalFormat = DecimalFormat("#")

        @Provides
        fun provideCoroutineExceptionHandler(): CoroutineExceptionHandler =
            CoroutineExceptionHandler { coroutineContext, throwable ->
                Timber.e(throwable, "Uncaught exception in $coroutineContext")
            }
    }
}
