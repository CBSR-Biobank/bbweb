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

  return mod;
});
