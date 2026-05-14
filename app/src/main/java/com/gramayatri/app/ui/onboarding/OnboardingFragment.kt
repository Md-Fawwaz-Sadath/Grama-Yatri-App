package com.gramayatri.app.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.snackbar.Snackbar
import com.gramayatri.app.R
import com.gramayatri.app.data.repository.FirebaseRepository
import com.gramayatri.app.data.repository.UserPrefsRepository
import com.gramayatri.app.databinding.FragmentOnboardingBinding
import com.gramayatri.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    @Inject lateinit var repository: FirebaseRepository
    @Inject lateinit var prefsRepository: UserPrefsRepository

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private var isSubmitting = false
    private val languageCodes = listOf(Constants.LANGUAGE_ENGLISH, Constants.LANGUAGE_KANNADA)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        binding.displayNameEditText.setText(
            prefsRepository.getDisplayName().takeUnless { it == "Anonymous" }.orEmpty()
        )
        setupLanguageSelector()
        binding.startButton.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun setupLanguageSelector() {
        val languageNames = listOf(
            getString(R.string.language_english),
            getString(R.string.language_kannada)
        )
        binding.onboardingLanguageSpinner.adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languageNames
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as? TextView)?.apply {
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 16f
                    setPadding(8, 0, 8, 0)
                }
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val selectedIndex = languageCodes.indexOf(prefsRepository.getLanguageCode()).coerceAtLeast(0)
        binding.onboardingLanguageSpinner.setSelection(selectedIndex)
    }

    private fun finishOnboarding() {
        if (isSubmitting) return
        setSubmitting(true)

        viewLifecycleOwner.lifecycleScope.launch {
            if (repository.getCurrentUid() == null) {
                repository.signInAnonymously()
                    .onFailure { error ->
                        setSubmitting(false)
                        showError(error.message ?: getString(R.string.onboarding_error))
                        return@launch
                    }
            }

            val displayName = binding.displayNameEditText.text?.toString()?.trim().orEmpty()
            if (displayName.isNotBlank()) {
                prefsRepository.setDisplayName(displayName)
            }
            val currentLanguage = prefsRepository.getLanguageCode()
            val selectedLanguage = languageCodes.getOrElse(
                binding.onboardingLanguageSpinner.selectedItemPosition
            ) {
                Constants.LANGUAGE_ENGLISH
            }
            prefsRepository.setLanguageCode(selectedLanguage)
            prefsRepository.setFirstLaunchDone()
            if (selectedLanguage != currentLanguage) {
                requireActivity().recreate()
                return@launch
            }
            findNavController().navigate(
                R.id.homeFragment,
                null,
                navOptions {
                    popUpTo(R.id.onboardingFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting
        binding.onboardingProgress.visibility = if (submitting) View.VISIBLE else View.GONE
        binding.startButton.isEnabled = !submitting
        binding.displayNameEditText.isEnabled = !submitting
        binding.onboardingLanguageSpinner.isEnabled = !submitting
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
