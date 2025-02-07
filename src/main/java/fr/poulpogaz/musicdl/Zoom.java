package fr.poulpogaz.musicdl;

import java.awt.image.BufferedImage;

public interface Zoom {

    double getScaleFactor(BufferedImage image, int availableWidth, int availableHeight);

    record Percent(double percent) implements Zoom {

        @Override
            public double getScaleFactor(BufferedImage image, int availableWidth, int availableHeight) {
                return percent;
            }

            @Override
            public String toString() {
                return (int) (percent * 100) + "%";
            }
        }
    
    class Fit implements Zoom {

        public static final Fit INSTANCE = new Fit();

        private Fit() {}

        @Override
        public double getScaleFactor(BufferedImage image, int availableWidth, int availableHeight) {
            if (availableWidth <= 0 || availableHeight <= 0) {
                return 0;
            } else if (image.getWidth() < availableWidth && image.getHeight() < availableHeight) {
                return 1;
            } else if (image.getWidth() < availableWidth) {
                return (double) availableHeight / image.getHeight();
            } else if (image.getHeight() < availableHeight) {
                return (double) availableWidth / image.getWidth();
            } else {
                double widthRatio = (double) availableWidth / image.getWidth();
                double heightRatio = (double) availableHeight / image.getHeight();

                return Math.min(widthRatio, heightRatio);
            }
        }

        @Override
        public String toString() {
            return "Fit";
        }
    }

    class FitStretched implements Zoom {

        public static final FitStretched INSTANCE = new FitStretched();

        private FitStretched() {}

        @Override
        public double getScaleFactor(BufferedImage image, int availableWidth, int availableHeight) {
            if (image.getWidth() < availableWidth && image.getHeight() < availableHeight) {
                double widthRatio = (double) availableWidth / image.getWidth();
                double heightRatio = (double) availableHeight / image.getHeight();

                return Math.min(widthRatio, heightRatio);
            } else {
                return Fit.INSTANCE.getScaleFactor(image, availableWidth, availableHeight);
            }
        }

        @Override
        public String toString() {
            return "Fit and stretched";
        }
    }

    class FillVertically implements Zoom {

        public static final FillVertically INSTANCE = new FillVertically();

        private FillVertically() {}

        @Override
        public double getScaleFactor(BufferedImage image, int availableWidth, int availableHeight) {
            return (double) availableWidth / image.getWidth();
        }

        @Override
        public String toString() {
            return "Fill vertically";
        }
    }

    class FillHorizontally implements Zoom {

        public static final FillHorizontally INSTANCE = new FillHorizontally();

        private FillHorizontally() {}

        @Override
        public double getScaleFactor(BufferedImage image, int availableWidth, int availableHeight) {
            return (double) availableHeight / image.getHeight();
        }

        @Override
        public String toString() {
            return "Fill horizontally";
        }
    }
}
