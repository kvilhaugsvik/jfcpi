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

    /**
     * Construct a new SourceFile with the given path and content.
     * @param path location of the source file.
     * @param content content of the source file.
     */
    public SourceFile(String path, String content) {
        this.path = path;
        this.content = content;
    }

    /**
     * Get the content of the source file.
     * @return the content of the file.
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the path to the source file.
     * @return the path to the source file.
     */
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
