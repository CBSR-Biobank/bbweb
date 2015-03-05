define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('validationService', validationService);

  //validationService.$inject = [];

  /**
   * Used for validating responses from the server.
   *
   * Ideas taken from Functional Javascript by Michael Fogus.
   */
  function validationService() {

    var service = {
      checker:       checker,
      validator:     validator,
      aMap:          aMap,
      aMapValidator: validator('must be a map', aMap),
      hasKeys:       hasKeys
    };
    return service;

    //-------

    /**
     * Creates a checker one or more validators.
     */
    function checker(/* validators */) {
      var validators = _.toArray(arguments);
      return function(obj) {
        return _.reduce(validators, function(errs, check) {
          if (check(obj)) {
            return errs;
          } else {
            return _.chain(errs).push(check.message).value();
          }
        }, []);
      };
    }

    /**
     * Used to create validators.
     */
    function validator(message, fun) {
      var f = function(/* args */) {
        return fun.apply(fun, arguments);
      };
      f.message = message;
      return f;
    }

    function aMap(obj) {
      return _.isObject(obj);
    }

    function hasKeys() {
      var keys = _.toArray(arguments);

      var fun = function(obj) {
        return _.every(keys, function(k) {
          //console.log(k, _.has(obj, k));
          return _.has(obj, k);
        });
      };
      fun.message = 'Must have values for keys: ' + keys.join(', ');
      return fun;
    }

  }

});
