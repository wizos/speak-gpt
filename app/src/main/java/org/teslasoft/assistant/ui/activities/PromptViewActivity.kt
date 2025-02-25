/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.Api
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.ui.assistant.AssistantActivity
import org.teslasoft.core.api.network.RequestNetwork
import java.net.MalformedURLException
import java.net.URL

class PromptViewActivity : FragmentActivity(), SwipeRefreshLayout.OnRefreshListener {

    private var activityTitle: TextView? = null
    private var progressBar: LoadingIndicator? = null
    private var loaderContainer: LinearLayout? = null
    private var noInternetLayout: ConstraintLayout? = null
    private var btnReconnect: MaterialButton? = null
    private var btnShowDetails: MaterialButton? = null
    private var promptBy: TextView? = null
    private var promptText: EditText? = null
    private var textCat: TextView? = null
    private var refreshPage: SwipeRefreshLayout? = null
    private var requestNetwork: RequestNetwork? = null
    private var btnCopy: MaterialButton? = null
    private var btnLike: MaterialButton? = null
    private var btnTry: MaterialButton? = null
    private var btnFlag: ImageButton? = null
    private var promptBg: ConstraintLayout? = null
    private var promptActions: ConstraintLayout? = null

    private var id = ""
    private var title = ""
    private var cat = ""
    private var networkError = ""
    private var likeState = false
    private var settings: SharedPreferences? = null
    private var promptFor: String? = null
    private var btnBack: ImageButton? = null
    private var root: ConstraintLayout? = null
    private var hideable: ConstraintLayout? = null
    private var uiIsUpdated = false

    private val dataListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        @SuppressLint("SetTextI18n")
        override fun onResponse(tag: String, message: String) {
            noInternetLayout?.visibility = View.GONE
            refreshPage?.isRefreshing = false

            try {
                val map: HashMap<String, String> = Gson().fromJson(
                    message, TypeToken.getParameterized(HashMap::class.java, String::class.java, String::class.java).type
                )

                promptText?.setText(map["prompt"])
                promptBy?.text = "By " + map["author"]
                btnLike?.text = map["likes"]
                promptFor = map["type"]

                title = map["name"].toString()
                activityTitle?.text = title

                updateUiFromCat(map["category"].toString())

                promptText?.alpha = 0f
                promptBg?.alpha = 0f
                loaderContainer?.alpha = 1f

                Handler(mainLooper).postDelayed({
                    promptBg?.animate()?.alpha(1f)?.setDuration(200)?.withEndAction {
                        promptBg?.alpha = 1f
                    }

                    loaderContainer?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
                        loaderContainer?.alpha = 0f
                        loaderContainer?.visibility = View.GONE
                    }

                    promptText?.animate()?.alpha(1f)?.setDuration(200)?.withEndAction {
                        promptText?.alpha = 1f
                    }
                }, 200)

                networkError = ""
            } catch (e: Exception) {
                networkError = e.printStackTrace().toString()
            }
        }

        override fun onErrorResponse(tag: String, message: String) {
            networkError = message
            refreshPage?.isRefreshing = false
            noInternetLayout?.visibility = View.VISIBLE
        }
    }

    private fun updateUiFromCat(cat: String?) {
        if (uiIsUpdated) return
        textCat?.text = when (cat) {
            "development" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_development))
            "music" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_music))
            "art" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_art))
            "culture" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_culture))
            "business" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_business))
            "gaming" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_gaming))
            "education" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_education))
            "history" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_history))
            "health" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_health))
            "food" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_food))
            "tourism" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_tourism))
            "productivity" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_productivity))
            "tools" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_tools))
            "entertainment" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_entertainment))
            "sport" -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_sport))
            else -> String.format(resources.getString(R.string.cat), resources.getString(R.string.cat_uncat))
        }

        val bgColor = when (cat) {
            "development" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_development, theme))
            "music" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_music, theme))
            "art" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_art, theme))
            "culture" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_culture, theme))
            "business" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_business, theme))
            "gaming" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_gaming, theme))
            "education" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_education, theme))
            "history" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_history, theme))
            "health" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_health, theme))
            "food" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_food, theme))
            "tourism" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_tourism, theme))
            "productivity" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_productivity, theme))
            "tools" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_tools, theme))
            "entertainment" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_entertainment, theme))
            "sport" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_cat_sport, theme))
            else -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.bg_grey, theme))
        }

        root?.backgroundTintList = ColorStateList.valueOf(bgColor)
        loaderContainer?.backgroundTintList = ColorStateList.valueOf(bgColor)

        val tintColor = when (cat) {
            "development" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_development, theme))
            "music" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_music, theme))
            "art" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_art, theme))
            "culture" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_culture, theme))
            "business" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_business, theme))
            "gaming" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_gaming, theme))
            "education" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_education, theme))
            "history" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_history, theme))
            "health" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_health, theme))
            "food" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_food, theme))
            "tourism" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_tourism, theme))
            "productivity" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_productivity, theme))
            "tools" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_tools, theme))
            "entertainment" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_entertainment, theme))
            "sport" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_cat_sport, theme))
            else -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.tint2_grey, theme))
        } + 0x15000000

        val catColor = when (cat) {
            "development" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_development, theme))
            "music" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_music, theme))
            "art" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_art, theme))
            "culture" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_culture, theme))
            "business" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_business, theme))
            "gaming" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_gaming, theme))
            "education" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_education, theme))
            "history" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_history, theme))
            "health" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_health, theme))
            "food" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_food, theme))
            "tourism" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_tourism, theme))
            "productivity" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_productivity, theme))
            "tools" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_tools, theme))
            "entertainment" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_entertainment, theme))
            "sport" -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.cat_sport, theme))
            else -> harmonizeColors(ResourcesCompat.getColor(resources, R.color.grey, theme))
        }

        val tintDrawable1 = GradientDrawable()
        tintDrawable1.shape = GradientDrawable.RECTANGLE
        tintDrawable1.setColor(0x000000)
        tintDrawable1.cornerRadius = dpToPx(24).toFloat()

        val tintDrawable2 = GradientDrawable()
        tintDrawable2.shape = GradientDrawable.RECTANGLE
        tintDrawable2.setColor(0x000000)
        tintDrawable2.cornerRadius = dpToPx(24).toFloat()

        val tintDrawable3 = GradientDrawable()
        tintDrawable3.shape = GradientDrawable.RECTANGLE
        tintDrawable3.setColor(0x000000)
        tintDrawable3.cornerRadius = dpToPx(16).toFloat()

        val tintDrawable4 = GradientDrawable()
        tintDrawable4.shape = GradientDrawable.RECTANGLE
        tintDrawable4.setColor(0x000000)
        tintDrawable4.cornerRadius = dpToPx(16).toFloat()

        val tintDrawable5 = GradientDrawable()
        tintDrawable5.shape = GradientDrawable.RECTANGLE
        tintDrawable5.setColor(0x000000)
        tintDrawable5.cornerRadius = dpToPx(16).toFloat()

        promptBg?.backgroundTintList = ColorStateList.valueOf(tintColor)
        promptActions?.backgroundTintList = ColorStateList.valueOf(tintColor)
        activityTitle?.setTextColor(catColor)
        textCat?.backgroundTintList = ColorStateList.valueOf(tintColor)
        promptBy?.backgroundTintList = ColorStateList.valueOf(tintColor)
        btnLike?.backgroundTintList = ColorStateList.valueOf(bgColor)
        btnLike?.iconTint = ColorStateList.valueOf(catColor)
        btnLike?.setTextColor(catColor)
        btnLike?.rippleColor = ColorStateList.valueOf(catColor)
        btnCopy?.backgroundTintList = ColorStateList.valueOf(bgColor)
        btnCopy?.iconTint = ColorStateList.valueOf(catColor)
        btnCopy?.setTextColor(catColor)
        btnCopy?.rippleColor = ColorStateList.valueOf(catColor)
        btnTry?.backgroundTintList = ColorStateList.valueOf(catColor)
        btnTry?.iconTint = ColorStateList.valueOf(bgColor)
        btnTry?.setTextColor(bgColor)
        btnTry?.rippleColor = ColorStateList.valueOf(bgColor)

        progressBar?.setIndicatorColor(catColor)

        btnFlag?.setColorFilter(catColor)
        btnBack?.setColorFilter(catColor)
        uiIsUpdated = true

        refreshPage?.setColorSchemeColors(catColor)
        refreshPage?.setProgressBackgroundColorSchemeColor(bgColor)
    }

    private fun harmonizeColors(color: Int) : Int {
        return MaterialColors.harmonize(color, ResourcesCompat.getColor(resources, R.color.accent_600, theme))
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private val likeListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            btnLike?.isEnabled = true
            likeState = true

            settings?.edit()?.putBoolean(id, true)?.apply()
            btnLike?.setIconResource(R.drawable.ic_like)

            loadData()
        }

        override fun onErrorResponse(tag: String, message: String) {
            btnLike?.isEnabled = true

            Toast.makeText(this@PromptViewActivity, getString(R.string.label_sorry_action_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private val dislikeListener: RequestNetwork.RequestListener = object : RequestNetwork.RequestListener {
        override fun onResponse(tag: String, message: String) {
            btnLike?.isEnabled = true
            likeState = false

            settings?.edit()?.putBoolean(id, false)?.apply()
            btnLike?.setIconResource(R.drawable.ic_like_outline)

            loadData()
        }

        override fun onErrorResponse(tag: String, message: String) {
            btnLike?.isEnabled = true

            Toast.makeText(this@PromptViewActivity, getString(R.string.label_sorry_action_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        // Reset preferences singleton
        Preferences.getPreferences(this, "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 30) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                finishActivity()
            }
        }

        val transition = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 300
        }

        transition.excludeTarget(R.id.prompt_bg, true)
        transition.excludeTarget(R.id.refresh_page, true)
        transition.excludeTarget(R.id.prompt_text_frame, true)
        transition.excludeTarget(R.id.prompt_text, true)
        transition.excludeTarget(R.id.prompt_by, true)
        transition.excludeTarget(R.id.text_cat, true)

        val transition2 = TransitionInflater.from(this).inflateTransition(android.R.transition.move).apply {
            interpolator = FastOutLinearInInterpolator()
            duration = 200
        }

        transition2.excludeTarget(R.id.prompt_bg, true)
        transition2.excludeTarget(R.id.refresh_page, true)
        transition2.excludeTarget(R.id.prompt_text_frame, true)
        transition2.excludeTarget(R.id.prompt_text, true)
        transition2.excludeTarget(R.id.prompt_by, true)
        transition2.excludeTarget(R.id.text_cat, true)

        // Set the transition as the shared element enter transition
        window.sharedElementEnterTransition = transition
        window.sharedElementExitTransition = transition2

        setContentView(R.layout.activity_view_prompt)

        val extras: Bundle? = intent.extras

        if (extras == null) {
            checkForURI()
        } else {
            id = extras.getString("id", "")
            title = extras.getString("title", "")
            cat = extras.getString("category", "")

            this@PromptViewActivity.setTitle(extras.getString("title", ""))

            if (id == "" || title == "") {
                checkForURI()
            } else {
                allowLaunch()
            }
        }
    }

    private fun checkForURI() {
        val uri = intent.data
        try {
            val url = URL(uri?.scheme, uri?.host, uri?.path)

            val paths = url.path.split("/")
            id = paths[paths.size - 1]

            allowLaunch()
        } catch (_: MalformedURLException) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
            finishActivity()
        }
    }

    @Suppress("DEPRECATION")
    private fun allowLaunch() {
        if (Build.VERSION.SDK_INT < 30) {
            window.statusBarColor = SurfaceColors.SURFACE_4.getColor(this)
        }

        initUI()

        Thread {
            runOnUiThread {
                initLogic()
            }
        }.start()
    }

    private fun initUI() {
        activityTitle = findViewById(R.id.activity_view_title)
        progressBar = findViewById(R.id.progress_bar_view)
        loaderContainer = findViewById(R.id.loader_container)
        noInternetLayout = findViewById(R.id.no_internet)
        btnReconnect = findViewById(R.id.btn_reconnect)
        btnShowDetails = findViewById(R.id.btn_show_details)
        promptBy = findViewById(R.id.prompt_by)
        promptText = findViewById(R.id.prompt_text)
        refreshPage = findViewById(R.id.refresh_page)
        btnFlag = findViewById(R.id.btn_flag)
        btnCopy = findViewById(R.id.btn_copy)
        btnLike = findViewById(R.id.btn_like)
        btnTry = findViewById(R.id.btn_try)
        textCat = findViewById(R.id.text_cat)
        btnBack = findViewById(R.id.btn_back)
        root = findViewById(R.id.root)
        promptBg = findViewById(R.id.prompt_bg)
        promptActions = findViewById(R.id.prompt_actions)
        hideable = findViewById(R.id.hideable)

        refreshPage?.setColorSchemeResources(R.color.accent_900)
        refreshPage?.setProgressBackgroundColorSchemeColor(
            SurfaceColors.SURFACE_2.getColor(this)
        )
        refreshPage?.setSize(SwipeRefreshLayout.LARGE)

        hideable?.alpha = 0f
        hideable?.animate()?.alpha(1f)?.setDuration(200)?.withEndAction {
            hideable?.alpha = 1f
        }

        loaderContainer?.setOnClickListener { /* Prevent user from interacting with the page until it finishes loading */ }

        noInternetLayout?.visibility = View.GONE
        updateUiFromCat(cat)
    }

    private fun initLogic() {
        activityTitle?.isSelected = true

        btnFlag?.setImageResource(R.drawable.ic_flag)
        btnBack?.setOnClickListener { finishActivity() }
        settings = getSharedPreferences("likes", MODE_PRIVATE)

        likeState = settings?.getBoolean(id, false) == true

        refreshPage?.setOnRefreshListener(this)

        if (likeState) {
            btnLike?.setIconResource(R.drawable.ic_like)
        } else {
            btnLike?.setIconResource(R.drawable.ic_like_outline)
        }

        btnCopy?.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("prompt", promptText?.text.toString())
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, getString(R.string.label_copy), Toast.LENGTH_SHORT).show()
        }

        btnLike?.setOnClickListener {
            if (likeState) {
                requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/dislike.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dislikeListener)
            } else {
                requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/like.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", likeListener)
            }

            btnLike?.isEnabled = false
        }

        btnTry?.setOnClickListener {
            if (promptFor == "GPT") {
                val i = Intent(
                    this,
                    AssistantActivity::class.java
                ).setAction(Intent.ACTION_VIEW)
                i.putExtra("prompt", promptText?.text.toString())
                startActivity(i)
            } else {
                val i = Intent(
                    this,
                    AssistantActivity::class.java
                ).setAction(Intent.ACTION_VIEW)
                i.putExtra("prompt", "/imagine " + promptText?.text.toString())
                i.putExtra("FORCE_SLASH_COMMANDS_ENABLED", true)
                startActivity(i)
            }
        }

        btnFlag?.setOnClickListener {
            val i = Intent(this, ReportAbuseActivity::class.java).setAction(Intent.ACTION_VIEW)
            i.putExtra("id", id)
            startActivity(i)
        }

        requestNetwork = RequestNetwork(this)

        activityTitle?.text = title
        btnReconnect?.setOnClickListener { loadData() }

        btnShowDetails?.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.App_MaterialAlertDialog)
                .setTitle(R.string.label_error_details)
                .setMessage(networkError)
                .setPositiveButton(R.string.btn_close) { _, _ -> }
                .show()
        }

        loadData()
    }

    override fun onRefresh() {
        promptBg?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
            promptBg?.alpha = 0f
        }

        loaderContainer?.visibility = View.VISIBLE
        loaderContainer?.animate()?.alpha(1f)?.setDuration(200)?.withEndAction {
            loaderContainer?.alpha = 1f
        }

        promptText?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
            promptText?.alpha = 0f
            loadData()
        }
    }

    private fun loadData() {
        noInternetLayout?.visibility = View.GONE

        requestNetwork?.startRequestNetwork("GET", "${Config.API_ENDPOINT}/prompt.php?api_key=${Api.TESLASOFT_API_KEY}&id=$id", "A", dataListener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        if (Build.VERSION.SDK_INT < 30) return
        try {
            val actionBar = findViewById<TextView>(R.id.activity_view_title)
            actionBar?.setPadding(
                actionBar.paddingLeft,
                window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top + actionBar.paddingTop,
                actionBar.paddingRight,
                actionBar.paddingBottom
            )
        } catch (_: Exception) { /* unused */ }
    }

    private fun finishActivity() {
        hideable?.animate()?.alpha(0f)?.duration = 200
        supportFinishAfterTransition()
    }
}
