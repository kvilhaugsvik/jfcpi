/*
 * Copyright (c) 2014. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.enteties.supporting;

import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.dependency.SimpleDependencyMaker;
import com.kvilhaugsvik.dependency.StringItem;
import com.kvilhaugsvik.dependency.UndefinedException;
import com.kvilhaugsvik.javaGenerator.Annotate;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.ABool;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.packet.*;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldType;
import org.freeciv.packetgen.enteties.Packet;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class PacketMaker extends SimpleDependencyMaker {
    private final List<WeakField> fields;
    private final String name;
    private final int number;
    private final List<Annotate> packetFlags;
    private final TreeSet<String> caps;

    PacketMaker(Requirement me, List<Requirement> allNeeded, List<WeakField> fields, String name, int number, List<Annotate> packetFlags, TreeSet<String> caps) {
        super(me, allNeeded.toArray(new Requirement[allNeeded.size()]));
        this.fields = fields;
        this.name = name;
        this.number = number;
        this.packetFlags = packetFlags;
        this.caps = caps;
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        assert wasRequired.length == fields.size() * 2 + 1 + 2 + 1 : "Wrong number of arguments";

        final String logger = ((StringItem) wasRequired[wasRequired.length - 1]).getValue();
        final boolean enableDeltaBoolFolding
                = BuiltIn.TRUE.equals(((Constant<ABool>) wasRequired[wasRequired.length - 2]).getValue());
        final boolean enableDelta
                = BuiltIn.TRUE.equals(((Constant<ABool>) wasRequired[wasRequired.length - 3]).getValue());
        final FieldType deltaFieldType = (FieldType)wasRequired[wasRequired.length - 4];

        /* First element in the delta or the first element in the non delta field type list */
        final int firstFieldTypePos = enableDelta ? fields.size() : 0;

        List<Field> fieldList = new LinkedList<Field>();
        for (int i = 0; i < fields.size(); i++)
            fieldList.add(new Field(fields.get(i).getName(), (FieldType) wasRequired[i + firstFieldTypePos], name,
                    fields.get(i).getFlags(), fields.get(i).getDeclarations()));

        return new Packet(name, number, logger, packetFlags,
                enableDelta, enableDeltaBoolFolding, enableDelta ? deltaFieldType : null,
                fieldList, caps);
    }

    private static List<Annotate> extractFlags(List<WeakFlag> flags) {
        List<Annotate> packetFlags = new LinkedList<Annotate>();
        byte sentBy = 0;
        LinkedList<String> canceled = new LinkedList<String>();
        for (WeakFlag flag : flags) {
            if ("sc".equals(flag.getName()))
                sentBy += 2;
            else if ("cs".equals(flag.getName()))
                sentBy += 1;
            else if ("no-delta".equals(flag.getName()))
                packetFlags.add(new Annotate(NoDelta.class));
            else if ("is-info".equals(flag.getName()))
                packetFlags.add(new Annotate(IsInfo.class));
            else if ("is-game-info".equals(flag.getName()))
                packetFlags.add(new Annotate(IsGameInfo.class));
            else if ("force".equals(flag.getName()))
                packetFlags.add(new Annotate(Force.class));
            else if ("post-send".equals(flag.getName()))
                packetFlags.add(new Annotate(PostSend.class));
            else if ("post-recv".equals(flag.getName()))
                packetFlags.add(new Annotate(PostRecv.class));
            else if ("cancel".equals(flag.getName()))
                canceled.add(flag.getArguments()[0]);
        }
        if (!canceled.isEmpty()) packetFlags.add(new Canceler(canceled));
        packetFlags.add(new Sender(sentBy));
        return packetFlags;
    }

    private static boolean hasFlag(String name, List<WeakFlag> flags) {
        for (WeakFlag flag : flags)
            if (name.equals(flag.getName()))
                return true;

        return false;
    }

    private static List<Requirement> extractFieldRequirements(List<WeakField> fields) {
        LinkedList<Requirement> allNeeded = new LinkedList<Requirement>();

        /* Non delta version */
        for (WeakField fieldType : fields) {
            String type = fieldType.getType();

            if (0 < fieldType.getDeclarations().length)
                type = type + "_" + fieldType.getDeclarations().length;

            allNeeded.add(new Requirement(type, FieldType.class));
        }

        /* Delta version */
        for (WeakField fieldType : fields) {
            String type = fieldType.getType();

            if (0 < fieldType.getDeclarations().length)
                type = type + "_" + (hasFlag("diff", fieldType.getFlags()) ? "DIFF" : "") + fieldType.getDeclarations().length;

            allNeeded.add(new Requirement(type, FieldType.class));
        }

        return allNeeded;
    }

    public static PacketMaker create(final String name, final int number, List<WeakFlag> flags, final List<WeakField> fields) {
        final TreeSet<String> caps = new TreeSet<String>();
        for (WeakField field : fields)
            for (WeakFlag flag : field.getFlags())
                if ("add-cap".equals(flag.getName()) || "remove-cap".equals(flag.getName()))
                    caps.add(flag.getArguments()[0]);

        Requirement me = new Requirement(name, Packet.class);

        final List<Annotate> packetFlags = extractFlags(flags);

        List<Requirement> allNeeded = extractFieldRequirements(fields);
        allNeeded.add(new Requirement("BV_DELTA_FIELDS", FieldType.class));
        allNeeded.add(new Requirement("enableDelta", Constant.class));
        allNeeded.add(new Requirement("enableDeltaBoolFolding", Constant.class));
        allNeeded.add(new Requirement("JavaLogger", StringItem.class));

        return new PacketMaker(me, allNeeded, fields, name, number, packetFlags, caps);
    }
}
