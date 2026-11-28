// NotiFilter listens to all device notifications and quietly manages those that match your filters.
//
// Copyright (C) 2026 Aditya Rajput
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License (version 3) as
// published by the Free Software Foundation.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.
//
// The developer is reachable by electronic mail at <mailto:mail@adityarajput.co>

package co.adityarajput.notifilter

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.services.Preferences
import co.adityarajput.notifilter.viewmodels.AppearanceViewModel
import co.adityarajput.notifilter.viewmodels.Provider
import co.adityarajput.notifilter.views.Navigator
import co.adityarajput.notifilter.views.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val tag = Preferences.language
        if (tag.isEmpty()) {
            super.attachBaseContext(newBase)
        } else {
            val config = Configuration(newBase.resources.configuration)
            config.setLocales(LocaleList.forLanguageTags(tag))
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Cache.update(packageManager)
            }
        }
        enableEdgeToEdge()

        setContent {
            val viewModel: AppearanceViewModel = viewModel(factory = Provider.Factory)

            Theme(viewModel.brightness) {
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) { Navigator(rememberNavController(), viewModel) }
            }
        }
    }
}
