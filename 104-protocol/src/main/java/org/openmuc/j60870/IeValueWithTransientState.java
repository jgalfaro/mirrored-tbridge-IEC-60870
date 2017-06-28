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
 * Represents a value with transient state indication (VTI) information element.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class IeValueWithTransientState extends InformationElement {

    private final int value;
    private final boolean transientState;

    /**
     * Creates a VTI (value with transient state indication) information element.
     * 
     * @param value
     *            value between -64 and 63
     * @param transientState
     *            true if in transient state
     */
    public IeValueWithTransientState(int value, boolean transientState) {

        if (value < -64 || value > 63) {
            throw new IllegalArgumentException("Value has to be in the range -64..63");
        }

        this.value = value;
        this.transientState = transientState;

    }

    IeValueWithTransientState(DataInputStream is) throws IOException {
        int b1 = (is.readByte() & 0xff);

        transientState = ((b1 & 0x80) == 0x80);

        if ((b1 & 0x40) == 0x40) {
            value = b1 | 0xffffff80;
        }
        else {
            value = b1 & 0x3f;
        }

    }

    @Override
    int encode(byte[] buffer, int i) {

        if (transientState) {
            buffer[i] = (byte) (value | 0x80);
        }
        else {
            buffer[i] = (byte) (value & 0x7f);
        }

        return 1;

    }

    public int getValue() {
        return value;
    }

    public boolean isTransientState() {
        return transientState;
    }

    @Override
    public String toString() {
        return "Value with transient state, value: " + getValue() + ", transient state: " + isTransientState();
    }
}