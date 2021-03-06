package com.patres.automation.gui.controller

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXTabPane
import com.patres.automation.ApplicationLauncher
import com.patres.automation.action.RootSchemaGroupModel
import com.patres.automation.excpetion.ApplicationException
import com.patres.automation.gui.animation.SliderAnimation
import com.patres.automation.gui.component.snackBar.SnackBarType
import com.patres.automation.gui.component.snackBar.addMessageLanguage
import com.patres.automation.gui.controller.model.RootSchemaGroupController
import com.patres.automation.gui.controller.saveBackScreen.activeSchema.ActiveSchemasController
import com.patres.automation.gui.controller.saveBackScreen.settings.GlobalSettingsController
import com.patres.automation.gui.dialog.DialogHandler
import com.patres.automation.gui.dialog.LogManager
import com.patres.automation.listener.RunStopKeyListener
import com.patres.automation.mapper.RootSchemaGroupMapper
import com.patres.automation.settings.GlobalSettingsLoader
import com.patres.automation.settings.LanguageManager
import com.patres.automation.system.ApplicationInfo
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.scene.layout.StackPane
import org.slf4j.LoggerFactory
import java.io.File


class MainController {

    companion object {
        private val logger = LoggerFactory.getLogger(MainController::class.java)
    }

    @FXML
    lateinit var root: StackPane

    @FXML
    lateinit var tabPane: JFXTabPane

    @FXML
    lateinit var centerStackPane: StackPane

    @FXML
    lateinit var fileMenu: Menu

    @FXML
    lateinit var newMenuItem: MenuItem

    @FXML
    lateinit var openMenuItem: MenuItem

    @FXML
    lateinit var saveMenuItem: MenuItem

    @FXML
    lateinit var saveAsMenuItem: MenuItem

    @FXML
    lateinit var closeTabMenuItem: MenuItem

    @FXML
    lateinit var activeSchemasMenu: Menu

    @FXML
    lateinit var activeSchemasListMenuItem: MenuItem

    @FXML
    lateinit var activeSchemasAddMenuItem: MenuItem

    @FXML
    lateinit var settingsMenu: Menu

    @FXML
    lateinit var globalSettingsMenuItem: MenuItem

    @FXML
    lateinit var localSettingsMenuItem: MenuItem

    @FXML
    lateinit var helpMenu: Menu

    @FXML
    lateinit var aboutMenuItem: MenuItem

    lateinit var snackBar: JFXSnackbar

    val tabContainers: ObservableList<TabContainer> = FXCollections.observableList(ArrayList<TabContainer>())
    val openedRootSchemas: List<RootSchemaGroupController>
        get() {
            return tabContainers.map { it.rootSchemaController }
        }

    private val globalSettingsController = GlobalSettingsController(this)
    private val emptyTabController = EmptyTabController(this)

    val rootSchemaLoader = RootSchemaLoader(this)
    val activeSchemasController = ActiveSchemasController(this)
    private val keyListener: RunStopKeyListener = RunStopKeyListener(this)

    fun initialize() {
        snackBar = JFXSnackbar(root)
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS

        val previousOpenModels = ApplicationLauncher.globalSettings.calculatePreviousOpenModels()
        previousOpenModels.forEach { loadModelFromFile(it) }

        if (tabContainers.isEmpty()) {
            centerStackPane.children.add(emptyTabController)
        }

        keyListener.activeListener()
        listenTabContainers()
        listenRootSchemaIsDisplayed()
        initLanguage()

        calculateEnableState(tabContainers.isNotEmpty())
    }

    private fun initLanguage() {
        fileMenu.textProperty().bind(LanguageManager.createStringBinding("menu.file"))
        newMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.new"))
        openMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.open"))
        saveMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.save"))
        saveAsMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.saveAs"))
        closeTabMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.closeTab"))
        activeSchemasMenu.textProperty().bind(LanguageManager.createStringBinding("menu.activeSchemas"))
        activeSchemasListMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.activeSchemas.list"))
        activeSchemasAddMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.activeSchemas.add"))
        settingsMenu.textProperty().bind(LanguageManager.createStringBinding("menu.settings"))
        globalSettingsMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.settings.globalSettings"))
        localSettingsMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.settings.localSettings"))
        helpMenu.textProperty().bind(LanguageManager.createStringBinding("menu.help"))
        aboutMenuItem.textProperty().bind(LanguageManager.createStringBinding("menu.about"))

    }

    private fun listenRootSchemaIsDisplayed() {
        centerStackPane.children.addListener { children: ListChangeListener.Change<out Node> ->
            while (children.next()) {
                if (children.addedSubList.contains(tabPane)) {
                    calculateEnableState(tabContainers.isNotEmpty())
                }

                if (children.removed.contains(tabPane)) {
                    calculateEnableState(false)
                }
            }
        }
        tabContainers.addListener { _: ListChangeListener.Change<out TabContainer>? ->
            calculateEnableState(tabContainers.isNotEmpty())
        }
    }

    private fun calculateEnableState(state: Boolean) {
        newMenuItem.isDisable = !(tabContainers.isEmpty() || state)
        openMenuItem.isDisable = !(tabContainers.isEmpty() || state)
        saveMenuItem.isDisable = !state
        saveAsMenuItem.isDisable = !state
        closeTabMenuItem.isDisable = !state

        localSettingsMenuItem.isDisable = !state
        activeSchemasAddMenuItem.isDisable = !state
    }

    private fun listenTabContainers() {
        tabContainers.addListener { _: ListChangeListener.Change<out TabContainer>? ->
            GlobalSettingsLoader.save(ApplicationLauncher.globalSettings)
        }
    }

    @FXML
    fun createNewRootSchema() {
        try {
            rootSchemaLoader.createNewRootSchema()
        } catch (e: Exception) {
            LogManager.showAndLogException(LanguageManager.getLanguageString("error.createFile.noPermission"), e)
        }
    }

    @FXML
    fun openRootSchema() {
        try {
            rootSchemaLoader.openRootSchema()
        } catch (e: Exception) {
            LogManager.showAndLogException(e)
        }
    }

    private fun loadModelFromFile(fileToLoad: File) {
        try {
            rootSchemaLoader.openRootSchema(fileToLoad)
        } catch (e: Exception) {
            logger.error("Cannot load file ${fileToLoad.absolutePath} Exception: {}", e.message, e)
        }
    }

    @FXML
    fun saveExistingRootSchema() {
        getSelectedTabContainer()?.let {
            val saved = rootSchemaLoader.saveExistingRootSchema(it)
            if (saved) {
                createSaveFileSnackBar(it.rootSchemaController.actionRunner.rootFiles.getName())
            }
        }
    }

    @FXML
    fun saveAsRootSchema() {
        getSelectedTabContainer()?.let {
            val saved = rootSchemaLoader.saveAsRootSchema(it)
            if (saved) {
                createSaveFileSnackBar(it.rootSchemaController.actionRunner.rootFiles.getName())
            }
        }
    }

    @FXML
    fun openGlobalSettings() {
        if (centerStackPane.children.contains(activeSchemasController)) {
            activeSchemasController.close()
        }

        if (!centerStackPane.children.contains(globalSettingsController)) {
            globalSettingsController.reload()
            SliderAnimation.goToTheWindow(globalSettingsController, currentTabPane(), centerStackPane)
        }
    }

    @FXML
    fun openLocalSettings() {
        if (centerStackPane.children.contains(globalSettingsController)) {
            globalSettingsController.close()
        }

        getSelectedTabContainer()?.rootSchemaController?.openLocalSettings()
    }

    @FXML
    fun closeCurrentTab() {
        getSelectedTabContainer()?.let {
            rootSchemaLoader.createOnCloseRequest(it).handle(null)
        }
    }

    @FXML
    fun openActiveSchemas() {
        if (centerStackPane.children.contains(globalSettingsController)) {
            globalSettingsController.close()
        }

        if (!centerStackPane.children.contains(activeSchemasController)) {
            activeSchemasController.reload()
            SliderAnimation.goToTheWindow(activeSchemasController, currentTabPane(), centerStackPane)
        }
    }

    @FXML
    fun addActiveSchema() {
        try {
            getSelectedTabContainer()?.let { tabContainer ->
                if (tabContainer.rootSchemaController.actionRunner.isSaved()) {
                    val rootSchema = RootSchemaGroupMapper.controllerToModel(tabContainer.rootSchemaController)
                    val serialized = RootSchemaGroupMapper.controllerToSerialize(tabContainer.rootSchemaController)
                    activeSchemasController.addNewSchemaModel(rootSchema, serialized)
                    removeTab(tabContainer)
                    snackBar.addMessageLanguage(SnackBarType.INFO, "message.snackbar.schemaAdded")
                } else {
                    snackBar.addMessageLanguage(SnackBarType.WARNING, "message.snackbar.saveSchemaBeforeAdding")
                }
            }
        } catch (e: ApplicationException) {
            LogManager.showAndLogException(e)
        }
    }

    @FXML
    fun openAbout() {
        val dialog = DialogHandler(ApplicationInfo.getApplicationInfoDescription())
        dialog.show()
    }

    fun getSelectedTabContainer(): TabContainer? = tabContainers.find { it.tab == tabPane.selectionModel?.selectedItem }

    fun changeDetect(controller: RootSchemaGroupController) {
        val tabContainer = tabContainers.find { it.rootSchemaController == controller }
        tabContainer?.tab?.graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE)
    }

    private fun createSaveFileSnackBar(fileName: String?) {
        snackBar.addMessageLanguage(SnackBarType.INFO, "message.snackbar.fileIsSaved", fileName ?: "")
    }

    fun removeTab(tabContainer: TabContainer) {
        tabContainers.remove(tabContainer)
        tabPane.tabs.remove(tabContainer.tab)
        if (tabPane.tabs.isEmpty()) {
            centerStackPane.children.add(emptyTabController)
        }
    }

    fun addTabToContainer(tabContainer: TabContainer) {
        tabContainers.add(tabContainer)
        centerStackPane.children.remove(emptyTabController)
    }

    fun findActionByName(actionName: String): RootSchemaGroupModel? {
        val actionFromActive = activeSchemasController.activeActions.find { it.model.actionRunner.getEndpointName() == actionName }
        if (actionFromActive != null) {
            return actionFromActive.model
        }
        val actionFromUi = openedRootSchemas.find { it.actionRunner.getEndpointName() == actionName }
        if (actionFromUi != null) {
            return RootSchemaGroupMapper.controllerToModel(actionFromUi)
        }
        return null
    }

    fun findAllowedActionRunner() = openedRootSchemas.map { it.actionRunner } + activeSchemasController.activeActions.map { it.model.actionRunner }

    fun currentTabPane(): Node {
        return if (tabContainers.isEmpty()) {
            emptyTabController
        } else {
            tabPane
        }
    }

}
