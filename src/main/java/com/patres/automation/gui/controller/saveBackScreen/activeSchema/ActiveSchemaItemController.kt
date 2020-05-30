package com.patres.automation.gui.controller.saveBackScreen.activeSchema

import com.jfoenix.controls.JFXButton
import com.patres.automation.action.ActionRunner
import com.patres.automation.action.RootSchemaGroupModel
import com.patres.automation.settings.LanguageManager
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.StackPane


class ActiveSchemaItemController(
        private val activeSchemasController: ActiveSchemasController,
        val rootSchemaGroupModel: RootSchemaGroupModel) : StackPane() {

    @FXML
    lateinit var activeSchemaLabel: Label

    @FXML
    lateinit var editButton: JFXButton

    @FXML
    lateinit var closeButton: JFXButton

    @FXML
    lateinit var runButton: JFXButton

    @FXML
    lateinit var stopButton: JFXButton

    @FXML
    lateinit var nameTooltip: Tooltip

    private val actionRunner = ActionRunner(rootSchemaGroupModel.automationRunningProperty)
    val schemaGroupModel = rootSchemaGroupModel.schemaGroupModel

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/ActiveActionItem.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.resources = LanguageManager.getBundle()
        fxmlLoader.load<ActiveSchemaItemController>()

        activeSchemaLabel.text = "• ${rootSchemaGroupModel.rootFiles.getName()}"
        nameTooltip.text = rootSchemaGroupModel.rootFiles.currentFile.absolutePath


    }

    fun initialize() {
        manageRunStopButtons(rootSchemaGroupModel.automationRunningProperty.get())
        rootSchemaGroupModel.automationRunningProperty.addListener { _, _, isRunning -> manageRunStopButtons(isRunning) }
    }

    @FXML
    fun editActiveSchema() {
        activeSchemasController.changeDetect()
        activeSchemasController.removeActiveSchemaFromUiList(this)
        activeSchemasController.toEditSchema.add(rootSchemaGroupModel)
    }

    @FXML
    fun closeActiveSchema() {
        activeSchemasController.changeDetect()
        activeSchemasController.removeActiveSchemaFromUiList(this)
        activeSchemasController.toRemoveSchema.add(rootSchemaGroupModel)

    }

    @FXML
    fun runAction() {
        actionRunner.runAutomation(schemaGroupModel)
    }

    @FXML
    fun stopAction() {
        actionRunner.stopAutomation()
    }

    private fun manageRunStopButtons(isRunning: Boolean) {
        Platform.runLater {
            runButton.isVisible = !isRunning
            stopButton.isVisible = isRunning
        }
    }

}
