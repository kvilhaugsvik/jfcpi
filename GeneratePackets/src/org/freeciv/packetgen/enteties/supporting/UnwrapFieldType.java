package org.freeciv.packetgen.enteties.supporting;

import com.kvilhaugsvik.dependency.*;
import org.freeciv.packetgen.enteties.FieldType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Create a field type from alias information in a wrapper.
 * If a wrapper wraps a field type and has the name of another field type
 * a field type that is an unseen alias to the wrapped field type with the
 * wrapper provided name will be created.
 */
public class UnwrapFieldType implements Dependency.Maker {
    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        return Arrays.<Requirement>asList(
                new Requirement(FieldType.class.getSimpleName() + ":" + toProduce.getName(),
                        Wrapper.Wrapped.class));
    }

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldType.class,
                Pattern.compile(".+(.+)"));
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        return ((FieldType) ((Wrapper.Wrapped) wasRequired[0]).getWrapped()).aliasUnseenToCode(toProduce.getName());
    }
}
