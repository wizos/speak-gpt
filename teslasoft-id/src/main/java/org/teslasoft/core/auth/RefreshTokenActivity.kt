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

import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.teslasoft.core.auth.util.PackageUtil.Companion.checkInstallation
import org.teslasoft.core.auth.util.PackageUtil.Companion.getAppName

class RefreshTokenActivity : FragmentActivity() {

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                .setTitle(getAppName())
                .setMessage("User credentials has changed. You may need to sign out and sign in to " + getAppName() + " again again for changes to take effect.")
                .setCancelable(false)
                .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                    setResult(RESULT_OK)
                    supportFinishAfterTransition()
                }.show()
        } else {
            setResult(RESULT_CANCELED)
            supportFinishAfterTransition()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        if (intent.extras == null) {
            setResult(RESULT_CANCELED)
            supportFinishAfterTransition()
            return
        }

        if (intent.extras?.getString("account_id", null) == null) {
            setResult(RESULT_CANCELED)
            supportFinishAfterTransition()
            return
        }

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) { /* Lock back gesture */ }
        } else {
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { /* Lock back gesture */ }
            })
        }

        if (!checkInstallation(this)) {
            MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                .setTitle(getAppName())
                .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                .setCancelable(false)
                .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                    setResult(RESULT_CANCELED)
                    supportFinishAfterTransition()
                }.show()
        } else {
            try {
                val apiIntent = Intent()
                apiIntent.component = ComponentName(
                    "com.teslasoft.libraries.support",
                    "org.teslasoft.core.api.account.RefreshTokenActivity"
                )

                apiIntent.putExtra("account_id", intent.extras?.getString("account_id", ""))

                activityResultLauncher.launch(apiIntent)
            } catch (_: Exception) {
                MaterialAlertDialogBuilder(this, R.style.TeslasoftID_MaterialAlertDialog)
                    .setTitle(getAppName())
                    .setMessage(String.format(getString(R.string.teslasoft_services_auth_core_unavailable), getAppName()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.teslasoft_services_auth_dialog_close) { _: DialogInterface?, _: Int ->
                        setResult(RESULT_CANCELED)
                        supportFinishAfterTransition()
                    }.show()
            }
        }
    }
}
