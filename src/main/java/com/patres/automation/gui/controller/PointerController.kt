package com.patres.automation.gui.controller

import com.patres.automation.Main
import com.patres.automation.Point
import com.patres.automation.Point.Companion.VECTOR_CHAR
import com.patres.automation.Point.Companion.transformPoint
import com.patres.automation.gui.controller.model.MousePointActionController
import javafx.animation.FadeTransition
import javafx.animation.ScaleTransition
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.util.Duration

class PointerController(private val stage: Stage, private val pointPane: MousePointActionController) {

    companion object {
        private const val CIRCLE_OPACITY = 0.6
        private const val RADIUS = 12.0
    }

    lateinit var scene: Scene
    private val pane: Pane = Pane()
    private var circlePoint: Circle = Circle(RADIUS)

    private var rightMousePoint: Point? = null
    private var leftMousePoint: Point? = null
    private var line: Line? = null


    init {
        setScene()
        setStyle()
        addMouseListener()
    }

    private fun setScene() {
        val primaryScreenBounds = Screen.getPrimary().visualBounds
        val width = primaryScreenBounds.width
        val height = primaryScreenBounds.height
        this.scene = Scene(pane, width, height)
    }

    private fun setStyle() {
        scene.fill = Color.TRANSPARENT
        scene.stylesheets.add(Main.getStylesheet())
        pane.styleClass.add("stackpane-background")
        pane.cursor = Cursor.CROSSHAIR
    }

    private fun loadCirclePoint() {
        circlePoint = Circle(12.0)
        pane.children.add(circlePoint)
    }

    private fun addMouseListener() {
        pane.setOnMousePressed { pressedEvent ->
            loadPressedHandler(pressedEvent)
            pane.onMouseDragged = EventHandler<MouseEvent> { this.loadDraggedHandler(it) }
            pane.onMouseReleased = EventHandler<MouseEvent> { this.loadReleasedHandler(it) }
        }
    }

    private fun loadPressedHandler(pressedEvent: MouseEvent) {
        loadCirclePoint()
        when (pressedEvent.button) {
            MouseButton.PRIMARY -> circlePoint.fill = Color.YELLOW
            MouseButton.SECONDARY -> circlePoint.fill = Color.AQUA
        }

        val fadeStart = FadeTransition(Duration.millis(200.0), circlePoint)
        fadeStart.fromValue = 0.0
        fadeStart.toValue = CIRCLE_OPACITY
        fadeStart.play()

        circlePoint.centerX = pressedEvent.screenX - stage.x
        circlePoint.centerY = pressedEvent.screenY - stage.y
    }

    private fun loadDraggedHandler(draggedEvent: MouseEvent) {
        circlePoint.centerX = draggedEvent.screenX - stage.x
        circlePoint.centerY = draggedEvent.screenY - stage.y
        line?.endX = circlePoint.centerX
        line?.endY = circlePoint.centerY
    }

    private fun loadReleasedHandler(releasedEvent: MouseEvent) {
        val scaleEnd = when (releasedEvent.button) {
            MouseButton.PRIMARY -> loadScaleTransitionForLeftMouseButton(releasedEvent)
            MouseButton.SECONDARY -> loadScaleTransitionForRightMouseButton(releasedEvent)
            else -> null
        }
        scaleEnd?.play()
    }

    private fun loadScaleTransition(): ScaleTransition {
        val scaleEnd = ScaleTransition(Duration.millis(200.0), circlePoint)
        scaleEnd.toX = 0.0
        scaleEnd.toY = 0.0
        return scaleEnd
    }

    private fun loadScaleTransitionForLeftMouseButton(releasedEvent: MouseEvent): ScaleTransition {
        val scaleEnd = loadScaleTransition()
        scaleEnd.setOnFinished {
            val x = (releasedEvent.screenX - stage.x).toInt()
            val y = (releasedEvent.screenY - stage.y).toInt()
            leftMousePoint = Point(x, y)

            val rightMousePoint = rightMousePoint
            val leftMousePoint = leftMousePoint

            if (rightMousePoint != null && leftMousePoint != null) {
                pointPane.valueText.text = VECTOR_CHAR + transformPoint(leftMousePoint, rightMousePoint)
            } else {
                pointPane.valueText.text = leftMousePoint.toString()
            }
            stage.close()
            Main.mainStage.isIconified = false
        }
        return scaleEnd
    }

    private fun loadScaleTransitionForRightMouseButton(releasedEvent: MouseEvent): ScaleTransition {
        val scaleEnd = loadScaleTransition()
        scaleEnd.setOnFinished {
            val x = (releasedEvent.screenX - stage.x).toInt()
            val y = (releasedEvent.screenY - stage.y).toInt()
            rightMousePoint = Point(x, y)

            line = Line(x.toDouble(), y.toDouble(), x.toDouble(), y.toDouble()).apply {
                stroke = Color.AQUA
            }
            pane.setOnMouseMoved { event ->
                line?.endX = event.sceneX
                line?.endY = event.sceneY
            }

            pane.children.add(line)

        }
        return scaleEnd
    }

}
