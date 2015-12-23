/*
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 */
package org.codice.imaging.nitf.nitfnetbeansfiletype;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import org.codice.imaging.nitf.core.FileReader;
import org.codice.imaging.nitf.core.NitfImageSegmentHeader;
import org.codice.imaging.nitf.core.NitfLabelSegmentHeader;
import org.codice.imaging.nitf.core.NitfReader;
import org.codice.imaging.nitf.core.NitfSymbolSegmentHeader;
import org.codice.imaging.nitf.core.NitfTextSegmentHeader;
import org.codice.imaging.nitf.core.SlottedNitfParseStrategy;
import org.codice.imaging.nitf.core.dataextension.NitfDataExtensionSegmentHeader;
import org.codice.imaging.nitf.core.graphic.NitfGraphicSegmentHeader;

class DeferredSegmentParseStrategy extends SlottedNitfParseStrategy {

    private final List<Long> imageSegmentHeaderOffsets = new ArrayList<>();
    private final List<Long> imageSegmentDataOffsets = new ArrayList<>();
    private final List<Long> graphicSegmentHeaderOffsets = new ArrayList<>();
    private final List<Long> graphicSegmentDataOffsets = new ArrayList<>();
    private final List<Long> symbolSegmentHeaderOffsets = new ArrayList<>();
    private final List<Long> symbolSegmentDataOffsets = new ArrayList<>();
    private final List<Long> labelSegmentHeaderOffsets = new ArrayList<>();
    private final List<Long> labelSegmentDataOffsets = new ArrayList<>();
    private final List<Long> textSegmentHeaderOffsets = new ArrayList<>();
    private final List<Long> textSegmentDataOffsets = new ArrayList<>();
    private final List<Long> dataExtensionSegmentHeaderOffsets = new ArrayList<>();
    private final List<Long> dataExtensionSegmentDataOffsets = new ArrayList<>();
    private FileReader fileReader;

    public DeferredSegmentParseStrategy() {
    }

    @Override
    protected final void handleImageSegment(final NitfReader reader, final int i) throws ParseException {
        long headerOffset = getCurrentOffset(reader);
        getImageSegmentHeaderOffsets().add(headerOffset);
        long dataOffset = headerOffset + nitfFileLevelHeader.getImageSegmentSubHeaderLengths().get(i);
        getImageSegmentDataOffsets().add(dataOffset);
        reader.seekToAbsoluteOffset(dataOffset + nitfFileLevelHeader.getImageSegmentDataLengths().get(i));
    }

    @Override
    protected final void handleSymbolSegment(final NitfReader reader, final int i) throws ParseException {
        long headerOffset = getCurrentOffset(reader);
        getSymbolSegmentHeaderOffsets().add(headerOffset);
        long dataOffset = headerOffset + nitfFileLevelHeader.getSymbolSegmentSubHeaderLengths().get(i);
        getSymbolSegmentDataOffsets().add(dataOffset);
        reader.seekToAbsoluteOffset(dataOffset + nitfFileLevelHeader.getSymbolSegmentDataLengths().get(i));
    }

    @Override
    protected final void handleLabelSegment(final NitfReader reader, final int i) throws ParseException {
        long headerOffset = getCurrentOffset(reader);
        getLabelSegmentHeaderOffsets().add(headerOffset);
        long dataOffset = headerOffset + nitfFileLevelHeader.getLabelSegmentSubHeaderLengths().get(i);
        getLabelSegmentDataOffsets().add(dataOffset);
        reader.seekToAbsoluteOffset(dataOffset + nitfFileLevelHeader.getLabelSegmentDataLengths().get(i));
    }

    @Override
    protected final void handleGraphicSegment(final NitfReader reader, final int i) throws ParseException {
        long headerOffset = getCurrentOffset(reader);
        getGraphicSegmentHeaderOffsets().add(headerOffset);
        long dataOffset = headerOffset + nitfFileLevelHeader.getGraphicSegmentSubHeaderLengths().get(i);
        getGraphicSegmentDataOffsets().add(dataOffset);
        reader.seekToAbsoluteOffset(dataOffset + nitfFileLevelHeader.getGraphicSegmentDataLengths().get(i));
    }

    @Override
    protected final void handleTextSegment(final NitfReader reader, final int i) throws ParseException {
        long headerOffset = getCurrentOffset(reader);
        getTextSegmentHeaderOffsets().add(headerOffset);
        long dataOffset = headerOffset + nitfFileLevelHeader.getTextSegmentSubHeaderLengths().get(i);
        getTextSegmentDataOffsets().add(dataOffset);
        reader.seekToAbsoluteOffset(dataOffset + nitfFileLevelHeader.getTextSegmentDataLengths().get(i));
    }

    @Override
    protected final void handleDataExtensionSegment(final NitfReader reader, final int i) throws ParseException {
        long headerOffset = getCurrentOffset(reader);
        getDataExtensionSegmentHeaderOffsets().add(headerOffset);
        long dataOffset = headerOffset + nitfFileLevelHeader.getDataExtensionSegmentSubHeaderLengths().get(i);
        getDataExtensionSegmentDataOffsets().add(dataOffset);
        reader.seekToAbsoluteOffset(dataOffset + nitfFileLevelHeader.getDataExtensionSegmentDataLengths().get(i));
    }

    private long getCurrentOffset(final NitfReader reader) {
        fileReader = (FileReader) reader;
        return reader.getCurrentOffset();
    }

    /**
     * @return the imageSegmentHeadersOffsets
     */
    public List<Long> getImageSegmentHeaderOffsets() {
        return imageSegmentHeaderOffsets;
    }

    /**
     * @return the imageSegmentDataOffsets
     */
    public List<Long> getImageSegmentDataOffsets() {
        return imageSegmentDataOffsets;
    }

    /**
     * @return the graphicSegmentHeadersOffsets
     */
    public List<Long> getGraphicSegmentHeaderOffsets() {
        return graphicSegmentHeaderOffsets;
    }

    /**
     * @return the graphicSegmentDataOffsets
     */
    public List<Long> getGraphicSegmentDataOffsets() {
        return graphicSegmentDataOffsets;
    }

    /**
     * @return the symbolSegmentHeadersOffsets
     */
    public List<Long> getSymbolSegmentHeaderOffsets() {
        return symbolSegmentHeaderOffsets;
    }

    /**
     * @return the symbolSegmentDataOffsets
     */
    public List<Long> getSymbolSegmentDataOffsets() {
        return symbolSegmentDataOffsets;
    }

    /**
     * @return the labelSegmentHeadersOffsets
     */
    public List<Long> getLabelSegmentHeaderOffsets() {
        return labelSegmentHeaderOffsets;
    }

    /**
     * @return the labelSegmentDataOffsets
     */
    public List<Long> getLabelSegmentDataOffsets() {
        return labelSegmentDataOffsets;
    }

    /**
     * @return the textSegmentHeaderOffsets
     */
    public List<Long> getTextSegmentHeaderOffsets() {
        return textSegmentHeaderOffsets;
    }

    /**
     * @return the textSegmentDataOffsets
     */
    public List<Long> getTextSegmentDataOffsets() {
        return textSegmentDataOffsets;
    }

    /**
     * @return the dataExtensionSegmentHeaderOffsets
     */
    public List<Long> getDataExtensionSegmentHeaderOffsets() {
        return dataExtensionSegmentHeaderOffsets;
    }

    /**
     * @return the dataExtensionSegmentDataOffsets
     */
    public List<Long> getDataExtensionSegmentDataOffsets() {
        return dataExtensionSegmentDataOffsets;
    }

    NitfImageSegmentHeader getImageSegmentHeader(final int index) throws ParseException {
        long segmentHeaderOffset = imageSegmentHeaderOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentHeaderOffset);
        return readImageSegmentHeader(fileReader, index);
    }

    NitfGraphicSegmentHeader getGraphicSegmentHeader(final int index) throws ParseException {
        long segmentHeaderOffset = graphicSegmentHeaderOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentHeaderOffset);
        return readGraphicSegmentHeader(fileReader, index);
    }

    NitfSymbolSegmentHeader getSymbolSegmentHeader(final int index) throws ParseException {
        long segmentHeaderOffset = symbolSegmentHeaderOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentHeaderOffset);
        return readSymbolSegmentHeader(fileReader, index);
    }

    NitfLabelSegmentHeader getLabelSegmentHeader(final int index) throws ParseException {
        long segmentHeaderOffset = labelSegmentHeaderOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentHeaderOffset);
        return readLabelSegmentHeader(fileReader, index);
    }

    String getLabelSegmentData(final NitfLabelSegmentHeader header, final int index) throws ParseException {
        long segmentDataOffset = labelSegmentDataOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentDataOffset);
        return fileReader.readBytes(header.getLabelDataLength());
    }

    NitfTextSegmentHeader getTextSegmentHeader(final int index) throws ParseException {
        long segmentHeaderOffset = textSegmentHeaderOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentHeaderOffset);
        return readTextSegmentHeader(fileReader, index);
    }

    String getTextSegmentData(final NitfTextSegmentHeader header, final int index) throws ParseException {
        long segmentDataOffset = textSegmentDataOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentDataOffset);
        return fileReader.readBytes(header.getTextDataLength());
    }

    NitfDataExtensionSegmentHeader getDataExtensionSegmentHeader(final int index) throws ParseException {
        long segmentHeaderOffset = dataExtensionSegmentHeaderOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentHeaderOffset);
        return readDataExtensionSegmentHeader(fileReader, index);
    }

    void parseDataExtensionSegmentData(final NitfDataExtensionSegmentHeader header, final int index) throws ParseException {
        long segmentDataOffset = dataExtensionSegmentDataOffsets.get(index);
        fileReader.seekToAbsoluteOffset(segmentDataOffset);
        readDataExtensionSegmentData(header, fileReader);
    }

    byte[] getDataExtensionSegmentData(final int index) {
        return this.dataExtensionSegmentData.get(index);
    }

    final InputStream getGraphicSegmentDataReader(final int index) throws ParseException {
        long segmentDataOffset = graphicSegmentDataOffsets.get(index);
        return fileReader.getInputStreamAt(segmentDataOffset);
    }

    final ImageInputStream getImageSegmentDataReader(final int index) throws ParseException {
        long segmentDataOffset = imageSegmentDataOffsets.get(index);
        return fileReader.getImageInputStreamAt(segmentDataOffset);
    }

    final InputStream getSymbolSegmentDataReader(final int index) throws ParseException {
        long segmentDataOffset = symbolSegmentDataOffsets.get(index);
        return fileReader.getInputStreamAt(segmentDataOffset);
    }
}
