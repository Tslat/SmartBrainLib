# Changelog
#### Version
2.0.3

## Changes
- Add random sensor offsets to mimic Vanilla handling and add some randomness
- Fix the javadocs on `getTimeUntilMemoryExpires`
- Synchronize COMPUTED_MEMORIES map to prevent cross-thread pollution
- Fix ExtendedSensors ticking one tick too late
- Defer custom behaviour handling, and ensure overriding methods properly re-apply their conditions for:
  - `ExtendedBehaviour`
  - `ConditionlessAttack`
  - `ConditionlessHeldAttack`
  - `CustomBehaviour`
  - `CustomDelayedBehaviour`
  - `CustomHeldBehaviour`
- Fix `BrainUtil#setTargetOfEntity` improperly comparing previous target
- Fix `EntityRetrievalUtil` entity retrieval via radius only searching half the radius
- Fix activity interruption/removal not checking if behaviours are started before stopping them
- Pass through packedBrain on creation - although you shouldn't really be relying on it for memories
- Fix `BrainUtil#getAllBehaviours` not doing anything
- Fix the TransformingListView impl not being quite right
- Fix scheduled activities replacing themselves every tick
- Expand surface placement logic for `EasyRandom`
- Adjust `FollowEntity` teleporting logic
- Fixed bad handling of `MoveToWalkTarget`
- Fixed hide predicate for `EscapeSun` being broken
- Fixed `FollowEntity` not resetting pathfinder malus properly
- Fix `BreakBlock` not continually updating its cached block
- Expanded the predicate conditions for `LookAtTarget`
- Fixed `DelayedBehaviour` sometimes preventing itself from running
- Fix `StrafeTarget`/`StayWithinDistanceOfAttackTarget` calculating proximity wrong
- Improve proximity matching for `BreedWithPartner`
- Prevent `SequentialBehaviour#runningBehaviour` from being stopped twice
- Prevent the default impl of `ItemTemptingSensor` crashing out if a mob without `TEMPT_RANGE` uses it
- Fixed `NearbyHostileSensor` using the wrong parameters
- Fixed `LeapAtTarget`'s broken attacking logic
- Fixed `BreezeSpecificSensor` not actually using its predicate
- Fixed `PiglinSpecificSensor` not filling the `NEARBY_ADULT_PIGLINS` memory
- Fixed `AxolotlSpecificSensor` using the wrong radius
- Fix `ConditionlessAttack` doing everything twice
- Adjust the `NearestVisibleEntityFilteredSensor` implementations to remove overlap and simplify their usage
- Remove implicit requirement for `AgeableMob` on `NearbyAdultSensor`