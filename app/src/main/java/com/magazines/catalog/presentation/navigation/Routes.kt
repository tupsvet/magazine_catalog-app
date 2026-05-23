package com.magazines.catalog.presentation.navigation

import android.net.Uri

object Routes {
    const val AUTH_GRAPH = "auth"
    const val MAIN_GRAPH = "main"

    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val CATALOG = "catalog"
    const val MAGAZINE_DETAIL = "magazine/{magazineId}"
    const val PDF_VIEWER = "pdf_viewer?url={pdfUrl}"
    const val FAVORITES = "favorites"
    const val MY_MAGAZINES = "my_magazines"
    const val UPLOAD_MAGAZINE = "upload_magazine"
    const val UPLOAD_ISSUE = "upload_issue/{magazineId}"
    const val PROFILE = "profile"
    const val ADMIN = "admin"

    fun magazineDetail(magazineId: String): String = "magazine/$magazineId"

    fun uploadIssue(magazineId: String): String = "upload_issue/$magazineId"

    fun pdfViewer(pdfUrl: String): String = "pdf_viewer?url=${Uri.encode(pdfUrl)}"
}
