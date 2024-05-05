(This only applies to multi pack setups)

## The problem
Imagine you have pack that applies high resolution textures to everything. Now imagine you wanted to add a pack that gives swords some custom models and textures.  
The problem comes from both packs needing to modify the same file (in this case `textures/item/diamond_sword.png`).

The mod first (if needed) unpacks the provided packs and then goes through and copies the contents of them into a single output pack.  
If two packs provide the same file, the one that is copied later on will overwrite the first one.

So how does the mod handle sorting the packs?

## The solution

**Priorities!**

Priorities tell the mod which packs to copy earlier.  
A priority is just a number, with the smallest one being `0`. (Meaning, no negatives!)  
A file must be prefixed with `PRIORITY-`. For example `20-`. Yes, the `-` is *required*.  
Here's an example of a pack with a priority of `10`: `10-FreshAnimations_v1.9.zip`.

So what does the priority do?  
A pack with a *higher* priority number is applied **earlier** than one with a lower priority.  
This means that the lower priority value pack can overwrite files from the pack with a higher priority number.

## Examples

Here's an example of the `packs` directory:

- packs/
    - 0-base/
    - 10-FreshAnimations_v1.9.zip

In this case any file that's in the fresh animations pack can be overwritten by the base pack.

<br />

Here's another one:

- packs/
    - 0-base/pack.mcmeta
    - 10-cool-sword/assets/textures/item/diamond_sword.png
    - 20-faithful.zip

In this case faithful provides high-res textures, the `cool-sword` pack overwrites some textures from faithful to have cool swords and the base pack is just there for the `pack.mcmeta` file.