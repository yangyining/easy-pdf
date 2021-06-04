package com.janeluo.easypdf.draw;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.layout.property.HorizontalAlignment;

/**
 * Element that draws a solid line from left to right.
 * Can be added directly to a document or column.
 * Can also be used to create a separator chunk.
 *
 * @author Paulo Soares
 * @since 2.1.2
 */
public class CustomLineSeparator implements ILineDrawer {

    /**
     * The thickness of the line.
     */
    protected float lineWidth = 1;
    /**
     * The width of the line as a percentage of the available page width.
     */
    protected float percentage = 100;
    /**
     * The color of the line.
     */
    protected Color color;
    /**
     * The alignment of the line.
     */
    protected HorizontalAlignment alignment = null;

    public CustomLineSeparator() {
    }

    public CustomLineSeparator(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public CustomLineSeparator(float lineWidth, float percentage) {
        this.lineWidth = lineWidth;
        this.percentage = percentage;
    }

    public CustomLineSeparator(float lineWidth, float percentage, Color color) {
        this.lineWidth = lineWidth;
        this.percentage = percentage;
        this.color = color;
    }

    public CustomLineSeparator(float lineWidth, float percentage, Color color, HorizontalAlignment alignment) {
        this.lineWidth = lineWidth;
        this.percentage = percentage;
        this.color = color;
        this.alignment = alignment;
    }

    @Override
    public void draw(PdfCanvas canvas, Rectangle drawArea) {

    }

    /**
     * Draws a horizontal line.
     *
     * @param canvas the canvas to draw on
     * @param leftX  the left x coordinate
     * @param rightX the right x coordindate
     * @param y      the y coordinate
     */
    public void drawLine(PdfCanvas canvas, float leftX, float rightX, float y) {
        float w;
        if (getPercentage() < 0) {
            w = -getPercentage();
        } else {
            w = (rightX - leftX) * getPercentage() / 100.0f;
        }
        float s;
        switch (getAlignment()) {
            case LEFT:
                s = 0;
                break;
            case RIGHT:
                s = rightX - leftX - w;
                break;
            default:
                s = (rightX - leftX - w) / 2;
                break;
        }
        canvas.setLineWidth(getLineWidth());
        if (getColor() != null) {
            canvas.setStrokeColor(getColor());
        }
        canvas.moveTo(s + leftX, y);
        canvas.lineTo(s + w + leftX, y);
        canvas.stroke();
    }


    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    @Override
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(HorizontalAlignment alignment) {
        this.alignment = alignment;
    }
}
