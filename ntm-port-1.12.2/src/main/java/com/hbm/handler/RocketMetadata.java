package com.hbm.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;

/** Native key group for the rocket's capsule, count, and five packed stage pairs.
 * Keys are allocated by the declaring entity, not by this non-entity helper.
 * No part lookup, rendering, item registry initialization, or numeric wire IDs live here.
 */
public final class RocketMetadata {
    public static final int STAGE_CAPACITY = 5;
    private final DataParameter<Integer> capsule;
    private final DataParameter<Integer> stageCount;
    private final List<DataParameter<Integer>> first;
    private final List<DataParameter<Integer>> second;

    public RocketMetadata(DataParameter<Integer> capsule, DataParameter<Integer> stageCount,
            List<DataParameter<Integer>> first, List<DataParameter<Integer>> second) {
        // This new key-group API describes exactly the existing five metadata slots.
        // This does not validate or truncate the runtime RocketStruct stage count.
        if(first.size() != STAGE_CAPACITY || second.size() != STAGE_CAPACITY)
            throw new IllegalArgumentException("Rocket metadata requires five stage key pairs");
        this.capsule = capsule;
        this.stageCount = stageCount;
        this.first = new ArrayList<>(first);
        this.second = new ArrayList<>(second);
    }

    public void register(EntityDataManager manager) {
        manager.register(capsule, 0);
        manager.register(stageCount, 0);
        for(int i = 0; i < first.size(); i++) {
            manager.register(first.get(i), 0);
            manager.register(second.get(i), 0);
        }
    }

    public int getCapsule(EntityDataManager manager) { return manager.get(capsule); }
    public void setCapsule(EntityDataManager manager, int value) { manager.set(capsule, value); }
    public int getStageCount(EntityDataManager manager) { return manager.get(stageCount); }
    public void setStageCount(EntityDataManager manager, int value) { manager.set(stageCount, value); }
    public int getFirst(EntityDataManager manager, int stage) { return manager.get(first.get(stage)); }
    public void setFirst(EntityDataManager manager, int stage, int value) { manager.set(first.get(stage), value); }
    public int getSecond(EntityDataManager manager, int stage) { return manager.get(second.get(stage)); }
    public void setSecond(EntityDataManager manager, int stage, int value) { manager.set(second.get(stage), value); }
}
