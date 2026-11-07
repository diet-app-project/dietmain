package com.example.dietapp.data.local

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private const val MealPhotoDirectoryName = "meal_photos"

internal class MealPhotoCopyException(cause: Exception) :
    IOException("Failed to copy meal photo", cause)

internal suspend fun saveWithMealPhoto(
    context: Context,
    uri: Uri,
    saveMeal: suspend (String) -> Long,
): Long = withContext(Dispatchers.IO) {
    var stagedFile: File? = null
    var committed = false

    try {
        val photoFile = try {
            require(uri.scheme == ContentResolver.SCHEME_CONTENT)
            val directory = checkedMealPhotoDirectory(context.filesDir)
            if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) {
                throw IOException("Could not create the meal photo directory")
            }
            File.createTempFile("meal-", ".img", directory).also { file ->
                stagedFile = file
                copyMealPhoto(context.contentResolver, uri, file)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            throw MealPhotoCopyException(failure)
        }

        currentCoroutineContext().ensureActive()
        // Room 저장 결과를 파일 소유권과 같은 취소 경계 안에서 확정한다.
        withContext(NonCancellable) {
            val photoPath = "$MealPhotoDirectoryName/${photoFile.name}"
            val mealId = saveMeal(photoPath)
            check(mealId > 0)
            committed = true
            mealId
        }
    } finally {
        if (!committed) {
            stagedFile?.let { file ->
                runCatching { Files.deleteIfExists(file.toPath()) }
                    .onFailure { Log.w("MealPhotoStorage", "Could not remove unsaved photo", it) }
            }
        }
    }
}

internal fun resolveMealPhotoFile(filesDir: File, storedPath: String): File? {
    val fileName = storedPath.removePrefix("$MealPhotoDirectoryName/")
    if (fileName.isBlank() || fileName == "." || fileName == ".." ||
        '/' in fileName || '\\' in fileName
    ) {
        return null
    }

    val directory = checkedMealPhotoDirectory(filesDir)
    val file = File(directory, fileName).canonicalFile
    return file.takeIf { it.parentFile == directory && it.isFile }
}

private fun checkedMealPhotoDirectory(filesDir: File): File {
    val directory = File(filesDir.canonicalFile, MealPhotoDirectoryName)
    if (directory.canonicalFile != directory) {
        throw IOException("Invalid meal photo directory")
    }
    return directory
}

private suspend fun copyMealPhoto(resolver: ContentResolver, uri: Uri, destination: File) {
    val jobContext = currentCoroutineContext()
    val input = resolver.openInputStream(uri) ?: throw IOException("Photo stream unavailable")
    input.use { source ->
        destination.outputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                jobContext.ensureActive()
                val count = source.read(buffer)
                if (count == -1) break
                if (count == 0) throw IOException("Photo stream returned no bytes")
                output.write(buffer, 0, count)
            }
        }
    }
    if (destination.length() == 0L) {
        throw IOException("Meal photo is empty")
    }
}