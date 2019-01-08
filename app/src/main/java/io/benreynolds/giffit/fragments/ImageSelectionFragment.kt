package io.benreynolds.giffit.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import io.benreynolds.giffit.R
import io.benreynolds.giffit.viewModels.ImageSelectionViewModel

class ImageSelectionFragment : Fragment() {
    private lateinit var viewModel: ImageSelectionViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(context as FragmentActivity) {
            viewModel = ViewModelProviders.of(this).get(ImageSelectionViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_selection, container, false)
    }
}