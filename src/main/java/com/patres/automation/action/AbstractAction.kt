package com.patres.automation.action

import com.patres.automation.gui.controller.box.AbstractBox
import com.patres.automation.gui.controller.model.AutomationController
import com.patres.automation.type.ActionBootable
import org.slf4j.LoggerFactory
import java.awt.Robot

abstract class AbstractAction(
        var actionBootType: ActionBootable,
        val box: AbstractBox<*>? = null
) {

    companion object {
        val robot: Robot = Robot()
        private val logger = LoggerFactory.getLogger(AbstractAction::class.java)
    }

    abstract fun runAction()

    fun runAndLogAction() {
        logger.info("Running action  - {}", toStringLog())
        runAction()
        logger.info("Finished action - {}", toStringLog())
    }

    open fun validate() {
    }

    abstract fun toStringLog(): String

}
