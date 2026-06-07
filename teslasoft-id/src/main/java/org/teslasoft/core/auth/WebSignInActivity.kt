/*******************************************************************************
 * Copyright (c) 2023-2026 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.teslasoft.core.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentActivity
import com.auth0.jwt.JWT
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.teslasoft.core.auth.internal.ApplicationSignature
import org.teslasoft.core.auth.internal.Config.Companion.TOKEN_SERVICE
import org.teslasoft.core.auth.internal.Config.Companion.WEB_AUTH_SERVER

/**
 * Fallback activity in case user has no Teslasoft Core installed.
 * This temporary fallback allows user to sign in only to this app.
 * All user data will be deleted after user uninstall the app. Data sync and secured
 * APIs are not available in this mode. Session is limited to 30 days as user device
 * is not checked for integrity and OEM certification and may not be trusted.
 * This activity will be used for example if user has only one Teslasoft product and
 * do not want to additionally install the Teslasoft Core module.
 *
 * For full functionality, Teslasoft Core module must be installed. Teslasoft Core provided the
 * following features:
 * - Single source of trust and device verification.
 * - Passwordless accounts and ability to add multiple accounts at the same time and easily switch between them.
 * - Account settings, offline data, access to secured APIs, 2FA builtin authenticator for OTP generation, QR code and smartcard sign in and more.
 * - Sync between devices and backup to the cloud.
 *
 * Services app architecture is designed to reduce the size of apps installed on the user device
 * and prevent duplication of machine code and resources. However, it may not always be suitable
 * for all users and use cases. This fallback allows users to choose the best option for them.
 * Additionally, services app architecture significantly reduces the amount of network requests
 * and reduces the CPU load by optimizing the number of security checks. Security is checked only
 * once and temporary trust token is issued. Without the architecture, each app could
 * potentially perform the checks and utility requests simultaneously, which may lead to
 * increased network traffic and CPU load and is unnecessary.
 * */
class WebSignInActivity : FragmentActivity() {

    private var webView: WebView? = null
    private var loading: CircularProgressIndicator? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_auth)

        webView = findViewById(R.id.webview)
        loading = findViewById(R.id.loading)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView?.canGoBack() == true) {
                    webView?.goBack()
                } else {
                    isEnabled = false
                    setResult(4)
                    supportFinishAfterTransition()
                }
            }
        })

        webView?.background = 0x00000000.toDrawable()

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.scrollIndicators = 0

        loading?.visibility = View.VISIBLE

        webView?.loadUrl("$WEB_AUTH_SERVER?ftpn=token&next=$TOKEN_SERVICE")

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (url?.startsWith(TOKEN_SERVICE) == true) {
                    val token = url.replace("$TOKEN_SERVICE?token=", "")
                    val tokenDecided = JWT.decode(token)
                    val userId = tokenDecided.getClaim("uid").asString()
                    val signature = ApplicationSignature(this@WebSignInActivity).getCertificateFingerprint("SHA256")
                    val resultIntent = Intent().putExtra("auth_token", token)
                        .putExtra("account_id", userId)
                        .putExtra("signature", signature)
                    setResult(20, resultIntent)
                    supportFinishAfterTransition()
                } else {
                    loading?.visibility = View.GONE
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loading?.visibility = View.VISIBLE
            }
        }
    }
}
