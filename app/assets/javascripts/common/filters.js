/** Common filters. */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.filters', []);

  /**
   * Extracts a given property from the value it is applied to.
   * {{{
   * (user | property:'name')
   * }}}
   */
  mod.filter('property', function(value, property) {
    if (angular.isObject(value)) {
      if (value.hasOwnProperty(property)) {
        return value[property];
      }
    }
    return 'invalid property on value: ' + property;
  });

  mod.filter('truncate', function () {
    return function (text, length, end) {
      if (isNaN(length))
        length = 10;

      if (end === undefined)
        end = '...';

      if (text.length <= length || text.length - end.length <= length) {
        return text;
      } else {
        return String(text).substring(0, length-end.length) + end;
      }
    };
  });

  /**
   * Taken from:
   *
   * http://stackoverflow.com/questions/21644493/how-to-split-the-ng-repeat-data-with-three-columns-using-bootstrap
   */
  mod.filter('partition', function() {
    var cache = {};
    return function(arr, size) {
      if (!arr) { return; }
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
    };
  });

  return mod;
});
