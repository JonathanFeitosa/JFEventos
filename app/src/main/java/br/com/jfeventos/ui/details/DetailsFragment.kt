package br.com.jfeventos.ui.details

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.jfeventos.databinding.FragmentDetailsBinding
import br.com.jfeventos.model.CheckIn
import br.com.jfeventos.model.Event
import br.com.jfeventos.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val viewModel by viewModel<DetailsViewModel>()
    private lateinit var mBinding: FragmentDetailsBinding
    private var loading = false
    private var idEvent : Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDetailsBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        idEvent = arguments?.getLong("id")

        if(idEvent == null){
            showDialogError(requireContext(), "Evento indisponivel")
            findNavController().popBackStack()
            return
        }
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
                            showDialogSucess(requireContext(), "CheckIn realizado com sucesso!")
                            loading = false
                         //   mBinding.swipeRefresh.isRefreshing = false
                        }
                        is NetworkResponse.Error -> {
                            mBinding.progressBar.visibility = View.GONE
                            showDialogError(requireContext(), resource.exception)
                            loading = false
                      //      mBinding.swipeRefresh.isRefreshing = false
                        }
                        is NetworkResponse.Loading -> {
                            mBinding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })
    }

    private fun setLocationEvent(context: Context, long: String, lat: String) = try {
        val geocoder = Geocoder(context, getBrazilianLocation())
        val addresses = geocoder.getFromLocation(lat.toDouble(), long.toDouble(), 1)
        mBinding.eventLocaleView.bind(addresses[0].subAdminArea,  addresses[0].adminArea)
        mBinding.eventInfoLocale.text = addresses[0].getAddressLine(0)
    } catch (ex: Exception) {
     //   cityEvent.text = "Indefinido"
    }

    private fun retrieveList(event: Event) {
        mBinding.eventDateView.bind(event.date)
        mBinding.eventHourView.bind(event.date)
        BidingUtils.loadImageView(mBinding.ownerPhoto, event.image)
        setLocationEvent(requireContext(), event.longitude, event.latitude)
        mBinding.eventDescription.text = event.description
    }

    override fun onRefresh() {
        TODO("Not yet implemented")
    }
}