/**
 * Jasmine test suite
 */
define(['underscore', 'biobank.testUtils'], function(_, testUtils) {
  'use strict';

  /**
   * @param {Array} context.valueMap - array of 2 item array. The fist item is the enum type name, the second
   *                                   is the function name.
   */
  function enumSharedSpec(context) {

    describe('(shared)', function() {

      var EnumerationClass, valueMap;

      beforeEach(inject(function($httpBackend, _funutils_) {
        testUtils.addCustomMatchers();

        EnumerationClass = context.enumerationClass;
        valueMap         = context.valueMap;
      }));

      it('should have expected functions', function() {
        _.each(valueMap, function (entry) {
          // console.log(entry[0], entry[1], EnumerationClass[entry[1]]);
          expect(EnumerationClass[entry[1]]).toBeFunction();
        });
        expect(EnumerationClass.values).toBeFunction();
      });

      it('should have correct number of values', function() {
        var values = EnumerationClass.values();
        expect(values).toBeArrayOfSize(valueMap.length);
      });

      it('should have correct values', function() {
        var values;

        _.each(valueMap, function (entry) {
          expect(EnumerationClass[entry[1]]()).toBe(entry[0]);
        });

        values = EnumerationClass.values();
        expect(values).toContainAll(valueMap);
      });

    });

  }

  return enumSharedSpec;
});
