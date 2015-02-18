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
      addItem: addItem,
      addItemNoId: addItemNoId,
      removeItems: removeItems,
      getAsString: getAsString
    };

    return commonTests;

    function addItem(parentObj, newItem, expectedAnnotationType) {
      var atDataItem;

      var originalAtDataItemCount = parentObj.annotationTypeDataSize();
      parentObj.addAnnotationTypeData(newItem);
      expect(parentObj.annotationTypeDataSize()).toBe(originalAtDataItemCount + 1);

      atDataItem = parentObj.getAnnotationTypeData(newItem.annotationTypeId);
      expect(atDataItem.annotationType).toEqual(expectedAnnotationType);
      expect(_.omit(atDataItem, 'annotationType')).toEqual(newItem);
    }

    function addItemNoId(parentObj, newItem) {
      expect(newItem.id).toBe('');

      var originalAtDataItemCount = parentObj.annotationTypeDataSize();
      var numNewItems = 5;
      _.each(_.range(numNewItems), function() { parentObj.addAnnotationTypeData(newItem); });

      expect(parentObj.annotationTypeDataSize()).toBe(originalAtDataItemCount + numNewItems);
    }

    function removeItems(parentObj) {
      var atDataItemCount = parentObj.annotationTypeDataSize();
      expect(atDataItemCount > 1).toBeTrue();

      _.each(parentObj.allAnnotationTypeDataIds(), function(id) {
        parentObj.removeAnnotationTypeData(id);
        atDataItemCount = atDataItemCount - 1;
        expect(parentObj.annotationTypeDataSize()).toBe(atDataItemCount);
      });

    }

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
