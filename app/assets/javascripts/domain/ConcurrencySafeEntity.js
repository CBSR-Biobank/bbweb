/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  //ConcurrencySafeEntityFactory.$inject = [];

  /**
   *
   */
  function ConcurrencySafeEntityFactory() {

    function ConcurrencySafeEntity(obj) {
      var defaults = {
        id:           null,
        version:      0,
        timeAdded:    null,
        timeModified: null
      };

      obj = obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    ConcurrencySafeEntity.prototype.isNew = function () {
      return (this.id === null);
    };

    return ConcurrencySafeEntity;
  }

  return ConcurrencySafeEntityFactory;
});
