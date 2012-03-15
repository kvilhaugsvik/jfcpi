package org.freeciv.packetgen;

import java.util.Set;

public interface IDependency {
    public Set<Requirement> getReqs();
    public Requirement getIFulfillReq();

}
