package io.benreynolds.giffit.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import io.benreynolds.giffit.R
import io.benreynolds.giffit.viewModels.ImageSelectionViewModel
import kotlinx.android.synthetic.main.fragment_image_selection.*
import java.io.File

private const val RC_CAPTURE_IMAGE = 1

class ImageSelectionFragment : Fragment() {
  private lateinit var viewModel: ImageSelectionViewModel
  private var capturedImage: File? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)

    with(context as FragmentActivity) {
      viewModel = ViewModelProviders.of(this).get(ImageSelectionViewModel::class.java)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_image_selection, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    btCapturePhoto.setOnClickListener { requestImageFromCamera() }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == RC_CAPTURE_IMAGE && resultCode == RESULT_OK) {
      capturedImage?.let {
        viewModel.onImageSelected(it) {
          Glide.with(requireContext())
            .load(it)
            .into(ivGiffiteaLogo)
        }
      }
    }
  }

  private fun requestImageFromCamera() {
    val context = requireContext()
    val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (captureImageIntent.resolveActivity(context.packageManager) == null) {
      return
    }

    capturedImage = File.createTempFile(
      System.currentTimeMillis().toString(),
      ".jpg",
      context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )

    capturedImage?.let {
      captureImageIntent.putExtra(
        MediaStore.EXTRA_OUTPUT,
        FileProvider.getUriForFile(requireContext(), "io.benreynolds.giffit.provider", it)
      )

      startActivityForResult(captureImageIntent, RC_CAPTURE_IMAGE)
    }
  }
}