# How to use the Holograms-API

## Setup the dependency

Add [jitpack](https://jitpack.io/) as a repository and the API as a dependency.

Gradle example:

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
    compile "com.github.randombyte-developer.holograms:holograms-api:v2.1"
}
```

That dependency only contains [this one file](https://github.com/randombyte-developer/holograms/blob/master/holograms-api/src/main/kotlin/de/randombyte/holograms/api/HologramsService.kt).

## How to use in code

Although everything is documented in the[`HologramService.kt`](https://github.com/randombyte-developer/holograms/blob/master/holograms-api/src/main/kotlin/de/randombyte/holograms/api/HologramsService.kt)
it may not be clear how to use that `Service` written in [Kotlin](https://kotlinlang.org/) from a plugin written in Java.
Basically, the fields prefixed with `var` like `text` and `location` have a getter and a setter method automatically generated to be used from the Java side.

Here is an example:
```java
Optional<HologramsService> hologramsServiceOptional = Sponge.getServiceManager().provide(HologramsService.class);
                    HologramsService hologramsService = hologramsServiceOptional.orElseThrow(
                            () -> new RuntimeException("HologramsAPI not available! Is the plugin 'holograms' installed?"));

                    // Creating a Hologram
                    Optional<HologramsService.Hologram> hologramOptional = hologramsService
                            .createHologram(player.getLocation(), Text.of(TextColors.GREEN, "Example text"));
                    if (!hologramOptional.isPresent()) {
                        player.sendMessage(Text.of("Hologram couldn't be spawned!"));
                        return CommandResult.success();
                    }

                    // Modifying the Hologram
                    Hologram hologram = hologramOptional.get();

                    hologram.setText(Text.of("New text"));

                    Location<World> newLocation = hologram.getLocation().add(0.0, 5.0, 0.0);
                    hologram.setLocation(newLocation);

                    // Finding the Hologram by UUID
                    // Both UUIDs are saved somewhere (e.g. in a config file)
                    UUID hologramUuid = hologram.getUuid();
                    UUID worldUuid = hologram.getWorldUuid();

                    Optional<World> worldOptional = Sponge.getServer().getWorld(worldUuid);
                    if (!worldOptional.isPresent()) {
                        player.sendMessage(Text.of("Couldn't find world!"));
                        return CommandResult.success();
                    }
                    World world = worldOptional.get();
                    Optional<? extends Hologram> loadedHologramOptional = hologramsService.getHologram(world, hologramUuid);
                    if (!loadedHologramOptional.isPresent()) {
                        player.sendMessage(Text.of("Couldn't find Hologram!"));
                        return CommandResult.success();
                    }
                    Hologram loadedHologram = loadedHologramOptional.get();
                    loadedHologram.setText(Text.of("This Hologram was found!"));

                    // Removing the Hologram
                    loadedHologram.remove();
```

If you have questions, please ask me!
