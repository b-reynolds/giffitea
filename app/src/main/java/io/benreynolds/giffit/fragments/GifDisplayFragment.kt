package io.benreynolds.giffit.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.benreynolds.giffit.R
import kotlinx.android.synthetic.main.fragment_gif_display.*
import timber.log.Timber

class GifDisplayFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gif_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(R.raw.giphy_logo)
            .into(ivGiphyLogo)

        arguments?.getString("URL")?.let {
            setStatusText(R.string.status_downloading_gif)
            showLoadingAnimation(true)

            Glide.with(requireContext())
                .load(it)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setStatusText(null)
                        showLoadingAnimation(false)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
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
