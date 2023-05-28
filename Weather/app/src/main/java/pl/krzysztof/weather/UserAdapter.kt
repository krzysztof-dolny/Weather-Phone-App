package pl.krzysztof.weather

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso
import pl.krzysztof.weather.databinding.WetharRvItemBinding
import java.util.*
import kotlin.collections.ArrayList

class UserAdapter(private val weatherRVModelArrayList: ArrayList<Data>) : RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    inner class MyViewHolder(binding: WetharRvItemBinding) : ViewHolder(binding.root) {
        val timeTV = binding.idTVCardTime
        val temperatureTV = binding.idTVCardTemperature
        val conditionIV = binding.idIVCondition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = WetharRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return weatherRVModelArrayList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = weatherRVModelArrayList[position]

        holder.temperatureTV.text = data.cardTemperature + " °C"

        val hourAndMinute = if (data.cardTime.length >= 16) {
            data.cardTime.substring(11, 16)
        } else {
            " "
        }
        holder.timeTV.text = hourAndMinute

        Picasso.get().load("https:" + data.cardIcon).into(holder.conditionIV)
    }
}