package fr.poulpogaz.musictagger.ui.text;

import java.awt.*;

public interface IMTextField {

    Component getTrailingComponent();

    void setTrailingComponent(Component trailingComponent);

    Component getLeadingComponent();

    void setLeadingComponent(Component leadingComponent);
}
