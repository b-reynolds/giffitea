package io.benreynolds.giffit.fragments

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import io.benreynolds.giffit.R
import io.benreynolds.giffit.viewmodels.GiffiteaError
import io.benreynolds.giffit.viewmodels.GiffiteaLoadingState
import io.benreynolds.giffit.viewmodels.ImageSelectionViewModel
import kotlinx.android.synthetic.main.fragment_image_selection.*
import timber.log.Timber
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

    private fun onLoadingStateChanged(loadingState: GiffiteaLoadingState) {
        val isLoading = loadingState != GiffiteaLoadingState.DONE

        showLoadingAnimation(isLoading)
        showGiffiteaLogo(!isLoading)
        showCaptureButton(!isLoading)

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
        Timber.e("GIF retrieval failed due to '${giffiteaError.name}', showing error dialog..")
        MaterialDialog(requireContext()).show {
            message(R.string.error_request_failed)
            positiveButton(R.string.button_ok)
        }
    }

    private fun sendImageCaptureRequest() {
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
        tvStatus.visibility = if (visible) TextView.VISIBLE else TextView.INVISIBLE
    }

    private fun showGiffiteaLogo(visible: Boolean) {
        Timber.d("${if (visible) "Displaying" else "Hiding"} Giffitea logo...")
        ivGiffiteaLogo.visibility = if (visible) ImageView.VISIBLE else ImageView.INVISIBLE
    }

    private fun showCaptureButton(visible: Boolean) {
        Timber.d("${if (visible) "Displaying" else "Hiding"} capture button...")
        btCaptureImage.visibility = if (visible) Button.VISIBLE else Button.INVISIBLE
    }
}
