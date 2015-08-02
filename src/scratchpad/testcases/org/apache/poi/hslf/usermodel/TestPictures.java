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

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.blip.*;
import org.apache.poi.sl.usermodel.PictureData.PictureType;

/**
 * Test adding/reading pictures
 *
 * @author Yegor Kozlov
 */
public final class TestPictures extends TestCase{
    private static POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

    //protected File cwd;

    /**
     * Test read/write Macintosh PICT
     */
    public void testPICT() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("cow.pict");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.PICT);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        HSLFPictureData[] pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures[0]);

        assertEquals(1, pictures.length);
        assertEquals(PictureType.PICT, pictures[0].getType());
        assertTrue(pictures[0] instanceof PICT);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures[0].getData();
        assertEquals(src_bytes.length, ppt_bytes.length);
        //in PICT the first 512 bytes are MAC specific and may not be preserved, ignore them
        byte[] b1 = new byte[src_bytes.length-512];
        System.arraycopy(src_bytes, 512, b1, 0, b1.length);
        byte[] b2 = new byte[ppt_bytes.length-512];
        System.arraycopy(ppt_bytes, 512, b2, 0, b2.length);
        assertArrayEquals(b1, b2);
    }

    /**
     * Test read/write WMF
     */
    public void testWMF() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("santa.wmf");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.WMF);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        HSLFPictureData[] pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures[0]);

        assertEquals(1, pictures.length);
        assertEquals(PictureType.WMF, pictures[0].getType());
        assertTrue(pictures[0] instanceof WMF);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures[0].getData();
        assertEquals(src_bytes.length, ppt_bytes.length);
        //in WMF the first 22 bytes - is a metafile header
        byte[] b1 = new byte[src_bytes.length-22];
        System.arraycopy(src_bytes, 22, b1, 0, b1.length);
        byte[] b2 = new byte[ppt_bytes.length-22];
        System.arraycopy(ppt_bytes, 22, b2, 0, b2.length);
        assertArrayEquals(b1, b2);
    }

    /**
     * Test read/write EMF
     */
    public void testEMF() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("wrench.emf");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.EMF);

        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can get this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        HSLFPictureData[] pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures[0]);

        assertEquals(1, pictures.length);
        assertEquals(PictureType.EMF, pictures[0].getType());
        assertTrue(pictures[0] instanceof EMF);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures[0].getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Test read/write PNG
     */
    public void testPNG() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("tomcat.png");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.PNG);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        HSLFPictureData[] pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures[0]);

        assertEquals(1, pictures.length);
        assertEquals(PictureType.PNG, pictures[0].getType());
        assertTrue(pictures[0] instanceof PNG);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures[0].getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Test read/write JPEG
     */
    public void testJPEG() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("clock.jpg");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.JPEG);

        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        HSLFPictureData[] pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures[0]);

        assertEquals(1, pictures.length);
        assertEquals(PictureType.JPEG, pictures[0].getType());
        assertTrue(pictures[0] instanceof JPEG);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures[0].getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Test read/write DIB
     */
    public void testDIB() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] src_bytes = slTests.readFile("clock.dib");
        HSLFPictureData data = ppt.addPicture(src_bytes, PictureType.DIB);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        assertEquals(data.getIndex(), pict.getPictureIndex());
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new HSLFSlideShowImpl(new ByteArrayInputStream(out.toByteArray())));

        //make sure we can read this picture shape and it refers to the correct picture data
        List<HSLFShape> sh = ppt.getSlides().get(0).getShapes();
        assertEquals(1, sh.size());
        pict = (HSLFPictureShape)sh.get(0);
        assertEquals(data.getIndex(), pict.getPictureIndex());

        //check picture data
        HSLFPictureData[] pictures = ppt.getPictureData();
        //the Picture shape refers to the PictureData object in the Presentation
        assertEquals(pict.getPictureData(), pictures[0]);

        assertEquals(1, pictures.length);
        assertEquals(PictureType.DIB, pictures[0].getType());
        assertTrue(pictures[0] instanceof DIB);
        //compare the content of the initial file with what is stored in the PictureData
        byte[] ppt_bytes = pictures[0].getData();
        assertArrayEquals(src_bytes, ppt_bytes);
    }

    /**
     * Read pictures in different formats from a reference slide show
     */
    public void testReadPictures() throws Exception {

        byte[] src_bytes, ppt_bytes, b1, b2;
        HSLFPictureShape pict;
        HSLFPictureData pdata;

        HSLFSlideShow ppt = new HSLFSlideShow(slTests.openResourceAsStream("pictures.ppt"));
        List<HSLFSlide> slides = ppt.getSlides();
        HSLFPictureData[] pictures = ppt.getPictureData();
        assertEquals(5, pictures.length);

        pict = (HSLFPictureShape)slides.get(0).getShapes().get(0); //the first slide contains JPEG
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof JPEG);
        assertEquals(PictureType.JPEG, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("clock.jpg");
        assertArrayEquals(src_bytes, ppt_bytes);

        pict = (HSLFPictureShape)slides.get(1).getShapes().get(0); //the second slide contains PNG
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof PNG);
        assertEquals(PictureType.PNG, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("tomcat.png");
        assertArrayEquals(src_bytes, ppt_bytes);

        pict = (HSLFPictureShape)slides.get(2).getShapes().get(0); //the third slide contains WMF
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("santa.wmf");
        assertEquals(src_bytes.length, ppt_bytes.length);
        //ignore the first 22 bytes - it is a WMF metafile header
        b1 = new byte[src_bytes.length-22];
        System.arraycopy(src_bytes, 22, b1, 0, b1.length);
        b2 = new byte[ppt_bytes.length-22];
        System.arraycopy(ppt_bytes, 22, b2, 0, b2.length);
        assertArrayEquals(b1, b2);

        pict = (HSLFPictureShape)slides.get(3).getShapes().get(0); //the forth slide contains PICT
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof PICT);
        assertEquals(PictureType.PICT, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("cow.pict");
        assertEquals(src_bytes.length, ppt_bytes.length);
        //ignore the first 512 bytes - it is a MAC specific crap
        b1 = new byte[src_bytes.length-512];
        System.arraycopy(src_bytes, 512, b1, 0, b1.length);
        b2 = new byte[ppt_bytes.length-512];
        System.arraycopy(ppt_bytes, 512, b2, 0, b2.length);
        assertArrayEquals(b1, b2);

        pict = (HSLFPictureShape)slides.get(4).getShapes().get(0); //the fifth slide contains EMF
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof EMF);
        assertEquals(PictureType.EMF, pdata.getType());
        src_bytes = pdata.getData();
        ppt_bytes = slTests.readFile("wrench.emf");
        assertArrayEquals(src_bytes, ppt_bytes);

    }

	/**
	 * Test that on a party corrupt powerpoint document, which has
	 *  crazy pictures of type 0, we do our best.
	 */
	public void testZeroPictureType() throws Exception {
		HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(slTests.openResourceAsStream("PictureTypeZero.ppt"));

		// Should still have 2 real pictures
		assertEquals(2, hslf.getPictures().length);
		// Both are real pictures, both WMF
		assertEquals(PictureType.WMF, hslf.getPictures()[0].getType());
		assertEquals(PictureType.WMF, hslf.getPictures()[1].getType());

		// Now test what happens when we use the SlideShow interface
		HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        List<HSLFSlide> slides = ppt.getSlides();
        HSLFPictureData[] pictures = ppt.getPictureData();
        assertEquals(12, slides.size());
        assertEquals(2, pictures.length);

		HSLFPictureShape pict;
		HSLFPictureData pdata;

        pict = (HSLFPictureShape)slides.get(0).getShapes().get(1); // 2nd object on 1st slide
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        pict = (HSLFPictureShape)slides.get(0).getShapes().get(2); // 3rd object on 1st slide
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());
	}

    /**
     * YK: The test is disabled because the owner asked to delete the test file from POI svn.
     * See "Please remove my file from your svn" on @poi-dev from Dec 12, 2013
     */
	public void disabled_testZeroPictureLength() throws Exception {
        // take the data from www instead of test directory
        URL url = new URL("http://www.cs.sfu.ca/~anoop/courses/CMPT-882-Fall-2002/chris.ppt");
		HSLFSlideShowImpl hslf = new HSLFSlideShowImpl(url.openStream());

		// Should still have 2 real pictures
		assertEquals(2, hslf.getPictures().length);
		// Both are real pictures, both WMF
		assertEquals(PictureType.WMF, hslf.getPictures()[0].getType());
		assertEquals(PictureType.WMF, hslf.getPictures()[1].getType());

		// Now test what happens when we use the SlideShow interface
		HSLFSlideShow ppt = new HSLFSlideShow(hslf);
        List<HSLFSlide> slides = ppt.getSlides();
        HSLFPictureData[] pictures = ppt.getPictureData();
        assertEquals(27, slides.size());
        assertEquals(2, pictures.length);

		HSLFPictureShape pict;
		HSLFPictureData pdata;

        pict = (HSLFPictureShape)slides.get(6).getShapes().get(13);
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        pict = (HSLFPictureShape)slides.get(7).getShapes().get(13);
        pdata = pict.getPictureData();
        assertTrue(pdata instanceof WMF);
        assertEquals(PictureType.WMF, pdata.getType());

        //add a new picture, it should be correctly appended to the Pictures stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(HSLFPictureData p : pictures) p.write(out);
        out.close();

        int streamSize = out.size();

        HSLFPictureData data = HSLFPictureData.create(PictureType.JPEG);
        data.setData(new byte[100]);
        int offset = hslf.addPicture(data);
        assertEquals(streamSize, offset);
        assertEquals(3, ppt.getPictureData().length);

    }

    public void testGetPictureName() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow(slTests.openResourceAsStream("ppt_with_png.ppt"));
        HSLFSlide slide = ppt.getSlides().get(0);

        HSLFPictureShape p = (HSLFPictureShape)slide.getShapes().get(0); //the first slide contains JPEG
        assertEquals("test", p.getPictureName());
    }

    public void testSetPictureName() throws Exception {
        HSLFSlideShow ppt = new HSLFSlideShow();

        HSLFSlide slide = ppt.createSlide();
        byte[] img = slTests.readFile("tomcat.png");
        HSLFPictureData data = ppt.addPicture(img, PictureType.PNG);
        HSLFPictureShape pict = new HSLFPictureShape(data);
        pict.setPictureName("tomcat.png");
        slide.addShape(pict);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        out.close();

        ppt = new HSLFSlideShow(new ByteArrayInputStream(out.toByteArray()));

        HSLFPictureShape p = (HSLFPictureShape)ppt.getSlides().get(0).getShapes().get(0);
        assertEquals("tomcat.png", p.getPictureName());
    }
}
