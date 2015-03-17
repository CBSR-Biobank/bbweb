define(['underscore'], function(_) {
  'use strict';

  //funutils.$inject = [];

  /**
   * Functional programming utilities.
   *
   * Taken from Functional Javascript by Michael Fogus.
   */
  function funutils() {
    var service = {
      cat:           cat,
      mapcat:        mapcat,
      construct:     construct,
      partial:       partial,
      partial1:      partial1,
      renameKeys:    renameKeys,
      pickOptional:  pickOptional
    };
    return service;

    //-------

    function cat() {
      var head = _.first(arguments);
      if (head !== null) {
        return head.concat.apply(head, _.rest(arguments));
      }
      return [];
    }

    function mapcat(fun, coll) {
      return cat.apply(null, _.map(coll, fun));
    }

    function construct(head, tail) {
      return cat([head], _.toArray(tail));
    }

    function partial(fun /*, pargs */) {
      var pargs = _.rest(arguments);

      return function(/* arguments */) {
        var args = cat(pargs, _.toArray(arguments));
        return fun.apply(fun, args);
      };
    }

    function partial1(fun, arg1) {
      return function(/* args */) {
        var args = construct(arg1, arguments);
        return fun.apply(fun, args);
      };
    }

    function renameKeys(obj, newNames) {
      return _.reduce(newNames,
                      function(o, nu, old) {
                        if (_.has(obj, old)) { o[nu] = obj[old]; }
                        return o;
                      },
                      _.omit.apply(null, construct(obj, _.keys(newNames))));
    }

    function pickOptional(obj /* keys */) {
      var keys = _.rest(arguments);
      var result = {};
      _.each(keys, function(key) {
        if ((obj[key] !== undefined) && (obj[key] !== null)) {
          result[key] = obj[key];
        }
      });
      return result;
    }

  }

  return funutils;
});
