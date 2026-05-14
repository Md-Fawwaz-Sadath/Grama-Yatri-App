package com.gramayatri.app.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.gramayatri.app.R
import com.gramayatri.app.data.repository.UserPrefsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    @Inject lateinit var prefsRepository: UserPrefsRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)
            val destination = if (prefsRepository.isFirstLaunch()) {
                R.id.onboardingFragment
            } else {
                R.id.homeFragment
            }
            findNavController().navigate(
                destination,
                null,
                navOptions {
                    popUpTo(R.id.splashFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    private companion object {
        const val SPLASH_DELAY_MS = 500L
    }
}
