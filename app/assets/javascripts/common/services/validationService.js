/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Used for validating responses from the server.
 *
 * Ideas taken from Functional Javascript by Michael Fogus.
 *
 * FIXME: should be removed since tv4 is now used (Nelson 2016-08-25).
 */
/* @ngInject */
function validationService(funutils) {

  var service = {
    condition1:    condition1,
    validator:     validator,
    hasKeys:       hasKeys
  };
  return service;

  //-------

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

  function hasKeys() {
    var keys = _.toArray(arguments);
    var fun = function(obj) {
      var result = _.every(keys, function(k) {
        //console.log(k, _.has(obj, k), obj[k], obj);
        return _.has(obj, k);
      });
      return result;
    };
    return fun;
  }

  function condition1(/* validators */) {
    var validators = _.toArray(arguments);

    return function(fun, arg) {
      var errors = funutils.mapcat(function(isValid) {
        return isValid(arg) ? [] : [isValid.message];
      }, validators);
      if (!_.isEmpty(errors)) {
        return errors.join(', ');
      }
      return fun(arg);
    };
  }
}

export default ngModule => ngModule.service('validationService', validationService)
