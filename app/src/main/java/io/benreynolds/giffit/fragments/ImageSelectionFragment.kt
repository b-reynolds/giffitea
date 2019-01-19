package io.benreynolds.giffit.fragments

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Build
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
import com.afollestad.materialdialogs.MaterialDialog
import io.benreynolds.giffit.CallbackManager
import io.benreynolds.giffit.R
import io.benreynolds.giffit.extensions.getFile
import io.benreynolds.giffit.viewModels.ImageSelectionViewModel
import kotlinx.android.synthetic.main.fragment_image_selection.*
import java.io.File

private const val CB_EXTERNAL_STORAGE_PERMISSIONS_GRANTED = 1
private const val CB_EXTERNAL_STORAGE_PERMISSIONS_DENIED = 2
private const val RC_PICK_GALLERY_IMAGE = 1
private const val RC_EXTERNAL_STORAGE_PERMISSIONS = 2
private const val RC_CAPTURE_IMAGE = 3

class ImageSelectionFragment : Fragment() {
  private val callBackManager = CallbackManager<Int>()
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

    if (requireContext().packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA) != true) {
//      fabCamera.hide()
    } else {
//      fabCamera.setOnClickListener { requestImageFromCamera() }
//      fabCamera.show()
    }

//    fabGallery.setOnClickListener { requestImageFromGallery() }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    results: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, results)

    when (requestCode) {
      RC_EXTERNAL_STORAGE_PERMISSIONS -> {
        callBackManager.invokeAll(
          if (results.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            CB_EXTERNAL_STORAGE_PERMISSIONS_GRANTED
          } else {
            CB_EXTERNAL_STORAGE_PERMISSIONS_DENIED
          }
        )
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    val context = requireContext()
    if (requestCode == RC_PICK_GALLERY_IMAGE && resultCode == RESULT_OK && data != null) {
      data.data?.getFile(context)?.let { viewModel.onImageSelected(it) }
    } else if (requestCode == RC_CAPTURE_IMAGE && resultCode == RESULT_OK) {
      capturedImage?.let { viewModel.onImageSelected(it) }
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

  private fun requestImageFromGallery() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val context = requireContext()
      if (context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_DENIED) {
        with(callBackManager) {
          if (get(CB_EXTERNAL_STORAGE_PERMISSIONS_GRANTED).isNullOrEmpty()) {
            add(CB_EXTERNAL_STORAGE_PERMISSIONS_GRANTED) { requestImageFromGallery() }
          }

          if (get(CB_EXTERNAL_STORAGE_PERMISSIONS_DENIED).isNullOrEmpty()) {
            add(CB_EXTERNAL_STORAGE_PERMISSIONS_DENIED) {
              MaterialDialog(context).show {
                title(R.string.app_name)
                message(R.string.reason_external_store_permissions)
                positiveButton(R.string.button_retry_permission_request) {
                  requestImageFromGallery()
                }
                negativeButton(R.string.button_cancel_permission_request)
                cancelable(false)
              }
            }
          }
        }

        requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), RC_EXTERNAL_STORAGE_PERMISSIONS)
        return
      }
    }

    val pickImageIntent = Intent(Intent.ACTION_PICK).apply {
      type = "image/*"
      putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
    }

    startActivityForResult(pickImageIntent, RC_PICK_GALLERY_IMAGE)
  }
}