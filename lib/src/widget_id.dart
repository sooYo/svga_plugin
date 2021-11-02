import 'dart:math';

class GeneratorFactory {
  GeneratorFactory._();

  static IDGenerator? generatorOf(IDStrategy strategy) {
    switch (strategy) {
      case IDStrategy.randomInt:
        return _RandomIDGenerator(100000000);
      case IDStrategy.timeStamp:
        return _TimestampIDGenerator();
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
// endregion Preset Generators
