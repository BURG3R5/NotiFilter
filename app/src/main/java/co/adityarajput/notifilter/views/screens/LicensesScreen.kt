package co.adityarajput.notifilter.views.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import co.adityarajput.notifilter.R
import co.adityarajput.notifilter.views.components.AppBar
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
fun LicensesScreen(goBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            AppBar(
                stringResource(R.string.licenses),
                true,
                goBack,
            )
        },
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val libraries by produceLibraries()

            LibrariesContainer(
                libraries,
                Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                showDescription = true,
                textStyles = LibraryDefaults.libraryTextStyles(nameMaxLines = 2),
            )
        }
    }
}
