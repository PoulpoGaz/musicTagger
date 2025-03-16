package fr.poulpogaz.musictagger.ui.layout;

public class HorizontalConstraint implements Cloneable {

    public static final int DEFAULT_GAP = -1;

    public HCOrientation orientation;
    public int leftGap;
    public int rightGap;

    public boolean endComponent;

    public float yAlignment;
    public boolean fillYAxis;

    public HorizontalConstraint() {
        this(HCOrientation.LEFT);
    }

    public HorizontalConstraint(HCOrientation orientation) {
        this(orientation, DEFAULT_GAP, DEFAULT_GAP, false, 0.5f, false);
    }

    public HorizontalConstraint(HCOrientation orientation, int leftGap, int rightGap, boolean endComponent, float yAlignment, boolean fillYAxis) {
        this.orientation = orientation;
        this.leftGap = leftGap;
        this.rightGap = rightGap;
        this.endComponent = endComponent;
        this.yAlignment = yAlignment;
        this.fillYAxis = fillYAxis;
    }

    @Override
    public String toString() {
        return "HorizontalConstraint{" +
                "orientation=" + orientation +
                ", leftGap=" + leftGap +
                ", rightGap=" + rightGap +
                ", endComponent=" + endComponent +
                ", yAlignment=" + yAlignment +
                '}';
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
}