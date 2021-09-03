package ru.elections.observer

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentMainBinding
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.elections.observer.database.Election

class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(inflater,
                        R.layout.fragment_main, container, false)

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ElectionViewModel::class.java)

        binding.electionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.pollingStationEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.pollingStationEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onPollingStationChosen(text.toInt())
            }
            false
        }

        binding.totalVotersEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.totalVotersEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onTotalVotersChosen(text.toInt())
            }
            false
        }

        viewModel.currentElection.observe(viewLifecycleOwner, {
            Log.i("MainFragment", "Check if null")
            Log.i("MainFragment", it.toString())
            if (it == null) navigateToTitle()
            else {
                binding.pollingStationNumber.text =
                    if (it.pollingStation == -1)  "-" else it.pollingStation.toString()
                binding.totalVotersNumber.text =
                    if (it.totalVoters == -1)  "-" else it.totalVoters.toString()
            }
        })

        binding.pollingStationIconEdit.setOnClickListener {
            onPollingStationIconEditSelected()
        }

        binding.totalVotersIconEdit.setOnClickListener {
            onTotalVotersIconEditSelected()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            AlertDialog.Builder(context)
                .setTitle("Выход")
                .setMessage("Вы действительно хотите выйти из приложения?")
                .setPositiveButton("Да") { _, _ -> ActivityCompat.finishAffinity(requireActivity()) }
                .setNegativeButton("Нет") { _, _ ->  }
                .show()
        }

        return binding.root
    }

    private fun navigateToTitle() {
        Log.i("MainFragment", "Navigate to title")
        this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToTitleFragment())
    }

    private fun onPollingStationChosen(station: Int) {
        binding.apply {
            pollingStationEdit.visibility = View.GONE
            pollingStationIconEdit.visibility = View.VISIBLE
            pollingStationNumber.visibility = View.VISIBLE
            viewModel.onPollingStationChanged(station)
        }
    }

    private fun onTotalVotersChosen(voters: Int) {
        binding.apply {
            totalVotersEdit.visibility = View.GONE
            totalVotersIconEdit.visibility = View.VISIBLE
            totalVotersNumber.visibility = View.VISIBLE
            viewModel.onTotalVotersChanged(voters)
        }
    }

    private fun onPollingStationIconEditSelected() {
        binding.apply {
            pollingStationEdit.visibility = View.VISIBLE
            pollingStationIconEdit.visibility = View.GONE
            pollingStationNumber.visibility = View.GONE
            if (viewModel.currentElection.value?.pollingStation == -1) {
                pollingStationEdit.text = Editable.Factory.getInstance().newEditable("")
            }
        }
    }

    private fun onTotalVotersIconEditSelected() {
        binding.apply {
            totalVotersEdit.visibility = View.VISIBLE
            totalVotersIconEdit.visibility = View.GONE
            totalVotersNumber.visibility = View.GONE
            if (viewModel.currentElection.value?.totalVoters == -1) {
                totalVotersEdit.text = Editable.Factory.getInstance().newEditable("")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish_election -> {
                AlertDialog.Builder(context)
                    .setTitle("Завершить выборы")
                    .setMessage("Вы действительно хотите завершить выборы?")
                    .setPositiveButton("Да") { _, _ -> onFinishYes() }
                    .setNegativeButton("Нет") { _, _ ->  }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFinishYes() {
        navigateToTitle()
        viewModel.finishElection()
        Toast.makeText(context, "Выборы успешно завершены", Toast.LENGTH_LONG).show()
    }
}