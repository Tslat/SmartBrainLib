# Changelog
#### Version
1.17

## Changes
### API
* Performed a general cleanup across the entire mod, bringing it in line with modern modding practices
* Added `SmartBrainBuilder` and extracted the various Brain-construction methods to it. `SmartBrainOwner` extends it by default, but extracting it now means you can have an external class contain your AI implementation if wanted with minimal additional effort
* Added `BehaviourUtil` - a helper class for `BehaviorControl` related methods
* Added `ExtendedBehaviour#tryExpire` - a method to allow for graceful expiration of behaviours when switching `Activities`
* Added `ExtendedSensor#onlyScanIf` - an optional condition for sensors to only scan if the condition is met
* Users no longer need to tick the brain, SBL ticks it automatically for you (unless you override `customServerAIStep` in your `mob`)
* Users no longer need to return a `SmartBrainProvider` or `SmartBrain` instance in your entity class. SBL creates it automatically for you
* Moved over to Jspecify's Nullability annotations per the new industry standard. See [here][https://jspecify.dev] for more information
* Renamed `SBLLoader` to `SBLPlatform`
* Renamed `SBLConstants#LOADER` to `SBLConstants#PLATFORM`
* Removed `EntityRetrievalUtil#streamEntities` - Its benefits just weren't worth the confusion it caused
* Removed `EntityRetrievalUtil#findEntities` - It only served to offer a 'max' entity short-circuit, which has now been built into the other methods
* Removed `BrainUtil#forEachBehaviour`
* Changed `GroupBehaviour#getBehaviours` from an `Iterator` to an `Iterable`
* Added `GroupBehaviour#add` to allow for manually adding individual behaviours to a `GroupBehaviour`
* Added `BrainBehaviourPredicate#isChildOfBehaviour` helper method
* Renamed the `net.tslat.smartbrainlib.object` package to `net.tslat.smartbrainlib.library.object`
* Moved `BrainBehaviourConsumer`, `BrainBehaviourPredicate`, `ToFloatBiFunction`, and `TriPredicate` to `net.tslat.smartbrainlib.library.interfaces`
* Made `ExtendedTargetingConditions` extend `BiPredicate`
* Renamed `FreePositionTracker` to `ExactPositionTracker`
* Renamed `MemoryTest#hasNoMemories` to `MemoryTest#noMemories`
* Renamed `MemoryTest#builder(int)` to `MemoryTest#sized(int)`
* Renamed `BrainActivityGroup` to `ActivityBuilder`
* Renamed `EntityFilteringSensor` to `NearestVisibleEntityFilteredSensor`
* Renamed `ExtendedBehaviour#runForBetween` to `#runFor`
* Renamed `ExtendedBehaviour#cooldownForBetween` to `#cooldownFor`
* Renamed the generic `T` type across all of SBL's brain-related classes to `BO` for consistent and clear type understanding
* Bubbled up polymorphic overloads of self-returning methods across behaviours and sensors
* Ensured consistency of generic type ordering across all of SBL's brain-related classes
* Added `BreezeSpecificSensor`
* Updated `ExtendedBehaviour#getMemoryRequirements` to use the `MemoryCondition` interface that Mojang added
* Renamed `SBLShufflingList` to `WeightedShuffleableList`

### Internal
* Greatly improved the Javadocs in `EntityRetrievalUtil`
* Converted all Javadocs to Markdown, per Java 25's new standard
* Reorganised the boilerplate/example code
* Added `package-info` declarations to each package for more consistent and cleaner API usage
* Significantly overhauled the Javadocs across the entire mod
* General code cleanup and improvements
* Implemented pre-collapsed code sections for boilerplate code, to further improve code clarity