package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.model.PaperMode
import com.example.ui.theme.DefaultInk
import com.example.ui.viewmodel.HandwritingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Handwriting Board`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Handwriting Board", appName)
    }

    @Test
    fun `drawing strokes and undo including clear works`() {
        val vm = HandwritingViewModel()
        vm.onCanvasSizeChanged(400, 600)
        assertFalse(vm.uiState.value.canUndo)

        vm.commitStroke(listOf(Offset(10f, 10f), Offset(50f, 50f)), 8f)
        assertTrue(vm.uiState.value.canUndo)

        vm.clear()
        assertTrue(vm.uiState.value.canUndo)

        vm.undo() // Undoes clear
        assertTrue(vm.uiState.value.canUndo)

        vm.undo() // Undoes stroke
        assertFalse(vm.uiState.value.canUndo)
    }

    @Test
    fun `paper mode swaps default dark and light ink for contrast`() {
        val vm = HandwritingViewModel()
        assertEquals(DefaultInk, vm.uiState.value.inkColor)

        vm.setPaperMode(PaperMode.DARK)
        assertEquals(Color.White, vm.uiState.value.inkColor)

        vm.setPaperMode(PaperMode.LIGHT)
        assertEquals(DefaultInk, vm.uiState.value.inkColor)
    }

    @Test
    fun `selecting a color turns off eraser`() {
        val vm = HandwritingViewModel()
        vm.toggleEraser()
        assertTrue(vm.uiState.value.isErasing)

        vm.setColor(Color(0xFF0984E3))
        assertFalse(vm.uiState.value.isErasing)
        assertEquals(Color(0xFF0984E3), vm.uiState.value.inkColor)
    }
}
