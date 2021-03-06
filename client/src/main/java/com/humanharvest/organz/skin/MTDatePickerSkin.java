package com.humanharvest.organz.skin;

import static com.humanharvest.organz.touch.TouchUtils.convertTouchEvent;

import java.time.LocalDate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.transform.Rotate;

import com.sun.javafx.scene.control.skin.DatePickerSkin;
import org.tuiofx.widgets.utils.Util;

public class MTDatePickerSkin extends DatePickerSkin implements IgnoreSynthesized {

    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    private boolean isShowing;
    private BooleanProperty touchPressed = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            getPopupContent().pseudoClassStateChanged(PRESSED_PSEUDO_CLASS, this.get());
        }

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "pressed";
        }
    };

    public MTDatePickerSkin(DatePicker datePicker) {
        super(datePicker);

        getPopup().setAutoHide(false);

        this.arrowButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (event.isSynthesized()) {
                getBehavior().mouseEntered(event);
            }
        });

        getPopupContent().focusedProperty().addListener((observable, oldValue, newValue) -> {
            Node owner = getSkinnable();
            double offsetY = getSkinnable().prefHeight(-1.0D);
            double angle = Util.getRotationDegreesLocalToScene(owner);
            getPopupContent().getTransforms().setAll(new Rotate(angle));
            Rotate rotate = new Rotate(angle);
            Point2D transformedPoint = rotate.transform(0.0D, offsetY);
            double popupTopLeftX = owner.getLocalToSceneTransform().getTx();
            double popupTopLeftY = owner.getLocalToSceneTransform().getTy();
            double anchorX = popupTopLeftX + transformedPoint.getX() + Util.getOffsetX(owner);
            double anchorY = popupTopLeftY + transformedPoint.getY() + Util.getOffsetY(owner);
            getPopup().setAnchorX(anchorX);
            getPopup().setAnchorY(anchorY);
        });

        final ComboBoxBase<LocalDate> comboBoxBase = getSkinnable();
        Node focusAreaNode = Util.getFocusAreaStartingNode(comboBoxBase);
        if (focusAreaNode != null) {
            focusAreaNode.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!event.isSynthesized() && !isComboBoxOrButton(event.getTarget(), comboBoxBase)) {
                    handleAutoHidingEvents();
                }

            });
            focusAreaNode.addEventFilter(TouchEvent.TOUCH_PRESSED, event -> {
                if (!isComboBoxOrButton(event.getTarget(), comboBoxBase)) {
                    handleAutoHidingEvents();
                }

            });
            comboBoxBase.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                if (!event.isSynthesized() && isComboBoxOrButton(event.getTarget(), comboBoxBase) && isShowing) {
                    handleAutoHidingEvents();
                    isShowing = false;
                } else {
                    isShowing = true;
                }

            });
            getBehavior().getControl().showingProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    getPopup().setAutoHide(false);
                }

            });
            datePicker.getScene().getWindow().focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    handleAutoHidingEvents();
                }

            });
            this.getPopupContent().addEventHandler(TouchEvent.TOUCH_PRESSED, event -> touchPressed.setValue(true));
        }

        getSkinnable().addEventFilter(TouchEvent.TOUCH_RELEASED, event -> {
            EventTarget eventTarget = findDatePicker(event.getTarget());
            getBehavior().mouseReleased(convertTouchEvent(event, eventTarget, 1,
                    MouseEvent.MOUSE_RELEASED));
            event.consume();
        });

        getPopupContent().addEventFilter(TouchEvent.TOUCH_RELEASED, Event::consume);
    }

    private static EventTarget findDatePicker(EventTarget target) {

        EventTarget node = target;

        while (node instanceof Node) {
            if (node instanceof DatePicker) {
                return node;
            }

            node = ((Node)node).getParent();
        }

        return target;
    }

    private boolean isComboBoxOrButton(EventTarget target, ComboBoxBase<LocalDate> comboBoxBase) {
        return target instanceof Node
                && ("arrow-button".equals(((Node) target).getId()) || comboBoxBase.equals(target));
    }

    private void handleAutoHidingEvents() {
        if (getSkinnable().isShowing()) {
            getPopup().hide();
            getSkinnable().hide();
            isShowing = false;
        }
    }
}
