package com.patres.automation.gui.dialog

import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXDialog
import com.patres.automation.ApplicationLauncher
import com.patres.automation.gui.controller.model.RootSchemaGroupController
import com.patres.automation.gui.custom.KeyboardButton
import com.patres.automation.mapper.model.AutomationActionSerialized
import com.patres.automation.mapper.model.TextFieldActionSerialized
import com.patres.automation.settings.LanguageManager
import com.patres.automation.type.ActionBootTextField
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

class SaveRecordedActionsDialog(
        private val actions: List<AutomationActionSerialized>,
        private val controller: RootSchemaGroupController,
        removeDelayDefaultValue: Boolean = true,
        removeLastActionDefaultValue: Boolean = true
) : StackPane() {

    companion object {
        const val STRIKE_STYLE = "strike-label"
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/dialog/SaveRecordedActions.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.resources = LanguageManager.getBundle()
        fxmlLoader.load<KeyboardButton>()
    }

    @FXML
    lateinit var removeDelayCheckBox: JFXCheckBox

    @FXML
    lateinit var removeLastActionCheckBox: JFXCheckBox

    @FXML
    lateinit var actionContainer: VBox

    private val jfxDialog = JFXDialog(ApplicationLauncher.mainPane, this, JFXDialog.DialogTransition.CENTER)
    private val actionTextMap: Map<AutomationActionSerialized, Label> = actions.map { it to Label(it.toTranslatedString()) }.toMap()
    private val delayLabels = actionTextMap.filterKeys { it is TextFieldActionSerialized && it.actionType == ActionBootTextField.DELAY }.values

    init {
        actionContainer.children.addAll(actionTextMap.values)
        removeDelayCheckBox.selectedProperty().addListener { _, _, newValue ->
            changeDelayLabels(newValue)
        }
        removeLastActionCheckBox.selectedProperty().addListener { _, _, newValue ->
            changeLastActionLabel(newValue)
        }

        removeDelayCheckBox.isSelected = removeDelayDefaultValue
        removeLastActionCheckBox.isSelected = removeLastActionDefaultValue
    }

    private fun changeLastActionLabel(newValue: Boolean) {
        if (newValue) {
            addStrike(actionTextMap.values.last())
        } else {
            removeStrike(actionTextMap.values.last())
        }
    }

    private fun changeDelayLabels(newValue: Boolean) {
        if (newValue) {
            delayLabels.forEach { addStrike(it) }
        } else {
            delayLabels.forEach { removeStrike(it) }
        }
    }

    fun showDialog() {
        jfxDialog.show()
    }

    private fun closeDialog() {
        jfxDialog.close()
    }

    @FXML
    fun save() {
        val controllers = actions
                .filterNot { removeDelayCheckBox.isSelected && it is TextFieldActionSerialized && it.actionType == ActionBootTextField.DELAY }
                .filterNot { removeLastActionCheckBox.isSelected && actions.indexOf(it) + 1 == actions.size }
                .map { it.serializedToController() }
        controllers.forEach { controller.addActionBlocks(it) }

        closeDialog()
    }

    @FXML
    fun doNotSave() {
        closeDialog()
    }

    private fun addStrike(label: Label) {
        if (!label.styleClass.contains(STRIKE_STYLE)) {
            label.styleClass.add(STRIKE_STYLE)
        }
    }

    private fun removeStrike(label: Label) {
        if (label.styleClass.contains(STRIKE_STYLE)) {
            label.styleClass.remove(STRIKE_STYLE)
        }
    }

}
