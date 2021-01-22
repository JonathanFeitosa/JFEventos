package com.example.jfeventos.ui.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jfeventos.databinding.FragmentDetailsBinding
import com.example.jfeventos.model.CheckIn
import com.example.jfeventos.model.Event
import com.example.jfeventos.utils.BidingUtils
import com.example.jfeventos.utils.NetworkResponse
import com.example.jfeventos.utils.showError
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val viewModel by viewModel<DetailsViewModel>()
    private lateinit var mBinding: FragmentDetailsBinding
    private var loading = false
    private var idEvent = arguments?.getLong("id")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDetailsBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.getEventDetail(requireActivity(), idEvent ?: 1L)

        setupEventDetailObserver()

        mBinding.eventCheckIn.bind(idEvent = idEvent ?: 1L, onCheckInSelected = { checkin ->
            // FAZER CHECKIN AQUI!
            viewModel.sendEventCheckIn(requireContext(), checkin)
            setupCheckInObserver(checkin)
        })

    }

    private fun setupEventDetailObserver() {
        viewModel.getEventDetail(requireContext(), idEvent ?: 1L)
            .observe(requireActivity(), {
                it?.let { resource ->
                    when (resource) {
                        is NetworkResponse.Success -> {
                            mBinding.progressBar.visibility = View.GONE
                            retrieveList(resource.data)
                            loading = false
                            //mBinding.swipeRefresh.isRefreshing = false
                        }
                        is NetworkResponse.Error -> {
                            mBinding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), resource.exception, Toast.LENGTH_LONG)
                                .show()
                            loading = false
                            //    mBinding.swipeRefresh.isRefreshing = false
                        }
                        is NetworkResponse.Loading -> {
                            mBinding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })
    }

    private fun setupCheckInObserver(checkin: CheckIn) {
        viewModel.sendEventCheckIn(requireContext(), checkin)
            .observe(requireActivity(), {
                it?.let { resource ->
                    when (resource) {
                        is NetworkResponse.Success -> {
                            mBinding.progressBar.visibility = View.GONE
                            resource.data.body()
                            Log.i("ResultadoJFS", "CheckIn realizado! ${resource.data.body()}")
                            Log.i("ResultadoJFS", "CheckIn realizado! ${resource.data.code()}")
                            loading = false
                            //mBinding.swipeRefresh.isRefreshing = false
                        }
                        is NetworkResponse.Error -> {
                            mBinding.progressBar.visibility = View.GONE
//                            Toast.makeText(requireContext(), resource.exception, Toast.LENGTH_LONG)
//                                .show()
                            showError(requireContext(), resource.exception)
                            loading = false
                            //    mBinding.swipeRefresh.isRefreshing = false
                        }
                        is NetworkResponse.Loading -> {
                            mBinding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })
    }

    private fun retrieveList(event: Event) {
        mBinding.eventDateView.bind(event.date)
        mBinding.eventHourView.bind(event.date)
        BidingUtils.loadImageView(mBinding.ownerPhoto, event.image)
        mBinding.eventLocaleView.bind("Manaus", "Amazonas")
        mBinding.eventDescription.text = event.description
        mBinding.eventInfoLocale.text = event.latitude
    }

    override fun onRefresh() {
        TODO("Not yet implemented")
    }
}