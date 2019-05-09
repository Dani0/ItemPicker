package com.kotlin.itempickerandroid

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textview.setOnClickListener { onShowPickerClicked() }
    }

    private fun onNoClicked(){
        Toast.makeText(this, "No", Toast.LENGTH_LONG).show()
    }

    private fun onYesClicked(){
        Toast.makeText(this, "Yes", Toast.LENGTH_LONG).show()
    }

    private fun onShowPickerClicked(){
        val picker = layoutInflater.inflate(R.layout.item_picker, rootView as ViewGroup, false)

        val mItemPicker : ItemPicker<Model> = picker.findViewById(R.id.itemPicker)
        mItemPicker
            .withItems(Model(""), getModelsList())
            .withOffset(2)
            .withTitle("Picker")
            .withView(picker)
            .withNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->  onNoClicked()})
            .withPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which -> onYesClicked() })
        textview.setOnClickListener { onShowPickerClicked() }
        mItemPicker.show()
    }

    private fun getModelsList(): List<Model> {
        val list = mutableListOf<Model>()
        for(i in 0..100){
            list.add(Model("Model $i"))
        }

        return list
    }

    class Model(var name: String) {

        override fun toString(): String {
            return name
        }
    }
}


