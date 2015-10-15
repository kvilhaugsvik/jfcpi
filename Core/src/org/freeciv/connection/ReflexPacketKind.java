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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles the running of reflex rules. Reflex rules run as a part of the
 * protocol implementation when a packet of a certain kind is handled. They
 * can therefore be used to do actions that shouldn't have to wait for the
 * protocol implementation user to finish processing previous packets.
 * @param <WorksOn> the connectionish thing the action is done to.
 */
public class ReflexPacketKind<WorksOn extends ConnectionRelated> {
    private final HashMap<Integer, ReflexReaction> quickRespond;
    private final WorksOn owner;
    private final ReentrantLock noReadBeforeWriteAndReflexesAreDone;

    public ReflexPacketKind(Map<Integer, ReflexReaction<WorksOn>> reflexes, WorksOn owner, ReentrantLock noReadBeforeWriteAndReflexesAreDone) {
        this.noReadBeforeWriteAndReflexesAreDone = noReadBeforeWriteAndReflexesAreDone;
        this.owner = owner;
        this.quickRespond = new HashMap<Integer, ReflexReaction>(reflexes);
    }

    /**
     * A packet of the specified kind is handled. If it has a reflex rule
     * the rule should fire.
     * @param packetKindNumber the packet kind number.
     */
    public void handle(int packetKindNumber) {
        if (quickRespond.containsKey(packetKindNumber))
            quickRespond.get(packetKindNumber).apply(owner);
    }

    /**
     * Acquire lock.
     *
     * A lock on sending and receiving inside a connection is needed
     * because a reflex rule may change the state of the connection
     * in a way that determines how a future packet should be sent or
     * received.
     */
    public void startedReceivingOrSending() {
        noReadBeforeWriteAndReflexesAreDone.lock();
    }

    /**
     * Release lock.
     *
     * A lock on sending and receiving inside a connection is needed
     * because a reflex rule may change the state of the connection
     * in a way that determines how a future packet should be sent or
     * received.
     */
    public void finishedRunningTheReflexes() {
        noReadBeforeWriteAndReflexesAreDone.unlock();
    }

    /**
     * Layer the rules in base under the rules in top.
     *
     * If both base and over has a rule handling the same packet the rule
     * in over will be used.
     *
     * @param base the base rules. Can be overridden by top.
     * @param over the new rules. Can override base.
     * @return the rules of over layered over the rules in base.
     */
    public static Map<Integer, ReflexReaction> layer(
            Map<Integer, ReflexReaction> base,
            Map<Integer, ReflexReaction> over) {
        HashMap<Integer, ReflexReaction> out = new HashMap<Integer, ReflexReaction>(base);
        out.putAll(over);
        return out;
    }
}
