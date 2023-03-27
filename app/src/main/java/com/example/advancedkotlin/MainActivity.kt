package com.example.advancedkotlin // Sovelluksen paketin nimi.

import android.graphics.Color // Tarvitaan Tummalle taustavärille kun valitaan teksti, jota halutaan muokata tai poistaa (rivi 148)
import android.view.LayoutInflater // XML tiedostojen muuttamista view-olioksi (rivi 131)
import android.view.ViewGroup // Viewholder olion säiliö (rivi 126)
import androidx.recyclerview.widget.RecyclerView //Resycleview on lista jota voi selata alaspäin (rivit 120 ja 123)
import androidx.appcompat.app.AppCompatActivity // Tukikirjasto joka parantaa yhteensopivuutta (rivi 15)
import android.os.Bundle // Käytetään datan siirtoon aktiviteettien välillä. (rivi 24)
import androidx.recyclerview.widget.LinearLayoutManager // Lajittelee RecycleViewin listan lineaarisesti. (Rivi 34)
import com.google.firebase.firestore.FirebaseFirestore // Tällä luokalla päästään käsiksi Firebase tietokantaan. (Rivi 19)
import com.google.firebase.firestore.PropertyName // Firebase luokka joka mahdollistaa kenttien nimen hakemisne dokumentista tai kokoelmasta.
import com.example.advancedkotlin.databinding.ActivityMainBinding //Databinding luokka joka mahdollistaa acitivity_main.xml sitomisen
import com.example.advancedkotlin.databinding.ItemLayoutBinding //Databinding luokka joka mahdollistaa item_layout.xml sitomisen

class MainActivity : AppCompatActivity() {

    // Alustetaan viewbinding, Firebase tietokanta, itemList, ja itemAdapter
    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val itemList = mutableListOf<Item>()
    private lateinit var itemAdapter: ItemAdapter

    // OnCreate metodi suoritetaan aina, kun sovellus käynnistetään tai kun sovellusprosessi on luotu uudelleen.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Käytetään view bindingiä näkymän inflatoimiseen
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Asetetaan aktiviteetin näkymän juurielementiksi (root)
        setContentView(binding.root)

        // itemAdapter ja RecyclerView olion luonti
        itemAdapter = ItemAdapter(itemList)
        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = itemAdapter
        }
        // Datan lataus Firebasesta
        loadData()

        // Asetetaan onClickListener "Create" napille
        binding.btnCreate.setOnClickListener {
            // Kun painetaa "Create" Luodaan nimetty tiedosto Firebaseen ja päivitetään käyttöliittymä.
            val name = binding.etName.text.toString()
            //Nimikenttä ei saa olla tyhjä, muuten tiedostoa ei lisätä.
            if (name.isNotBlank()) {
                addItem(name)
                binding.etName.setText("")
            }
        }

        // Asetetaan onClickListener "Update" napille
        binding.btnUpdate.setOnClickListener {
            // Kun painetaa "Update" päivitetään valittu tiedosto Firebaseen ja päivitetään käyttöliittymä.
            val name = binding.etName.text.toString()
            if (name.isNotBlank()) {
                updateItem(name)
                binding.etName.setText("")
            }
        }

        // Asetetaan onClickListener "Delete" napille.
        binding.btnDelete.setOnClickListener {
            //  Kun "Delete" painetaan, poistetaan valittu tiedosto Firebasesta ja päivitetään käyttöliittymä.
            deleteItem()
        }
    }

    // Ladataan dataa Firebasesta
    private fun loadData() {
        db.collection("items").get().addOnSuccessListener { documents ->
            // Käydään läpi tietokanta ja lisätään ne listalle
            for (document in documents) {
                val item = Item(document.id, document.getString("name") ?: "")
                itemList.add(item)
            }
            // Ilmoittaa adapterille jos data on muuttunut
            itemAdapter.notifyDataSetChanged()
        }
    }

    // Lisätään tiedosto Firebaseen ja päivitetään käyttöliittymä
    private fun addItem(name: String) {
        val newItem = Item("", name)
        db.collection("items").add(newItem).addOnSuccessListener { documentReference ->
            newItem.id = documentReference.id
            itemList.add(newItem)
            itemAdapter.notifyItemInserted(itemList.size - 1)
        }
    }

    // Päivitetään valittu tiedosto Firebaseen ja päivitetään käyttöliittymä
    private fun updateItem(name: String) {
        val selectedItem = itemList.find { it.isSelected }
        selectedItem?.let { item ->
            item.name = name
            item.isSelected = false
            db.collection("items").document(item.id).update("name", name).addOnSuccessListener {
                itemAdapter.notifyDataSetChanged()
            }
        }
    }

    // Poistetaan valittu tiedosto Firebasesta ja päivitetään käyttöliittymä
    private fun deleteItem() {
        val selectedItem = itemList.find { it.isSelected }
        selectedItem?.let { item ->
            db.collection("items").document(item.id).delete().addOnSuccessListener {
                itemList.remove(item)
                itemAdapter.notifyDataSetChanged()
            }
        }
    }

    //Data luokka, eli mitä tietokantaan tallennetaan
    data class Item(
        @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
        @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
        @get:PropertyName("isSelected") @set:PropertyName("isSelected") var isSelected: Boolean = false
    )


    inner class ItemAdapter(private val items: List<Item>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root)

        // Called when RecyclerView needs a new ViewHolder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Käyttää LayoutInflateria muuntamaan XML-tiedoston layoutiksi.
            val binding =
                ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        // Kutsutaan kun RecyclerView haluaa bindata dataa ViewHolderiin
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // ottaa tiedoston halutusta paikasta
            val item = items[position]
            // Bindaa tiedoston nimi TextViewiin, ViewHolder layoutissa
            holder.binding.tvName.text = item.name
            // OnClickListeneri ViewHolder's root näkymään
            holder.binding.root.setOnClickListener {
                // Laittaa päälle/pois valitun tiedoston tilan ja päivittää käyttöliittymän
                item.isSelected = !item.isSelected
                notifyDataSetChanged()
            }
            // Valitun tiedoston tausta muuttuu tummaksi kun se on valittu
            holder.binding.root.setBackgroundColor(if (item.isSelected) Color.LTGRAY else Color.WHITE)
        }

        // Kutsutaan kun RecyclerViewin tarvitsee tietää kuinka monta tiedostoa on data setissä
        override fun getItemCount(): Int = items.size
    }

}
