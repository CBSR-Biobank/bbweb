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
  mod.filter('property', ['value', 'property', function(value, property) {
    if (angular.isObject(value)) {
      if (value.hasOwnProperty(property)) {
        return value[property];
      }
    }
    return 'invalid property on value: ' + property;
  }]);

  mod.filter('truncate', function () {
    return function (text, length, end) {
      if (isNaN(length)) {
        length = 10;
      }

      if (end === undefined) {
        end = '...';
      }

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
    };
  });

  mod.filter('getById', function() {
    return function(input, id) {
      var i=0, len=input.length;
      for (; i<len; i++) {
        if (input[i].id === id) {
          return input[i];
        }
      }
      return null;
    };
  });

  mod.filter('getByName', function() {
    return function(input, name) {
      var i=0, len=input.length;
      for (; i<len; i++) {
        if (input[i].name === name) {
          return input[i];
        }
      }
      return null;
    };
  });


  // originally taken from
  //
  // https://gist.github.com/constellates/314cdede9d3097e23d3e
  mod.filter('timeago', function () {

    /*
     * time: the time
     * local: compared to what time? default: now
     * raw: wheter you want in a format of '5 minutes ago', or '5 minutes'
     */

    // get difference between UTC and local time in milliseconds
    var timeZoneOffset = (new Date().getTimezoneOffset()) * 60000;

    // filter -----------------------------------------------------------------------------

    return function (time, local, raw) {

      if (!time) {
        return 'never';
      }

      if (!local) {
        (local = Date.now());
      }

      if (angular.isDate(time)) {
        time = time.getTime();
      } else if (typeof time === 'string') {
        time = new Date(time);
      }

      // convert UTC to local
      time = time - timeZoneOffset;

      if (angular.isDate(local)) {
        local = local.getTime();
      } else if (typeof local === 'string') {
        local = new Date(local).getTime();
      }

      if (typeof time !== 'number' || typeof local !== 'number') {
        throw new Error('invalid format for time or local');
      }

      var span = [],
      MINUTE = 60,
      HOUR = 3600,
      DAY = 86400,
      WEEK = 604800,
      /* MONTH = 2629744, */
      YEAR = 31556926,
      DECADE = 315569260;


      var offset = Math.abs((local - time) / 1000);

      if (offset <= MINUTE)              { span = [ '', raw ? 'now' : 'a minute' ]; }
      else if (offset < (MINUTE * 60))   { span = [ Math.round(Math.abs(offset / MINUTE)), 'min' ]; }
      else if (offset < (HOUR * 24))     { span = [ Math.round(Math.abs(offset / HOUR)), 'hour' ]; }
      else if (offset < (DAY * 7))       { span = [ Math.round(Math.abs(offset / DAY)), 'day' ]; }
      else if (offset < (WEEK * 52))     { span = [ Math.round(Math.abs(offset / WEEK)), 'week' ]; }
      else if (offset < (YEAR * 10))     { span = [ Math.round(Math.abs(offset / YEAR)), 'year' ]; }
      else if (offset < (DECADE * 100))  { span = [ Math.round(Math.abs(offset / DECADE)), 'decade' ]; }
      else                               { span = [ '', 'a long time' ]; }

      span[1] += (span[0] === 0 || span[0] > 1) ? 's' : '';
      span = span.join(' ');

      if (raw === true) {
        return span;
      }
      return (time <= local) ? span + ' ago' : 'in ' + span;
    };

  });

  return mod;
});
