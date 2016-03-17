/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  entityTestSuiteFactory.$inject = ['$httpBackend'];

  /**
   * A mixin for test suites for domain entities.
   */
  function entityTestSuiteFactory($httpBackend) {
    var mixin = {
      updateEntity: updateEntity
    };

    return mixin;

    //--

    /**
     * @param {object} entity the entity to be updated.
     *
     * @param {string} updateFuncName
     *
     * @param {array, object} updateParams the parameters to pass to the update function. If this is not an
     * array, it will be converted to a single item array.
     *
     * @param {string} url the URL on the server where the request will be POSTed to.
     *
     * @param {string} json the JSON to pass along with the POST request.
     *
     * @param {object} reply the mocked reply from the server.
     *
     * @param {function} thenFunc the success function to be called if the update call on the entity is successfull.
     *
     * @param {function} catchFunc the fail function to be called if the update call on the entity fails.
     */
    function updateEntity(entity, updateFuncName, updateParams, url, json, reply, thenFunc, catchFunc) {
      /* jshint validthis: true */
      _.extend(json, { expectedVersion: 0 });
      $httpBackend.expectPOST(url, json).respond(201, { status: 'success', data: reply });
      expect(entity[updateFuncName]).toBeFunction();

      if (!Array.isArray(updateParams)) {
        updateParams = [ updateParams ];
      }

      entity[updateFuncName].apply(entity, updateParams).then(thenFunc).catch(catchFunc);
      $httpBackend.flush();
    }
  }

  return entityTestSuiteFactory;

});
