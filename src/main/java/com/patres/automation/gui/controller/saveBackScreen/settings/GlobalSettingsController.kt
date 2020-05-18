package com.patres.automation.gui.controller.saveBackScreen.settings

import com.patres.automation.ApplicationLauncher
import com.patres.automation.action.delay.TimeContainer
import com.patres.automation.gui.animation.SliderAnimation
import com.patres.automation.gui.component.snackBar.SnackBarType
import com.patres.automation.gui.component.snackBar.addMessageLanguageWhenIsLoaded
import com.patres.automation.gui.controller.MainController
import com.patres.automation.gui.controller.model.*
import com.patres.automation.gui.controller.saveBackScreen.SaveBackScreenController
import com.patres.automation.type.*
import com.patres.automation.util.extension.toLongOrZero
import javafx.collections.ListChangeListener


class GlobalSettingsController(private val mainController: MainController) : SaveBackScreenController("menu.settings.globalSettings") {

    private val stopKeysSetting = KeyboardButtonActionController(ActionBootKeyboard.STOP_KEYS_SETTINGS)
    private val startRecordKeysSettings = KeyboardButtonActionController(ActionBootKeyboard.START_RECORDING_KEYS_SETTINGS)
    private val stopRecordKeysSettings = KeyboardButtonActionController(ActionBootKeyboard.STOP_RECORDING_KEYS_SETTINGS)
    private val languageComboBox = ChooseLanguageActionBootComboBox().createController().invoke()

    private val additionalDelayBetweenActionsText = TimeActionController(ActionBootTime.ADDITIONAL_DELAY_BETWEEN_ACTIONS)

    private val enableRestCheckBox = CheckBoxActionController(ActionBootCheckBox.ENABLE_REST)
    private val portText = TextFieldActionController(ActionBootTextField.PORT)
    private val enableAuthenticatorCheckBox = CheckBoxActionController(ActionBootCheckBox.ENABLE_AUTHENTICATOR)
    private val serverUsernameText = TextFieldActionController(ActionBootTextField.SERVER_USERNAME)
    private val serverPasswordText = PasswordFieldActionController(ActionBootPasswordField.SERVER_PASSWORD)

    private val allSettings = listOf<AutomationController<*>>(
            languageComboBox,
            stopKeysSetting,
            startRecordKeysSettings,
            stopRecordKeysSettings,
            additionalDelayBetweenActionsText,
            enableRestCheckBox,
            portText,
            enableAuthenticatorCheckBox,
            serverUsernameText,
            serverPasswordText
    )

    init {
        loadGlobalSettings()
        initChangeDetectors()
    }

    override fun saveChanges() {
        allSettings.forEach { it.checkValidation() }
        ApplicationLauncher.globalSettings.editAndSave {
            stopActionKeys = ArrayList(stopKeysSetting.keyboardField.keys)
            startRecordKeys = ArrayList(startRecordKeysSettings.keyboardField.keys)
            stopRecordKeys = ArrayList(stopRecordKeysSettings.keyboardField.keys)
            language = languageComboBox.comboBox.value
            additionalDelayBetweenActions = TimeContainer(additionalDelayBetweenActionsText.value.toLongOrZero(), additionalDelayBetweenActionsText.selectedDelayTime())
            port = portText.value.toInt()
            enableRest = enableRestCheckBox.checkBox.isSelected
            enableAuthenticator = enableAuthenticatorCheckBox.checkBox.isSelected
            serverUsername = if (enableAuthenticator) serverUsernameText.value else null
            serverPassword = if (enableAuthenticator) serverPasswordText.value else null
        }
        snackBar.addMessageLanguageWhenIsLoaded(isLoaded, SnackBarType.INFO, "message.snackbar.settingsSave")
    }

    override fun initChangeDetectors() {
        stopKeysSetting.keyboardField.keys.addListener(ListChangeListener { changeDetect() })
        startRecordKeysSettings.keyboardField.keys.addListener(ListChangeListener { changeDetect() })
        stopRecordKeysSettings.keyboardField.keys.addListener(ListChangeListener { changeDetect() })
        languageComboBox.comboBox.valueProperty().addListener { _ -> changeDetect() }
        additionalDelayBetweenActionsText.valueText.textProperty().addListener { _ -> changeDetect() }
        additionalDelayBetweenActionsText.comboBox.valueProperty().addListener { _ -> changeDetect() }
        portText.valueText.textProperty().addListener { _ -> changeRestDetect() }
        enableRestCheckBox.checkBox.selectedProperty().addListener { _, _, newValue ->
            portText.isVisible = newValue
            changeRestDetect()
        }
        enableAuthenticatorCheckBox.checkBox.selectedProperty().addListener { _, _, newValue ->
            serverUsernameText.isVisible = newValue
            serverPasswordText.isVisible = newValue
            changeRestDetect()
        }
        serverUsernameText.valueText.textProperty().addListener { _ -> changeRestDetect() }
        serverPasswordText.valueText.textProperty().addListener { _ -> changeRestDetect() }
    }

    private fun changeRestDetect() {
        snackBar.addMessageLanguageWhenIsLoaded(isLoaded, SnackBarType.WARNING, "message.snackbar.changesAppliedAfterRestart")
        changeDetect()
    }

    private fun loadGlobalSettings() {
        mainVBox.children.addAll(allSettings)
    }

    override fun backToPreviousWindow() {
        SliderAnimation.backToTheWindow(mainController.currentTabPane(), this, mainController.centerStackPane)
    }

    override fun reloadSettings() {
        ApplicationLauncher.globalSettings.run {
            stopKeysSetting.keyboardField.setKeyboardButtons(stopActionKeys)
            startRecordKeysSettings.keyboardField.setKeyboardButtons(startRecordKeys)
            stopRecordKeysSettings.keyboardField.setKeyboardButtons(stopRecordKeys)
            languageComboBox.comboBox.value = language
            enableRestCheckBox.checkBox.isSelected = enableRest
            additionalDelayBetweenActionsText.comboBox.value = additionalDelayBetweenActions.type
            additionalDelayBetweenActionsText.value = additionalDelayBetweenActions.value.toString()
            portText.isVisible = enableRest
            portText.value = port.toString()

            enableAuthenticatorCheckBox.checkBox.isSelected = enableAuthenticator
            serverUsernameText.isVisible = enableAuthenticator
            serverUsernameText.value = serverUsername?: ""
            serverPasswordText.isVisible = enableAuthenticator
            serverPasswordText.value = serverPassword?: ""
        }
    }

}
