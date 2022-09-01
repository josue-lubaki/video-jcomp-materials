/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.librarian.ui.addBook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.raywenderlich.android.librarian.R
import com.raywenderlich.android.librarian.model.Book
import com.raywenderlich.android.librarian.model.Genre
import com.raywenderlich.android.librarian.model.state.AddBookState
import com.raywenderlich.android.librarian.repository.LibrarianRepository
import com.raywenderlich.android.librarian.ui.composeUi.ActionButton
import com.raywenderlich.android.librarian.ui.composeUi.GenrePicker
import com.raywenderlich.android.librarian.ui.composeUi.InputField
import com.raywenderlich.android.librarian.ui.composeUi.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddBookActivity : AppCompatActivity(), AddBookView {

  private val _addBookState = mutableStateOf(AddBookState())
  private val _genreState = mutableStateOf(emptyList<Genre>())

  @Inject
  lateinit var repository: LibrarianRepository

  companion object {
    fun getIntent(context: Context): Intent = Intent(context, AddBookActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { AddBookContent() }

    loadGenres()
  }

  private fun loadGenres() {
    lifecycleScope.launch {
      _genreState.value = repository.getGenres()
    }
  }

  @Composable
  fun AddBookContent() {
    Scaffold(topBar = { AddBookTopBar() }) {
      AddBookFormContent()
    }
  }

  /*
  * Update Note: onBackPressed() is deprecated.
  * Use onBackPressedDispatcher.onBackPressed() instead.
  * */
  @Composable
  fun AddBookTopBar() {
    TopBar(
      title = stringResource(id = R.string.add_book_title),
      onBackPressed = { onBackPressedDispatcher.onBackPressed() })
  }

  @Composable
  fun AddBookFormContent() {
    val genres = _genreState.value ?: emptyList()
    val bookNameState = remember { mutableStateOf("") }
    val bookDescriptionState = remember { mutableStateOf("") }

    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally) {
      InputField(value = bookNameState.value,
        onStateChanged = { newValue ->
          bookNameState.value = newValue
          _addBookState.value = _addBookState.value.copy(name = newValue)
        },
        label = stringResource(id = R.string.book_title_hint))

      InputField(value = bookDescriptionState.value,
        onStateChanged = { newValue ->
          bookDescriptionState.value = newValue
          _addBookState.value = _addBookState.value.copy(description = newValue)
        },
        label = stringResource(id = R.string.book_description_hint))

      GenrePicker(
        genres = genres, selectedGenreId = _addBookState.value.genreId,
        onItemPicked = {
          _addBookState.value = _addBookState.value.copy(genreId = it.id)
        })

      ActionButton(
        text = stringResource(id = R.string.add_book_button_text),
        onClick = { onAddBookTapped() },
        isEnabled = true)
    }
  }

  fun onAddBookTapped() {
    val bookState = _addBookState.value ?: return

    if (bookState.name.isNotEmpty() &&
      bookState.description.isNotEmpty() &&
      bookState.genreId.isNotEmpty()
    ) {
      lifecycleScope.launch {
        repository.addBook(
          Book(
            name = bookState.name,
            description = bookState.description,
            genreId = bookState.genreId
          )
        )

        onBookAdded()
      }
    }
  }

  override fun onBookAdded() {
    setResult(RESULT_OK)
    finish()
  }
}