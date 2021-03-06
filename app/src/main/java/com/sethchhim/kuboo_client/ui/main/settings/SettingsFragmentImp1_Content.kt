package com.sethchhim.kuboo_client.ui.main.settings

import android.widget.TextView
import com.sethchhim.kuboo_client.Extensions.toHourMinuteSecond
import com.sethchhim.kuboo_client.R
import com.sethchhim.kuboo_client.Settings
import com.sethchhim.kuboo_client.util.DialogUtil
import org.jetbrains.anko.sdk25.coroutines.onClick


open class SettingsFragmentImp1_Content : SettingsFragmentImp0_View() {

    override fun onResume() {
        super.onResume()

        setFavoritePreference()
        setLoginPreference()
        setMarkFinishedPreference()
        setPreviewPreference()
        setReverseLayoutPreference()
        setThemePreference()
        setOrientationPreference()
        setVolumePageTurnPreference()

        setAboutVersionPreference()

        setEpubTextZoomPreference()
        setEpubMarginPreference()

        setComicDualPanePreference()
        setComicRtlPreference()
        setComicScaleTypePreference()

        setDownloadSavePath()
        setDownloadTrackingLimit()
        setDownloadTrackingInterval()

        setHomeLayout()
    }

    private fun setAboutVersionPreference() = aboutVersionPreference.apply {
        summary = systemUtil.getVersionName()
    }

    private fun setDownloadSavePath() = downloadSavePath.apply {
        summary = Settings.DOWNLOAD_SAVE_PATH
        setOnPreferenceClickListener {
            val storageList = mainActivity.systemUtil.getStorageList()
            val storageListFormatted = mainActivity.systemUtil.getStorageListFormatted()
            dialogUtil.getDialogDownloadSavePath(mainActivity, storageList, storageListFormatted, object : DialogUtil.OnDialogSelectSingleChoice {
                override fun onSelect(which: Int) {
                    val path = storageList[which]
                    Settings.DOWNLOAD_SAVE_PATH = path
                    sharedPrefsHelper.saveDownloadSavePath()
                    summary = Settings.DOWNLOAD_SAVE_PATH
                }
            }).show()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setDownloadTrackingLimit() = downloadTrackingLimit.apply {
        summary = "${Settings.DOWNLOAD_TRACKING_LIMIT}"
        setOnPreferenceClickListener {
            dialogUtil.getDialogTrackingLimit(mainActivity).apply {
                show()

                val textView = findViewById<TextView>(R.id.dialog_layout_settings_tracking_limit_textView0)!!
                val buttonDecrease = findViewById<TextView>(R.id.dialog_layout_settings_tracking_limit_button0)!!
                val buttonIncrease = findViewById<TextView>(R.id.dialog_layout_settings_tracking_limit_button1)!!

                textView.text = "${Settings.DOWNLOAD_TRACKING_LIMIT}"
                buttonDecrease.onClick {
                    Settings.DOWNLOAD_TRACKING_LIMIT -= 1
                    if (Settings.DOWNLOAD_TRACKING_LIMIT < 1) Settings.DOWNLOAD_TRACKING_LIMIT = 1
                    sharedPrefsHelper.saveDownloadTrackingLimit()
                    textView.text = "${Settings.DOWNLOAD_TRACKING_LIMIT}"
                    summary = "${Settings.DOWNLOAD_TRACKING_LIMIT}"
                }
                buttonIncrease.onClick {
                    Settings.DOWNLOAD_TRACKING_LIMIT += 1
                    sharedPrefsHelper.saveDownloadTrackingLimit()
                    textView.text = "${Settings.DOWNLOAD_TRACKING_LIMIT}"
                    summary = "${Settings.DOWNLOAD_TRACKING_LIMIT}"
                }
            }
            return@setOnPreferenceClickListener true
        }
    }

    private fun setDownloadTrackingInterval() = downloadTrackingInterval.apply {
        setOnPreferenceClickListener {
            dialogUtil.getDialogTrackingInterval(mainActivity).apply {
                setOnDismissListener { mainActivity.setDownloadTrackingService() }

                show()
                val textView = findViewById<TextView>(R.id.dialog_layout_settings_tracking_interval_textView0)!!
                val buttonDecrease = findViewById<TextView>(R.id.dialog_layout_settings_tracking_interval_button0)!!
                val buttonIncrease = findViewById<TextView>(R.id.dialog_layout_settings_tracking_interval_button1)!!

                textView.text = "${Settings.DOWNLOAD_TRACKING_INTERVAL} ${getString(R.string.settings_minutes)}"
                buttonDecrease.onClick {
                    when (Settings.DOWNLOAD_TRACKING_INTERVAL <= 10) {
                        true -> {
                            Settings.DOWNLOAD_TRACKING_INTERVAL -= 1
                            if (Settings.DOWNLOAD_TRACKING_INTERVAL < 1) Settings.DOWNLOAD_TRACKING_INTERVAL = 1
                        }
                        false -> {
                            Settings.DOWNLOAD_TRACKING_INTERVAL -= 10
                            if (Settings.DOWNLOAD_TRACKING_INTERVAL < 10) Settings.DOWNLOAD_TRACKING_INTERVAL = 10
                        }
                    }
                    sharedPrefsHelper.saveDownloadTrackingInterval()
                    textView.text = "${Settings.DOWNLOAD_TRACKING_INTERVAL} ${getString(R.string.settings_minutes)}"
                    summary = "${Settings.DOWNLOAD_TRACKING_INTERVAL} ${getString(R.string.settings_minutes)} (${mainActivity.timeUntilLiveData.value?.toHourMinuteSecond()} ${getString(R.string.settings_remaining)})"
                }
                buttonIncrease.onClick {
                    when (Settings.DOWNLOAD_TRACKING_INTERVAL < 10) {
                        true -> {
                            Settings.DOWNLOAD_TRACKING_INTERVAL += 1
                            if (Settings.DOWNLOAD_TRACKING_INTERVAL < 1) Settings.DOWNLOAD_TRACKING_INTERVAL = 1
                        }
                        false -> {
                            Settings.DOWNLOAD_TRACKING_INTERVAL += 10
                            if (Settings.DOWNLOAD_TRACKING_INTERVAL < 10) Settings.DOWNLOAD_TRACKING_INTERVAL = 10
                        }
                    }
                    sharedPrefsHelper.saveDownloadTrackingInterval()
                    textView.text = "${Settings.DOWNLOAD_TRACKING_INTERVAL} ${getString(R.string.settings_minutes)}"
                    summary = "${Settings.DOWNLOAD_TRACKING_INTERVAL} ${getString(R.string.settings_minutes)} (${mainActivity.timeUntilLiveData.value?.toHourMinuteSecond()} ${getString(R.string.settings_remaining)})"
                }
            }
            return@setOnPreferenceClickListener true
        }
    }

    private fun setEpubTextZoomPreference() = epubTextZoomPreference.apply {
        summary = "${Settings.EPUB_TEXT_ZOOM} %"
        setOnPreferenceClickListener {
            dialogUtil.getDialogBookTextZoom(mainActivity).apply {
                show()

                val textView = findViewById<TextView>(R.id.dialog_layout_settings_text_zoom_textView0)!!
                val buttonDecrease = findViewById<TextView>(R.id.dialog_layout_settings_text_zoom_button0)!!
                val buttonIncrease = findViewById<TextView>(R.id.dialog_layout_settings_text_zoom_button1)!!

                textView.text = "${Settings.EPUB_TEXT_ZOOM} %"
                buttonDecrease.onClick {
                    Settings.EPUB_TEXT_ZOOM -= 5
                    sharedPrefsHelper.saveEpubTextZoom()
                    textView.text = "${Settings.EPUB_TEXT_ZOOM} %"
                    summary = "${Settings.EPUB_TEXT_ZOOM} %"
                }
                buttonIncrease.onClick {
                    Settings.EPUB_TEXT_ZOOM += 5
                    sharedPrefsHelper.saveEpubTextZoom()
                    textView.text = "${Settings.EPUB_TEXT_ZOOM} %"
                    summary = "${Settings.EPUB_TEXT_ZOOM} %"
                }
            }
            return@setOnPreferenceClickListener true
        }
    }

    private fun setEpubMarginPreference() = epubMarginPreference.apply {
        summary = "${Settings.EPUB_MARGIN_SIZE}"
        setOnPreferenceClickListener {
            dialogUtil.getDialogBookMargin(mainActivity).apply {
                show()

                val textView = findViewById<TextView>(R.id.dialog_layout_settings_margin_textView0)!!
                val buttonDecrease = findViewById<TextView>(R.id.dialog_layout_settings_margin_button0)!!
                val buttonIncrease = findViewById<TextView>(R.id.dialog_layout_settings_margin_button1)!!

                textView.text = "${Settings.EPUB_MARGIN_SIZE}"
                buttonDecrease.onClick {
                    Settings.EPUB_MARGIN_SIZE -= 4
                    sharedPrefsHelper.saveEpubMarginSize()
                    textView.text = "${Settings.EPUB_MARGIN_SIZE}"
                    summary = "${Settings.EPUB_MARGIN_SIZE}"
                }
                buttonIncrease.onClick {
                    Settings.EPUB_MARGIN_SIZE += 4
                    sharedPrefsHelper.saveEpubMarginSize()
                    textView.text = "${Settings.EPUB_MARGIN_SIZE}"
                    summary = "${Settings.EPUB_MARGIN_SIZE}"
                }
            }
            return@setOnPreferenceClickListener true
        }
    }

    private fun setHomeLayout() = homeLayoutPreference.apply {
        val stringArray = resources.getStringArray(R.array.settings_layout_entries)
        summary = when (Settings.HOME_LAYOUT) {
            0 -> stringArray[0]
            1 -> stringArray[1]
            2 -> stringArray[2]
            else -> "ERROR"
        }

        setOnPreferenceClickListener {
            dialogUtil.getDialogHomeLayout(mainActivity, object : DialogUtil.OnDialogSelect2 {
                override fun onSelect0() = saveHomeLayout(0)
                override fun onSelect1() = saveHomeLayout(1)
                override fun onSelect2() = saveHomeLayout(2)

                private fun saveHomeLayout(layout: Int) {
                    Settings.HOME_LAYOUT = layout
                    sharedPrefsHelper.saveHomeLayout()
                    summary = stringArray[layout]
                }
            }).show()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setComicDualPanePreference() = comicDualPanePreference.apply {
        isChecked = Settings.DUAL_PANE
        setOnPreferenceClickListener {
            Settings.DUAL_PANE = !Settings.DUAL_PANE
            sharedPrefsHelper.saveDualPane()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setFavoritePreference() = browserFavoritePreference.apply {
        isChecked = Settings.FAVORITE
        setOnPreferenceClickListener {
            Settings.FAVORITE = !Settings.FAVORITE
            sharedPrefsHelper.saveFavorite()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setLoginPreference() = serverLoginPreference.apply {
        setOnPreferenceClickListener {
            mainActivity.showFragmentLoginBrowser()
            return@setOnPreferenceClickListener true
        }

        val activeLogin = viewModel.getActiveLogin()
        when (activeLogin.isEmpty()) {
            true -> {
                R.layout.settings_item_preference_normal
                layoutResource = R.layout.settings_item_preference_error
                summary = getString(R.string.settings_none)
            }
            false -> {
                layoutResource = R.layout.settings_item_preference_normal
                summary = when (activeLogin.nickname.isEmpty()) {
                    true -> activeLogin.server
                    false -> activeLogin.nickname
                }
            }
        }
    }

    private fun setMarkFinishedPreference() = browserMarkFinishedPreference.apply {
        isChecked = Settings.MARK_FINISHED
        setOnPreferenceClickListener {
            Settings.MARK_FINISHED = !Settings.MARK_FINISHED
            sharedPrefsHelper.saveMarkFinished()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setPreviewPreference() = browserPreviewPreference.apply {
        isChecked = Settings.PREVIEW
        setOnPreferenceClickListener {
            Settings.PREVIEW = !Settings.PREVIEW
            sharedPrefsHelper.savePreview()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setReverseLayoutPreference() = browserReverseLayoutPreference.apply {
        isChecked = Settings.REVERSE_LAYOUT
        setOnPreferenceClickListener {
            Settings.REVERSE_LAYOUT = !Settings.REVERSE_LAYOUT
            sharedPrefsHelper.saveReverseLayout()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setComicRtlPreference() = comicRtlPreference.apply {
        isChecked = Settings.RTL
        setOnPreferenceClickListener {
            Settings.RTL = !Settings.RTL
            sharedPrefsHelper.saveRtl()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setComicScaleTypePreference() = comicScaleTypePreference.apply {
        val stringArray = resources.getStringArray(R.array.settings_scale_entries)
        summary = when (Settings.SCALE_TYPE) {
            0 -> stringArray[0]
            1 -> stringArray[1]
            2 -> stringArray[2]
            else -> "ERROR"
        }

        setOnPreferenceClickListener {
            dialogUtil.getDialogScaleType(mainActivity, object : DialogUtil.OnDialogSelect2 {
                override fun onSelect0() = saveScaleType(0)
                override fun onSelect1() = saveScaleType(1)
                override fun onSelect2() = saveScaleType(2)

                private fun saveScaleType(scaleType: Int) {
                    Settings.SCALE_TYPE = scaleType
                    sharedPrefsHelper.saveScaleType()
                    summary = stringArray[scaleType]
                }
            }).show()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setThemePreference() = systemThemePreference.apply {
        val stringArray = resources.getStringArray(R.array.settings_theme_entries)
        summary = when (Settings.APP_THEME) {
            0 -> stringArray[0]
            1 -> stringArray[1]
            2 -> stringArray[2]
            else -> "ERROR"
        }

        setOnPreferenceClickListener {
            dialogUtil.getDialogAppTheme(mainActivity, object : DialogUtil.OnDialogSelect2 {
                override fun onSelect0() = saveTheme(0)
                override fun onSelect1() = saveTheme(1)
                override fun onSelect2() = saveTheme(2)

                private fun saveTheme(appTheme: Int) {
                    //show dialog first before applying theme
                    dialogUtil.getDialogRequestRestart(mainActivity, object : DialogUtil.OnDialogSelect0 {
                        override fun onSelect0() {
                            mainActivity.recreate()
                        }
                    }).show()

                    Settings.APP_THEME = appTheme
                    sharedPrefsHelper.saveAppTheme()
                }
            }).show()
            return@setOnPreferenceClickListener true
        }
    }

    private fun setOrientationPreference() = systemOrientationPreference.apply {
        summary = getScreenOrientationSummary()

        setOnPreferenceClickListener {
            dialogUtil.getDialogAppOrientation(mainActivity, object : DialogUtil.OnDialogSelect2 {
                override fun onSelect0() = saveOrientation(0)
                override fun onSelect1() = saveOrientation(1)
                override fun onSelect2() = saveOrientation(2)

                private fun saveOrientation(appOrientation: Int) {
                    Settings.SCREEN_ORIENTATION = appOrientation
                    sharedPrefsHelper.saveScreenOrientation()

                    summary = getScreenOrientationSummary()

                    mainActivity.forceOrientationSetting()
                }
            }).show()
            return@setOnPreferenceClickListener true
        }
    }

    private fun getScreenOrientationSummary(): String {
        val stringArray = resources.getStringArray(R.array.settings_orientation_entries)
        return when (Settings.SCREEN_ORIENTATION) {
            0 -> stringArray[0]
            1 -> stringArray[1]
            2 -> stringArray[2]
            else -> "ERROR"
        }
    }

    private fun setVolumePageTurnPreference() = systemVolumePageTurnPreference.apply {
        isChecked = Settings.VOLUME_PAGE_TURN
        setOnPreferenceClickListener {
            Settings.VOLUME_PAGE_TURN = !Settings.VOLUME_PAGE_TURN
            sharedPrefsHelper.saveVolumePageTurn()
            return@setOnPreferenceClickListener true
        }
    }

}