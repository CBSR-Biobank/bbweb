/**
 * Common tests for the Panel object.
 */
define(
  'biobank.annotationTypeDataSetCommon',
  [
    'underscore',
    'faker',
    'moment',
    'biobank.testUtils'
  ],
  function(_, faker, moment, utils) {

    var commonTests = {
      getAsString: getAsString
    };

    return commonTests;

    function getAsString(parentObj) {
      var str = parentObj.getAnnotationTypesAsString();
      var regex = /(\w+) \((\w+)\)/g;
      var matches = regex.exec(str);

      while (matches !== null) {
        checkAnnotationTypeMatches(parentObj, matches);
        matches = regex.exec(str);
      }

      function getAtDataItemByName(name) {
        var atDataItems = _.map(parentObj.allAnnotationTypeDataIds(), function (id) {
          return parentObj.getAnnotationTypeData(id);
        });
        return _.find(atDataItems, function(item) { return item.annotationType.name === name; });
      }

      function checkAnnotationTypeMatches(parentObj, matches) {
        var found;

        expect(matches).toBeArrayOfSize(3);

        // find the annotation type data item with the matching name
        found = getAtDataItemByName(matches[1]);

        expect(found).toBeDefined();
        if (found.required) {
          expect(matches[2]).toBe('Req');
        } else {
          expect(matches[2]).toBe('N/R');
        }
      }

    }

  }

);
