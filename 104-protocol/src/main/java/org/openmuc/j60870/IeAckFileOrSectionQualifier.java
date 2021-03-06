/*
 * Copyright 2014-17 Fraunhofer ISE
 *
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * j60870 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j60870 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with j60870.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package main.java.org.openmuc.j60870;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Represents an acknowledge file or section qualifier (AFQ) information element.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class IeAckFileOrSectionQualifier extends InformationElement {

    private final int action;
    private final int notice;

    public IeAckFileOrSectionQualifier(int action, int notice) {
        this.action = action;
        this.notice = notice;
    }

    IeAckFileOrSectionQualifier(DataInputStream is) throws IOException {
        int b1 = (is.readByte() & 0xff);
        action = b1 & 0x0f;
        notice = (b1 >> 4) & 0x0f;
    }

    @Override
    int encode(byte[] buffer, int i) {
        buffer[i] = (byte) (action | (notice << 4));
        return 1;
    }

    public int getRequest() {
        return action;
    }

    public int getFreeze() {
        return notice;
    }

    @Override
    public String toString() {
        return "Acknowledge file or section qualifier, action: " + action + ", notice: " + notice;
    }
}
