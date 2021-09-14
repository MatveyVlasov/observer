package ru.elections.observer.title

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.graphics.toColorInt
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hyy.highlightpro.HighlightPro
import com.hyy.highlightpro.parameter.Constraints
import com.hyy.highlightpro.parameter.HighlightParameter
import com.hyy.highlightpro.parameter.MarginOffset
import com.hyy.highlightpro.shape.RectShape
import com.hyy.highlightpro.util.dp
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.ElectionViewModelFactory
import ru.elections.observer.R
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentTitleBinding
import ru.elections.observer.databinding.GuideStep1Binding


class TitleFragment : Fragment() {
    lateinit var binding: FragmentTitleBinding
    lateinit var viewModel: ElectionViewModel
    lateinit var guideView: GuideStep1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_title, container, false)
        guideView = DataBindingUtil.inflate(inflater,
            R.layout.guide_step_1, container, false)

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ElectionViewModel::class.java)

        binding.electionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { exitApp() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.navigateToMainFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                navigateToMain()
                viewModel.doneNavigatingToMain()
            }
        })

        viewModel.navigateToPastFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                navigateToPast()
                viewModel.doneNavigatingToPast()
            }
        })

        viewModel.isTraining.observe(viewLifecycleOwner, {
            if (it == true) showHighlightSteps()
        })

        viewModel.guideText.observe(viewLifecycleOwner, {
            guideView.tvTips.text = it
        })

        binding.buttonAbout.setOnClickListener {
            onAboutButton()
        }
    }

    private fun getConstraints(): List<Constraints> =
        getVerticalConstraint() + geHorizontalConstraint()

    private fun getVerticalConstraint() = Constraints.TopToBottomOfHighlight

    private fun geHorizontalConstraint() = Constraints.StartToStartOfHighlight

    private fun navigateToMain() {
        findNavController().navigate(
            TitleFragmentDirections.actionTitleFragmentToMainFragment()
        )
    }

    private fun navigateToPast() {
        findNavController().navigate(
            TitleFragmentDirections.actionTitleFragmentToPastFragment()
        )
    }

    private fun showHighlightSteps() {
        val translateAnimation = TranslateAnimation(-500f,0f,0f,0f)
        translateAnimation.duration = 500
        translateAnimation.interpolator = BounceInterpolator()

        HighlightPro.with(this)
            .setHighlightParameter {
                HighlightParameter.Builder()
                    .setHighlightViewId(R.id.button_new_election)
                    .setTipsView(guideView.root)
                    .setHighlightShape(RectShape(4f.dp, 4f.dp, 6f))
                    .setHighlightHorizontalPadding(8f.dp)
                    .setConstraints(getConstraints())
                    .setMarginOffset(MarginOffset(4.dp, 4.dp, 4.dp, 4.dp))
                    .setTipViewDisplayAnimation(translateAnimation)
                    .build()
            }
            .setHighlightParameter {
                HighlightParameter.Builder()
                    .setHighlightViewId(R.id.button_past_elections)
                    .setTipsView(guideView.root)
                    .setHighlightShape(RectShape(4f.dp, 4f.dp, 6f))
                    .setHighlightHorizontalPadding(8f.dp)
                    .setConstraints(getConstraints())
                    .setMarginOffset(MarginOffset(4.dp, 4.dp, 4.dp, 4.dp))
                    .setTipViewDisplayAnimation(translateAnimation)
                    .build()
            }
            .setHighlightParameter {
                HighlightParameter.Builder()
                    .setHighlightViewId(R.id.button_about)
                    .setTipsView(guideView.root)
                    .setHighlightShape(RectShape(4f.dp, 4f.dp, 6f))
                    .setHighlightHorizontalPadding(8f.dp)
                    .setConstraints(getConstraints())
                    .setMarginOffset(MarginOffset(4.dp, 4.dp, 4.dp, 4.dp))
                    .setTipViewDisplayAnimation(translateAnimation)
                    .build()
            }
            .setBackgroundColor("#80000000".toColorInt())
            .setOnShowCallback {
                viewModel.guideTextChanged(when (it) {
                    0 -> "123"
                    1 -> "456"
                    else -> "789"
                })
            }
            .show()
    }

    private fun onAboutButton() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.about_app))
            .setMessage(
                HtmlCompat.fromHtml(getString(R.string.about_app_text), HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton(getString(R.string.ok),null)
            .show()
    }

    private fun exitApp() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.exit))
            .setMessage(getString(R.string.exit_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> finishAffinity(requireActivity()) }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
