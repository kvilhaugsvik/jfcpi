package org.freeciv.packetgen.enteties;

import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.dependency.Requirement;

import java.util.Collection;
import java.util.Collections;

/**
 * Source code used to generate source code
 */
public class SourceFile implements Dependency.Item, ReqKind {
    private final String path;
    private final String content;

    public SourceFile(String path, String content) {
        this.path = path;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.emptyList();
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(path, SourceFile.class);
    }
}
