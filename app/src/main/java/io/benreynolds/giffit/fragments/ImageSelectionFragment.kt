package io.benreynolds.giffit.fragments

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import io.benreynolds.giffit.CallbackManager
import io.benreynolds.giffit.R
import io.benreynolds.giffit.viewModels.ImageSelectionViewModel
import kotlinx.android.synthetic.main.fragment_image_selection.*
import timber.log.Timber
import java.io.File

private const val CB_EXTERNAL_STORAGE_PERMISSIONS_GRANTED = 1
private const val CB_EXTERNAL_STORAGE_PERMISSIONS_DENIED = 2
private const val RC_PICK_GALLERY_IMAGE = 1
private const val RC_EXTERNAL_STORAGE_PERMISSIONS = 2

class ImageSelectionFragment : Fragment() {
  private val callBackManager = CallbackManager<Int>()
  private lateinit var viewModel: ImageSelectionViewModel

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

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    fabGallery.setOnClickListener { requestImageFromGallery() }
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

    if (requestCode == RC_PICK_GALLERY_IMAGE) {
      when (resultCode) {
        RESULT_OK -> {
          val resultData = data?.data
          if (resultData == null) {
            Timber.e("RC_PICK_GALLERY_IMAGE returned $resultCode but data was null")
            return
          }

          imageView.setImageBitmap(
            BitmapFactory.decodeFile(resultData.getFile().absolutePath)
          )
        }
      }
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

  private fun Uri.getFile(): File {
    val cursor = requireContext().contentResolver.query(this, null, null, null, null)
      ?: return File(path)
    cursor.use {
      it.moveToFirst()
      return File(it.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)))
    }
  }
}