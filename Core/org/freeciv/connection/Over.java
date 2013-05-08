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

package org.freeciv.connection;

public interface Over extends ConnectionRelated {
    /**
     * Stop reading when no more data is waiting
     */
    void setStopReadingWhenOutOfInput();

    /**
     * Run this when all is over to close stuff etc
     */
    void whenDone();

    /**
     * Is no more input expected?
     * @return true if no more input is expected
     */
    boolean shouldIStopReadingWhenOutOfInput();

    /**
     * Is the underlying resource open?
     * @return true if it is
     */
    boolean isOpen();
}
