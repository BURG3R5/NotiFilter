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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import co.adityarajput.notifilter.data.Cache
import co.adityarajput.notifilter.views.Navigator
import co.adityarajput.notifilter.views.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Cache.update(packageManager)
            }
        }
        enableEdgeToEdge()

        setContent {
            Theme {
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) { Navigator(rememberNavController()) }
            }
        }
    }
}
