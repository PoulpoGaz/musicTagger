package fr.poulpogaz.musicdl.ui.text;

import java.awt.*;

public interface IMTextField {

    Component getTrailingComponent();

    void setTrailingComponent(Component trailingComponent);

    Component getLeadingComponent();

    void setLeadingComponent(Component leadingComponent);
}
