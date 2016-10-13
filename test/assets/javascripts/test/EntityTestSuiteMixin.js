/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  EntityTestSuiteMixinFactory.$inject = ['$httpBackend', 'TestSuiteMixin'];

  function EntityTestSuiteMixinFactory($httpBackend, TestSuiteMixin) {

    /**
     * A mixin for domain entity test suites.
     *
     * @mixin entityTestSuite
     */
    function EntityTestSuiteMixin() {
      TestSuiteMixin.call(this);
    }

    EntityTestSuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    EntityTestSuiteMixin.prototype.constructor = EntityTestSuiteMixin;

    /**
     * @method
     * @memberof entityTestSuite
     * @instance
     *
     * Used to test updating an attribute on an entity.
     *
     * @param {Object} entity - An instance of the entity to be updated.
     *
     * @param {string} updateFuncName - The name of the update function on the entity to be tested.
     *
     * @param {(string[]|Object)} updateParams - the parameters to pass to the update function. If this is not
     * an string array, it will be converted to a single item array.
     *
     * @param {string} url - the URL on the server where the request will be POSTed to.
     *
     * @param {string} json - the JSON to pass along with the POST request.
     *
     * @param {object} reply - The mocked reply from the server.
     *
     * @param {function(Object)} thenFunc the success function to be called if the update to the entity is
     * successfull.
     *
     * @param {function(string)} catchFunc the fail function to be called if the update to the entity fails.
     */
    EntityTestSuiteMixin.prototype.updateEntity = function (entity,
                                                            updateFuncName,
                                                            updateParams,
                                                            url,
                                                            json,
                                                            reply,
                                                            thenFunc,
                                                            catchFunc) {
      _.extend(json, { expectedVersion: 0 });
      $httpBackend.expectPOST(url, json).respond(201, { status: 'success', data: reply });
      expect(entity[updateFuncName]).toBeFunction();

      if (!Array.isArray(updateParams)) {
        updateParams = [ updateParams ];
      }

      entity[updateFuncName].apply(entity, updateParams).then(thenFunc).catch(catchFunc);
      $httpBackend.flush();
    };

    return EntityTestSuiteMixin;
  }

  return EntityTestSuiteMixinFactory;

});
