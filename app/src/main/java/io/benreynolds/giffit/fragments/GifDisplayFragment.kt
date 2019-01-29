package io.benreynolds.giffit.fragments

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
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
import io.benreynolds.giffit.enums.GiffiteaError
import io.benreynolds.giffit.viewmodels.GifDisplayViewModel
import kotlinx.android.synthetic.main.fragment_gif_display.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class GifDisplayFragment : Fragment() {
    private lateinit var viewModel: GifDisplayViewModel
    private var gifFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gif_display, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        setHasOptionsMenu(true)
        with(context as FragmentActivity) {
            viewModel = ViewModelProviders.of(this).get(GifDisplayViewModel::class.java)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)

        menuInflater.inflate(R.menu.gif_display_menu, menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item_share -> {
                gifFile?.let { gifFile ->
                    Intent(ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(
                            Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                requireContext(),
                                "io.benreynolds.giffit.provider",
                                gifFile
                            )
                        )
                    }
                }?.also {
                    startActivity(Intent.createChooser(it, "Share GIF"))
                }
            }
            else -> super.onOptionsItemSelected(menuItem)
        }

        return true
    }

    private fun onGifDownloaded(gifFile: File) {
        requireActivity().runOnUiThread {
            this.gifFile = gifFile
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
