package pl.krzysztof.weather

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import pl.krzysztof.weather.databinding.FragmentStartBinding
import java.util.*
import okhttp3.*
import org.json.JSONException
import java.text.Normalizer


@Suppress("DEPRECATION")
class StartFragment : Fragment() {

    private var _binding : FragmentStartBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentCityName: String = "current city"
    // domyślna lokalizacja to poznań (w przypadku błędu wczytania lokacji)
    private var latitude: Double = 52.4018280
    private var longitude: Double = 16.9510806

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocation()

        binding.idIVSearch.setOnClickListener(){
            val city = binding.idEdtCity.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter city Name", Toast.LENGTH_SHORT).show()
            } else {
                val weatherDataFragment = WeatherDataFragment.newInstance(city)
                findNavController().navigate(R.id.action_startFragment_to_weatherDataFragment, weatherDataFragment.arguments)
            }
        }

        if (latitude != 0.0 && longitude != 0.0){
            binding.idBtnGetCurrentWeather.setOnClickListener(){
                val city = currentCityName
                if (city.isEmpty()) {
                    Toast.makeText(requireContext(), "Failed to get data for current location", Toast.LENGTH_SHORT).show()
                }else {
                    val weatherDataFragment = WeatherDataFragment.newInstance(city)
                    findNavController().navigate(R.id.action_startFragment_to_weatherDataFragment, weatherDataFragment.arguments)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLocation(){
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        val location = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener {
            if (it != null){
                latitude = it.latitude
                longitude = it.longitude
            }
        }

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        if (addressList != null) {
            currentCityName = addressList[0].locality.toString()
            binding.idTVCurrentCity.text = currentCityName
            getBasicData(currentCityName)
        }
    }

    private fun getBasicData(currentCity: String){
        val normalizedCurrentCity = removePolishAccents(currentCity)
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=dac14f07b3b342a88ca150354231205&q=$normalizedCurrentCity&days=1&aqi=yes&alerts=yes"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val currentTime = response.getJSONObject("location").getString("localtime")
                    val timeText = requireActivity().getString(R.string.time_placeholder, currentTime)
                    binding.idTVCurrentDate.text = timeText

                    val temperature = response.getJSONObject("current").getString("temp_c")
                    val temperatureText = requireActivity().getString(R.string.temperature_placeholder, temperature)
                    binding.idTVCurrentTemperature.text = temperatureText

                    val conditionIcon = response.getJSONObject("current")
                        .getJSONObject("condition").getString("icon")
                    Picasso.get()
                        .load("https:".plus(conditionIcon)).resize(400, 400)
                        .into(binding.idIVCurrentIcon)

                    val wind = response.getJSONObject("current").getString("wind_kph")
                    val windText = requireActivity().getString(R.string.wind_speed_placeholder, wind)
                    binding.idTVCurrentWind.text = windText

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to get user location weather data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun removePolishAccents(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(normalized, "")
    }
}
