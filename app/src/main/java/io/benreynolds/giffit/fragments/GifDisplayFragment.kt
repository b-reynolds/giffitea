package io.benreynolds.giffit.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.benreynolds.giffit.R
import io.benreynolds.giffit.viewmodels.GifDisplayViewModel
import io.benreynolds.giffit.viewmodels.GiffiteaError
import kotlinx.android.synthetic.main.fragment_gif_display.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class GifDisplayFragment : Fragment() {
    private lateinit var viewModel: GifDisplayViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gif_display, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(context as FragmentActivity) {
            viewModel = ViewModelProviders.of(this).get(GifDisplayViewModel::class.java)
        }
    }

    private fun onGifDownloaded(gifFile: File) {
        requireActivity().runOnUiThread {
            Glide.with(requireActivity())
                .asGif()
                .load(gifFile)
                .listener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setStatusText(null)
                        showLoadingAnimation(false)
                        return false
                    }

                    override fun onResourceReady(
                        resource: GifDrawable?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setStatusText(null)
                        showLoadingAnimation(false)
                        return false
                    }

                })
                .into(ivGif)
        }
    }

    private fun onGifDownloadFailed(giffiteaError: GiffiteaError) {
        setStatusText(null)
        showLoadingAnimation(false)

        Timber.e("GIF retrieval failed due to '${giffiteaError.name}', showing error dialog..")
        MaterialDialog(requireContext()).show {
            message(R.string.error_download_failed)
            positiveButton(R.string.button_ok)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(R.raw.giphy_logo)
            .into(ivGiphyLogo)

        arguments?.getString("URL")?.let { url ->
            setStatusText(R.string.status_downloading_gif)
            showLoadingAnimation(true)

            GlobalScope.launch {
                viewModel.downloadGif(
                    url,
                    onDownloaded = { file -> onGifDownloaded(file) },
                    onDownloadFailed = { error -> onGifDownloadFailed(error) }
                )
            }
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
}
