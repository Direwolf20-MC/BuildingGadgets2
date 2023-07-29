package com.direwolf20.buildinggadgets2.api.gadgets;

import com.direwolf20.buildinggadgets2.util.modes.*;
import com.google.common.collect.ImmutableSortedSet;

import java.util.HashMap;
import java.util.LinkedHashSet;

public enum GadgetModes {
    INSTANCE;

    private final HashMap<GadgetTarget, LinkedHashSet<BaseMode>> gadgetModes = new HashMap<>();

    GadgetModes() {
        setupDefaultModes();
    }

    /**
     * Building gadgets comes with the following default modes, you can not remove these, only add to them
     * using the {@link #registerMode(GadgetTarget, BaseMode)} method
     */
    private void setupDefaultModes() {
        var modes = new HashMap<GadgetTarget, LinkedHashSet<BaseMode>>();

        // Building Gadget
        modes.put(GadgetTarget.BUILDING, new LinkedHashSet<>() {{
            add(new BuildToMe());
            add(new VerticalWall());
            add(new VerticalColumn());
            add(new Surface(false));
            add(new HorizontalWall());
            add(new Stairs());
        }});

        // Exchanging Gadget
        modes.put(GadgetTarget.EXCHANGING, new LinkedHashSet<>() {{
            add(new Surface(true));
        }});

        modes.put(GadgetTarget.DESTRUCTION, new LinkedHashSet<>() {{
        }});

        modes.put(GadgetTarget.COPYPASTE, new LinkedHashSet<>() {{
            add(new Copy());
            add(new Paste());
        }});

        modes.put(GadgetTarget.CUTPASTE, new LinkedHashSet<>() {{
            add(new Cut());
            add(new Paste());
        }});

        this.gadgetModes.putAll(modes);
    }

    /**
     * Register a given mode to a given gadget
     *
     * @param target the target gadget
     * @param mode   the mode you want to register
     */
    public boolean registerMode(GadgetTarget target, BaseMode mode) {
        return this.gadgetModes
                .computeIfAbsent(target, key -> new LinkedHashSet<>())
                .add(mode);
    }

    /**
     * Get an immutable set of building modes for any given gadget target
     */
    public ImmutableSortedSet<BaseMode> getModesForGadget(GadgetTarget target) {
        return ImmutableSortedSet.copyOf(gadgetModes.computeIfAbsent(target, key -> new LinkedHashSet<>()));
    }
}
