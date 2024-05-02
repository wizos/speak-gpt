/**************************************************************************
 * Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
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

package org.teslasoft.assistant.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import org.teslasoft.assistant.Config
import org.teslasoft.assistant.R

class AISetAdapter(private val mContext: Context, private val dataArray: ArrayList<Map<String, String>>) : BaseAdapter() {

    private var ui: ConstraintLayout? = null
    private var setIcon: ImageView? = null
    private var setName: TextView? = null
    private var setDescription: TextView? = null
    private var setOwner: TextView? = null
    private var setModel: TextView? = null
    private var btnUseGlobally: MaterialButton? = null
    private var btnCreateChat: MaterialButton? = null
    private var btnGetApiKey: MaterialButton? = null

    private var listener: OnInteractionListener? = null

    fun setOnInteractionListener(listener: OnInteractionListener) {
        this.listener = listener
    }

    override fun getCount(): Int {
        return dataArray.size
    }

    override fun getItem(position: Int): Any {
        return dataArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var mView: View? = convertView

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_ai_set, null)
        }

        ui = mView?.findViewById(R.id.ui)
        setIcon = mView?.findViewById(R.id.set_icon)
        setName = mView?.findViewById(R.id.set_name)
        setDescription = mView?.findViewById(R.id.set_description)
        setOwner = mView?.findViewById(R.id.set_owner)
        setModel = mView?.findViewById(R.id.set_model)
        btnUseGlobally = mView?.findViewById(R.id.btn_use_globally)
        btnCreateChat = mView?.findViewById(R.id.btn_create_chat)
        btnGetApiKey = mView?.findViewById(R.id.btn_get_api_key)

        setName?.text = dataArray[position]["name"]
        setDescription?.text = dataArray[position]["desc"]
        setOwner?.text = "Provided by: ${dataArray[position]["owner"]}"
        setModel?.text = "AI Model: ${dataArray[position]["model"]}"

        val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(dpToPx(28f).toInt()))
        Glide.with(mContext)
            .load("https://" + Config.API_SERVER_NAME + "/api/v1/exp/" + dataArray[position]["icon"])
            .apply(requestOptions)
            .into(setIcon!!)

        btnCreateChat?.setOnClickListener {
            listener?.onCreateChatClick(dataArray[position]["model"] ?: "", dataArray[position]["apiEndpoint"] ?: "", dataArray[position]["apiEndpointName"] ?: "", dataArray[position]["suggestedChatName"] ?: "")
        }

        btnUseGlobally?.setOnClickListener {
            listener?.onUseGloballyClick(dataArray[position]["model"] ?: "", dataArray[position]["apiEndpoint"] ?: "", dataArray[position]["apiEndpointName"] ?: "")
        }

        btnGetApiKey?.setOnClickListener {
            listener?.onGetApiKeyClicked(dataArray[position]["apiKeyUrl"] ?: "")
        }

        return mView!!
    }

    interface OnInteractionListener {
        fun onUseGloballyClick(model: String, endpointUrl: String, endpointName: String)
        fun onCreateChatClick(model: String, endpointUrl: String, endpointName: String, suggestedChatName: String)
        fun onGetApiKeyClicked(apiKeyUrl: String)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * mContext.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    }
}