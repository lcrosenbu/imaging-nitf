/**
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
 **/
package org.codice.nitf.filereader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class NitfFileParser extends AbstractNitfSegmentParser {

    private long nitfFileLength = -1;
    private int nitfHeaderLength = -1;

    private int numberImageSegments = 0;
    private int numberGraphicSegments = 0;
    private int numberTextSegments = 0;
    private int numberLabelSegments = 0;
    private int numberDataExtensionSegments = 0;
    private int numberReservedExtensionSegments = 0;

    private List<Integer> lish = new ArrayList<Integer>();
    private List<Long> li = new ArrayList<Long>();
    private List<Integer> lssh = new ArrayList<Integer>();
    private List<Integer> ls = new ArrayList<Integer>();
    private List<Integer> llsh = new ArrayList<Integer>();
    private List<Integer> ll = new ArrayList<Integer>();
    private List<Integer> ltsh = new ArrayList<Integer>();
    private List<Integer> lt = new ArrayList<Integer>();
    private List<Integer> ldsh = new ArrayList<Integer>();
    private List<Integer> ld = new ArrayList<Integer>();
    private int userDefinedHeaderDataLength = 0;
    private int extendedHeaderDataLength = 0;

    private static final int FHDR_LENGTH = 4;
    private static final int FVER_LENGTH = 5;
    private static final int CLEVEL_LENGTH = 2;
    private static final int STYPE_LENGTH = 4;
    private static final int OSTAID_LENGTH = 10;
    private static final int FTITLE_LENGTH = 80;
    private static final int ONAME_LENGTH = 24;
    private static final int ONAME20_LENGTH = 27;
    private static final int OPHONE_LENGTH = 18;
    private static final int FL_LENGTH = 12;
    private static final int HL_LENGTH = 6;
    private static final int NUMI_LENGTH = 3;
    private static final int LISH_LENGTH = 6;
    private static final int LI_LENGTH = 10;
    private static final int NUMS_LENGTH = 3;
    private static final int LSSH_LENGTH = 4;
    private static final int LS_LENGTH = 6;
    private static final int NUMX_LENGTH = 3;
    private static final int NUML20_LENGTH = 3;
    private static final int LLSH_LENGTH = 4;
    private static final int LL_LENGTH = 3;
    private static final int NUMT_LENGTH = 3;
    private static final int LTSH_LENGTH = 4;
    private static final int LT_LENGTH = 5;
    private static final int NUMDES_LENGTH = 3;
    private static final int LDSH_LENGTH = 4;
    private static final int LD_LENGTH = 9;
    private static final int NUMRES_LENGTH = 3;
    private static final int UDHDL_LENGTH = 5;
    private static final int UDHOFL_LENGTH = 3;
    private static final int XHDL_LENGTH = 5;
    private static final int XHDLOFL_LENGTH = 3;
    private static final int MIN_COMPLEXITY_LEVEL = 0;
    private static final int MAX_COMPLEXITY_LEVEL = 99;

    private static final long STREAMING_FILE_MODE = 999999999999L;

    private NitfFile nitf = null;
    private Set<ParseOption> parseOptionSet = null;

    NitfFileParser(final InputStream nitfInputStream,
                          final Set<ParseOption> parseOptions,
                          final NitfFile nitfFile) throws ParseException {
        nitf = nitfFile;
        parseOptionSet = parseOptions;

        reader = new NitfReader(new BufferedInputStream(nitfInputStream), 0);
        readFHDRFVER();
        reader.setFileType(nitf.getFileType());
        readCLEVEL();
        readSTYPE();
        readOSTAID();
        readFDT();
        readFTITLE();
        nitf.setFileSecurityMetadata(new NitfFileSecurityMetadata(reader));
        readENCRYP();
        if ((reader.getFileType() == FileType.NITF_TWO_ONE) || (reader.getFileType() == FileType.NSIF_ONE_ZERO)) {
            readFBKGC();
        }
        readONAME();
        readOPHONE();
        readFL();
        if (nitfFileLength == STREAMING_FILE_MODE) {
            if (!reader.canSeek()) {
                throw new ParseException("No support for streaming mode unless input is seekable", 0);
            }
            // TODO If we can ever seek, we need to read the streaming mode DES and update properties here.
        }
        readHL();
        readNUMI();
        for (int i = 0; i < numberImageSegments; ++i) {
            readLISH();
            readLI();
        }
        readNUMS();
        for (int i = 0; i < numberGraphicSegments; ++i) {
            readLSSH();
            readLS();
        }
        readNUMX();
        for (int i = 0; i < numberLabelSegments; ++i) {
            readLLSH();
            readLL();
        }
        readNUMT();
        for (int i = 0; i < numberTextSegments; ++i) {
            readLTSH();
            readLT();
        }
        readNUMDES();
        for (int i = 0; i < numberDataExtensionSegments; ++i) {
            readLDSH();
            readLD();
        }
        readNUMRES();
        for (int i = 0; i < numberReservedExtensionSegments; ++i) {
            // TODO: find a case that exercises this and implement it
            throw new UnsupportedOperationException("IMPLEMENT RES PARSING");
        }
        readUDHDL();
        if (userDefinedHeaderDataLength > 0) {
            readUDHOFL();
            readUDHD();
        }
        readXHDL();
        if (extendedHeaderDataLength > 0) {
            readXHDLOFL();
            readXHD();
        }
        readImageSegments();
        if (reader.getFileType() == FileType.NITF_TWO_ZERO) {
            readSymbolSegments();
            readLabelSegments();
        } else {
            readGraphicSegments();
        }
        readTextSegments();
        readDataExtensionSegments();
    }

    private void readFHDRFVER() throws ParseException {
        String fhdrfver = reader.readBytes(FHDR_LENGTH + FVER_LENGTH);
        nitf.setFileType(FileType.getEnumValue(fhdrfver));
    }

    private void readCLEVEL() throws ParseException {
        nitf.setComplexityLevel(reader.readBytesAsInteger(CLEVEL_LENGTH));
        if ((nitf.getComplexityLevel() < MIN_COMPLEXITY_LEVEL) || (nitf.getComplexityLevel() > MAX_COMPLEXITY_LEVEL)) {
            throw new ParseException(String.format("CLEVEL out of range: %i", nitf.getComplexityLevel()), reader.getCurrentOffset());
        }
    }

    private void readSTYPE() throws ParseException {
        nitf.setStandardType(reader.readTrimmedBytes(STYPE_LENGTH));
    }

    private void readOSTAID() throws ParseException {
        nitf.setOriginatingStationId(reader.readTrimmedBytes(OSTAID_LENGTH));
    }

    private void readFDT() throws ParseException {
        nitf.setFileDateTime(reader.readNitfDateTime());
    }

    private void readFTITLE() throws ParseException {
        nitf.setFileTitle(reader.readTrimmedBytes(FTITLE_LENGTH));
    }

    private void readFBKGC() throws ParseException {
        nitf.setFileBackgroundColour(readRGBColour());
    }

    private void readONAME() throws ParseException {
        if ((nitf.getFileType() == FileType.NITF_TWO_ONE) || (nitf.getFileType() == FileType.NSIF_ONE_ZERO)) {
            nitf.setOriginatorsName(reader.readTrimmedBytes(ONAME_LENGTH));
        } else {
            nitf.setOriginatorsName(reader.readTrimmedBytes(ONAME20_LENGTH));
        }
    }

    private void readOPHONE() throws ParseException {
        nitf.setOriginatorsPhoneNumber(reader.readTrimmedBytes(OPHONE_LENGTH));
    }

    private void readFL() throws ParseException {
        nitfFileLength = reader.readBytesAsLong(FL_LENGTH);
    }

    private void readHL() throws ParseException {
        nitfHeaderLength = reader.readBytesAsInteger(HL_LENGTH);
    }

    private void readNUMI() throws ParseException {
        numberImageSegments = reader.readBytesAsInteger(NUMI_LENGTH);
    }

    private void readLISH() throws ParseException {
        lish.add(reader.readBytesAsInteger(LISH_LENGTH));
    }

    private void readLI() throws ParseException {
        li.add(reader.readBytesAsLong(LI_LENGTH));
    }

    // The next three methods are also used for NITF 2.0 Symbol segment lengths
    private void readNUMS() throws ParseException {
        numberGraphicSegments = reader.readBytesAsInteger(NUMS_LENGTH);
    }

    private void readLSSH() throws ParseException {
        lssh.add(reader.readBytesAsInteger(LSSH_LENGTH));
    }

    private void readLS() throws ParseException {
        ls.add(reader.readBytesAsInteger(LS_LENGTH));
    }

    private void readNUMX() throws ParseException {
        if (reader.getFileType() == FileType.NITF_TWO_ZERO) {
            numberLabelSegments = reader.readBytesAsInteger(NUML20_LENGTH);
        } else {
            reader.skip(NUMX_LENGTH);
        }
    }

    private void readLLSH() throws ParseException {
        llsh.add(reader.readBytesAsInteger(LLSH_LENGTH));
    }

    private void readLL() throws ParseException {
        ll.add(reader.readBytesAsInteger(LL_LENGTH));
    }

    private void readNUMT() throws ParseException {
       numberTextSegments = reader.readBytesAsInteger(NUMT_LENGTH);
    }

    private void readLTSH() throws ParseException {
        ltsh.add(reader.readBytesAsInteger(LTSH_LENGTH));
    }

    private void readLT() throws ParseException {
        lt.add(reader.readBytesAsInteger(LT_LENGTH));
    }

    private void readNUMDES() throws ParseException {
        numberDataExtensionSegments = reader.readBytesAsInteger(NUMDES_LENGTH);
    }

    private void readLDSH() throws ParseException {
        ldsh.add(reader.readBytesAsInteger(LDSH_LENGTH));
    }

    private void readLD() throws ParseException {
        ld.add(reader.readBytesAsInteger(LD_LENGTH));
    }

    private void readNUMRES() throws ParseException {
        numberReservedExtensionSegments = reader.readBytesAsInteger(NUMRES_LENGTH);
    }

    private void readUDHDL() throws ParseException {
        userDefinedHeaderDataLength = reader.readBytesAsInteger(UDHDL_LENGTH);
    }

    private void readUDHOFL() throws ParseException {
        nitf.setUserDefinedHeaderOverflow(reader.readBytesAsInteger(UDHOFL_LENGTH));
    }

    private void readUDHD() throws ParseException {
        TreParser treParser = new TreParser();
        TreCollection userDefinedHeaderTREs = treParser.parse(reader, userDefinedHeaderDataLength - UDHOFL_LENGTH);
        nitf.mergeTREs(userDefinedHeaderTREs);
    }

    private void readXHDL() throws ParseException {
        extendedHeaderDataLength = reader.readBytesAsInteger(XHDL_LENGTH);
    }

    private void readXHDLOFL() throws ParseException {
        nitf.setExtendedHeaderDataOverflow(reader.readBytesAsInteger(XHDLOFL_LENGTH));
    }

    private void readXHD() throws ParseException {
        TreParser treParser = new TreParser();
        TreCollection extendedHeaderTres = treParser.parse(reader, extendedHeaderDataLength - XHDLOFL_LENGTH);
        nitf.mergeTREs(extendedHeaderTres);
    }

    private void readImageSegments() throws ParseException {
        for (int i = 0; i < numberImageSegments; ++i) {
            NitfImageSegment imageSegment = new NitfImageSegment();
            imageSegment.parse(reader, li.get(i), parseOptionSet);
            nitf.addImageSegment(imageSegment);
        }
    }

    private void readGraphicSegments() throws ParseException {
        for (int i = 0; i < numberGraphicSegments; ++i) {
            NitfGraphicSegment graphicSegment = new NitfGraphicSegment();
            graphicSegment.parse(reader, ls.get(i), parseOptionSet);
            nitf.addGraphicSegment(graphicSegment);
        }
    }

    // We reuse the Graphic Segment length values here, but generate different type
    private void readSymbolSegments() throws ParseException {
        for (int i = 0; i < numberGraphicSegments; ++i) {
            NitfSymbolSegment symbolSegment = new NitfSymbolSegment();
            symbolSegment.parse(reader, ls.get(i), parseOptionSet);
            nitf.addSymbolSegment(symbolSegment);
        }
    }

    private void readLabelSegments() throws ParseException {
        for (int i = 0; i < numberLabelSegments; ++i) {
            NitfLabelSegment labelSegment = new NitfLabelSegment();
            labelSegment.parse(reader, ll.get(i), parseOptionSet);
            nitf.addLabelSegment(labelSegment);
        }
    }

    private void readTextSegments() throws ParseException {
        for (int i = 0; i < numberTextSegments; ++i) {
            NitfTextSegment textSegment = new NitfTextSegment();
            textSegment.parse(reader, lt.get(i), parseOptionSet);
            nitf.addTextSegment(textSegment);
        }
    }

    private void readDataExtensionSegments() throws ParseException {
        for (int i = 0; i < numberDataExtensionSegments; ++i) {
            NitfDataExtensionSegment segment = new NitfDataExtensionSegment();
            segment.parse(reader, ld.get(i));
            nitf.addDataExtensionSegment(segment);
        }
    }
}
