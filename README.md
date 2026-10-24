## Summary

Updates the Destruction Gadget GUI to make its range controls more flexible while preserving the existing maximum destruction volume.

Instead of limiting Left, Right, Up, Down, and Depth to fixed values of 16, the sliders now dynamically adjust their maximum values based on the currently selected dimensions and the remaining allowed block volume.

## Changes

* Replaces the fixed Destruction Gadget slider limit of 16 with dynamic limits.
* Preserves the original maximum destruction volume of **17,424 blocks** by default.
* Adds a configurable per-slider hard maximum, defaulting to **32**.
* Adds a configurable maximum block count, defaulting to **17,424**.
* Left, Right, Up, and Down retain a minimum value of **0**.
* Depth now has a minimum value of **1**.
* Adds `Math.max(1, depth)` as a defensive fallback for invalid or legacy Depth values.
* Dynamically recalculates each slider's maximum based on the other four currently selected values.
* Existing slider values are preserved when another slider is changed; the slider being adjusted is constrained by the remaining block budget.
* Adds a live block-count display below the current dimensions:

  * `Block Count: 9 / 17424`
* Holding **Shift** while clicking the `+` or `-` buttons changes the slider value by **10** instead of **1**.
* Shift increments still respect the current dynamic minimum and maximum values.

## Dynamic Limit Calculation

The selected destruction volume is calculated as:

```text
Width  = 1 + Left + Right
Height = 1 + Up + Down
Depth  = max(1, Depth)

Block Count = Width × Height × Depth
```

Each slider's maximum is recalculated so that:

```text
Block Count <= maxBlocks
```

while also respecting the configured `hardMax`.

For example, reducing the width or height can allow Depth to increase beyond the previous fixed limit of 16, as long as the total block count remains within the configured maximum. However if Up/Down/Left/Right are all set to 16 then that means Depth is now also dynamically constrained to 16 as it will match the configured maxBlocks.

## New Config Options

```toml
[limits.destruction_gadget]
	#Absolute maximum value for each Destruction Gadget range slider
	#Range: 1 ~ 2147483647
	hardMax = 32

	#Maximum number of blocks the Destruction Gadget may target at once
	#17424 preserves the original maximum volume of 33 x 33 x 16
	#Range: 1 ~ 2147483647
	maxBlocks = 17424
```

## Default Behavior

The default maximum block count remains equivalent to the previous maximum selection:

```text
33 × 33 × 16 = 17,424 blocks
```

This means the overall destruction capacity is unchanged by default; users simply have more flexibility in how that volume is distributed between width, height, and depth.

## Modified Files

```text
src/main/java/com/direwolf20/buildinggadgets2/setup/Config.java

src/main/java/com/direwolf20/buildinggadgets2/client/screen/DestructionGUI.java

src/main/java/com/direwolf20/buildinggadgets2/client/screen/widgets/IncrementalSliderWidget.java

src/main/java/com/direwolf20/buildinggadgets2/util/GadgetUtils.java
```

## Testing

Tested successfully on:

* Minecraft 1.21.1
* NeoForge 21.1.243
* StoneBlock 4

Verified:

* Dynamic slider maximums update correctly.
* Maximum block-count constraint is respected.
* Per-slider hard maximum is respected.
* Depth cannot be reduced below 1 through the GUI.
* Existing slider values are not unexpectedly changed when another slider is adjusted.
* Shift-click increments and decrements by 10.
* Normal clicks continue to increment and decrement by 1.
* Block-count display updates correctly as dimensions change.
