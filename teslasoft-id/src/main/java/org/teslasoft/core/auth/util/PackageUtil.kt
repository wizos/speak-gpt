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

package org.teslasoft.core.auth.util

import android.content.Context
import android.content.pm.PackageManager

class PackageUtil {
    companion object {
        fun checkInstallation(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                val packageInfo = packageManager.getPackageInfo("com.teslasoft.libraries.support", PackageManager.GET_ACTIVITIES)

                return packageInfo != null
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()
    }
}