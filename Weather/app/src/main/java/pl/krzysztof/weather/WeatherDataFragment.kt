package pl.krzysztof.weather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import pl.krzysztof.weather.databinding.FragmentWeatherDataBinding
import java.text.Normalizer

class WeatherDataFragment : Fragment() {

    private var _binding: FragmentWeatherDataBinding? = null
    private val binding get() = _binding!!
    private lateinit var city: String
    private lateinit var weatherRVModelArrayList: ArrayList<Data>
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherDataBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val givenCity = arguments?.getString("city")
        if (givenCity != null) {
            city = capitalizeFirstLetter(givenCity)
        }
        binding.idTVCityName.text = city

        weatherRVModelArrayList = ArrayList()
        adapter = UserAdapter(weatherRVModelArrayList)
        binding.idRVWeather.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.idRVWeather.adapter = adapter

        getWeatherData()

        binding.idBtnBack.setOnClickListener(){
            findNavController().navigate(R.id.action_weatherDataFragment_to_startFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getWeatherData() {
        val cityName = city
        val normalizedCurrentCity = removePolishAccents(cityName)
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=dac14f07b3b342a88ca150354231205&q=$normalizedCurrentCity&days=1&aqi=yes&alerts=yes"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // PROGNOZA POGODY NA JEDEN DZIEŃ
                    val currentTime = response.getJSONObject("location").getString("localtime")
                    val timeText = requireActivity().getString(R.string.time_placeholder, currentTime)
                    binding.idTVTimeCity.text = timeText

                    val temperature = response.getJSONObject("current").getString("temp_c")
                    val temperatureText = requireActivity().getString(R.string.temperature_placeholder, temperature)
                    binding.idTVTemperature.text = temperatureText

                    val condition = response.getJSONObject("current")
                        .getJSONObject("condition").getString("text")
                    val conditionIcon = response.getJSONObject("current")
                        .getJSONObject("condition").getString("icon")
                    Picasso.get()
                        .load("https:".plus(conditionIcon)).resize(400, 400)
                        .into(binding.idIVIcon)
                    binding.idTVCondition.text = condition

                    val wind = response.getJSONObject("current").getString("wind_kph")
                    val windText = requireActivity().getString(R.string.wind_speed_placeholder, wind)
                    binding.idTVWind.text = windText

                    val pressure = response.getJSONObject("current").getString("pressure_mb")
                    val pressureText = requireActivity().getString(R.string.pressure_placeholder, pressure)
                    binding.idTVPressure.text = pressureText

                    val humidity = response.getJSONObject("current").getString("humidity")
                    val humidityText = requireActivity().getString(R.string.humidity_placeholder, humidity)
                    binding.idTVHumidity.text = humidityText

                    val forecastObj = response.getJSONObject("forecast")
                    val forcast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                    val hourArray = forcast0.getJSONArray("hour")
                    val lista = ArrayList<Data>()

                    for (i in 0 until hourArray.length()) {
                        val hourObj = hourArray.getJSONObject(i)
                        val time = hourObj.getString("time")
                        val temper = hourObj.getString("temp_c")
                        val img = hourObj.getJSONObject("condition").getString("icon")
                        lista.add(Data(time, temper, img))
                    }
                    weatherRVModelArrayList.clear()
                    weatherRVModelArrayList.addAll(lista)
                    adapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid city name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    companion object {
        fun newInstance(city: String): WeatherDataFragment {
            val fragment = WeatherDataFragment()
            val args = Bundle()
            args.putString("city", city)
            fragment.arguments = args
            return fragment
        }
    }
    private fun removePolishAccents(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(normalized, "")
    }

    fun capitalizeFirstLetter(input: String): String {
        if (input.isBlank()) {
            return input
        }

        val firstChar = input[0]
        val capitalizedFirstChar = firstChar.uppercaseChar()

        return capitalizedFirstChar + input.substring(1)
    }

}
