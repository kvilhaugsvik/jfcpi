package org.freeciv.packetgen;

import java.util.HashMap;

public class PacketsStore {
    private boolean devMode;

    private HashMap<String, JavaSrc> types = new HashMap<String, JavaSrc>();

    public PacketsStore(boolean devMode) {
        this.devMode = devMode;
    }

    public void registerTypeAlias(String alias, String aliased) throws UndefinedException {
        if (null != Hardcoded.getJTypeFor(aliased)) {
            types.put(alias, Hardcoded.getJTypeFor(aliased));
        } else if (types.containsKey(aliased)) {
            types.put(alias, types.get(aliased));
        } else {
            String errorMessage = aliased + " not declared before used in " + alias + ".";
            if (devMode) {
                System.err.println(errorMessage);
                System.err.println("Continuing since in development mode...");
            } else {
                throw new UndefinedException(errorMessage);
            }
        }
    }

    public boolean hasTypeAlias(String name) {
        return types.containsKey(name);
    }

    public HashMap<String, String> getJavaCode() {
        HashMap<String, String> out = new HashMap<String, String>();
        for (String name: types.keySet()) {
            out.put(name, types.get(name).toString(name));
        }
        return out;
    }
}
