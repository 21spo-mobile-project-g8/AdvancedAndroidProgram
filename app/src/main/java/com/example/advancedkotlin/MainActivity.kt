// Import necessary packages
package com.example.advancedkotlin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.example.advancedkotlin.databinding.ActivityMainBinding
import com.example.advancedkotlin.databinding.ItemLayoutBinding

// MainActivity class
class MainActivity : AppCompatActivity() {

    // Initialize view binding, Firebase Firestorm, item list, and item adapter
    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val itemList = mutableListOf<Item>()
    private lateinit var itemAdapter: ItemAdapter

    // OnCreate method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the item adapter and RecyclerView
        itemAdapter = ItemAdapter(itemList)
        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = itemAdapter
        }
        // Load data from Firebase
        loadData()
        // Set onClickListener for add button
        binding.btnAdd.setOnClickListener {
            // Add item to Firebase and update UI
            val name = binding.etName.text.toString()
            if (name.isNotBlank()) {
                addItem(name)
                binding.etName.setText("")
            }
        }

        // Set onClickListener for update button
        binding.btnUpdate.setOnClickListener {
            // Update selected item in Firebase and update UI
            val name = binding.etName.text.toString()
            if (name.isNotBlank()) {
                updateItem(name)
                binding.etName.setText("")
            }
        }

        // Set onClickListener for delete button
        binding.btnDelete.setOnClickListener {
            // Delete selected item from Firebase and update UI
            deleteItem()
        }
    }

    // Load data from Firebase
    private fun loadData() {
        db.collection("items").get().addOnSuccessListener { documents ->
            // Iterate through documents and add them to the item list
            for (document in documents) {
                val item = Item(document.id, document.getString("name") ?: "")
                itemList.add(item)
            }
            // Notify the adapter that the data set has changed
            itemAdapter.notifyDataSetChanged()
        }
    }

    // Add item to Firebase and update UI
    private fun addItem(name: String) {
        val newItem = Item("", name)
        db.collection("items").add(newItem).addOnSuccessListener { documentReference ->
            newItem.id = documentReference.id
            itemList.add(newItem)
            itemAdapter.notifyItemInserted(itemList.size - 1)
        }
    }

    // Update selected item in Firebase and update UI
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

    // Delete selected item from Firebase and update UI
    private fun deleteItem() {
        val selectedItem = itemList.find { it.isSelected }
        selectedItem?.let { item ->
            db.collection("items").document(item.id).delete().addOnSuccessListener {
                itemList.remove(item)
                itemAdapter.notifyDataSetChanged()
            }
        }
    }

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
            // Inflate the item layout and return a new ViewHolder
            val binding =
                ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        // Called when RecyclerView wants to bind data to a ViewHolder
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Get the item at the given position
            val item = items[position]
            // Bind the item's name to the TextView in the ViewHolder's layout
            holder.binding.tvName.text = item.name
            // Set an OnClickListener for the ViewHolder's root view
            holder.binding.root.setOnClickListener {
                // Toggle the selected state of the item and update the UI
                item.isSelected = !item.isSelected
                notifyDataSetChanged()
            }
            // Set the background color of the ViewHolder's root view based on the selected state of the item
            holder.binding.root.setBackgroundColor(if (item.isSelected) Color.LTGRAY else Color.WHITE)
        }

        // Called when RecyclerView needs to know how many items are in the data set
        override fun getItemCount(): Int = items.size
    }

}
