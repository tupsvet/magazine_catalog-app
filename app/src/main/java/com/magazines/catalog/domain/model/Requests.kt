package com.magazines.catalog.domain.model

data class CreateMagazineRequest(
    val title: String,
    val publisher: String,
    val yearFounded: Int,
    val categoryId: String,
    val description: String?,
)

data class CreateReviewRequest(
    val magazineId: String,
    val rating: Int,
    val comment: String?,
)

data class UpdateReviewRequest(
    val rating: Int,
    val comment: String?,
)

data class UploadIssueRequest(
    val magazineId: String,
    val issueNumber: Int,
    val publicationDate: String,
    val pagesCount: Int?,
)

data class FileData(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FileData
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (!bytes.contentEquals(other.bytes)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
