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

package com.kvilhaugsvik.javaGenerator.formating;

import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;

import javax.naming.OperationNotSupportedException;
import java.util.List;

public interface TokensToStringStyle {
    String OUTER_LEVEL = "Outside";
    String GROUP = "Group";
    String ARGUMENTS = "Arguments";

    List<String> asFormattedLines(CodeAtoms from);

    interface FormattingProcess {
        void start() throws OperationNotSupportedException;
        void insertSpace();
        void breakLineBlock();
        void breakLine();
        void indent();
        void scopeReset();
        void scopeEnter();
        void scopeExit();
        void insertStar();
    }
}
