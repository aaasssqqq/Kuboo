package com.sethchhim.kuboo_client.ui.main.browser.adapter

import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.like.LikeButton
import com.like.OnLikeListener
import com.matrixxun.starry.badgetextview.MaterialBadgeTextView
import com.sethchhim.kuboo_client.BaseApplication
import com.sethchhim.kuboo_client.Extensions.colorFilterGrey
import com.sethchhim.kuboo_client.Extensions.colorFilterNull
import com.sethchhim.kuboo_client.Extensions.colorFilterRed
import com.sethchhim.kuboo_client.Extensions.fadeInvisible
import com.sethchhim.kuboo_client.Extensions.fadeVisible
import com.sethchhim.kuboo_client.Extensions.gone
import com.sethchhim.kuboo_client.Extensions.invisible
import com.sethchhim.kuboo_client.Extensions.isBrowserMediaType
import com.sethchhim.kuboo_client.Extensions.isFileType
import com.sethchhim.kuboo_client.Extensions.visible
import com.sethchhim.kuboo_client.R
import com.sethchhim.kuboo_client.Settings
import com.sethchhim.kuboo_client.data.ViewModel
import com.sethchhim.kuboo_client.data.enum.Source
import com.sethchhim.kuboo_client.data.model.Browser
import com.sethchhim.kuboo_client.data.model.ReadData
import com.sethchhim.kuboo_client.ui.main.browser.BrowserBaseFragmentImpl1_Content
import com.sethchhim.kuboo_client.ui.main.browser.custom.BrowserContentGridLayoutManager
import com.sethchhim.kuboo_client.ui.main.browser.custom.BrowserContentRecyclerView
import com.sethchhim.kuboo_client.util.DiffUtilHelper
import com.sethchhim.kuboo_client.util.SystemUtil
import com.sethchhim.kuboo_remote.model.Book
import kotlinx.android.synthetic.main.browser_item_content_folder.view.*
import kotlinx.android.synthetic.main.browser_item_content_media.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.sdk25.coroutines.onClick
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BrowserContentAdapter(val browserFragment: BrowserBaseFragmentImpl1_Content, val viewModel: ViewModel) : BaseMultiItemQuickAdapter<Browser, BrowserContentAdapter.BrowserHolder>(viewModel.browserRepository.contentList) {

    init {
        BaseApplication.appComponent.inject(this)
        setHasStableIds(true)

        addItemType(Browser.FOLDER, R.layout.browser_item_content_folder)
        addItemType(Browser.MEDIA, R.layout.browser_item_content_media)
    }

    @Inject lateinit var context: Context
    @Inject lateinit var systemUtil: SystemUtil

    private lateinit var browserContentRecyclerView: BrowserContentRecyclerView
    private lateinit var layoutManager: BrowserContentGridLayoutManager

    private val mainActivity = browserFragment.mainActivity

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.browserContentRecyclerView = recyclerView as BrowserContentRecyclerView
        this.layoutManager = browserContentRecyclerView.layoutManager as BrowserContentGridLayoutManager
    }

    override fun convert(holder: BrowserHolder, item: Browser) {
        //load new data
        holder.bookId = item.book.id
        when (holder.itemViewType) {
            Browser.FOLDER -> FolderHandler().process(holder, item)
            Browser.MEDIA -> MediaHandler().process(holder, item)
        }
    }

    override fun onViewRecycled(holder: BrowserHolder) {
        super.onViewRecycled(holder)
        //reset views to default state
        holder.bookId = -1
        when (holder.itemViewType) {
            Browser.FOLDER -> {
                Glide.with(browserFragment).clear(holder.itemView.browser_item_content_folder_imageView3)
                holder.itemView.browser_item_content_folder_imageView1.visible()
                holder.itemView.browser_item_content_folder_imageView2.visible()
                holder.itemView.browser_item_content_folder_imageView3.invisible()
                holder.itemView.browser_item_content_folder_materialBadgeTextView.invisible()
                if (!holder.itemView.browser_item_content_folder_likeButton.isLiked) holder.itemView.browser_item_content_folder_likeButton.isLiked = false
            }
            Browser.MEDIA -> holder.itemView.browser_item_content_media_imageView.colorFilterNull()
        }
    }

    override fun getItemId(position: Int) = data[position].book.id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowserHolder {
        val holder = super.onCreateViewHolder(parent, viewType)
        holder.setLikeButtonVisibility()
        holder.itemView.onClick { holder.onItemSelected() }
        holder.itemView.setOnLongClickListener { holder.onItemLongSelected() }
        return holder
    }

    inner class BrowserHolder(view: View) : BaseViewHolder(view) {
        internal val isRoot = viewModel.getPathPosition() <= 1
        internal var bookId = -1

        internal fun onItemSelected() {
            val isStateSelected = browserFragment.mainActivity.isMenuStateSelected()
            when (isStateSelected) {
                true -> onItemSelectedMenuStateIsSelected()
                false -> onItemSelectedMenuStateIsUnselected()
            }
        }

        private fun onItemSelectedMenuStateIsSelected() {
            val book = viewModel.getBrowserContentItemAt(adapterPosition)?.book
            if (book != null && book.isFileType()) {
                browserFragment.mainActivity.enableSelectionMode(this, book)
            }
        }

        private fun onItemSelectedMenuStateIsUnselected() {
            val book = viewModel.getBrowserContentItemAt(adapterPosition)?.book
            when (book?.isFileType()) {
                true -> startPreview(book)
                false -> populateContent(book)
            }
        }

        internal fun onItemLongSelected(): Boolean {
            val isStateSelected = browserFragment.mainActivity.isMenuStateSelected()
            when (isStateSelected) {
                true -> onItemLongSelectedMenuStateIsSelected()
                false -> onItemLongSelectedMenuStateIsUnselected()
            }
            return true
        }

        private fun onItemLongSelectedMenuStateIsUnselected() {
            val book = data[adapterPosition].book
            if (book.isFileType()) {
                browserFragment.mainActivity.enableSelectionMode(this, book)
            }
        }

        private fun onItemLongSelectedMenuStateIsSelected() = browserFragment.mainActivity.disableSelectionMode()

        private fun startPreview(book: Book) {
            val previewUrl = book.getPreviewUrl(Settings.THUMBNAIL_SIZE_RECENT)
            val imageView = itemView.browser_item_content_media_imageView
            imageView.transitionName = previewUrl
            mainActivity.startPreview(ReadData(book = book, sharedElement = imageView, source = Source.BROWSER))
        }

        private fun populateContent(book: Book) {
            launch(UI) {
                //prevent double click
                itemView.isClickable = false

                //load new data
                delay(100, TimeUnit.MILLISECONDS)
                try {
                    browserFragment.populateContent(book, loadState = false)
                } catch (e: Exception) {
                    //views could be destroyed during delay, do nothing
                }

                //restore click
                delay(500, TimeUnit.MILLISECONDS)

                try {
                    itemView.isClickable = true
                } catch (e: Exception) {
                    //views could be destroyed during delay, do nothing
                }
            }
        }

        internal fun setLikeButtonVisibility() = itemView.browser_item_content_folder_likeButton?.let { if (!Settings.FAVORITE) it.gone() }
    }

    private inner class FolderHandler {
        internal fun process(holder: BrowserHolder, item: Browser) {
            val itemView = holder.itemView
            val book = item.book

            //load new data
            val isFavorite = viewModel.isFavorite(book)

            val isStateSelected = mainActivity.isMenuStateSelected()
            if (isStateSelected) mainActivity.disableSelectionMode()

            itemView.browser_item_content_folder_textView1.text = book.title

            itemView.browser_item_content_folder_textView2.text =
                    when (holder.isRoot && isFavorite) {
                        true -> context.getString(R.string.browser_favorite)
                        false -> when (book.isBrowserMediaType()) {
                            true -> context.getString(R.string.browser_media)
                            false -> context.getString(R.string.browser_folder)
                        }
                    }

            itemView.browser_item_content_folder_imageView4.apply {
                when (book.isBrowserMediaType()) {
                    true -> visible()
                    false -> gone()
                }
            }

            itemView.browser_item_content_folder_likeButton.apply {
                if (Settings.FAVORITE) {
                    isLiked = when (isFavorite) {
                        true -> true
                        false -> false
                    }

                    setOnLikeListener(object : OnLikeListener {
                        override fun liked(likeButton: LikeButton) {
                            viewModel.addFavorite(book).observe(browserFragment, Observer { result ->
                                result?.let { viewModel.setFavoriteList(it) }
                            })
                        }

                        override fun unLiked(likeButton: LikeButton) {
                            viewModel.removeFavorite(book).observe(browserFragment, Observer { result ->
                                result?.let { viewModel.setFavoriteList(it) }
                            })
                        }
                    })
                }
            }

            //slight delay to prevent loading while fast scrolling
            launch(UI) {
                delay(Settings.RECYCLERVIEW_DELAY)
                try {
                    if (holder.bookId == book.id) {
                        itemView.browser_item_content_folder_imageView3.loadFolderThumbnail(holder, book)
                        itemView.browser_item_content_folder_materialBadgeTextView.loadItemCount(holder, book)
                    }
                } catch (e: Exception) {
                    //views could be destroyed during delay, do nothing
                }
            }
        }

        internal fun ImageView.loadFolderThumbnail(holder: BrowserHolder, book: Book) {
            when (book.isBrowserMediaType()) {
                true -> viewModel.getFirstByBook(book).apply {
                    observe(browserFragment, Observer { result ->
                        if (result != null && holder.bookId == book.id) {
                            handleFirstBook(holder, book, result)
                        }
                    })
                }
                false -> {
                    holder.itemView.browser_item_content_folder_imageView1.visible()
                    holder.itemView.browser_item_content_folder_imageView2.visible()
                    holder.itemView.browser_item_content_folder_imageView3.invisible()
                }
            }
        }

        private fun handleFirstBook(holder: BrowserHolder, book: Book, result: Book) {
            val stringUrl = result.server + result.linkThumbnail
            val requestOptions = RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)

            Glide.with(browserFragment)
                    .load(stringUrl)
                    .apply(requestOptions)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Timber.e("Thumbnail failed to load! ${book.server}${book.linkThumbnail}")
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            holder.itemView.browser_item_content_folder_imageView1.invisible()
                            holder.itemView.browser_item_content_folder_imageView2.invisible()
                            holder.itemView.browser_item_content_folder_imageView3.fadeVisible()
                            return false
                        }
                    })
                    .into(holder.itemView.browser_item_content_folder_imageView3)
        }

        private fun MaterialBadgeTextView.loadItemCount(holder: BrowserHolder, book: Book) =
                when (book.isBrowserMediaType()) {
                    true -> viewModel.getItemCountByBook(book).observe(browserFragment, Observer { result ->
                        result?.let {
                            if (holder.bookId == book.id) {
                                holder.itemView.browser_item_content_folder_materialBadgeTextView.setBadgeCount(result.toInt())
                                holder.itemView.browser_item_content_folder_imageView4.fadeInvisible()
                                holder.itemView.browser_item_content_folder_materialBadgeTextView.fadeVisible()
                            }
                        }
                    })
                    false -> visibility = View.INVISIBLE
                }
    }

    private inner class MediaHandler {
        internal fun process(holder: BrowserHolder, item: Browser) {
            val itemView = holder.itemView
            val book = item.book

            itemView.browser_item_content_media_imageView.apply {
                transitionName = item.book.getPreviewUrl(Settings.THUMBNAIL_SIZE_RECENT)

                loadImage(book, object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Timber.e("Thumbnail failed to load! ${book.server}${book.linkThumbnail}")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        setMediaColorState()
                        return false
                    }

                    private fun setMediaColorState() {
                        //set color state locally
                        loadColorState(holder, book)

                        //set color state remotely
                        launch(UI) {
                            //add delay to prevent remote request while fast scrolling
                            delay(Settings.RECYCLERVIEW_DELAY)
                            try {
                                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                val lastVisible = layoutManager.findLastVisibleItemPosition()
                                val currentPosition = holder.adapterPosition
                                if (currentPosition in firstVisible..lastVisible) {
                                    viewModel.getRemoteUserApi(book).observe(browserFragment, Observer { result ->
                                        if (result != null) {
                                            viewModel.updateBrowserItem(result)
                                            loadColorState(holder, result)
                                        }
                                    })
                                }
                            } catch (e: Exception) {
                                //views could be destroyed during delay, do nothing
                            }
                        }

                    }
                })
            }
        }

        private fun ImageView.loadImage(item: Book, requestListener: RequestListener<Drawable>) {
            val stringUrl = item.server + item.linkThumbnail
            val requestOptions = RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.mipmap.ic_launcher)
                    .fitCenter()
                    .dontAnimate()
                    .dontTransform()
                    .disallowHardwareConfig()

            Glide.with(browserFragment)
                    .load(stringUrl)
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .listener(requestListener)
                    .into(this@loadImage)
        }
    }

    internal fun update(result: List<Book>) {
        val diffUtilHelper = DiffUtilHelper(this)
        diffUtilHelper.liveData.observe(browserFragment, Observer {
            if (it == true) onDiffUtilUpdateFinished(result)
        })
        diffUtilHelper.updateBrowserList(data, result)
    }

    private fun onDiffUtilUpdateFinished(result: List<Book>) = viewModel.setBrowserContentList(result)

    internal fun loadColorState(holder: BrowserHolder, book: Book) = holder.itemView.browser_item_content_media_imageView.apply {
        when (viewModel.isSelected(book)) {
            true -> colorFilterRed()
            false ->
                when (Settings.MARK_FINISHED) {
                    true -> when (book.isFinished && holder.bookId == book.id) {
                        true -> colorFilterGrey()
                        false -> colorFilterNull()
                    }
                    false -> colorFilterNull()
                }
        }
    }

    internal fun resetAllColorState() = try {
        (0 until browserContentRecyclerView.childCount)
                .mapNotNull { browserContentRecyclerView.getChildViewHolder(browserContentRecyclerView.getChildAt(it)) as BrowserContentAdapter.BrowserHolder }
                .forEachWithIndex { _, viewHolder ->
                    if (viewHolder.itemViewType == Browser.MEDIA) {
                        viewHolder.itemView.browser_item_content_media_imageView?.let {
                            val data = (browserContentRecyclerView.adapter as BrowserContentAdapter).data
                            val book = data[viewHolder.adapterPosition].book
                            loadColorState(viewHolder, book)
                        }
                    }
                }
    } catch (e: Exception) {
        Timber.e("${e.message}")
    }

}