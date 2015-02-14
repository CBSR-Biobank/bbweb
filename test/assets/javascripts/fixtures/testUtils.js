define('biobank.testUtils', ['faker', 'moment'], function(faker, moment) {

  var entityNames = [];

  var utils = {
    uuid: uuid,
    randomBoolean: randomBoolean
  };

  /**
   * Taken from fixer version 2.1.2. When karma-fixer uses the same version this
   * function can be removed.
   */
  function uuid() {
    var RFC4122_TEMPLATE = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
    var replacePlaceholders = function (placeholder) {
      var random = Math.random()*16|0;
      var value = placeholder == 'x' ? random : (random &0x3 | 0x8);
      return value.toString(16);
    };
    return RFC4122_TEMPLATE.replace(/[xy]/g, replacePlaceholders);
  }

  function randomBoolean() {
    return faker.random.number() === 1;
  }

  return utils;
});
