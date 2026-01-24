package co.adityarajput.notifilter.data.models

import kotlinx.serialization.Serializable

@Serializable
data class App(val name: String, val packageName: String)
