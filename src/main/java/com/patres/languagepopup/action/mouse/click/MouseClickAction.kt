package com.patres.languagepopup.action.mouse.click

import com.patres.languagepopup.action.mouse.MouseAction
import com.patres.languagepopup.model.RootSchemaGroupModel
import com.patres.languagepopup.model.SchemaGroupModel

abstract class MouseClickAction(
        root: RootSchemaGroupModel,
        parent: SchemaGroupModel
) : MouseAction(root, parent) {

    override fun runMouseAction() {
        robot.mousePress(buttonBit)
        robot.mouseRelease(buttonBit)
    }

}
