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

package org.freeciv.packetgen.enteties;

import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.*;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.WeakVarDec;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import org.freeciv.types.FCEnum;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class Struct extends ClassWriter implements Dependency.Item, Dependency.Maker, DataType {
    private final Set<Requirement> iRequire;
    private final Requirement iProvide;
    private final String cName;
    private final String ioName;
    private final RequiredMulti basicFTForMe;
    private final Pattern basicFTForMePattern;
    private final List<String> fieldNames;
    private final List<Requirement> fieldTypes;

    @Deprecated
    public Struct(String name, List<WeakVarDec> fields, List<TargetClass> partTypes) {
        this(name, fields, partTypes, null);
    }

    public Struct(String name, List<WeakVarDec> fields, List<TargetClass> partTypes, List<Requirement> fieldTypes) {
        this(name, fields, partTypes, fieldTypes, true);
    }

    public Struct(String name, List<WeakVarDec> fields, List<TargetClass> partTypes, List<Requirement> fieldTypes,
                  boolean requiredAsAStruct) {
        super(ClassKind.CLASS,
                TargetPackage.from(FCEnum.class.getPackage()),
                Imports.are(), "Freeciv C code", Collections.<Annotate>emptyList(), name,
                DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        addConstructorFields();

        for (int i = 0; i < fields.size(); i++) {
            final WeakVarDec field = fields.get(i);
            final TargetClass type = partTypes.get(i);

            addObjectConstant(type, field.getName());
            addMethod(Method.newPublicReadObjectState(Comment.no(),
                    type, getterNameJavaish(getField(field.getName())),
                    new Block(RETURN(getField(field.getName()).ref()))));
        }

        Typed<? extends AValue> varsToString = literal("(");
        for (int i = 0; i < fields.size(); i++) {
            if (0 != i)
                varsToString = sum(varsToString, literal(", "));
            varsToString = sum(
                    varsToString,
                    literal(fields.get(i).getName() + ": "),
                    getField(fields.get(i).getName()).ref());
        }
        varsToString = sum(varsToString, literal(")"));
        addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.from(String.class), "toString",
                new Block(RETURN(varsToString))));

        LinkedList<String> parts = new LinkedList<String>();
        for (WeakVarDec field : fields)
            parts.add(getterNameJavaish(getField(field.getName())));

        HashSet<Requirement> neededByFields = new HashSet<Requirement>();
        for (WeakVarDec field : fields) {
            assert (null != field.getTypeRequirement()) : "Type can't be null";
            neededByFields.add(field.getTypeRequirement());
            for (WeakVarDec.ArrayDeclaration dec : field.getDeclarations())
                neededByFields.addAll(dec.maxSize.getReqs());
        }

        this.iRequire = Collections.unmodifiableSet(neededByFields);
        this.ioName = name;
        this.cName = (requiredAsAStruct ? "struct" + " " : "") + name;
        this.iProvide = new Requirement(cName, DataType.class);
        /*
         * TODO: Consider if creating a new implementation of Required in stead of using RequiredMulti is saner
         */
        this.basicFTForMePattern = Pattern.compile("(" + name + "|" + "(\\{(\\{*\\w+\\}*)(;\\{*\\w+\\}*)*\\})" + ")" +
                "\\(" + cName + "\\)");
        this.basicFTForMe = new RequiredMulti(FieldType.class, basicFTForMePattern);
        this.fieldTypes = fieldTypes;
        this.fieldNames = Collections.unmodifiableList(parts);
    }

    @Override
    public Collection<Requirement> getReqs() {
        return iRequire;
    }

    @Override
    public Requirement getIFulfillReq() {
        return iProvide;
    }

    @Override
    public Required getICanProduceReq() {
        return basicFTForMe;
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        final String ioPart = extractIOPart(toProduce);

        if (ioName.equals(ioPart))
            return Arrays.asList(new Requirement(FieldType.class.getSimpleName() + ":" + toProduce.getName(),
                    Wrapper.Wrapped.class));

        final ArrayList<String> subIOs = splitIOPart(ioPart);

        if (subIOs.size() != fieldNames.size())
            throw new IllegalArgumentException("Wrong number of network io in " + toProduce);

        return requireFieldsAsFieldTypes(subIOs);
    }

    private ArrayList<String> splitIOPart(String ioPart) {
        final ArrayList<String> subIOs = new ArrayList<String>();

        StringBuilder subIO = new StringBuilder();
        int subGroups = 0;
        for (int i = 1; i < ioPart.length() - 1; i++) {
            char ch = ioPart.charAt(i);
            switch (ch) {
                case ';':
                    if (0 == subGroups) {
                        subIOs.add(subIO.toString());
                        subIO = new StringBuilder();
                        continue;
                    } else {
                        break;
                    }
                case '{':
                    subGroups++;
                    break;
                case '}':
                    subGroups--;
                    break;
            }
            subIO.append(ch);
        }
        subIOs.add(subIO.toString());
        return subIOs;
    }

    private List<Requirement> requireFieldsAsFieldTypes(List<String> readWrite) {
        final List<Requirement> requirements = new LinkedList<Requirement>();

        for (int i = 0; i < readWrite.size(); i++)
            requirements.add(new Requirement(readWrite.get(i) + "(" + fieldTypes.get(i).getName() + ")",
                    FieldType.class));

        return requirements;
    }

    private String extractIOPart(Requirement toProduce) {
        final Matcher match = basicFTForMePattern.matcher(toProduce.getName());

        if (!match.matches())
            throw new IllegalArgumentException("Can't produce " + cName + " basic field type " + toProduce);

        return match.group(1);
    }

    @Override
    public Dependency.Item produce(Requirement toProduce, final Dependency.Item... wasRequired) throws UndefinedException {
        if (ioName.equals(extractIOPart(toProduce)))
            return ((FieldType)((Wrapper.Wrapped)wasRequired[0]).getWrapped()).createFieldType(toProduce.getName());

        final TargetClass me = getAddress();
        final String ios = extractIOPart(toProduce);
        final HashSet<Requirement> resultMustRequire = requireMeAndTypeOfFields(wasRequired);

        final List<TargetClass> fieldTypeClasses = new ArrayList<TargetClass>();
        for (Dependency.Item field : wasRequired)
            fieldTypeClasses.add(((FieldType)field).getAddress());

        return new FieldType(ios, cName, me,
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(Hardcoded.pValue.ref()));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        final Typed[] readFromNet = new Typed[fieldNames.size()];
                        for (int i = 0; i < readFromNet.length; i++)
                            readFromNet[i] = fieldTypeClasses.get(i).newInstance(from.ref(), Hardcoded.noLimit)
                                    .callV("getValue");

                        return new Block(to.assign(me.newInstance(readFromNet)));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        final Block writeToNet = new Block();
                        for (int i = 0; i < fieldNames.size(); i++)
                            writeToNet.addStatement(fieldTypeClasses.get(i)
                                    .newInstance(val.ref().callV(fieldNames.get(i)), Hardcoded.noLimit)
                                    .call("encodeTo", to.ref()));
                        return writeToNet;
                    }
                },
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var arg1) {
                        if (0 == fieldNames.size())
                            return literal(0);

                        final Typed[] sizes = new Typed[fieldNames.size()];
                        for (int i = 0; i < sizes.length; i++)
                            sizes[i] = fieldTypeClasses.get(i).newInstance(arg1.ref()
                                    .callV(fieldNames.get(i)), Hardcoded.noLimit).callV("encodedLength");

                        return BuiltIn.sum(sizes);
                    }
                },
                TO_STRING_OBJECT,
                false,
                resultMustRequire,
                Collections.<Var<AValue>>emptyList(),
                Collections.<Method>emptyList()
        );
    }

    private HashSet<Requirement> requireMeAndTypeOfFields(Item[] wasRequired) {
        final HashSet<Requirement> reqs = new HashSet<Requirement>();
        reqs.add(getIFulfillReq());
        for (Item used : wasRequired)
            reqs.add(used.getIFulfillReq());
        return reqs;
    }
}
