package org.freeciv.packetgen;

import java.util.Collection;

public interface IDependency {
    public Collection<Requirement> getReqs();
    public Requirement getIFulfillReq();

}
