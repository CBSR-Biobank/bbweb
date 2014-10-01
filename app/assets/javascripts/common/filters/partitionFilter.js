define(['../module'], function(module) {
  'use strict';

  module.filter('partition', partitionFactory);

  partitionFactory.$inject = [];

  /**
   * Taken from:
   *
   * http://stackoverflow.com/questions/21644493/how-to-split-the-ng-repeat-data-with-three-columns-using-bootstrap
   *
   */
  function partitionFactory() {
    var cache = {};
    return partition;

    function partition(arr, size) {
      if (!arr) {
        throw new Error('arr is an invalid argument');
      }
      var newArr = [];
      for (var i=0; i<arr.length; i+=size) {
        newArr.push(arr.slice(i, i+size));
      }
      var arrString = JSON.stringify(arr);
      var fromCache = cache[arrString+size];
      if (JSON.stringify(fromCache) === JSON.stringify(newArr)) {
        return fromCache;
      }
      cache[arrString+size] = newArr;
      return newArr;

    }

  }

});
