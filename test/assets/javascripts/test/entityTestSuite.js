/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  entityTestSuiteFactory.$inject = [];

  /**
   * A mixin for test suites for domain entities.
   */
  function entityTestSuiteFactory() {
    var mixin = {
      updateEntity: updateEntity
    };

    return mixin;

    //--

    function updateEntity(entity, updateFuncName, updateParam, url, json, reply, thenFunc, catchFunc) {
      /* jshint validthis: true */
      var self = this;

      _.extend(json, { expectedVersion: 0 });
      self.httpBackend.expectPOST(url, json).respond(201, { status: 'success', data: reply });
      expect(entity[updateFuncName]).toBeFunction();
      entity[updateFuncName].call(entity, updateParam).then(thenFunc).catch(catchFunc);
      self.httpBackend.flush();
    }
  }

  return entityTestSuiteFactory;

});
