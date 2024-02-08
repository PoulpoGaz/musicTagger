package fr.poulpogaz.musicdb.ui.text;

import com.formdev.flatlaf.ui.FlatPanelUI;
import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class ErrorTextFieldUI extends FlatPanelUI {

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c)
                ? FlatUIUtils.createSharedUI(ErrorTextFieldUI.class, () -> new ErrorTextFieldUI(true))
                : new ErrorTextFieldUI(false);
    }

    private boolean defaultInitialized = false;

    protected Color errorBorderColor;
    protected Color warningBorderColor;

    protected ErrorTextFieldUI(boolean shared) {
        super(shared);
    }

    @Override
    protected void installDefaults(JPanel p) {
        super.installDefaults(p);

        if (!defaultInitialized) {
            errorBorderColor = UIManager.getColor("Component.error.borderColor");
            warningBorderColor = UIManager.getColor("Component.warning.borderColor");

            defaultInitialized = true;
        }
    }

    public Color getErrorBorderColor() {
        return errorBorderColor;
    }

    public Color getWarningBorderColor() {
        return warningBorderColor;
    }
}