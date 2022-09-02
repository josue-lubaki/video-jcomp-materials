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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.raywenderlich.android.librarian.R
import com.raywenderlich.android.librarian.model.state.AddBookState
import com.raywenderlich.android.librarian.ui.composeUi.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBookActivity : AppCompatActivity(), AddBookView {

  private val addBookViewModel by viewModels<AddBookViewModel>()

  companion object {
    fun getIntent(context: Context): Intent = Intent(context, AddBookActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LibrarianTheme {
        AddBookContent()
      }
    }
    addBookViewModel.setView(this)
    addBookViewModel.loadGenres()
  }

  @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
  @Composable
  fun AddBookContent() {
    Scaffold(topBar = { AddBookTopBar() }) {
      AddBookFormContent()
    }
  }

  @Composable
  fun AddBookTopBar() {
    TopBar(
      title = stringResource(id = R.string.add_book_title),
      onBackPressed = { onBackPressedDispatcher.onBackPressed() })
  }

  @Composable
  fun AddBookFormContent() {
    val genres by addBookViewModel.genresState.observeAsState(emptyList())
    val addBookState by addBookViewModel.addBookState.observeAsState(AddBookState())

    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally) {
      InputField(value = addBookState.name,
        onStateChanged = { newValue -> addBookViewModel.onNameChanged(newValue) },
        label = stringResource(id = R.string.book_title_hint),
        isInputValid = addBookState.name.isNotEmpty())

      InputField(value = addBookState.description,
        onStateChanged = { newValue -> addBookViewModel.onDescriptionChanged(newValue) },
        label = stringResource(id = R.string.book_description_hint),
        isInputValid = addBookState.description.isNotEmpty())

      SpinnerPicker(
        pickerText = stringResource(id = R.string.genre_select),
        items = genres,
        itemToName = { it.name },
        onItemPicked = { addBookViewModel.genrePicked(it) })

      ActionButton(
        text = stringResource(id = R.string.add_book_button_text),
        onClick = { addBookViewModel.onAddBookTapped() },
        isEnabled = addBookState.name.isNotEmpty()
            && addBookState.description.isNotEmpty()
            && addBookState.genreId.isNotEmpty())
    }
  }

  override fun onBookAdded() {
    setResult(RESULT_OK)
    finish()
  }
}