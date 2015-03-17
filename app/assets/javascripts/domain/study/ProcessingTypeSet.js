define(['underscore'], function(_) {
  'use strict';

  //ProcessingTypeSetFactory.$inject = [];

  /**
   * A set of processing types.
   */
  function ProcessingTypeSetFactory() {

    /**
     * @param processingTypes a list of processing types returned by the server.
     */
    function ProcessingTypeSet(processingTypes) {
      if (_.isUndefined(processingTypes)) {
        throw new Error('annotationTypes is undefined');
      }
      this.processingTypes = _.indexBy(processingTypes, 'id');
    }

    /**
     * Returns the processing type with the given id.
     *
     * @param id the ID of the required wprocessing type.
     */
    ProcessingTypeSet.prototype.get = function (id) {
      var result = this.processingTypes[id];
      if (!result) {
        throw new Error('processing type not found: ' + id);
      }
      return result;
    };

    /**
     * Returns the processing type with the given id or undefined if it does not exist.
     *
     * @param id the ID of the required wprocessing type.
     */
    ProcessingTypeSet.prototype.find = function (id) {
      return this.processingTypes[id];
    };

    return ProcessingTypeSet;
  }

  return ProcessingTypeSetFactory;
});
