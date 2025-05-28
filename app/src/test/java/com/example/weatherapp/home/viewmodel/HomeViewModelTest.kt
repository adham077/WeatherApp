package com.example.weatherapp.home.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.example.weatherapp.model.repository.weather.WeatherRepository.*
import getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repo: WeatherRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        repo = mockk()
        viewModel = HomeViewModel(repo)

        coEvery { repo.getWeatherData(any(), any()) } returns WeatherResult(
            weatherTimed = null,
            status = Status.SUCCESS
        )

        coEvery { repo.getCurrentWeatherData(any()) } returns CurrentWeatherResult(
            currentWeatherResponse = null,
            status = Status.SUCCESS
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getWeather_anyCoordinates_updateWeatherLiveData() = runTest(testDispatcher) {
        viewModel.getWeather(Source.REMOTE, Coordinates(0.0, 0.0))

        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.weatherResult.getOrAwaitValue()
        assertThat(result.status, `is`(Status.SUCCESS))
    }

    @Test
    fun getWeatherCurrent_anyCoordiantes_updateCurrentWeatherLiveData() = runTest(testDispatcher) {
        viewModel.getCurrentWeather( Coordinates(0.0, 0.0))
        testDispatcher.scheduler.advanceUntilIdle()
        val result = viewModel.currentWeatherResult.getOrAwaitValue()

        assertThat(result.status,`is`(Status.SUCCESS))
    }
}