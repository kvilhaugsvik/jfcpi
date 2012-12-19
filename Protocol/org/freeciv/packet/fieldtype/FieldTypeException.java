/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.freeciv.packet.fieldtype;

public class FieldTypeException extends IllegalStateException {
    private String field = null;
    private String inPacket = null;

    public FieldTypeException(String message) {
        super(message);
    }

    public FieldTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setInPacket(String inPacket) {
        this.inPacket = inPacket;
    }

    public String getField() {
        return field;
    }

    public String getInPacket() {
        return inPacket;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        if (null != inPacket) {
            message.append(inPacket);
            message.append(": ");
        }
        if (null != field) {
            message.append(field);
            message.append(": ");
        }
        message.append(super.getMessage());

        return message.toString();
    }
}
