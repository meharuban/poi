/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.usermodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.util.*;

/**
 *  <p>
  * Represents a Shape which is the elemental object that composes a drawing.
 *  This class is a wrapper around EscherSpContainer which holds all information
 *  about a shape in PowerPoint document.
 *  </p>
 *  <p>
 *  When you add a shape, you usually specify the dimensions of the shape and the position
 *  of the upper'left corner of the bounding box for the shape relative to the upper'left
 *  corner of the page, worksheet, or slide. Distances in the drawing layer are measured
 *  in points (72 points = 1 inch).
 *  </p>
 * <p>
  *
  * @author Yegor Kozlov
 */
public abstract class HSLFShape implements Shape {

    // For logging
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    /**
     * In Escher absolute distances are specified in
     * English Metric Units (EMUs), occasionally referred to as A units;
     * there are 360000 EMUs per centimeter, 914400 EMUs per inch, 12700 EMUs per point.
     */
    public static final int EMU_PER_INCH = 914400;
    public static final int EMU_PER_POINT = 12700;
    public static final int EMU_PER_CENTIMETER = 360000;

    /**
     * Master DPI (576 pixels per inch).
     * Used by the reference coordinate system in PowerPoint.
     */
    public static final int MASTER_DPI = 576;

    /**
     * Pixels DPI (96 pixels per inch)
     */
    public static final int PIXEL_DPI = 96;

    /**
     * Points DPI (72 pixels per inch)
     */
    public static final int POINT_DPI = 72;

    /**
     * Either EscherSpContainer or EscheSpgrContainer record
     * which holds information about this shape.
     */
    protected EscherContainerRecord _escherContainer;

    /**
     * Parent of this shape.
     * <code>null</code> for the topmost shapes.
     */
    protected ShapeContainer<HSLFShape> _parent;

    /**
     * The <code>Sheet</code> this shape belongs to
     */
    protected HSLFSheet _sheet;

    /**
     * Fill
     */
    protected HSLFFill _fill;

    /**
     * Create a Shape object. This constructor is used when an existing Shape is read from from a PowerPoint document.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent             the parent of this Shape
     */
      protected HSLFShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape> parent){
        _escherContainer = escherRecord;
        _parent = parent;
     }

    /**
     * Creates the lowerlevel escher records for this shape.
     */
    protected abstract EscherContainerRecord createSpContainer(boolean isChild);

    /**
     *  @return the parent of this shape
     */
    public ShapeContainer<HSLFShape> getParent(){
        return _parent;
    }

    /**
     * @return name of the shape.
     */
    public String getShapeName(){
        return getShapeType().nativeName;
    }

    /**
     * @return type of the shape.
     * @see org.apache.poi.hslf.record.RecordTypes
     */
    public ShapeType getShapeType(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return ShapeType.forId(spRecord.getShapeType(), false);
    }

    /**
     * @param type type of the shape.
     * @see org.apache.poi.hslf.record.RecordTypes
     */
    public void setShapeType(ShapeType type){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        spRecord.setShapeType( (short) type.nativeId );
        spRecord.setVersion( (short) 0x2 );
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape.
     * All coordinates are expressed in points (72 dpi).
     *
     * @return the anchor of this shape
     */
    public java.awt.Rectangle getAnchor(){
        Rectangle2D anchor2d = getAnchor2D();
        return anchor2d.getBounds();
    }

    /**
     * Returns the anchor (the bounding box rectangle) of this shape.
     * All coordinates are expressed in points (72 dpi).
     *
     * @return the anchor of this shape
     */
    public Rectangle2D getAnchor2D(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        int x,y,w,h;
        EscherChildAnchorRecord childRec = getEscherChild(EscherChildAnchorRecord.RECORD_ID);
        boolean useChildRec = ((flags & EscherSpRecord.FLAG_CHILD) != 0);
        if (useChildRec && childRec != null){
            x = childRec.getDx1();
            y = childRec.getDy1();
            w = childRec.getDx2()-childRec.getDx1();
            h = childRec.getDy2()-childRec.getDy1();
        } else {
            if (useChildRec) {
                logger.log(POILogger.WARN, "EscherSpRecord.FLAG_CHILD is set but EscherChildAnchorRecord was not found");
            }
            EscherClientAnchorRecord clientRec = getEscherChild(EscherClientAnchorRecord.RECORD_ID);
            x = clientRec.getFlag();
            y = clientRec.getCol1();
            w = clientRec.getDx1()-clientRec.getFlag();
            h = clientRec.getRow1()-clientRec.getCol1();
        }

        // TODO: find out where this -1 value comes from at #57820 (link to ms docs?)
        Rectangle2D anchor = new Rectangle2D.Float(
            (float)(x == -1 ? -1 : Units.masterToPoints(x)),
            (float)(y == -1 ? -1 : Units.masterToPoints(y)),
            (float)(w == -1 ? -1 : Units.masterToPoints(w)),
            (float)(h == -1 ? -1 : Units.masterToPoints(h))
        );
        
        return anchor;
    }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in points (72 dpi).
     *
     * @param anchor new anchor
     */
    public void setAnchor(Rectangle2D anchor){
        int x = Units.pointsToMaster(anchor.getX());
        int y = Units.pointsToMaster(anchor.getY());
        int w = Units.pointsToMaster(anchor.getWidth() + anchor.getX());
        int h = Units.pointsToMaster(anchor.getHeight() + anchor.getY());
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flags = spRecord.getFlags();
        if ((flags & EscherSpRecord.FLAG_CHILD) != 0){
            EscherChildAnchorRecord rec = (EscherChildAnchorRecord)getEscherChild(EscherChildAnchorRecord.RECORD_ID);
            rec.setDx1(x);
            rec.setDy1(y);
            rec.setDx2(w);
            rec.setDy2(h);
        } else {
            EscherClientAnchorRecord rec = (EscherClientAnchorRecord)getEscherChild(EscherClientAnchorRecord.RECORD_ID);
            rec.setFlag((short)x);
            rec.setCol1((short)y);
            rec.setDx1((short)w);
            rec.setRow1((short)h);
        }

    }

    /**
     * Moves the top left corner of the shape to the specified point.
     *
     * @param x the x coordinate of the top left corner of the shape
     * @param y the y coordinate of the top left corner of the shape
     */
    public void moveTo(float x, float y){
        Rectangle2D anchor = getAnchor2D();
        anchor.setRect(x, y, anchor.getWidth(), anchor.getHeight());
        setAnchor(anchor);
    }

    /**
     * Helper method to return escher child by record ID
     *
     * @return escher record or <code>null</code> if not found.
     */
    public static <T extends EscherRecord> T getEscherChild(EscherContainerRecord owner, int recordId){
        return owner.getChildById((short)recordId);
    }

    public <T extends EscherRecord> T getEscherChild(int recordId){
        return _escherContainer.getChildById((short)recordId);
    }
    
    /**
     * Returns  escher property by id.
     *
     * @return escher property or <code>null</code> if not found.
     */
     public static <T extends EscherProperty> T getEscherProperty(EscherOptRecord opt, int propId){
         if (opt == null) return null;
         return opt.lookup(propId);
     }

    /**
     * Set an escher property for this shape.
     *
     * @param opt       The opt record to set the properties to.
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     * @param value     value of the property. If value = -1 then the property is removed.
     */
     public static void setEscherProperty(EscherOptRecord opt, short propId, int value){
        java.util.List<EscherProperty> props = opt.getEscherProperties();
        for ( Iterator<EscherProperty> iterator = props.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getPropertyNumber() == propId){
                iterator.remove();
                break;
            }
        }
        if (value != -1) {
            opt.addEscherProperty(new EscherSimpleProperty(propId, value));
            opt.sortProperties();
        }
    }

    /**
     * Set an simple escher property for this shape.
     *
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     * @param value     value of the property. If value = -1 then the property is removed.
     */
    public void setEscherProperty(short propId, int value){
        EscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, propId, value);
    }

    /**
     * Get the value of a simple escher property for this shape.
     *
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     */
   public int getEscherProperty(short propId){
        EscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, propId);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Get the value of a simple escher property for this shape.
     *
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     */
   public int getEscherProperty(short propId, int defaultValue){
        EscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, propId);
        return prop == null ? defaultValue : prop.getPropertyValue();
    }

    /**
     * @return  The shape container and it's children that can represent this
     *          shape.
     */
    public EscherContainerRecord getSpContainer(){
        return _escherContainer;
    }

    /**
     * Event which fires when a shape is inserted in the sheet.
     * In some cases we need to propagate changes to upper level containers.
     * <br>
     * Default implementation does nothing.
     *
     * @param sh - owning shape
     */
    protected void afterInsert(HSLFSheet sh){
        if(_fill != null) {
            _fill.afterInsert(sh);
        }
    }

    /**
     *  @return the <code>SlideShow</code> this shape belongs to
     */
    public HSLFSheet getSheet(){
        return _sheet;
    }

    /**
     * Assign the <code>SlideShow</code> this shape belongs to
     *
     * @param sheet owner of this shape
     */
    public void setSheet(HSLFSheet sheet){
        _sheet = sheet;
    }

    Color getColor(short colorProperty, short opacityProperty, int defaultColor){
        EscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty p = getEscherProperty(opt, colorProperty);
        if(p == null && defaultColor == -1) return null;

        int val = (p == null) ? defaultColor : p.getPropertyValue();

        EscherColorRef ecr = new EscherColorRef(val);
        
        boolean fPaletteIndex = ecr.hasPaletteIndexFlag();
        boolean fPaletteRGB = ecr.hasPaletteRGBFlag();
        boolean fSystemRGB = ecr.hasSystemRGBFlag();
        boolean fSchemeIndex = ecr.hasSchemeIndexFlag();
        boolean fSysIndex = ecr.hasSysIndexFlag();
        
        int rgb[] = ecr.getRGB();

        HSLFSheet sheet = getSheet();
        if (fSchemeIndex && sheet != null) {
            //red is the index to the color scheme
            ColorSchemeAtom ca = sheet.getColorScheme();
            int schemeColor = ca.getColor(ecr.getSchemeIndex());

            rgb[0] = (schemeColor >> 0) & 0xFF;
            rgb[1] = (schemeColor >> 8) & 0xFF;
            rgb[2] = (schemeColor >> 16) & 0xFF;
        } else if (fPaletteIndex){
            //TODO
        } else if (fPaletteRGB){
            //TODO
        } else if (fSystemRGB){
            //TODO
        } else if (fSysIndex){
            //TODO
        }

        double alpha = getAlpha(opacityProperty);
        return new Color(rgb[0], rgb[1], rgb[2], (int)(alpha*255.0));
    }

    double getAlpha(short opacityProperty) {
        EscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty op = getEscherProperty(opt, opacityProperty);
        int defaultOpacity = 0x00010000;
        int opacity = (op == null) ? defaultOpacity : op.getPropertyValue();
        return Units.fixedPointToDouble(opacity);
    }
    
    Color toRGB(int val){
        int a = (val >> 24) & 0xFF;
        int b = (val >> 16) & 0xFF;
        int g = (val >> 8) & 0xFF;
        int r = (val >> 0) & 0xFF;

        if(a == 0xFE){
            // Color is an sRGB value specified by red, green, and blue fields.
        } else if (a == 0xFF){
            // Color is undefined.
        } else {
            // index in the color scheme
            ColorSchemeAtom ca = getSheet().getColorScheme();
            int schemeColor = ca.getColor(a);

            r = (schemeColor >> 0) & 0xFF;
            g = (schemeColor >> 8) & 0xFF;
            b = (schemeColor >> 16) & 0xFF;
        }
        return new Color(r, g, b);
    }

    /**
     * @return id for the shape.
     */
    public int getShapeId(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return spRecord == null ? 0 : spRecord.getShapeId();
    }

    /**
     * Sets shape ID
     *
     * @param id of the shape
     */
    public void setShapeId(int id){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        if(spRecord != null) spRecord.setShapeId(id);
    }

    /**
     * Fill properties of this shape
     *
     * @return fill properties of this shape
     */
    public HSLFFill getFill(){
        if(_fill == null) {
            _fill = new HSLFFill(this);
        }
        return _fill;
    }

    public FillStyle getFillStyle() {
        return getFill().getFillStyle();
    }

    /**
     * Returns the hyperlink assigned to this shape
     *
     * @return the hyperlink assigned to this shape
     * or <code>null</code> if not found.
     */
    public HSLFHyperlink getHyperlink(){
        return HSLFHyperlink.find(this);
    }

    public void draw(Graphics2D graphics){
        logger.log(POILogger.INFO, "Rendering " + getShapeName());
    }

    public EscherOptRecord getEscherOptRecord() {
        EscherOptRecord opt = getEscherChild(EscherOptRecord.RECORD_ID);
        if (opt == null) {
            opt = getEscherChild(RecordTypes.EscherUserDefined);
        }
        return opt;
    }
    
    public boolean getFlipHorizontal(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return (spRecord.getFlags()& EscherSpRecord.FLAG_FLIPHORIZ) != 0;
    }
     
    public void setFlipHorizontal(boolean flip) {
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flag = spRecord.getFlags() | EscherSpRecord.FLAG_FLIPHORIZ;
        spRecord.setFlags(flag);
    }

    public boolean getFlipVertical(){
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        return (spRecord.getFlags()& EscherSpRecord.FLAG_FLIPVERT) != 0;
    }
    
    public void setFlipVertical(boolean flip) {
        EscherSpRecord spRecord = getEscherChild(EscherSpRecord.RECORD_ID);
        int flag = spRecord.getFlags() | EscherSpRecord.FLAG_FLIPVERT;
        spRecord.setFlags(flag);
    }

    public double getRotation(){
        int rot = getEscherProperty(EscherProperties.TRANSFORM__ROTATION);
        return Units.fixedPointToDouble(rot);
    }
    
    public void setRotation(double theta){
        int rot = Units.doubleToFixedPoint(theta % 360.0);
        setEscherProperty(EscherProperties.TRANSFORM__ROTATION, rot);
    }

    public boolean isPlaceholder() {
        return false;
    }
}