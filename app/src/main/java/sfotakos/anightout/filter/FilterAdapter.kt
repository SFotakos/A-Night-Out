package sfotakos.anightout.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_filter.view.*
import sfotakos.anightout.R


class FilterAdapter(items: List<String> = listOf(
        "accounting", "airport", "amusement_park", "aquarium", "art_gallery", "atm", "bakery", "bank", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley",
        "bus_station", "cafe", "campground", "car_dealer", "car_rental", "car_repair", "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store",
        "convenience_store", "courthouse", "dentist", "department_store", "doctor", "electrician", "electronics_store", "embassy", "fire_station", "florist", "funeral_home",
        "furniture_store", "gas_station", "gym", "hair_care", "hardware_store", "hindu_temple", "home_goods_store", "hospital", "insurance_agency", "jewelry_store", "laundry",
        "lawyer", "library", "liquor_store", "local_government_office", "locksmith", "lodging", "meal_delivery", "meal_takeaway", "mosque", "movie_rental", "movie_theater",
        "moving_company", "museum", "night_club", "painter", "park", "parking", "pet_store", "pharmacy", "physiotherapist", "plumber", "police", "post_office",
        "real_estate_agency", "restaurant", "roofing_contractor", "rv_park", "school", "shoe_store", "shopping_mall", "spa", "stadium", "storage", "store", "subway_station",
        "supermarket", "synagogue", "taxi_stand", "train_station", "transit_station", "travel_agency", "veterinary_care", "zoo")) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var filterItems : MutableList<Pair<String, Boolean>> = mutableListOf()

    init {
        items.map { it to false }.toCollection(filterItems)
        println(items)
        println(filterItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false))
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.itemView.filterItemLabel.text = filterItems[position].first
        setDesign(holder.itemView.filterItemLabel, holder.itemView.context, filterItems[position].second)

        holder.itemView.filterItemLabel.setOnClickListener {
            filterItems[position] = filterItems[position].copy(second = !filterItems[position].second)
            setDesign(holder.itemView.filterItemLabel, holder.itemView.context, filterItems[position].second)
            println(filterItems)
        }

    }

    private fun setDesign(view: TextView, context: Context, hasBeenSelected: Boolean){
        if (hasBeenSelected){
            view.background = ContextCompat.getDrawable(context, R.drawable.item_filter_selected_background)
            view.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            view.background = ContextCompat.getDrawable(context, R.drawable.item_filter_background)
            view.setTextColor(ContextCompat.getColor(context, R.color.material_color_white_70_percent))
        }
    }

    override fun getItemCount(): Int {
        return filterItems.size
    }

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    }
}