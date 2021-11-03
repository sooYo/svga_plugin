import 'dart:math';

class GeneratorFactory {
  GeneratorFactory._();

  static IDGenerator? generatorOf(IDStrategy strategy) {
    switch (strategy) {
      case IDStrategy.randomInt:
        return _RandomIDGenerator(100000000);
      case IDStrategy.timeStamp:
        return _TimestampIDGenerator();
      case IDStrategy.combine:
        return _TimestampRandomSeedIDGenerator();
      default:
        assert(false, 'Undefined generator for strategy: $strategy');
        return null;
    }
  }
}

abstract class IDGenerator {
  int generateID();
}

enum IDStrategy {
  timeStamp,
  randomInt,
  combine,
}

// region Preset Generators
class _TimestampIDGenerator extends IDGenerator {
  @override
  int generateID() => DateTime.now().millisecondsSinceEpoch;
}

class _RandomIDGenerator extends IDGenerator {
  _RandomIDGenerator(this.max, [this.seed]);

  final int max;
  final int? seed;

  @override
  int generateID() => Random(seed).nextInt(max);
}

class _TimestampRandomSeedIDGenerator extends IDGenerator {
  final _timestampGenerator = _TimestampIDGenerator();
  final _randomGenerator = _RandomIDGenerator(1000);

  @override
  int generateID() {
    final timeBasedID = _timestampGenerator.generateID();
    final randomID = _randomGenerator.generateID();

    try {
      return timeBasedID + randomID;
    } catch (_) {
      return timeBasedID;
    }
  }
}
// endregion Preset Generators
