package io.benreynolds.giffit.fragments

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_CANCELED
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
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.lottie.LottieAnimationView
import io.benreynolds.giffit.R
import io.benreynolds.giffit.enums.GiffiteaError
import io.benreynolds.giffit.enums.GiffiteaLoadingState
import io.benreynolds.giffit.viewmodels.ImageSelectionViewModel
import kotlinx.android.synthetic.main.fragment_image_selection.*
import timber.log.Timber
import java.io.File
import kotlin.properties.Delegates.observable

private const val RC_CAPTURE_IMAGE = 1
private const val RC_WRITE_EXTERNAL_STORAGE = 2

class ImageSelectionFragment : Fragment() {
    private lateinit var viewModel: ImageSelectionViewModel
    private var capturedImage: File? = null
    private var hasExternalStoragePermissions: Boolean by observable(false) { _, _, hasPermission ->
        onExternalStoragePermissionsChanged(hasPermission)
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

        btCaptureImage.setOnClickListener { sendImageCaptureRequest() }
        viewModel.loading.observe(this, Observer<GiffiteaLoadingState> { loadingState ->
            onLoadingStateChanged(loadingState)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CAPTURE_IMAGE) {
            when (resultCode) {
                RESULT_CANCELED -> Timber.d("Image capture request was cancelled")
                RESULT_OK -> {
                    capturedImage?.let { image ->
                        Timber.d("Image captured successfully, requesting related GIF...")
                        viewModel.requestRandomGifForImage(
                            image,
                            onSuccess = { url -> onGifRetrieved(url) },
                            onFailure = { error -> onGifRetrievalFailed(error) }
                        )
                    } ?: Timber.w("Image was captured but null")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)

        when (requestCode) {
            RC_WRITE_EXTERNAL_STORAGE -> {
                hasExternalStoragePermissions = results.firstOrNull() == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(context as FragmentActivity) {
            viewModel = ViewModelProviders.of(this).get(ImageSelectionViewModel::class.java)
        }
    }

    private fun onExternalStoragePermissionsChanged(hasPermission: Boolean) {
        if (!hasPermission) {
            MaterialDialog(requireContext()).show {
                message(R.string.justification_write_external_storage)
                positiveButton(R.string.button_ok)
            }
        } else {
            sendImageCaptureRequest()
        }
    }

    private fun onLoadingStateChanged(loadingState: GiffiteaLoadingState) {
        val isLoading = loadingState != GiffiteaLoadingState.DONE

        if (isLoading) {
            setCaptureButtonState(false)
            showGiffiteaLogo(false)
            showLoadingAnimation(true)
        }

        setStatusText(
            when (loadingState) {
                GiffiteaLoadingState.IDENTIFYING_IMAGE -> R.string.status_identifying_image
                GiffiteaLoadingState.RETRIEVING_GIF -> R.string.status_searching_for_gif
                GiffiteaLoadingState.DONE -> null
            }
        )
    }

    private fun onGifRetrieved(gifUrl: String) {
        Timber.d("GIF received, starting GifDisplayFragment...")

        val gifDisplayFragment = GifDisplayFragment().apply {
            arguments = Bundle().apply {
                putString("URL", gifUrl)
            }
        }

        fragmentManager?.beginTransaction()
            ?.replace(R.id.clRoot, gifDisplayFragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun onGifRetrievalFailed(giffiteaError: GiffiteaError) {
        showGiffiteaLogo(true)
        setCaptureButtonState(true)
        Timber.e("GIF retrieval failed due to '${giffiteaError.name}', showing error dialog..")
        MaterialDialog(requireContext()).show {
            message(R.string.error_request_failed)
            positiveButton(R.string.button_ok)
        }
    }

    private fun sendImageCaptureRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requireContext().checkSelfPermission(
                WRITE_EXTERNAL_STORAGE
            ) == PERMISSION_DENIED
        ) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), RC_WRITE_EXTERNAL_STORAGE)
            return
        }


        Timber.d("Requesting image from camera...")
        val context = requireContext()
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureImageIntent.resolveActivity(context.packageManager) == null) {
            Timber.w("No activity available to handle capture image request")
            MaterialDialog(context).show {
                message(text = "No activity available to handle capture image request")
            }

            return
        }

        capturedImage = File.createTempFile(
            System.currentTimeMillis().toString(),
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )?.also { image ->
            captureImageIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(context, "io.benreynolds.giffit.provider", image)
            )

            startActivityForResult(captureImageIntent, RC_CAPTURE_IMAGE)
        }
    }

    private fun setStatusText(@StringRes resourceId: Int?) {
        if (resourceId != null) {
            Timber.d("Setting status text to '${resources.getResourceEntryName(resourceId)}'...")
            tvStatus.setText(resourceId)
            tvStatus.visibility = TextView.VISIBLE
        } else {
            Timber.d("Hiding status text...")
            tvStatus.text = ""
            tvStatus.visibility = TextView.INVISIBLE
        }
    }

    private fun showLoadingAnimation(visible: Boolean) {
        Timber.d("${if (visible) "Displaying" else "Hiding"} loading animation...")
        avLoadingSpinner.visibility =
                if (visible) LottieAnimationView.VISIBLE else LottieAnimationView.INVISIBLE
    }

    private fun showGiffiteaLogo(visible: Boolean) {
        Timber.d("${if (visible) "Displaying" else "Hiding"} Giffitea logo...")
        ivGiffiteaLogo.visibility = if (visible) ImageView.VISIBLE else ImageView.INVISIBLE
    }

    private fun setCaptureButtonState(interactable: Boolean) {
        Timber.d("${if (interactable) "Enabling" else "Disabling"} capture button...")
        btCaptureImage.isEnabled = interactable
    }
}
